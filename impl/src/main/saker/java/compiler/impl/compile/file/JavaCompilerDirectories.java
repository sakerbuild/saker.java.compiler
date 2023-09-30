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
package saker.java.compiler.impl.compile.file;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import javax.annotation.processing.FilerException;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject.Kind;

import saker.build.exception.InvalidPathFormatException;
import saker.build.file.ByteArraySakerFile;
import saker.build.file.DirectoryVisitPredicate;
import saker.build.file.SakerDirectory;
import saker.build.file.SakerFile;
import saker.build.file.SakerFileBase;
import saker.build.file.content.ContentDescriptor;
import saker.build.file.content.HashContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.SakerPathFiles;
import saker.build.task.TaskContext;
import saker.build.task.TaskExecutionUtilities;
import saker.build.thirdparty.saker.rmi.annot.transfer.RMIWrap;
import saker.build.thirdparty.saker.rmi.io.RMIObjectInput;
import saker.build.thirdparty.saker.rmi.io.RMIObjectOutput;
import saker.build.thirdparty.saker.rmi.io.wrap.RMIWrapper;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.function.LazySupplier;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayInputStream;
import saker.build.thirdparty.saker.util.rmi.wrap.RMITreeMapSerializeKeyRemoteValueWrapper;
import saker.java.compiler.impl.JavaUtil;
import saker.java.compiler.impl.compile.file.IncrementalDirectoryPaths.IncrementalDirectoryLocation.IncrementalDirectoryFile;
import saker.java.compiler.impl.compile.handler.ExternalizableLocation;
import saker.java.compiler.impl.compile.handler.invoker.IncrementalCompilationDirector;
import saker.java.compiler.impl.options.OutputBytecodeManipulationOption;
import testing.saker.java.compiler.TestFlag;

@RMIWrap(JavaCompilerDirectories.JavaCompilerDirectoriesRMIWrapper.class)
public class JavaCompilerDirectories implements IncrementalDirectoryPaths {
	private static final Pattern PATTERN_DOT = Pattern.compile("[.]+");

	protected final TaskContext taskContext;
	protected final TaskExecutionUtilities taskUtils;

	private NavigableMap<ExternalizableLocation, DirectoryLocationImpl> dirLocations = new TreeMap<>();
	private SakerDirectory resourceOutputDirectory = null;

	private ConcurrentNavigableMap<String, SakerFile> outputClassFiles = new ConcurrentSkipListMap<>();
	private ConcurrentNavigableMap<SakerPath, SakerFile> allOutputFiles = new ConcurrentSkipListMap<>();

	private OutputBytecodeManipulationOption bytecodeManipulation = null;

	private NavigableMap<String, IncrementalDirectoryLocation> modulePathLocations = new TreeMap<>();

	private boolean noCommandLineClassPath;

	//true by default
	private boolean allowCommandLineBootClassPath = true;

	public JavaCompilerDirectories(TaskContext taskContext) {
		this.taskContext = taskContext;
		this.taskUtils = taskContext.getTaskUtilities();
	}

	public void setNoCommandLineClassPath(boolean noCommandLineClassPath) {
		this.noCommandLineClassPath = noCommandLineClassPath;
	}

	public void setAllowCommandLineBootClassPath(boolean allowCommandLineBootClassPath) {
		this.allowCommandLineBootClassPath = allowCommandLineBootClassPath;
	}

	@Override
	public boolean isNoCommandLineClassPath() {
		return noCommandLineClassPath;
	}

	@Override
	public boolean isAllowCommandLineBootClassPath() {
		return allowCommandLineBootClassPath;
	}

	public void addModulePath(String modulename, SakerDirectory directory) {
		if (modulePathLocations.containsKey(modulename)) {
			throw new IllegalArgumentException("Duplicate module on module path: " + modulename);
		}
		modulePathLocations.put(modulename, new DirectoryLocationImpl(directory));
	}

	public void addModulePaths(Map<String, SakerDirectory> modulepathdirs) {
		if (ObjectUtils.isNullOrEmpty(modulepathdirs)) {
			return;
		}
		for (Entry<String, SakerDirectory> entry : modulepathdirs.entrySet()) {
			addModulePath(entry.getKey(), entry.getValue());
		}
	}

	public void setBytecodeManipulation(OutputBytecodeManipulationOption bytecodeManipulation) {
		this.bytecodeManipulation = bytecodeManipulation;
	}

	@Override
	public NavigableMap<String, ? extends IncrementalDirectoryLocation> getModulePathLocations() {
		return modulePathLocations;
	}

	@Override
	public NavigableMap<ExternalizableLocation, ? extends IncrementalDirectoryLocation> getDirectoryLocations() {
		return dirLocations;
	}

	public void setResourceOutputDirectory(SakerDirectory directory) {
		this.resourceOutputDirectory = directory;
	}

	public SakerDirectory getResourceOutputDirectory() {
		return resourceOutputDirectory;
	}

	public void addDirectory(Location location, SakerDirectory dir) {
		addDirectory(new ExternalizableLocation(location), dir);
	}

	public void addDirectory(ExternalizableLocation extloc, SakerDirectory directory) {
		Objects.requireNonNull(directory, "directory");
		NavigableMap<SakerPath, SakerDirectory> dirs = getDirectoriesModifiable(extloc);
		if (extloc.isOutputLocation() && !dirs.isEmpty()) {
			throw new IllegalArgumentException(
					"Cannot specify multiple output directories for location: " + extloc.getName());
		}
		dirs.put(SakerPathFiles.requireAbsolutePath(directory), directory);
	}

	public void addDirectory(ExternalizableLocation extloc,
			NavigableMap<SakerPath, ? extends SakerDirectory> directories) {
		if (ObjectUtils.isNullOrEmpty(directories)) {
			return;
		}
		NavigableMap<SakerPath, SakerDirectory> dirs = getDirectoriesModifiable(extloc);
		if (extloc.isOutputLocation() && (!dirs.isEmpty() || directories.size() > 1)) {
			throw new IllegalArgumentException(
					"Cannot specify multiple output directories for location: " + extloc.getName());
		}
		for (Entry<SakerPath, ? extends SakerDirectory> entry : directories.entrySet()) {
			dirs.put(SakerPathFiles.requireAbsolutePath(entry.getKey()), entry.getValue());
		}
	}

	public SakerFile getExistingResourceFile(ExternalizableLocation location, SakerPath resourcepath) {
		Collection<SakerDirectory> dirs = getLocationDirectories(location);
		if (!dirs.isEmpty()) {
			for (SakerDirectory dir : dirs) {
				SakerFile got = taskUtils.resolveAtRelativePath(dir, resourcepath);
				if (got != null) {
					return got;
				}
			}
		}
		if (resourceOutputDirectory != null) {
			SakerDirectory dir = resourceOutputDirectory.getDirectory(location.getName());
			if (dir != null) {
				SakerFile got = taskUtils.resolveAtRelativePath(dir, resourcepath);
				if (got != null) {
					return got;
				}
			}
		}
		return null;
	}

	public void putResourceOutputFile(ExternalizableLocation location, SakerPath resourcePath, SakerFile file)
			throws IOException {
		Collection<SakerDirectory> dirs = getLocationDirectories(location);
		SakerDirectory outputdir;
		if (!dirs.isEmpty()) {
			outputdir = dirs.iterator().next();
		} else if (resourceOutputDirectory != null) {
			outputdir = resourceOutputDirectory.getDirectoryCreate(location.getName());
		} else {
			throw new IOException("Location not found: " + location);
		}
		if (resourcePath.getNameCount() > 1) {
			outputdir = getDirectoryForRelativeName(outputdir, resourcePath.getParent());
		}
		outputdir.add(file);
	}

	public SakerFile getExistingSourceClassOutputFile(Location location, String className, Kind kind) {
		Collection<SakerDirectory> dirs = getLocationDirectories(location);
		if (dirs.isEmpty()) {
			return null;
		}
		String packagename = classPackageOf(className);
		String filename = classSimpleNameOf(className) + kind.extension;
		for (SakerDirectory dir : dirs) {
			SakerDirectory parentdir = getPackageSubDirectory(dir, packagename);
			if (parentdir == null) {
				continue;
			}
			SakerFile prevfile = parentdir.get(filename);
			return prevfile;
		}
		return null;
	}

	public void putJavaSakerFileForOutput(Location location, String pkg, SakerFile file) throws FilerException {
		Collection<SakerDirectory> dirs = getLocationDirectories(location);
		if (dirs.isEmpty()) {
			throw new FilerException(location + " location not found.");
		}
		SakerDirectory dir = dirs.iterator().next();
		SakerDirectory parentdir = getPackageSubDirectoryCreate(dir, pkg);
		parentdir.add(file);
	}

	@Override
	public SakerPath putJavaFileForOutput(ExternalizableLocation location, String classname, String extension,
			OutputFileObject output) {
		DirectoryLocationImpl dirs = dirLocations.get(location);
		if (dirs == null) {
			return null;
		}
		Entry<SakerPath, SakerDirectory> firstdirentry = dirs.directories.firstEntry();
		if (firstdirentry == null) {
			return null;
		}
		String[] split = splitClassNameForFilePath(classname, extension);
		if (split == null) {
			return null;
		}

		String filename = split[split.length - 1];
		boolean classfileoutput = location.equals(ExternalizableLocation.LOCATION_CLASS_OUTPUT)
				&& Kind.CLASS.extension.equals(extension);
		SakerFile mfile = LazyByteArraySakerFile.create(filename, classfileoutput ? bytecodeManipulation : null,
				output);
		SakerDirectory dir = SakerPathFiles.resolveDirectoryAtRelativePathNamesCreate(firstdirentry.getValue(),
				ImmutableUtils.unmodifiableArrayList(split, 0, split.length - 1));
		dir.add(mfile);
		SakerPath resultpath = mfile.getSakerPath();
		if (classfileoutput) {
			outputClassFiles.put(classname, mfile);
		}
		allOutputFiles.put(resultpath, mfile);

		if (TestFlag.ENABLED) {
			SakerPath expectedpath = firstdirentry.getKey().resolve(split);
			if (!resultpath.equals(expectedpath)) {
				throw new AssertionError("Inconsistent paths: " + resultpath + " with " + expectedpath);
			}
		}
		return resultpath;
	}

	@Override
	public void bulkPutJavaFilesForOutput(NavigableMap<OutputJavaFileData, ByteArrayRegion> data) {
		//directory instance caches
		DirectoryLocationImpl dirs = null;
		ExternalizableLocation dirloc = null;
		for (Entry<OutputJavaFileData, ByteArrayRegion> entry : data.entrySet()) {
			OutputJavaFileData filedata = entry.getKey();
			SakerPath path = filedata.getPath();

			ExternalizableLocation loc = filedata.getLocation();
			if (!loc.equals(dirloc)) {
				//location changed, get from cache again
				dirs = dirLocations.get(loc);
				dirloc = loc;
				if (dirs == null) {
					throw new IllegalArgumentException(
							"Invalid output java file, no location found: " + loc + " : " + path);
				}
			}
			Entry<SakerPath, SakerDirectory> firstdirentry = dirs.directories.firstEntry();
			if (firstdirentry == null) {
				throw new IllegalArgumentException(
						"Invalid output java file, no directory found: " + loc + " : " + path);
			}
			SakerPath dirpath = firstdirentry.getKey();
			if (!path.startsWith(dirpath)) {
				throw new IllegalArgumentException(
						"Output file path: " + path + " doesn't start with directory path: " + dirpath);
			}
			SakerPath reldirpath = path.subPath(dirpath.getNameCount(), path.getNameCount() - 1);

			String filename = path.getFileName();
			boolean classfileoutput = loc.equals(ExternalizableLocation.LOCATION_CLASS_OUTPUT)
					&& filename.endsWith(Kind.CLASS.extension);
			ByteArrayRegion bytes = entry.getValue();
			if (classfileoutput && JavaUtil.isBytecodeManipulationAffects(filename, bytecodeManipulation)) {
				bytes = JavaUtil.performBytecodeManipulation(filename, bytes, bytecodeManipulation);
			}
			ByteArraySakerFile mfile = new ByteArraySakerFile(filename, bytes);

			SakerDirectory dir = SakerPathFiles.resolveDirectoryAtRelativePathCreate(firstdirentry.getValue(),
					reldirpath);
			dir.add(mfile);

			if (classfileoutput) {
				outputClassFiles.put(filedata.getClassName(), mfile);
			}
			allOutputFiles.put(path, mfile);
		}

	}

	public static String[] splitClassNameForFilePath(String classname, String extension) {
		String[] split = PATTERN_DOT.split(classname);
		if (split.length == 0) {
			//sanity check
			return null;
		}
		if (!ObjectUtils.isNullOrEmpty(extension)) {
			split[split.length - 1] += extension;
		}
		return split;
	}

	@Override
	public SakerPath putFileForOutput(ExternalizableLocation location, String packagename, String relativename,
			OutputFileObject output) {
		DirectoryLocationImpl dirs = dirLocations.get(location);
		if (dirs == null) {
			return null;
		}
		Entry<SakerPath, SakerDirectory> firstdirentry = dirs.directories.firstEntry();
		if (firstdirentry == null) {
			return null;
		}
		SakerPath resourcepath = IncrementalCompilationDirector.toPackageRelativePath(packagename, relativename);

		SakerDirectory dir = firstdirentry.getValue();
		if (resourcepath.getNameCount() > 1) {
			dir = SakerPathFiles.resolveDirectoryAtRelativePathCreate(dir, resourcepath.getParent());
		}
		LazyByteArraySakerFile mfile = new LazyByteArraySakerFile(resourcepath.getFileName(), output);
		dir.add(mfile);
		SakerPath resultpath = mfile.getSakerPath();
		allOutputFiles.put(resultpath, mfile);

		if (TestFlag.ENABLED) {
			SakerPath expectedpath = firstdirentry.getKey().resolve(resourcepath);
			if (!resultpath.equals(expectedpath)) {
				throw new AssertionError("Inconsistent paths: " + resultpath + " with " + expectedpath);
			}
		}
		return resultpath;
	}

	public ConcurrentNavigableMap<String, ? extends SakerFile> getOutputClassFiles() {
		return outputClassFiles;
	}

	public ConcurrentNavigableMap<SakerPath, ? extends SakerFile> getAllOutputFiles() {
		return allOutputFiles;
	}

	@Override
	public ByteArrayRegion getFileBytes(SakerPath path) throws IOException {
		SakerFile f = taskUtils.resolveAtPath(path);
		if (f == null) {
			throw new FileNotFoundException("File not found at path: " + path);
		}
		return f.getBytes();
	}

	//TODO rework this file type?
	private static class LazyByteArraySakerFile extends SakerFileBase {
		private Supplier<ByteArrayRegion> supplier;
		private volatile ContentDescriptor contentDescriptor = null;

		protected LazyByteArraySakerFile(String name, Supplier<ByteArrayRegion> supplier)
				throws NullPointerException, InvalidPathFormatException {
			super(name);
			this.supplier = supplier;
		}

		public LazyByteArraySakerFile(String name, OutputFileObject output) throws NullPointerException {
			this(name, LazySupplier.of(output::getOutputBytes));
		}

		public static LazyByteArraySakerFile create(String name, OutputBytecodeManipulationOption bytecodeManipulation,
				OutputFileObject output) {
			if (!JavaUtil.isBytecodeManipulationAffects(name, bytecodeManipulation)) {
				return new LazyByteArraySakerFile(name, output);
			}
			return new LazyByteArraySakerFile(name, toRewritingSupplier(name, bytecodeManipulation, output));
		}

		private static Supplier<ByteArrayRegion> toRewritingSupplier(String name,
				OutputBytecodeManipulationOption bytecodeManipulation, OutputFileObject output) {
			if (!JavaUtil.isBytecodeManipulationAffects(name, bytecodeManipulation)) {
				return LazySupplier.of(output::getOutputBytes);
			}
			return LazySupplier.of(() -> {
				return JavaUtil.performBytecodeManipulation(name, output.getOutputBytes(), bytecodeManipulation);
			});
		}

		@Override
		public int getEfficientOpeningMethods() {
			return OPENING_METHODS_ALL;
		}

		@Override
		public ContentDescriptor getContentDescriptor() {
			ContentDescriptor cd = contentDescriptor;
			if (cd != null) {
				return cd;
			}
			synchronized (this) {
				cd = contentDescriptor;
				if (cd != null) {
					return cd;
				}
				cd = HashContentDescriptor.hash(getBytesImpl());
				contentDescriptor = cd;
				return cd;
			}
		}

		@Override
		public ByteArrayRegion getBytesImpl() {
			return supplier.get();
		}

		@Override
		public String getContentImpl() throws IOException {
			return getBytesImpl().toString();
		}

		@Override
		public InputStream openInputStreamImpl() throws IOException {
			//XXX should we somehow disallow downcasting the returned stream to make sure the byte array is not modifiable
			return new UnsyncByteArrayInputStream(getBytesImpl());
		}

		@Override
		public void writeToStreamImpl(OutputStream os) throws IOException {
			getBytesImpl().writeTo(os);
		}
	}

	@RMIWrap(IncrementalDirectoryLocationRMIWrapper.class)
	protected class DirectoryLocationImpl implements IncrementalDirectoryLocation {
		protected NavigableMap<SakerPath, SakerDirectory> directories = new TreeMap<>();

		public DirectoryLocationImpl() {
		}

		public DirectoryLocationImpl(SakerDirectory directory) {
			this.directories.put(SakerPathFiles.requireAbsolutePath(directory), directory);
		}

		@Override
		public NavigableSet<SakerPath> getDirectoryPaths() {
			return directories.navigableKeySet();
		}

		@Override
		public IncrementalDirectoryFile getFileAtPackageRelative(String packagename, String relativename) {
			SakerPath resourcepath = IncrementalCompilationDirector.toPackageRelativePath(packagename, relativename);

			return findFileWithResourcePath(resourcepath);
		}

		protected IncrementalDirectoryFile findFileWithResourcePath(SakerPath resourcepath) {
			for (SakerDirectory dir : directories.values()) {
				SakerFile file = taskUtils.resolveAtRelativePath(dir, resourcepath);
				if (file == null) {
					continue;
				}
				if (file instanceof SakerDirectory) {
					//cant use a directory
					continue;
				}
				return new DirectoryFileImpl(file, null);
			}
			return null;
		}

		@Override
		public IncrementalDirectoryFile getJavaFileAt(String classname, Kind kind) {
			SakerPath resourcepath = SakerPath.valueOf(classname.replace('.', '/') + kind.extension);

			for (SakerDirectory dir : directories.values()) {
				SakerFile file = taskUtils.resolveAtRelativePath(dir, resourcepath);
				if (file == null) {
					continue;
				}
				if (file instanceof SakerDirectory) {
					//cant use a directory
					continue;
				}
				return new DirectoryFileImpl(file, classname);
			}
			return null;
		}

		@Override
		public Iterable<IncrementalDirectoryFile> list(String packagename, Set<Kind> kinds, boolean recurse) {
			String[] packagesplit;
			String packbinaryprefix;
			if (packagename.isEmpty()) {
				packbinaryprefix = "";
				packagesplit = ObjectUtils.EMPTY_STRING_ARRAY;
			} else {
				packagesplit = PATTERN_DOT.split(packagename);
				packbinaryprefix = packagename + ".";
			}
			List<String> packagesplitlist = ImmutableUtils.asUnmodifiableArrayList(packagesplit);
			ArrayList<IncrementalDirectoryFile> result = new ArrayList<>();
			StringBuilder sb = new StringBuilder();
			for (SakerDirectory dir : directories.values()) {
				SakerDirectory packdir = taskUtils.resolveDirectoryAtRelativePathNames(dir, packagesplitlist);
				if (packdir == null) {
					continue;
				}
				if (recurse) {
					NavigableMap<SakerPath, SakerFile> recfiles = packdir.getFilesRecursiveByPath(SakerPath.EMPTY,
							new KindFilterFileDirectoryVisitPredicate(kinds));
					if (!recfiles.isEmpty()) {
						for (Entry<SakerPath, SakerFile> entry : recfiles.entrySet()) {
							SakerFile f = entry.getValue();
							Kind kind = JavaUtil.getKindFromName(f.getName());
							result.add(new DirectoryFileImpl(f,
									packbinaryprefix + getBinaryNameFromRelativePath(entry.getKey(), sb, kind)));
						}
					}
				} else {
					NavigableMap<String, ? extends SakerFile> dirchildren = packdir.getChildren();
					if (!dirchildren.isEmpty()) {
						for (Entry<String, ? extends SakerFile> entry : dirchildren.entrySet()) {
							SakerFile f = entry.getValue();
							if (f instanceof SakerDirectory) {
								continue;
							}
							String fname = entry.getKey();
							Kind kind = JavaUtil.getKindFromName(fname);
							if (!kinds.contains(kind)) {
								continue;
							}
							result.add(new DirectoryFileImpl(f,
									packbinaryprefix + fname.substring(0, fname.length() - kind.extension.length())));
						}
					}
				}
			}
			return result;
		}
	}

	private static class KindFilterFileDirectoryVisitPredicate implements DirectoryVisitPredicate {
		private Set<Kind> kinds;

		public KindFilterFileDirectoryVisitPredicate(Set<Kind> kinds) {
			this.kinds = kinds;
		}

		@Override
		public boolean visitFile(String name, SakerFile file) {
			return kinds.contains(JavaUtil.getKindFromName(name));
		}

		@Override
		public boolean visitDirectory(String name, SakerDirectory directory) {
			return false;
		}
	}

	@RMIWrap(IncrementalDirectoryFileRMIWrapper.class)
	protected static class DirectoryFileImpl implements IncrementalDirectoryFile {
		protected SakerFile file;
		protected String inferredBinaryName;

		public DirectoryFileImpl(SakerFile file, String inferredBinaryName) {
			this.file = file;
			this.inferredBinaryName = inferredBinaryName;
		}

		@Override
		public ByteArrayRegion getBytes() throws IOException {
			return file.getBytes();
		}

		@Override
		public SakerPath getPath() {
			return file.getSakerPath();
		}

		@Override
		public String getInferredBinaryName() {
			return inferredBinaryName;
		}

		@Override
		public String toString() {
			return "DirectoryFileImpl[" + (file != null ? "file=" + file + ", " : "")
					+ (inferredBinaryName != null ? "inferredBinaryName=" + inferredBinaryName : "") + "]";
		}
	}

	public static class IncrementalDirectoryFileRMIWrapper implements RMIWrapper, IncrementalDirectoryFile {
		private IncrementalDirectoryFile file;

		private transient SakerPath path;
		private transient String inferredBinaryName;

		public IncrementalDirectoryFileRMIWrapper() {
		}

		public IncrementalDirectoryFileRMIWrapper(IncrementalDirectoryFile file) {
			this.file = file;
		}

		@Override
		public Object getWrappedObject() {
			return file;
		}

		@Override
		public Object resolveWrapped() {
			return this;
		}

		@Override
		public void writeWrapped(RMIObjectOutput out) throws IOException {
			out.writeRemoteObject(file);
			out.writeObject(file.getPath());
			out.writeObject(file.getInferredBinaryName());
		}

		@Override
		public void readWrapped(RMIObjectInput in) throws IOException, ClassNotFoundException {
			this.file = (IncrementalDirectoryFile) in.readObject();
			this.path = (SakerPath) in.readObject();
			this.inferredBinaryName = (String) in.readObject();
		}

		@Override
		public ByteArrayRegion getBytes() throws IOException {
			return file.getBytes();
		}

		@Override
		public SakerPath getPath() {
			return path;
		}

		@Override
		public String getInferredBinaryName() {
			return inferredBinaryName;
		}
	}

	public static class IncrementalDirectoryLocationRMIWrapper implements RMIWrapper, IncrementalDirectoryLocation {
		private IncrementalDirectoryLocation location;
		private NavigableSet<SakerPath> directoryPaths;

		public IncrementalDirectoryLocationRMIWrapper() {
		}

		public IncrementalDirectoryLocationRMIWrapper(IncrementalDirectoryLocation location) {
			this.location = location;
		}

		@Override
		public NavigableSet<SakerPath> getDirectoryPaths() {
			return directoryPaths;
		}

		@Override
		public IncrementalDirectoryFile getJavaFileAt(String classname, Kind kind) {
			return location.getJavaFileAt(classname, kind);
		}

		@Override
		public IncrementalDirectoryFile getFileAtPackageRelative(String packagename, String relativename) {
			return location.getFileAtPackageRelative(packagename, relativename);
		}

		@Override
		public Iterable<? extends IncrementalDirectoryFile> list(String packagename, Set<Kind> kinds, boolean recurse) {
			return location.list(packagename, kinds, recurse);
		}

		@Override
		public Object getWrappedObject() {
			return location;
		}

		@Override
		public Object resolveWrapped() {
			return this;
		}

		@Override
		public void writeWrapped(RMIObjectOutput out) throws IOException {
			out.writeRemoteObject(location);
			SerialUtils.writeExternalCollection(out, location.getDirectoryPaths());
		}

		@Override
		public void readWrapped(RMIObjectInput in) throws IOException, ClassNotFoundException {
			location = (IncrementalDirectoryLocation) in.readObject();
			directoryPaths = SerialUtils.readExternalSortedImmutableNavigableSet(in);
		}
	}

	private static String getBinaryNameFromRelativePath(SakerPath relative, StringBuilder sb, Kind kind) {
		sb.setLength(0);
		for (Iterator<String> it = relative.nameIterator(); it.hasNext();) {
			String name = it.next();
			if (it.hasNext()) {
				sb.append(name);
				sb.append('.');
			} else {
				//last one, remove extension
				sb.append(name.substring(0, name.length() - kind.extension.length()));
			}
		}
		return sb.toString();
	}

	private SakerFile getExistingResourceFile(SakerDirectory basedir, String pkg, SakerPath relativepath) {
		SakerDirectory pkgdir = getPackageSubDirectory(basedir, pkg);
		if (pkgdir == null) {
			return null;
		}
		return SakerPathFiles.resolveAtRelativePath(pkgdir, relativepath);
	}

	private NavigableMap<SakerPath, SakerDirectory> getDirectoriesModifiable(Location location) {
		ExternalizableLocation extloc = new ExternalizableLocation(location);
		return getDirectoriesModifiable(extloc);
	}

	private NavigableMap<SakerPath, SakerDirectory> getDirectoriesModifiable(ExternalizableLocation extloc) {
		return dirLocations.computeIfAbsent(extloc, x -> createDirectoryLocation()).directories;
	}

	//may be overridden by subclass
	protected DirectoryLocationImpl createDirectoryLocation() {
		return new DirectoryLocationImpl();
	}

	public Collection<SakerDirectory> getLocationDirectories(Location locationname) {
		ExternalizableLocation extloc = new ExternalizableLocation(locationname);
		return getLocationDirectories(extloc);
	}

	public Collection<SakerDirectory> getLocationDirectories(ExternalizableLocation extloc) {
		DirectoryLocationImpl dirs = dirLocations.get(extloc);
		if (dirs == null) {
			return Collections.emptySet();
		}
		return dirs.directories.values();
	}

	private static SakerDirectory getPackageSubDirectoryCreate(SakerDirectory base, String pkg) {
		if (pkg.isEmpty()) {
			return base;
		}
		return SakerPathFiles.resolveDirectoryAtRelativePathNamesCreate(base,
				ImmutableUtils.unmodifiableArrayList(PATTERN_DOT.split(pkg)));
	}

	private static SakerDirectory getDirectoryForRelativeName(SakerDirectory basedir, SakerPath relativePath) {
		return SakerPathFiles.resolveDirectoryAtRelativePathCreate(basedir, relativePath);
	}

	private static SakerDirectory getPackageSubDirectory(SakerDirectory base, String pkg) {
		if (pkg.isEmpty()) {
			return base;
		}
		return SakerPathFiles.resolveDirectoryAtRelativePathNames(base,
				ImmutableUtils.unmodifiableArrayList(PATTERN_DOT.split(pkg)));
	}

	private static String classSimpleNameOf(String qualifiedname) {
		return qualifiedname.substring(qualifiedname.lastIndexOf('.') + 1);
	}

	private static String classPackageOf(String qualifiedname) {
		int dot = qualifiedname.lastIndexOf('.');
		if (dot < 0) {
			return "";
		}
		return qualifiedname.substring(0, dot);
	}

	public static class JavaCompilerDirectoriesRMIWrapper implements RMIWrapper, IncrementalDirectoryPaths {
		private IncrementalDirectoryPaths dirs;
		private NavigableMap<ExternalizableLocation, ? extends IncrementalDirectoryLocation> directoryLocations;
		private NavigableMap<String, ? extends IncrementalDirectoryLocation> modulePathLocations;
		private boolean noCommandLineClassPath;
		private boolean allowCommandLineBootClassPath;

		public JavaCompilerDirectoriesRMIWrapper() {
		}

		public JavaCompilerDirectoriesRMIWrapper(IncrementalDirectoryPaths dirs) {
			this.dirs = dirs;
		}

		@Override
		public Object getWrappedObject() {
			return dirs;
		}

		@Override
		public Object resolveWrapped() {
			return this;
		}

		@Override
		public void writeWrapped(RMIObjectOutput out) throws IOException {
			out.writeRemoteObject(dirs);
			out.writeBoolean(dirs.isNoCommandLineClassPath());
			out.writeBoolean(dirs.isAllowCommandLineBootClassPath());
			out.writeWrappedObject(dirs.getDirectoryLocations(), RMITreeMapSerializeKeyRemoteValueWrapper.class);
			out.writeWrappedObject(dirs.getModulePathLocations(), RMITreeMapSerializeKeyRemoteValueWrapper.class);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void readWrapped(RMIObjectInput in) throws IOException, ClassNotFoundException {
			dirs = (IncrementalDirectoryPaths) in.readObject();
			noCommandLineClassPath = in.readBoolean();
			allowCommandLineBootClassPath = in.readBoolean();
			directoryLocations = (NavigableMap<ExternalizableLocation, ? extends IncrementalDirectoryLocation>) in
					.readObject();
			modulePathLocations = (NavigableMap<String, ? extends IncrementalDirectoryLocation>) in.readObject();
		}

		@Override
		public NavigableMap<ExternalizableLocation, ? extends IncrementalDirectoryLocation> getDirectoryLocations() {
			return directoryLocations;
		}

		@Override
		public boolean isNoCommandLineClassPath() {
			return noCommandLineClassPath;
		}

		@Override
		public boolean isAllowCommandLineBootClassPath() {
			return allowCommandLineBootClassPath;
		}

		@Override
		public NavigableMap<String, ? extends IncrementalDirectoryLocation> getModulePathLocations() {
			return modulePathLocations;
		}

		@Override
		public SakerPath putJavaFileForOutput(ExternalizableLocation location, String classname, String extension,
				OutputFileObject output) {
			return dirs.putJavaFileForOutput(location, classname, extension, output);
		}

		@Override
		public void bulkPutJavaFilesForOutput(NavigableMap<OutputJavaFileData, ByteArrayRegion> data) {
			dirs.bulkPutJavaFilesForOutput(data);
		}

		@Override
		public SakerPath putFileForOutput(ExternalizableLocation location, String packagename, String relativename,
				OutputFileObject output) {
			return dirs.putFileForOutput(location, packagename, relativename, output);
		}

		@Override
		@SuppressWarnings("deprecation")
		public ByteArrayRegion getFileBytes(SakerPath path) throws IOException {
			return dirs.getFileBytes(path);
		}

	}
}
