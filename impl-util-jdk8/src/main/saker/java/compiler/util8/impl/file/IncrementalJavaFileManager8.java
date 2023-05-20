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
package saker.java.compiler.util8.impl.file;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Supplier;

import javax.annotation.processing.FilerException;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.ConcatIterable;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.function.LazySupplier;
import saker.java.compiler.impl.compile.file.IncrementalDirectoryPaths;
import saker.java.compiler.impl.compile.file.IncrementalDirectoryPaths.IncrementalDirectoryLocation;
import saker.java.compiler.impl.compile.file.IncrementalDirectoryPaths.IncrementalDirectoryLocation.IncrementalDirectoryFile;
import saker.java.compiler.impl.compile.file.JavaCompilerFileObject;
import saker.java.compiler.impl.compile.file.JavaCompilerJavaFileObject;
import saker.java.compiler.impl.compile.handler.ExternalizableLocation;
import saker.java.compiler.impl.compile.handler.invoker.IncrementalDirectoryFileInputFileObject;
import saker.java.compiler.impl.compile.handler.invoker.IncrementalDirectoryJavaInputFileObject;
import saker.java.compiler.impl.compile.handler.invoker.SakerPathJavaOutputFileObject;
import saker.java.compiler.impl.compile.handler.invoker.SakerPathOutputFileObject;

@com.sun.tools.javac.api.ClientCodeWrapper.Trusted
public class IncrementalJavaFileManager8 extends ForwardingJavaFileManager<StandardJavaFileManager> {
	//note: the --release option requires the file manager to implement StandardJavaFileManager, but only on Java 9.

	//TODO handle if relative names contain "." or "..", as they are invalid

	private IncrementalDirectoryPaths directoryPaths;

	private Map<ExternalizableLocation, Supplier<IncrementalDirectoryLocation>> locationDirectoryLocations = new TreeMap<>();
	private final boolean noCommandLineClassPath;
	private final boolean allowCommandLineBootClassPath;

	public IncrementalJavaFileManager8(StandardJavaFileManager fileManager, IncrementalDirectoryPaths directorypaths) {
		super(fileManager);
		this.directoryPaths = directorypaths;
		Set<ExternalizableLocation> presentlocations = directoryPaths.getPresentLocations();
		for (ExternalizableLocation extloc : presentlocations) {
			this.locationDirectoryLocations.put(extloc,
					LazySupplier.of(() -> directoryPaths.getDirectoryLocation(extloc)));
		}
		noCommandLineClassPath = directorypaths.isNoCommandLineClassPath();
		allowCommandLineBootClassPath = directorypaths.isAllowCommandLineBootClassPath();
	}

	@Override
	public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse)
			throws IOException {
		IncrementalDirectoryLocation dirloc = ObjectUtils
				.getSupplier(locationDirectoryLocations.get(new ExternalizableLocation(location)));
		Iterable<JavaFileObject> superresult;
		if (location == StandardLocation.CLASS_PATH && noCommandLineClassPath) {
			superresult = Collections.emptySet();
		} else if (location == StandardLocation.PLATFORM_CLASS_PATH && !allowCommandLineBootClassPath) {
			superresult = Collections.emptySet();
		} else {
			superresult = super.list(location, packageName, kinds, recurse);
		}
		if (dirloc == null) {
			return superresult;
		}

		Collection<JavaFileObject> selfresult = listInDirectoryLocationImpl(packageName, kinds, recurse, dirloc);
		if (selfresult == null) {
			return superresult;
		}
		@SuppressWarnings("unchecked")
		ConcatIterable<JavaFileObject> result = new ConcatIterable<>(
				ImmutableUtils.asUnmodifiableArrayList(superresult, selfresult));
		return result;
	}

	protected static Collection<JavaFileObject> listInDirectoryLocationImpl(String packageName, Set<Kind> kinds,
			boolean recurse, IncrementalDirectoryLocation dirloc) {
		if (dirloc == null) {
			return null;
		}
		Iterable<? extends IncrementalDirectoryFile> listed = dirloc.list(packageName, kinds, recurse);
		if (listed != null) {
			Iterator<? extends IncrementalDirectoryFile> it = listed.iterator();
			if (!it.hasNext()) {
				return null;
			}
			Collection<JavaFileObject> selfresult = new ArrayList<>();
			do {
				IncrementalDirectoryFile dirfile = it.next();
				selfresult.add(new IncrementalDirectoryJavaInputFileObject(dirfile));
			} while (it.hasNext());
			return selfresult;
		}
		return null;
	}

	@Override
	public String inferBinaryName(Location location, JavaFileObject file) {
		if (file instanceof JavaCompilerJavaFileObject) {
			return ((JavaCompilerJavaFileObject) file).getInferredBinaryName();
		}
		return super.inferBinaryName(location, file);
	}

	@Override
	public boolean isSameFile(FileObject a, FileObject b) {
		if (a == b) {
			return true;
		}
		if (a instanceof JavaCompilerFileObject) {
			if (b instanceof JavaCompilerFileObject) {
				return ((JavaCompilerFileObject) a).getFileObjectSakerPath()
						.equals(((JavaCompilerFileObject) b).getFileObjectSakerPath());
			}
			return a.toUri().compareTo(b.toUri()) == 0;
		}
		if (b instanceof JavaCompilerFileObject) {
			return b.toUri().compareTo(a.toUri()) == 0;
		}
		return super.isSameFile(a, b);
	}

	@Override
	public boolean hasLocation(Location location) {
		if (location == StandardLocation.ANNOTATION_PROCESSOR_PATH) {
			return false;
		}
		if (location == StandardLocation.CLASS_OUTPUT) {
			//return false for CLASS_OUTPUT, as the javac implementation tries to validate the location
			//when the file manager implements StandardJavaFileManager
			//      (that is requires for --release option on Java 9)
			//the Arguments class tries to validate the CLASS_OUTPUT location
			//it does that by calling hasLocation(CLASS_OUTPUT) 
			//and then calling getLocationAsPaths on it, without checking that it
			//actually returned non null and non empty iterable.
			//this causes it to NPE, and fail.
			//Around com.sun.tools.javac.main.Arguments.java:504
			//by returning false for CLASS_OUTPUT, we circumwent javac to fail
			//with further testing, we haven't seen any changes in other usage of the compiler API
			//therefore we conclude that it is safe to return false in this case.
			return false;
		}
		if (location == StandardLocation.NATIVE_HEADER_OUTPUT) {
			//this is so javac doesnt try to generate native files
			return false;
		}
		if (locationDirectoryLocations.containsKey(new ExternalizableLocation(location))) {
			return true;
		}
		if (location == StandardLocation.CLASS_PATH && noCommandLineClassPath) {
			return false;
		}
		if (location == StandardLocation.PLATFORM_CLASS_PATH && !allowCommandLineBootClassPath) {
			return false;
		}
		if (super.hasLocation(location)) {
			return true;
		}
		return false;
	}

	@Override
	public ClassLoader getClassLoader(Location location) {
		//the javac shouldn't do any classloading by itself
		return null;
	}

	@Override
	public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind) throws IOException {
		Objects.requireNonNull(location, "location");
		Objects.requireNonNull(className, "class name");
		Objects.requireNonNull(kind, "kind");

		if (!location.isOutputLocation()) {
			//do not allow javac to access the files at output locations
			//javac might query module-info class files in the CLASS_OUTPUT directory 

			IncrementalDirectoryLocation dirloc = ObjectUtils
					.getSupplier(locationDirectoryLocations.get(new ExternalizableLocation(location)));
			JavaFileObject found = getJavaFileForInputAtDirectoryLocationImpl(className, kind, dirloc);
			if (found != null) {
				return found;
			}
		}
		if (noCommandLineClassPath && location == StandardLocation.CLASS_PATH) {
			return null;
		}
		if (location == StandardLocation.PLATFORM_CLASS_PATH && !allowCommandLineBootClassPath) {
			return null;
		}
		return super.getJavaFileForInput(location, className, kind);
	}

	@Override
	public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling)
			throws IOException {
		Objects.requireNonNull(className, "class name");
		Objects.requireNonNull(kind, "kind");
		requireOutputLocation(location);

		SakerPathJavaOutputFileObject result = new SakerPathJavaOutputFileObject(kind, className);
		SakerPath fpath = directoryPaths.putJavaFileForOutput(new ExternalizableLocation(location), className,
				kind.extension, result);
		if (fpath == null) {
			throw new FilerException("Location not found: " + location);
		}
		result.setPath(fpath);
		return result;
	}

	@Override
	public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
		Objects.requireNonNull(location, "location");
		Objects.requireNonNull(packageName, "package name");
		Objects.requireNonNull(relativeName, "relative name");

		if (!location.isOutputLocation()) {
			//do not allow javac to access the files at output locations
			IncrementalDirectoryLocation dirloc = ObjectUtils
					.getSupplier(locationDirectoryLocations.get(new ExternalizableLocation(location)));
			FileObject found = getFileForInputAtDirectoryLocationImpl(packageName, relativeName, dirloc);
			if (found != null) {
				return found;
			}
		}
		if (noCommandLineClassPath && location == StandardLocation.CLASS_PATH) {
			return null;
		}
		if (location == StandardLocation.PLATFORM_CLASS_PATH && !allowCommandLineBootClassPath) {
			return null;
		}
		return super.getFileForInput(location, packageName, relativeName);
	}

	@Override
	public FileObject getFileForOutput(Location location, String packageName, String relativeName, FileObject sibling)
			throws IOException {
		Objects.requireNonNull(packageName, "package name");
		Objects.requireNonNull(relativeName, "relative name");
		requireOutputLocation(location);

		SakerPathOutputFileObject result = new SakerPathOutputFileObject();
		SakerPath fpath = directoryPaths.putFileForOutput(new ExternalizableLocation(location), packageName,
				relativeName, result);
		if (fpath == null) {
			throw new FilerException("Location not found: " + location);
		}
		result.setPath(fpath);
		return result;
	}

	protected static JavaFileObject getJavaFileForInputAtDirectoryLocationImpl(String className, Kind kind,
			IncrementalDirectoryLocation dirloc) {
		if (dirloc == null) {
			return null;
		}
		IncrementalDirectoryFile dirfile = dirloc.getJavaFileAt(className, kind);
		if (dirfile != null) {
			return new IncrementalDirectoryJavaInputFileObject(dirfile, kind, className);
		}
		return null;
	}

	protected static FileObject getFileForInputAtDirectoryLocationImpl(String packageName, String relativeName,
			IncrementalDirectoryLocation dirloc) {
		if (dirloc == null) {
			return null;
		}
		IncrementalDirectoryFile dirfile = dirloc.getFileAtPackageRelative(packageName, relativeName);
		if (dirfile == null) {
			return null;
		}
		return new IncrementalDirectoryFileInputFileObject(dirfile);
	}

	private static void requireOutputLocation(Location location) {
		Objects.requireNonNull(location, "location");
		if (!location.isOutputLocation()) {
			throw new IllegalArgumentException("Location is not an output location: " + location);
		}
	}

}
