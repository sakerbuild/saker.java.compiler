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
package saker.java.compiler.util9.impl.file;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Set;

import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;

import saker.build.thirdparty.saker.util.ConcatIterable;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.compile.file.IncrementalDirectoryPaths;
import saker.java.compiler.impl.compile.file.IncrementalDirectoryPaths.IncrementalDirectoryLocation;
import saker.java.compiler.util8.impl.file.IncrementalJavaFileManager8;

public class IncrementalJavaFileManager9 extends IncrementalJavaFileManager8 {
	private NavigableMap<String, ? extends IncrementalDirectoryLocation> modulePathLocations;

	public IncrementalJavaFileManager9(StandardJavaFileManager fileManager, IncrementalDirectoryPaths directorypaths) {
		super(fileManager, directorypaths);
		modulePathLocations = directorypaths.getModulePathLocations();
	}

	@Override
	public boolean hasLocation(Location location) {
		if (location == StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH
				|| location == StandardLocation.MODULE_SOURCE_PATH || location == StandardLocation.PATCH_MODULE_PATH
				|| location == StandardLocation.UPGRADE_MODULE_PATH) {
			return false;
		}
		if (location instanceof ModulePathLocationImpl) {
			return true;
		}
		return super.hasLocation(location);
	}

	@Override
	public String inferModuleName(Location location) throws IOException {
		if (location instanceof ModulePathLocationImpl) {
			return ((ModulePathLocationImpl) location).getModuleName();
		}
		return super.inferModuleName(location);
	}

	@Override
	public Iterable<Set<Location>> listLocationsForModules(Location location) throws IOException {
		if (location == StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH
				|| location == StandardLocation.MODULE_SOURCE_PATH || location == StandardLocation.PATCH_MODULE_PATH
				|| location == StandardLocation.UPGRADE_MODULE_PATH) {
			return Collections.emptySet();
		}
		Iterable<Set<Location>> result = super.listLocationsForModules(location);
		if (location == StandardLocation.MODULE_PATH) {
			if (!ObjectUtils.isNullOrEmpty(modulePathLocations)) {
				Set<Location> addset = new LinkedHashSet<>();
				for (Entry<String, ? extends IncrementalDirectoryLocation> entry : modulePathLocations.entrySet()) {
					addset.add(new ModulePathLocationImpl(entry));
				}
				@SuppressWarnings("unchecked")
				Iterable<Set<Location>> nresult = new ConcatIterable<>(
						ImmutableUtils.asUnmodifiableArrayList(ImmutableUtils.singletonSet(addset), result));
				result = nresult;
			}
		}
		return result;
	}

	@Override
	public Iterable<JavaFileObject> list(Location location, String packageName, Set<Kind> kinds, boolean recurse)
			throws IOException {
		if (location instanceof ModulePathLocationImpl) {
			ModulePathLocationImpl loc = (ModulePathLocationImpl) location;
			//dont call super.list, as they wouldn't recognize the location
			Collection<JavaFileObject> listed = listInDirectoryLocationImpl(packageName, kinds, recurse,
					loc.getDirectoryLocation());
			if (listed == null) {
				return Collections.emptyList();
			}
			return listed;
		}
		return super.list(location, packageName, kinds, recurse);
	}

	@Override
	public JavaFileObject getJavaFileForInput(Location location, String className, Kind kind) throws IOException {
		if (location instanceof ModulePathLocationImpl) {
			IncrementalDirectoryLocation dirloc = ((ModulePathLocationImpl) location).getDirectoryLocation();
			JavaFileObject found = getJavaFileForInputAtDirectoryLocationImpl(className, kind, dirloc);
			if (found != null) {
				return found;
			}
		}
		return super.getJavaFileForInput(location, className, kind);
	}

	@Override
	public FileObject getFileForInput(Location location, String packageName, String relativeName) throws IOException {
		if (location instanceof ModulePathLocationImpl) {
			IncrementalDirectoryLocation dirloc = ((ModulePathLocationImpl) location).getDirectoryLocation();
			FileObject found = getFileForInputAtDirectoryLocationImpl(packageName, relativeName, dirloc);
			if (found != null) {
				return found;
			}
		}
		return super.getFileForInput(location, packageName, relativeName);
	}

	private static class ModulePathLocationImpl implements Location {
		private Entry<String, ? extends IncrementalDirectoryLocation> moduleEntry;

		public ModulePathLocationImpl(Entry<String, ? extends IncrementalDirectoryLocation> moduleEntry) {
			this.moduleEntry = moduleEntry;
		}

		public String getModuleName() {
			return moduleEntry.getKey();
		}

		public IncrementalDirectoryLocation getDirectoryLocation() {
			return moduleEntry.getValue();
		}

		@Override
		public String getName() {
			return "MODULE_PATH[" + getModuleName() + "]";
		}

		@Override
		public boolean isOutputLocation() {
			return false;
		}

		@Override
		public int hashCode() {
			return getModuleName().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ModulePathLocationImpl other = (ModulePathLocationImpl) obj;
			if (!getModuleName().equals(other.getModuleName())) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			return getName();
		}
	}
}
