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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
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
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.function.LazySupplier;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayInputStream;
import saker.java.compiler.impl.JavaUtil;
import saker.java.compiler.impl.compile.file.IncrementalDirectoryPaths.IncrementalDirectoryLocation.IncrementalDirectoryFile;
import saker.java.compiler.impl.compile.handler.ExternalizableLocation;
import saker.java.compiler.impl.compile.handler.invoker.IncrementalCompilationDirector;

public class JavaCompilerDirectories implements IncrementalDirectoryPaths {
	private static final Pattern PATTERN_DOT = Pattern.compile("[.]+");

	protected final TaskContext taskContext;
	protected final TaskExecutionUtilities taskUtils;

	private NavigableMap<ExternalizableLocation, DirectoryLocationImpl> dirLocations = new TreeMap<>();
	private SakerDirectory resourceOutputDirectory = null;

	private ConcurrentNavigableMap<String, SakerFile> outputClassFiles = new ConcurrentSkipListMap<>();
	private ConcurrentNavigableMap<SakerPath, SakerFile> allOutputFiles = new ConcurrentSkipListMap<>();

	private String moduleMainClassInjectValue = null;
	private String moduleVersionInjectValue = null;

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

	public void setModuleMainClassInjectValue(String moduleMainClassInjectValue) {
		this.moduleMainClassInjectValue = moduleMainClassInjectValue;
	}

	public void setModuleVersionInjectValue(String moduleVersionInjectValue) {
		this.moduleVersionInjectValue = moduleVersionInjectValue;
	}

	@Override
	public NavigableMap<String, IncrementalDirectoryLocation> getModulePathLocations() {
		return modulePathLocations;
	}

	@Override
	public IncrementalDirectoryLocation getDirectoryLocation(ExternalizableLocation location) {
		return dirLocations.get(location);
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

	public void addDirectory(ExternalizableLocation extloc, SakerDirectory dir) {
		Objects.requireNonNull(dir, "directory");
		Collection<SakerDirectory> dirs = getDirectoriesModifiable(extloc);
		if (extloc.isOutputLocation() && !dirs.isEmpty()) {
			throw new IllegalArgumentException(
					"Cannot specify multiple output directories for location: " + extloc.getName());
		}
		dirs.add(dir);
	}

	public void addDirectory(Location location, Collection<? extends SakerDirectory> dir) {
		ExternalizableLocation extloc = new ExternalizableLocation(location);
		addDirectory(extloc, dir);
	}

	public void addDirectory(ExternalizableLocation extloc, Collection<? extends SakerDirectory> dir) {
		if (ObjectUtils.isNullOrEmpty(dir)) {
			return;
		}
		Collection<SakerDirectory> dirs = getDirectoriesModifiable(extloc);
		if (extloc.isOutputLocation() && (!dirs.isEmpty() || dir.size() > 1)) {
			throw new IllegalArgumentException(
					"Cannot specify multiple output directories for location: " + extloc.getName());
		}
		dirs.addAll(dir);
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
		if (dirs == null || dirs.directories.isEmpty()) {
			return null;
		}
		String[] split = PATTERN_DOT.split(classname);
		split[split.length - 1] += extension;
		SakerDirectory dir = dirs.directories.iterator().next();
		dir = SakerPathFiles.resolveDirectoryAtRelativePathNamesCreate(dir,
				ImmutableUtils.unmodifiableArrayList(split, 0, split.length - 1));
		SakerFile mfile;
		if (isAnyModuleAttributeInjected() && "module-info".equals(classname) && ".class".equals(extension)
				&& location.equals(ExternalizableLocation.LOCATION_CLASS_OUTPUT)) {
			mfile = new ModuleAttributeInjectLazyByteArraySakerFile(split[split.length - 1], moduleMainClassInjectValue,
					moduleVersionInjectValue, output);
		} else {
			mfile = new LazyByteArraySakerFile(split[split.length - 1], output);
		}
		dir.add(mfile);
		SakerPath resultpath = mfile.getSakerPath();
		if (location.equals(ExternalizableLocation.LOCATION_CLASS_OUTPUT) && Kind.CLASS.extension.equals(extension)) {
			outputClassFiles.put(classname, mfile);
		}
		allOutputFiles.put(resultpath, mfile);
		return resultpath;
	}

	private boolean isAnyModuleAttributeInjected() {
		return moduleMainClassInjectValue != null || moduleVersionInjectValue != null;
	}

	@Override
	public SakerPath putFileForOutput(ExternalizableLocation location, String packagename, String relativename,
			OutputFileObject output) {
		DirectoryLocationImpl dirs = dirLocations.get(location);
		if (dirs == null || dirs.directories.isEmpty()) {
			return null;
		}
		SakerPath resourcepath = IncrementalCompilationDirector.toPackageRelativePath(packagename, relativename);

		SakerDirectory dir = dirs.directories.iterator().next();
		if (resourcepath.getNameCount() > 1) {
			dir = SakerPathFiles.resolveDirectoryAtRelativePathCreate(dir, resourcepath.getParent());
		}
		LazyByteArraySakerFile mfile = new LazyByteArraySakerFile(resourcepath.getFileName(), output);
		dir.add(mfile);
		SakerPath resultpath = mfile.getSakerPath();
		allOutputFiles.put(resultpath, mfile);
		return resultpath;
	}

	public ConcurrentNavigableMap<String, ? extends SakerFile> getOutputClassFiles() {
		return outputClassFiles;
	}

	public ConcurrentNavigableMap<SakerPath, ? extends SakerFile> getAllOutputFiles() {
		return allOutputFiles;
	}

	private static class ModuleAttributeInjectLazyByteArraySakerFile extends LazyByteArraySakerFile {
		protected ModuleAttributeInjectLazyByteArraySakerFile(String name, String mainclassname, String moduleversion,
				OutputFileObject output) throws NullPointerException, InvalidPathFormatException {
			super(name, toRewritingSupplier(mainclassname, moduleversion, output));
		}

		private static Supplier<ByteArrayRegion> toRewritingSupplier(String mainclassname, String moduleversion,
				OutputFileObject output) {
			return LazySupplier.of(() -> {
				ByteArrayRegion bytes = output.getOutputBytes();
				ClassReader reader = new ClassReader(bytes.getArray(), bytes.getOffset(), bytes.getLength());
				ClassWriter writer = new ClassWriter(reader, 0);
				reader.accept(new ModuleMainClassInjectorClassVisitor(writer, mainclassname, moduleversion), 0);
				return ByteArrayRegion.wrap(writer.toByteArray());
			});
		}
	}

	private static class ModuleMainClassInjectorClassVisitor extends ClassVisitor {
		private String mainClassName;
		private String moduleVersion;

		public ModuleMainClassInjectorClassVisitor(ClassVisitor classVisitor, String mainclassname,
				String moduleVersion) {
			super(Opcodes.ASM7, classVisitor);
			this.mainClassName = mainclassname;
			this.moduleVersion = moduleVersion;
		}

		@Override
		public ModuleVisitor visitModule(String name, int access, String version) {
			if (version == null) {
				version = this.moduleVersion;
			}
			ModuleVisitor sv = super.visitModule(name, access, version);
			if (mainClassName == null) {
				return sv;
			}
			return new ModuleMainClassInjectorModuleVisitor(sv);
		}

		private class ModuleMainClassInjectorModuleVisitor extends ModuleVisitor {
			private boolean alreadyHasMainClass = false;

			public ModuleMainClassInjectorModuleVisitor(ModuleVisitor moduleVisitor) {
				super(Opcodes.ASM7, moduleVisitor);
			}

			@Override
			public void visitMainClass(String mainClass) {
				alreadyHasMainClass = true;
				super.visitMainClass(mainClass);
			}

			@Override
			public void visitEnd() {
				if (!alreadyHasMainClass) {
					super.visitMainClass(mainClassName);
				}
				super.visitEnd();
			}
		}
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

	@Override
	public Set<ExternalizableLocation> getPresentLocations() {
		//create a new set so it is RMI transferrable
		return ImmutableUtils.makeImmutableNavigableSet(dirLocations.navigableKeySet());
	}

	@Override
	public ByteArrayRegion getFileBytes(SakerPath path) throws IOException {
		SakerFile f = taskUtils.resolveAtPath(path);
		if (f == null) {
			throw new FileNotFoundException("File not found at path: " + path);
		}
		return f.getBytes();
	}

	protected class DirectoryLocationImpl implements IncrementalDirectoryLocation {
		protected Collection<SakerDirectory> directories = new LinkedHashSet<>();

		public DirectoryLocationImpl() {
		}

		public DirectoryLocationImpl(SakerDirectory directory) {
			this.directories.add(directory);
		}

		@Override
		public IncrementalDirectoryFile getFileAtPackageRelative(String packagename, String relativename) {
			SakerPath resourcepath = IncrementalCompilationDirector.toPackageRelativePath(packagename, relativename);

			return findFileWithResourcePath(resourcepath);
		}

		protected IncrementalDirectoryFile findFileWithResourcePath(SakerPath resourcepath) {
			for (SakerDirectory dir : directories) {
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

			for (SakerDirectory dir : directories) {
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
			for (SakerDirectory dir : directories) {
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

	private Collection<SakerDirectory> getDirectoriesModifiable(Location location) {
		ExternalizableLocation extloc = new ExternalizableLocation(location);
		return getDirectoriesModifiable(extloc);
	}

	private Collection<SakerDirectory> getDirectoriesModifiable(ExternalizableLocation extloc) {
		return dirLocations.computeIfAbsent(extloc, x -> createDirectoryLocation()).directories;
	}

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
		return dirs.directories;
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

}
