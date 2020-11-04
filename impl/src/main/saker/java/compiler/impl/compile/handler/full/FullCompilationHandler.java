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
package saker.java.compiler.impl.compile.handler.full;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;

import javax.annotation.processing.Processor;

import saker.build.file.SakerDirectory;
import saker.build.file.SakerFile;
import saker.build.file.content.ContentDescriptor;
import saker.build.file.content.HashContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.build.runtime.environment.SakerEnvironment;
import saker.build.runtime.execution.SakerLog;
import saker.build.task.TaskContext;
import saker.build.task.TaskDependencyFuture;
import saker.build.task.utils.StructuredTaskResult;
import saker.build.thirdparty.saker.rmi.connection.RMIVariables;
import saker.build.thirdparty.saker.rmi.exception.RMIRuntimeException;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.ReflectUtils;
import saker.java.compiler.api.classpath.ClassPathEntry;
import saker.java.compiler.api.classpath.ClassPathEntryInputFile;
import saker.java.compiler.api.classpath.ClassPathEntryInputFileVisitor;
import saker.java.compiler.api.classpath.ClassPathReference;
import saker.java.compiler.api.classpath.ClassPathVisitor;
import saker.java.compiler.api.classpath.CompilationClassPath;
import saker.java.compiler.api.classpath.FileClassPath;
import saker.java.compiler.api.classpath.JavaClassPath;
import saker.java.compiler.api.classpath.JavaClassPathBuilder;
import saker.java.compiler.api.classpath.JavaSourceDirectory;
import saker.java.compiler.api.classpath.SDKClassPath;
import saker.java.compiler.api.compile.JavaAnnotationProcessor;
import saker.java.compiler.api.compile.exc.JavaCompilationFailedException;
import saker.java.compiler.api.modulepath.CompilationModulePath;
import saker.java.compiler.api.modulepath.FileModulePath;
import saker.java.compiler.api.modulepath.JavaModulePath;
import saker.java.compiler.api.modulepath.ModulePathVisitor;
import saker.java.compiler.api.modulepath.SDKModulePath;
import saker.java.compiler.api.option.JavaAddExports;
import saker.java.compiler.api.option.JavaAddReads;
import saker.java.compiler.api.processor.ProcessorCreationContext;
import saker.java.compiler.api.processor.ProcessorCreator;
import saker.java.compiler.impl.RemoteJavaRMIProcess;
import saker.java.compiler.impl.compile.ClassPathIDEConfigurationEntry;
import saker.java.compiler.impl.compile.CompileFileTags;
import saker.java.compiler.impl.compile.InternalJavaCompilerOutput;
import saker.java.compiler.impl.compile.ModulePathIDEConfigurationEntry;
import saker.java.compiler.impl.compile.handler.CompilationHandler;
import saker.java.compiler.impl.compile.handler.ProcessorCreationContextImpl;
import saker.java.compiler.impl.compile.handler.incremental.RemoteCompiler;
import saker.java.compiler.impl.compile.handler.incremental.RemoteJavaCompilerCacheKey;
import saker.java.compiler.impl.options.OutputBytecodeManipulationOption;
import saker.java.compiler.impl.options.SimpleJavaSourceDirectoryOption;
import saker.java.compiler.impl.sdk.JavaSDKReference;
import saker.sdk.support.api.SDKPathReference;
import saker.sdk.support.api.SDKReference;
import saker.sdk.support.api.SDKSupportUtils;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileLocation;
import saker.std.api.file.location.FileLocationVisitor;
import saker.std.api.file.location.LocalFileLocation;
import saker.std.api.util.SakerStandardUtils;
import testing.saker.java.compiler.TestFlag;

public class FullCompilationHandler extends CompilationHandler {

	private static final Constructor<FullCompilationInvokerImpl> CONSTRUCTOR_FULL_COMPILATION_INVOKER_IMPL = ReflectUtils
			.getConstructorAssert(FullCompilationInvokerImpl.class);
	private TaskContext taskContext;
	private SakerDirectory outputClassDirectory;
	private SakerDirectory outputNativeHeaderDirectory;
	private SakerDirectory outputSourceDirectory;
	private SakerDirectory outputResourceDirectory;

	private JavaClassPath classPath;
	private JavaModulePath modulePath;
	private JavaClassPath bootClassPath;
	private Collection<JavaAnnotationProcessor> annotationProcessors;

	private List<String> parameters;
	private String sourceVersionName;
	private String targetVersionName;
	private Collection<JavaAddExports> addExports;
	private Collection<JavaAddReads> addReads;
	private boolean generateNativeHeaders;

	private Map<FileLocation, ClassPathIDEConfigurationEntry> classPathEntryMap;
	private Map<FileLocation, ClassPathIDEConfigurationEntry> bootClassPathEntryMap;
	private Map<FileLocation, ModulePathIDEConfigurationEntry> modulePathEntryMap;

	private Map<String, String> annotationProcessorOptions;
	private SDKReference compilationJavaSDKReference;
	private NavigableMap<SakerPath, SakerFile> sourceFiles;

	private NavigableMap<SakerPath, ContentDescriptor> outputFileContents = Collections.emptyNavigableMap();
	private NavigableMap<SakerPath, ContentDescriptor> processorAccessedFileContents = Collections.emptyNavigableMap();
	private ContentDescriptor implementationHash;
	private ContentDescriptor abiVersionKeyHash;
	private NavigableMap<String, SakerPath> processorInputLocations;
	private OutputBytecodeManipulationOption bytecodeManipulation;

	private String outModuleName;
	private NavigableMap<String, SDKReference> sdkReferences;
	private boolean parameterNames;
	private Set<String> debugInfos;

	public FullCompilationHandler(TaskContext taskcontext, SakerDirectory outputClassDirectory,
			SakerDirectory outputNativeHeaderDirectory, SakerDirectory outputSourceDirectory,
			SakerDirectory outputOtherDirectory) {
		this.taskContext = taskcontext;
		this.outputClassDirectory = outputClassDirectory;
		this.outputNativeHeaderDirectory = outputNativeHeaderDirectory;
		this.outputSourceDirectory = outputSourceDirectory;
		this.outputResourceDirectory = outputOtherDirectory;
	}

	public void setPassOptions(String sourceversionname, String targetversionname,
			SDKReference compilationJavaSDKReference, List<String> parameters, JavaClassPath classpath,
			JavaModulePath modulepath, Map<String, String> annotationprocessoroptions,
			Collection<JavaAnnotationProcessor> annotationprocessors, Collection<JavaAddExports> addexports,
			Collection<JavaAddReads> addreads, JavaClassPath bootclasspath,
			NavigableMap<SakerPath, SakerFile> sourcefiles, NavigableMap<String, SakerPath> processorInputLocations,
			OutputBytecodeManipulationOption bytecodeManipulation, NavigableMap<String, SDKReference> sdkrefs,
			boolean parameterNames, Set<String> debugInfos) {
		this.sourceFiles = sourcefiles;
		this.bytecodeManipulation = bytecodeManipulation;
		this.sdkReferences = sdkrefs;
		this.parameterNames = parameterNames;
		this.debugInfos = debugInfos;
		if (parameters == null) {
			parameters = Collections.emptyList();
		}
		if (annotationprocessoroptions == null) {
			annotationprocessoroptions = Collections.emptyMap();
		}
		if (annotationprocessors == null) {
			annotationprocessors = Collections.emptyList();
		}
		if (addexports == null) {
			addexports = Collections.emptyList();
		}
		if (addreads == null) {
			addreads = Collections.emptyList();
		}

		this.sourceVersionName = sourceversionname;
		this.targetVersionName = targetversionname;

		this.compilationJavaSDKReference = compilationJavaSDKReference;
		this.parameters = parameters;
		this.classPath = classpath;
		this.modulePath = modulepath;
		this.annotationProcessorOptions = annotationprocessoroptions;
		this.annotationProcessors = annotationprocessors;
		this.addExports = addexports;
		this.addReads = addreads;
		//can be null
		this.bootClassPath = bootclasspath;

		this.processorInputLocations = processorInputLocations;
	}

	public void setGenerateNativeHeaders(boolean generateNativeHeaders) {
		this.generateNativeHeaders = generateNativeHeaders;
	}

	@Override
	public Collection<? extends ClassPathIDEConfigurationEntry> getClassPathIDEConfigurationEntries() {
		Map<FileLocation, ClassPathIDEConfigurationEntry> entrymap = classPathEntryMap;
		if (entrymap == null) {
			return Collections.emptySet();
		}
		return entrymap.values();
	}

	@Override
	public Collection<? extends ClassPathIDEConfigurationEntry> getBootClassPathIDEConfigurationEntries() {
		Map<FileLocation, ClassPathIDEConfigurationEntry> entrymap = bootClassPathEntryMap;
		if (entrymap == null) {
			return Collections.emptySet();
		}
		return entrymap.values();
	}

	@Override
	public Collection<? extends ModulePathIDEConfigurationEntry> getModulePathIDEConfigurationEntries() {
		Map<FileLocation, ModulePathIDEConfigurationEntry> entrymap = modulePathEntryMap;
		if (entrymap == null) {
			return Collections.emptySet();
		}
		return entrymap.values();
	}

	public NavigableMap<SakerPath, ContentDescriptor> getOutputFiles() {
		return outputFileContents;
	}

	public NavigableMap<SakerPath, ContentDescriptor> getProcessorAccessedFileContents() {
		return processorAccessedFileContents;
	}

	public ContentDescriptor getAbiVersionKeyHash() {
		return abiVersionKeyHash;
	}

	public ContentDescriptor getImplementationVersionKeyHash() {
		return implementationHash;
	}

	@Override
	public String getModuleName() {
		return outModuleName;
	}

	@Override
	public void build() throws Exception {
		if (TestFlag.ENABLED) {
			sourceFiles.keySet().forEach(TestFlag.metric()::javacCompilingFile);
		}

		System.out.println("Performing full Java compilation.");

		classPathEntryMap = collectClassPathEntryMap(classPath);
		bootClassPathEntryMap = collectClassPathEntryMap(bootClassPath);
		modulePathEntryMap = collectModulePathEntryMap(modulePath);
		if (sourceFiles.isEmpty()) {
			SakerLog.log().verbose().println("No Java source files found.");
			SakerLog.log().verbose().println();
			return;
		}
		Set<FileLocation> classpathfiles = classPathEntryMap.keySet();
		Set<FileLocation> bootclasspathfiles = classPathEntryMap.keySet();
		Set<SakerPath> classpathpaths = new LinkedHashSet<>();
		Set<SakerPath> bootclasspathpaths = new LinkedHashSet<>();
		Set<SakerPath> modulepathpaths = new LinkedHashSet<>();
		for (FileLocation cpfile : classpathfiles) {
			addFileMirrorPathToPaths("Class path", cpfile, classpathpaths, CompileFileTags.INPUT_CLASSPATH);
		}
		for (FileLocation cpfile : bootclasspathfiles) {
			addFileMirrorPathToPaths("Boot class path", cpfile, bootclasspathpaths,
					CompileFileTags.INPUT_BOOT_CLASSPATH);
		}
		Set<FileLocation> modulepathfiles = modulePathEntryMap.keySet();
		for (FileLocation mpfile : modulepathfiles) {
			addFileMirrorPathToPaths("Module path", mpfile, modulepathpaths, CompileFileTags.INPUT_MODULEPATH);
		}

		boolean[] nocmdlineclasspath = { false };
		boolean[] nocmdlinebootclasspath = { false };

		Collection<String> options = createOptions(parameters, sourceVersionName, targetVersionName, addExports,
				addReads, bootclasspathpaths, classpathpaths, modulepathpaths, parameterNames, debugInfos,
				nocmdlineclasspath, nocmdlinebootclasspath);
		boolean allowcmdlinebootclasspath = bootClassPath == null || !nocmdlinebootclasspath[0];

		if (!ObjectUtils.isNullOrEmpty(this.annotationProcessorOptions)) {
			StringBuilder sb = new StringBuilder();
			for (Entry<String, String> entry : this.annotationProcessorOptions.entrySet()) {
				sb.setLength(0);
				sb.append("-A");
				sb.append(entry.getKey());
				String v = entry.getValue();
				if (v != null) {
					sb.append('=');
					sb.append(v);
				}
				options.add(sb.toString());
			}
		}

		List<Processor> processors;
		if (!ObjectUtils.isNullOrEmpty(annotationProcessors)) {
			processors = new ArrayList<>();
			ProcessorCreationContext proccreationcontext = new ProcessorCreationContextImpl(taskContext);
			for (JavaAnnotationProcessor apr : annotationProcessors) {
				ProcessorCreator proccreator = apr.getProcessor().getCreator();
				try {
					processors.add(proccreator.create(proccreationcontext));
				} catch (Exception e) {
					throw new IllegalArgumentException("Failed to instantiate processor: " + proccreator.getName(), e);
				}
			}
		} else {
			//set empty processors to bypass default discovery mechanism
			processors = Collections.emptyList();
		}

		try {
			if (shouldExternallyCompile()) {
				//externally compile
				if (TestFlag.ENABLED) {
					TestFlag.metric().externallyCompiling();
				}
				SakerPath javaexe = compilationJavaSDKReference.getPath(JavaSDKReference.PATH_JAVA_EXE);
				if (javaexe == null) {
					throw new NullPointerException("Java SDK doesn't contain java.exe path. ("
							+ compilationJavaSDKReference + " : " + JavaSDKReference.PATH_JAVA_EXE + ")");
				}
				SakerEnvironment env = taskContext.getExecutionContext().getEnvironment();
				RemoteJavaCompilerCacheKey key = new RemoteJavaCompilerCacheKey(env, javaexe.toString());
				RemoteCompiler remotecompiler;
				try {
					remotecompiler = env.getCachedData(key);
				} catch (Exception e) {
					throw new JavaCompilationFailedException("Failed to use JDK: " + compilationJavaSDKReference, e);
				}
				RemoteJavaRMIProcess proc = remotecompiler.getRmiProcess();

				try (RMIVariables vars = proc.getConnection().newVariables()) {
					FullCompilationInvoker invoker;
					try {
						invoker = (FullCompilationInvoker) vars
								.newRemoteInstance(CONSTRUCTOR_FULL_COMPILATION_INVOKER_IMPL);
					} catch (InvocationTargetException e) {
						throw new JavaCompilationFailedException(
								"Failed to create compilation invoker in remote process at JDK: "
										+ compilationJavaSDKReference,
								e);
					}
					invokeCompilation(invoker, options, processors, nocmdlineclasspath[0], allowcmdlinebootclasspath);
					return;
				} catch (RMIRuntimeException e) {
					throw new JavaCompilationFailedException(
							"Failed to communicate with compilation invoker in remote process at JDK: "
									+ compilationJavaSDKReference,
							e);
				}
			}
			invokeCompilation(new FullCompilationInvokerImpl(), options, processors, nocmdlineclasspath[0],
					allowcmdlinebootclasspath);
		} catch (JavaCompilationFailedException e) {
			throw e;
		} catch (Exception | com.sun.tools.javac.util.FatalError e) {
			throw new JavaCompilationFailedException("Unexpected error.", e);
		}
	}

	private void addFileMirrorPathToPaths(String pathdisplayname, FileLocation cpfile, Set<SakerPath> paths,
			Object inputdependencytag) {
		cpfile.accept(new FileLocationVisitor() {
			@Override
			public void visit(ExecutionFileLocation loc) {
				SakerPath path = loc.getPath();
				SakerFile file = taskContext.getTaskUtilities().resolveAtPath(path);
				try {
					if (file == null) {
						throw new FileNotFoundException(pathdisplayname + " file not found: " + path);
					}
					taskContext.reportInputFileDependency(inputdependencytag, path, file.getContentDescriptor());
					paths.add(SakerPath.valueOf(taskContext.mirror(file)));
				} catch (IOException e) {
					throw ObjectUtils.sneakyThrow(e);
				}
			}

			@Override
			public void visit(LocalFileLocation loc) {
				SakerPath path = loc.getLocalPath();
				taskContext.getTaskUtilities().getReportExecutionDependency(SakerStandardUtils
						.createLocalFileContentDescriptorExecutionProperty(path, taskContext.getTaskId()));
				paths.add(path);
			}
		});
	}

	private boolean shouldExternallyCompile() {
		if (TestFlag.ENABLED) {
			if (TestFlag.metric().forceExternalCompilation()) {
				return true;
			}
		}
		return !JavaSDKReference.getCurrent().equals(compilationJavaSDKReference);
	}

	private void invokeCompilation(FullCompilationInvoker invoker, Collection<String> options,
			List<Processor> processors, boolean nocmdlineclasspath, boolean allowcommandlinebootclasspath)
			throws IOException, JavaCompilationFailedException {
		FullCompilationDirector director = new FullCompilationDirector(invoker);
		director.setOptions(taskContext, outputClassDirectory, outputNativeHeaderDirectory, outputSourceDirectory,
				outputResourceDirectory, generateNativeHeaders, options, processors, sourceFiles,
				processorInputLocations, bytecodeManipulation, nocmdlineclasspath, allowcommandlinebootclasspath);
		director.invokeCompilation();
		this.outputFileContents = director.getOutputFileContents();
		this.processorAccessedFileContents = director.getProcessorAccessedFileContents();
		this.implementationHash = HashContentDescriptor.createWithHash(director.getImplementationVersionKeyHash());
		this.abiVersionKeyHash = HashContentDescriptor.createWithHash(director.getAbiVersionKeyHash());
		this.outModuleName = director.getModuleName();
	}

	private Map<FileLocation, ModulePathIDEConfigurationEntry> collectModulePathEntryMap(JavaModulePath modulePath) {
		if (modulePath == null) {
			return Collections.emptyMap();
		}
		//TODO better incrementality for depencency results
		Map<FileLocation, ModulePathIDEConfigurationEntry> result = new LinkedHashMap<>();
		modulePath.accept(new ModulePathVisitor() {
			@Override
			public void visit(CompilationModulePath modulepath) {
				TaskDependencyFuture<?> depresult = taskContext
						.getTaskDependencyFuture(modulepath.getCompilationWorkerTaskIdentifier());
				InternalJavaCompilerOutput output = (InternalJavaCompilerOutput) depresult.getFinished();
				ExecutionFileLocation filelocation = ExecutionFileLocation.create(output.getClassDirectory());
				if (result.containsKey(filelocation)) {
					return;
				}

				ModulePathIDEConfigurationEntry ideconfig = new ModulePathIDEConfigurationEntry(filelocation);
				ideconfig.setSourceDirectories(output.getSourceDirectories());
				result.put(filelocation, ideconfig);
				JavaModulePath outputmp = output.getModulePath();
				if (outputmp != null) {
					outputmp.accept(this);
				}
			}

			@Override
			public void visit(FileModulePath modulepath) {
				FileLocation location = modulepath.getFileLocation();
				result.put(location, new ModulePathIDEConfigurationEntry(location));
			}

			@Override
			public void visit(SDKModulePath modulepath) {
				SakerPath path = SDKSupportUtils.getSDKPathReferencePath(modulepath.getSDKPathReference(),
						sdkReferences);
				LocalFileLocation filelocation = LocalFileLocation.create(path);
				result.put(filelocation, new ModulePathIDEConfigurationEntry(filelocation));
			}
		});
		return result;
	}

	private Map<FileLocation, ClassPathIDEConfigurationEntry> collectClassPathEntryMap(JavaClassPath classPath) {
		if (classPath == null) {
			return Collections.emptyMap();
		}
		//TODO better incrementality for depencency results
		Map<FileLocation, ClassPathIDEConfigurationEntry> result = new LinkedHashMap<>();
		classPath.accept(new ClassPathVisitor() {
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
					ClassPathEntryInputFile inputfile = entry.getInputFile();
					if (inputfile == null) {
						SakerLog.warning().println("No class path input for: " + entry);
						continue;
					}
					ClassPathVisitor cpvisitor = this;
					inputfile.accept(new ClassPathEntryInputFileVisitor() {
						@Override
						public void visit(FileClassPath inputfilecp) {
							FileLocation filelocation = inputfilecp.getFileLocation();
							if (filelocation == null) {
								SakerLog.warning().println("No class path file location for: " + entry);
								return;
							}
							if (result.containsKey(filelocation)) {
								return;
							}
							Collection<? extends JavaSourceDirectory> sourcedirs = entry.getSourceDirectories();
							Collection<JavaSourceDirectory> sourcediroptions;
							if (ObjectUtils.isNullOrEmpty(sourcedirs)) {
								sourcediroptions = null;
							} else {
								sourcediroptions = new LinkedHashSet<>();
								for (JavaSourceDirectory sdir : sourcedirs) {
									sourcediroptions.add(
											new SimpleJavaSourceDirectoryOption(sdir.getDirectory(), sdir.getFiles()));
								}
							}
							ClassPathIDEConfigurationEntry value = new ClassPathIDEConfigurationEntry(filelocation,
									sourcediroptions, entry.getSourceAttachment(), entry.getDocumentationAttachment());
							value.setDisplayName(entry.getDisplayName());
							result.put(filelocation, value);

							Collection<? extends ClassPathReference> additionalclasspaths = entry
									.getAdditionalClassPathReferences();
							if (!ObjectUtils.isNullOrEmpty(additionalclasspaths)) {
								JavaClassPathBuilder additionalcpbuilder = JavaClassPathBuilder.newBuilder();
								for (ClassPathReference additionalcp : additionalclasspaths) {
									additionalcpbuilder.addClassPath(additionalcp);
								}
								JavaClassPath additionalcp = additionalcpbuilder.build();
								additionalcp.accept(cpvisitor);
							}
						}

						@Override
						public void visit(SDKClassPath classpath) {
							visitSDKPathReference(result, classpath.getSDKPathReference(), entry.getSourceDirectories(),
									entry.getSourceAttachment(), entry.getDocumentationAttachment());
						}
					});
				}
			}

			@Override
			public void visit(CompilationClassPath classpath) {
				TaskDependencyFuture<?> depresult = taskContext
						.getTaskDependencyFuture(classpath.getCompilationWorkerTaskIdentifier());
				InternalJavaCompilerOutput output = (InternalJavaCompilerOutput) depresult.getFinished();
				ExecutionFileLocation filelocation = ExecutionFileLocation.create(output.getClassDirectory());
				if (result.containsKey(filelocation)) {
					return;
				}

				ClassPathIDEConfigurationEntry ideconfig = new ClassPathIDEConfigurationEntry(filelocation,
						output.getSourceDirectories(), null, null);
				if (output.hadAnnotationProcessors()) {
					ideconfig.setSourceGenDirectory(output.getSourceGenDirectory());
				}
				result.put(filelocation, ideconfig);
				JavaClassPath outputcp = output.getClassPath();
				if (outputcp != null) {
					outputcp.accept(this);
				}
			}

			@Override
			public void visit(FileClassPath classpath) {
				FileLocation location = classpath.getFileLocation();
				result.put(location, new ClassPathIDEConfigurationEntry(location));
			}

			@Override
			public void visit(SDKClassPath classpath) {
				SDKPathReference sdkpathref = classpath.getSDKPathReference();
				visitSDKPathReference(result, sdkpathref, null, null, null);
			}

			private void visitSDKPathReference(Map<FileLocation, ClassPathIDEConfigurationEntry> result,
					SDKPathReference sdkpathref, Collection<? extends JavaSourceDirectory> srcdirs,
					StructuredTaskResult sourceattachment, StructuredTaskResult docattachment) {
				SakerPath path = SDKSupportUtils.getSDKPathReferencePath(sdkpathref, sdkReferences);
				LocalFileLocation fileloc = LocalFileLocation.create(path);
				result.put(fileloc, new ClassPathIDEConfigurationEntry(ClassPathEntryInputFile.create(sdkpathref),
						srcdirs, sourceattachment, docattachment));
			}
		});
		return result;
	}
}
