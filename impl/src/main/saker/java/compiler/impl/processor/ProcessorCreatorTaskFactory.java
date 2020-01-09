/*
 * Copyright (C) 2020 Bence Sipka
 *
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package saker.java.compiler.impl.processor;

import java.io.Externalizable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import saker.build.file.DirectoryVisitPredicate;
import saker.build.file.SakerDirectory;
import saker.build.file.SakerFile;
import saker.build.file.content.ContentDescriptor;
import saker.build.file.content.DirectoryContentDescriptor;
import saker.build.file.content.MultiPathContentDescriptor;
import saker.build.file.content.SerializableContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.FileEntry;
import saker.build.file.provider.FileHashResult;
import saker.build.file.provider.LocalFileProvider;
import saker.build.file.provider.SakerPathFiles;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.ExecutionProperty;
import saker.build.runtime.execution.SakerLog;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskDependencyFuture;
import saker.build.task.TaskExecutionUtilities;
import saker.build.task.TaskFactory;
import saker.build.task.dependencies.FileCollectionStrategy;
import saker.build.task.identifier.TaskIdentifier;
import saker.build.task.utils.dependencies.RecursiveIgnoreCaseExtensionFileCollectionStrategy;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import saker.build.thirdparty.saker.util.io.ByteSink;
import saker.build.thirdparty.saker.util.io.FileUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayInputStream;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayOutputStream;
import saker.build.thirdparty.saker.util.io.function.IOConsumer;
import saker.java.compiler.api.classpath.ClassPathEntry;
import saker.java.compiler.api.classpath.ClassPathReference;
import saker.java.compiler.api.classpath.ClassPathVisitor;
import saker.java.compiler.api.classpath.CompilationClassPath;
import saker.java.compiler.api.classpath.FileClassPath;
import saker.java.compiler.api.classpath.JavaClassPath;
import saker.java.compiler.api.classpath.JavaClassPathBuilder;
import saker.java.compiler.api.classpath.SDKClassPath;
import saker.java.compiler.api.compile.JavaCompilationWorkerTaskIdentifier;
import saker.java.compiler.api.compile.JavaCompilerWorkerTaskOutput;
import saker.java.compiler.api.compile.SakerJavaCompilerUtils;
import saker.java.compiler.api.processor.ProcessorCreator;
import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.compile.CompileFileTags;
import saker.java.compiler.impl.compile.WorkerJavaCompilerTaskFactoryBase;
import saker.java.compiler.impl.compile.util.LocalPathFileContentDescriptorExecutionProperty;
import saker.java.compiler.impl.util.ClassPathEntryFileLocationExecutionProperty;
import saker.nest.bundle.NestBundleClassLoader;
import saker.sdk.support.api.SDKDescription;
import saker.sdk.support.api.SDKPathReference;
import saker.sdk.support.api.SDKReference;
import saker.sdk.support.api.SDKSupportUtils;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileLocation;
import saker.std.api.file.location.FileLocationVisitor;
import saker.std.api.file.location.LocalFileLocation;

public final class ProcessorCreatorTaskFactory
		implements TaskFactory<ProcessorCreator>, Task<ProcessorCreator>, Externalizable, TaskIdentifier {
	private static final long serialVersionUID = 1L;

	//TODO we should clean up cached JARs that are old and no longer used. The MemoryTrimmer could be used to do that

	private static final FileTime FILE_TIME_ZERO = FileTime.fromMillis(0);

	private String processorClassName;
	private JavaClassPath classPath;
	private NavigableMap<String, SDKDescription> sdks;

	/**
	 * For {@link Externalizable}.
	 */
	public ProcessorCreatorTaskFactory() {
	}

	public ProcessorCreatorTaskFactory(String processorClassName, JavaClassPath classPath,
			Map<String, SDKDescription> sdks) {
		this.processorClassName = processorClassName;
		this.classPath = classPath;
		if (ObjectUtils.isNullOrEmpty(sdks)) {
			this.sdks = ImmutableUtils.emptyNavigableMap(SDKSupportUtils.getSDKNameComparator());
		} else {
			this.sdks = new TreeMap<>(SDKSupportUtils.getSDKNameComparator());
			this.sdks.putAll(sdks);
		}
	}

	@Override
	public ProcessorCreator run(TaskContext taskcontext) throws Exception {
		Map<String, SDKReference> sdkreferences = WorkerJavaCompilerTaskFactoryBase.toSDKReferences(taskcontext, sdks);
		Map<LocalFileLocation, NavigableSet<SakerPath>> localdirectorycontents = new HashMap<>();
		Set<FileLocation> staticclasspaths = new HashSet<>();
		Map<FileLocation, ContentDescriptor> filelocations = collectFileLocations(taskcontext, classPath, sdkreferences,
				localdirectorycontents::put, staticclasspaths::add);
		Set<LocalFileLocation> cachedfiles = new LinkedHashSet<>();
		if (!filelocations.isEmpty()) {
			NestBundleClassLoader cl = (NestBundleClassLoader) this.getClass().getClassLoader();
			Path jarcachedir = cl.getBundle().getBundleStoragePath().resolve("jar_cache");
			LocalFileProvider fp = LocalFileProvider.getInstance();
			fp.createDirectories(jarcachedir);

			for (Entry<FileLocation, ContentDescriptor> entry : filelocations.entrySet()) {
				entry.getKey().accept(new FileLocationVisitor() {
					@Override
					public void visit(ExecutionFileLocation loc) {
						if (staticclasspaths.contains(loc)) {
							Path localpath = taskcontext.getExecutionContext().getPathConfiguration()
									.toLocalPath(loc.getPath());
							if (localpath != null) {
								cachedfiles.add(LocalFileLocation.create(SakerPath.valueOf(localpath)));
								return;
							}
							//its not a local path, we can't use it. we need to copy it to the local storage
						}

						SakerFile f = taskcontext.getTaskUtilities().resolveAtPath(loc.getPath());
						if (f == null) {
							throw ObjectUtils
									.sneakyThrow(new FileNotFoundException("Class path not found: " + loc.getPath()));
						}
						ByteArrayRegion filebytes;
						if (f instanceof SakerDirectory) {
							//a directory classpath.
							//put all files in the classpath into a ZIP (JAR, but no manifest)
							//   and cache that
							NavigableMap<SakerPath, SakerFile> cpfiles = SakerPathFiles
									.getFilesRecursiveByPath((SakerDirectory) f, DirectoryVisitPredicate.subFiles());
							try (UnsyncByteArrayOutputStream baos = new UnsyncByteArrayOutputStream()) {
								try (ZipOutputStream zos = new ZipOutputStream(baos)) {
									//don't compress, not necessary. performance is better
									zos.setMethod(ZipOutputStream.DEFLATED);
									zos.setLevel(0);
									for (Entry<SakerPath, SakerFile> fentry : cpfiles.entrySet()) {
										ZipEntry zentry = new ZipEntry(fentry.getKey().toString());
										zentry.setCreationTime(FILE_TIME_ZERO);
										zentry.setLastAccessTime(FILE_TIME_ZERO);
										zentry.setLastModifiedTime(FILE_TIME_ZERO);

										zos.putNextEntry(zentry);
										fentry.getValue().writeTo(zos);
										zos.closeEntry();
									}
								} catch (IOException e) {
									throw ObjectUtils.sneakyThrow(e);
								}
								filebytes = baos.toByteArrayRegion();
							}
						} else {
							try {
								filebytes = f.getBytes();
							} catch (IOException e) {
								throw ObjectUtils.sneakyThrow(e);
							}
						}
						try {
							MessageDigest hash = FileUtils.getDefaultFileHasher();
							hash.update(filebytes.getArray(), filebytes.getOffset(), filebytes.getLength());
							Path cachejarpath = jarcachedir.resolve(StringUtils.toHexString(hash.digest()) + ".jar");

							performJarCaching(fp, cachejarpath, cachedfiles, temp -> {
								try (UnsyncByteArrayInputStream in = new UnsyncByteArrayInputStream(filebytes)) {
									fp.writeToFile(in, temp);
								}
							});
						} catch (IOException e) {
							throw ObjectUtils.sneakyThrow(e);
						}
					}

					@Override
					public void visit(LocalFileLocation loc) {
						if (staticclasspaths.contains(loc)) {
							cachedfiles.add(loc);
							return;
						}
						SakerPath cppath = loc.getLocalPath();
						FileEntry fattrs;
						try {
							fattrs = fp.getFileAttributes(cppath);
						} catch (IOException e) {
							throw ObjectUtils.sneakyThrow(e);
						}
						if (fattrs.isDirectory()) {
							NavigableSet<SakerPath> dircontents = localdirectorycontents.get(loc);
							if (dircontents == null) {
								throw new ConcurrentModificationException(
										"Class path was concurrently modified: " + loc);
							}
							ByteArrayRegion filebytes;
							try (UnsyncByteArrayOutputStream baos = new UnsyncByteArrayOutputStream()) {
								try (ZipOutputStream zos = new ZipOutputStream(baos)) {
									ByteSink zipbytesink = ByteSink.valueOf(zos);

									//don't compress, not necessary. performance is better
									zos.setMethod(ZipOutputStream.DEFLATED);
									zos.setLevel(0);
									for (SakerPath childpath : dircontents) {
										SakerPath childabsolutepath = cppath.resolve(childpath);

										ZipEntry zentry = new ZipEntry(childpath.toString());
										zentry.setCreationTime(FILE_TIME_ZERO);
										zentry.setLastAccessTime(FILE_TIME_ZERO);
										zentry.setLastModifiedTime(FILE_TIME_ZERO);

										zos.putNextEntry(zentry);
										fp.writeTo(childabsolutepath, zipbytesink);
										zos.closeEntry();
									}
								} catch (IOException e) {
									throw ObjectUtils.sneakyThrow(e);
								}
								filebytes = baos.toByteArrayRegion();
							}
							try {
								MessageDigest hash = FileUtils.getDefaultFileHasher();
								hash.update(filebytes.getArray(), filebytes.getOffset(), filebytes.getLength());
								Path cachejarpath = jarcachedir
										.resolve(StringUtils.toHexString(hash.digest()) + ".jar");

								performJarCaching(fp, cachejarpath, cachedfiles, temp -> {
									try (UnsyncByteArrayInputStream in = new UnsyncByteArrayInputStream(filebytes)) {
										fp.writeToFile(in, temp);
									}
								});
							} catch (IOException e) {
								throw ObjectUtils.sneakyThrow(e);
							}
						} else {
							try {
								FileHashResult hash = fp.hash(cppath, FileUtils.DEFAULT_FILE_HASH_ALGORITHM);
								Path cachejarpath = jarcachedir
										.resolve(StringUtils.toHexString(hash.getHash()) + ".jar");

								performJarCaching(fp, cachejarpath, cachedfiles, temp -> {
									try (InputStream in = fp.openInputStream(LocalFileProvider.toRealPath(cppath))) {
										fp.writeToFile(in, temp);
									}
								});
							} catch (NoSuchAlgorithmException | IOException e) {
								throw ObjectUtils.sneakyThrow(e);
							}
						}
					}

					//suppress unused lock as it is only used for waiting
					@SuppressWarnings("try")
					private void performJarCaching(LocalFileProvider fp, Path cachejarpath,
							Set<LocalFileLocation> cachedfiles, IOConsumer<? super Path> contentwriter)
							throws IOException {
						Path temp = cachejarpath.resolveSibling(cachejarpath.getFileName() + "_load");
						//synchronize to avoid overlapped locking
						synchronized (("saker.java.compiler.jar_cache:" + cachejarpath).intern()) {
							try {
								fp.getFileAttributes(cachejarpath);
								//the file exists. we don't check if it is a regular file or not, as if it is not,
								//then somebody did something wrong with the storage. that should be fixed by the user
								cachedfiles.add(LocalFileLocation.create(SakerPath.valueOf(cachejarpath)));
							} catch (IOException e) {
								Path lockpath = cachejarpath.resolveSibling(cachejarpath.getFileName() + "_lock");
								try (FileChannel fc = FileChannel.open(lockpath, StandardOpenOption.CREATE,
										StandardOpenOption.WRITE, StandardOpenOption.READ)) {
									try (FileLock lock = fc.tryLock()) {
										if (lock == null) {
											//it is already locked by some other agent on the machine. might be when concurrently loading
											//in this case wait the other agent to load by locking again
											try (FileLock l2 = fc.lock()) {
												//do nothing. the lock waits for it to complete
												//the cached jar should be in a valid state when we receive the lock
												//can return without doing anything
												cachedfiles
														.add(LocalFileLocation.create(SakerPath.valueOf(cachejarpath)));
												return;
											}
										}
										//we got the lock, and should write to the temp file
										contentwriter.accept(temp);
										try {
											Files.move(temp, cachejarpath);
											cachedfiles.add(LocalFileLocation.create(SakerPath.valueOf(cachejarpath)));
										} catch (FileAlreadyExistsException fae) {
											// somebody put there something that we didn't notice
											//its fine, we use that
											cachedfiles.add(LocalFileLocation.create(SakerPath.valueOf(cachejarpath)));
											return;
										}
									}
								} finally {
									Files.deleteIfExists(temp);
								}
							}
						}
					}
				});
			}
		}
		return new ClassLoaderProcessorCreator(processorClassName, cachedfiles);
	}

	private static Map<FileLocation, ContentDescriptor> collectFileLocations(TaskContext taskcontext,
			JavaClassPath classpath, Map<String, SDKReference> sdkreferences,
			BiConsumer<LocalFileLocation, NavigableSet<SakerPath>> localdircontentsconsumer,
			Consumer<? super FileLocation> staticclasspathconsumer) throws IOException {
		return collectFileLocationsWithImplementationDependencyReporting(taskcontext, classpath,
				CompileFileTags.INPUT_CLASSPATH, sdkreferences, entry -> {
					//we need to add a dependency on the file locations returned from the ClassPathEntry instances
					//if we don't, then the entries can return different file locations while the classpath is still the same
					//in that case this task wouldn't re-run, and the processor creator would fail
					//e.g. if a nest bundle is moved from pending to local, the classpath doesn't change only the file location
					return taskcontext.getTaskUtilities()
							.getReportExecutionDependency(new ClassPathEntryFileLocationExecutionProperty(entry));
				}, localdircontentsconsumer, staticclasspathconsumer);
	}

	@Override
	public Task<? extends ProcessorCreator> createTask(ExecutionContext executioncontext) {
		return this;
	}

	/**
	 * @return The content descriptors may be <code>null</code>.
	 */
	public static Map<FileLocation, ContentDescriptor> collectFileLocationsWithImplementationDependencyReporting(
			TaskContext taskcontext, JavaClassPath classpath, Object tag, Map<String, SDKReference> sdks,
			Function<ClassPathEntry, FileLocation> classpathentryfilelocationhandler,
			BiConsumer<LocalFileLocation, NavigableSet<SakerPath>> localdircontentsconsumer,
			Consumer<? super FileLocation> staticlasspathconsumer) throws IOException {
		if (classpath == null) {
			return Collections.emptyMap();
		}
		Map<FileLocation, ContentDescriptor> result = new LinkedHashMap<>();
		classpath.accept(new ClassPathVisitor() {
			private Set<JavaCompilationWorkerTaskIdentifier> handledWorkerTaskIds = new HashSet<>();

			@Override
			public void visit(ClassPathReference classpath) {
				Collection<? extends ClassPathEntry> entries = classpath.getEntries();
				if (ObjectUtils.isNullOrEmpty(entries)) {
					SakerLog.warning().println("No class path entries found for: " + classpath);
					return;
				}
				for (ClassPathEntry entry : entries) {
					if (entry == null) {
						SakerLog.warning().println("Class path entry is null for: " + classpath);
						continue;
					}
					FileLocation filelocation = classpathentryfilelocationhandler.apply(entry);
					if (filelocation == null) {
						SakerLog.warning().println("No class path file location for: " + entry);
						continue;
					}
					if (entry.isStaticFile()) {
						staticlasspathconsumer.accept(filelocation);
					}
					handleFileLocation(filelocation);

					Collection<? extends ClassPathReference> additionalclasspaths = entry
							.getAdditionalClassPathReferences();
					if (!ObjectUtils.isNullOrEmpty(additionalclasspaths)) {
						JavaClassPathBuilder additionalcpbuilder = JavaClassPathBuilder.newBuilder();
						for (ClassPathReference additionalcp : additionalclasspaths) {
							additionalcpbuilder.addClassPath(additionalcp);
						}
						JavaClassPath additionalcp = additionalcpbuilder.build();
						additionalcp.accept(this);
					}
				}
			}

			@Override
			public void visit(CompilationClassPath classpath) {
				JavaCompilationWorkerTaskIdentifier workertaskid = classpath.getCompilationWorkerTaskIdentifier();
				if (!handledWorkerTaskIds.add(workertaskid)) {
					//don't get the task result to not install another dependency
					return;
				}
				TaskDependencyFuture<?> depresult = taskcontext.getTaskDependencyFuture(workertaskid);
				JavaCompilerWorkerTaskOutput output = (JavaCompilerWorkerTaskOutput) depresult.getFinished();
				SakerPath classdirpath = output.getClassDirectory();
				ExecutionFileLocation filelocation = ExecutionFileLocation.create(classdirpath);
				JavaClassPath outputcp = output.getClassPath();

				Object implversionkey = output.getImplementationVersionKey();
				if (implversionkey != null) {
					depresult.setTaskOutputChangeDetector(SakerJavaCompilerUtils
							.getCompilerOutputImplementationVersionKeyTaskOutputChangeDetector(implversionkey));
					depresult.setTaskOutputChangeDetector(
							SakerJavaCompilerUtils.getCompilerOutputClassPathTaskOutputChangeDetector(outputcp));
					result.put(filelocation, new SerializableContentDescriptor(implversionkey));
				} else {
					SakerDirectory classesdir = taskcontext.getTaskUtilities().resolveDirectoryAtPath(classdirpath);
					if (classesdir == null) {
						throw ObjectUtils.sneakyThrow(
								new FileNotFoundException("Compilation class directory not found: " + classesdir));
					}

					FileCollectionStrategy classfileadditiondep = RecursiveIgnoreCaseExtensionFileCollectionStrategy
							.create(classdirpath, "." + JavaTaskUtils.EXTENSION_CLASSFILE);
					NavigableMap<SakerPath, SakerFile> classfiles = taskcontext.getTaskUtilities()
							.collectFilesReportInputFileAndAdditionDependency(tag, classfileadditiondep);

					NavigableMap<SakerPath, ContentDescriptor> contentmap = SakerPathFiles.toFileContentMap(classfiles);
					result.put(filelocation, new MultiPathContentDescriptor(contentmap));
				}
				if (outputcp != null) {
					outputcp.accept(this);
				}
			}

			@Override
			public void visit(FileClassPath classpath) {
				FileLocation location = classpath.getFileLocation();
				handleFileLocation(location);
			}

			@Override
			public void visit(SDKClassPath classpath) {
				SDKPathReference sdkpathref = classpath.getSDKPathReference();
				SakerPath path = SDKSupportUtils.getSDKPathReferencePath(sdkpathref, sdks);
				LocalFileLocation fileloc = LocalFileLocation.create(path);
				result.put(fileloc, null);
				staticlasspathconsumer.accept(fileloc);
			}

			private ContentDescriptor handleExecutionFileLocation(SakerPath path, SakerFile cpfile) {
				if (cpfile instanceof SakerDirectory) {
					FileCollectionStrategy classfileadditiondep = RecursiveIgnoreCaseExtensionFileCollectionStrategy
							.create(path, "." + JavaTaskUtils.EXTENSION_CLASSFILE);
					NavigableMap<SakerPath, SakerFile> classfiles = taskcontext.getTaskUtilities()
							.collectFilesReportInputFileAndAdditionDependency(tag, classfileadditiondep);
					return new MultiPathContentDescriptor(SakerPathFiles.toFileContentMap(classfiles));
				}
				taskcontext.getTaskUtilities().reportInputFileDependency(tag, cpfile);
				return cpfile.getContentDescriptor();
			}

			private void handleFileLocation(FileLocation location) {
				if (result.containsKey(location)) {
					return;
				}
				ContentDescriptor[] cdres = { null };
				location.accept(new FileLocationVisitor() {
					@Override
					public void visit(ExecutionFileLocation loc) {
						SakerPath path = loc.getPath();
						SakerFile cpfile = taskcontext.getTaskUtilities().resolveAtPath(path);
						if (cpfile == null) {
							throw ObjectUtils
									.sneakyThrow(new FileNotFoundException("Class path file not found: " + path));
						}
						cdres[0] = handleExecutionFileLocation(path, cpfile);
					}

					@Override
					public void visit(LocalFileLocation loc) {
						SakerPath path = loc.getLocalPath();
						TaskExecutionUtilities taskutils = taskcontext.getTaskUtilities();
						ContentDescriptor cd = taskutils.getReportExecutionDependency(
								new LocalPathFileContentDescriptorExecutionProperty(path));
						if (cd == null) {
							throw ObjectUtils
									.sneakyThrow(new FileNotFoundException("Class path local file not found: " + path));
						}

						if (DirectoryContentDescriptor.INSTANCE.equals(cd)) {
							//the class path denotes a directory
							//add the dependencies on the files (all files, not only .class files)

							LocalDirectoryFilesExecutionProperty.PropertyValue pval = taskutils
									.getReportExecutionDependency(new LocalDirectoryFilesExecutionProperty(path));
							cdres[0] = new MultiPathContentDescriptor(pval.getContents());
							localdircontentsconsumer.accept(loc, pval.getContents().navigableKeySet());
						} else {
							cdres[0] = cd;
						}
					}
				});
				result.put(location, cdres[0]);
			}
		});
		return result;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(processorClassName);
		out.writeObject(classPath);
		SerialUtils.writeExternalMap(out, sdks);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		processorClassName = (String) in.readObject();
		classPath = (JavaClassPath) in.readObject();
		sdks = SerialUtils.readExternalSortedImmutableNavigableMap(in, SDKSupportUtils.getSDKNameComparator());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((classPath == null) ? 0 : classPath.hashCode());
		result = prime * result + ((processorClassName == null) ? 0 : processorClassName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProcessorCreatorTaskFactory other = (ProcessorCreatorTaskFactory) obj;
		if (classPath == null) {
			if (other.classPath != null)
				return false;
		} else if (!classPath.equals(other.classPath))
			return false;
		if (processorClassName == null) {
			if (other.processorClassName != null)
				return false;
		} else if (!processorClassName.equals(other.processorClassName))
			return false;
		if (sdks == null) {
			if (other.sdks != null)
				return false;
		} else if (!sdks.equals(other.sdks))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "["
				+ (processorClassName != null ? "processorClassName=" + processorClassName + ", " : "")
				+ (classPath != null ? "classPath=" + classPath + ", " : "") + (sdks != null ? "sdks=" + sdks : "")
				+ "]";
	}

	public static class LocalDirectoryFilesExecutionProperty
			implements ExecutionProperty<LocalDirectoryFilesExecutionProperty.PropertyValue>, Externalizable {
		private static final long serialVersionUID = 1L;

		public static class PropertyValue implements Externalizable {
			private static final long serialVersionUID = 1L;

			private NavigableMap<SakerPath, ContentDescriptor> contents;

			/**
			 * For {@link Externalizable}.
			 */
			public PropertyValue() {
			}

			public PropertyValue(NavigableMap<SakerPath, ContentDescriptor> contents) {
				this.contents = contents;
			}

			public NavigableMap<SakerPath, ? extends ContentDescriptor> getContents() {
				return contents;
			}

			@Override
			public void writeExternal(ObjectOutput out) throws IOException {
				SerialUtils.writeExternalMap(out, contents);
			}

			@Override
			public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
				contents = SerialUtils.readExternalImmutableNavigableMap(in);
			}

			@Override
			public int hashCode() {
				final int prime = 31;
				int result = 1;
				result = prime * result + ((contents == null) ? 0 : contents.hashCode());
				return result;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj)
					return true;
				if (obj == null)
					return false;
				if (getClass() != obj.getClass())
					return false;
				PropertyValue other = (PropertyValue) obj;
				if (!ObjectUtils.mapOrderedEquals(this.contents, other.contents)) {
					return false;
				}
				return true;
			}

			@Override
			public String toString() {
				return getClass().getSimpleName() + "[" + (contents != null ? "contents=" + contents : "") + "]";
			}

		}

		private SakerPath path;

		/**
		 * For {@link Externalizable}.
		 */
		public LocalDirectoryFilesExecutionProperty() {
		}

		public LocalDirectoryFilesExecutionProperty(SakerPath path) {
			this.path = path;
		}

		@Override
		public PropertyValue getCurrentValue(ExecutionContext executioncontext) throws Exception {
			NavigableMap<SakerPath, ContentDescriptor> result = new TreeMap<>();
			for (Entry<SakerPath, ? extends FileEntry> entry : LocalFileProvider.getInstance()
					.getDirectoryEntriesRecursively(path).entrySet()) {
				if (!entry.getValue().isRegularFile()) {
					continue;
				}
				SakerPath keypath = entry.getKey();
				SakerPath cpabspath = path.resolve(keypath);
				ContentDescriptor classfilecd = executioncontext.getExecutionPropertyCurrentValue(
						new LocalPathFileContentDescriptorExecutionProperty(cpabspath));
				if (classfilecd == null) {
					continue;
				}
				result.put(cpabspath, classfilecd);
			}
			return new PropertyValue(ImmutableUtils.unmodifiableNavigableMap(result));
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(path);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			path = (SakerPath) in.readObject();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((path == null) ? 0 : path.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			LocalDirectoryFilesExecutionProperty other = (LocalDirectoryFilesExecutionProperty) obj;
			if (path == null) {
				if (other.path != null)
					return false;
			} else if (!path.equals(other.path))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "[" + path + "]";
		}
	}
}