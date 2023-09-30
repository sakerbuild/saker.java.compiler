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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;

import javax.tools.JavaFileObject.Kind;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.rmi.annot.invoke.RMICacheResult;
import saker.build.thirdparty.saker.rmi.annot.transfer.RMIWrap;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import saker.build.thirdparty.saker.util.rmi.wrap.RMIArrayListWrapper;
import saker.build.thirdparty.saker.util.rmi.wrap.RMITreeMapSerializeKeyRemoteValueWrapper;
import saker.build.thirdparty.saker.util.rmi.wrap.RMITreeMapSerializeKeySerializeValueWrapper;
import saker.build.thirdparty.saker.util.rmi.wrap.RMITreeMapWrapper;
import saker.build.thirdparty.saker.util.rmi.wrap.RMITreeSetSerializeElementWrapper;
import saker.java.compiler.impl.compile.handler.ExternalizableLocation;
import saker.java.compiler.impl.compile.handler.invoker.rmi.JavaFileObjectKindEnumSetRMIWrapper;

public interface IncrementalDirectoryPaths {
	@Deprecated // use and cache getDirectoryLocations() instead
	public default IncrementalDirectoryLocation getDirectoryLocation(ExternalizableLocation location) {
		return getDirectoryLocations().get(location);
	}

	@RMICacheResult
	@RMIWrap(RMITreeMapSerializeKeyRemoteValueWrapper.class)
	public NavigableMap<ExternalizableLocation, ? extends IncrementalDirectoryLocation> getDirectoryLocations();

	@RMICacheResult
	public boolean isNoCommandLineClassPath();

	@RMICacheResult
	public boolean isAllowCommandLineBootClassPath();

	@RMIWrap(RMITreeSetSerializeElementWrapper.class)
	@RMICacheResult
	@Deprecated // use and cache getDirectoryLocations() instead
	public default NavigableSet<ExternalizableLocation> getPresentLocations() {
		return getDirectoryLocations().navigableKeySet();
	}

	@RMICacheResult
	@RMIWrap(RMITreeMapSerializeKeyRemoteValueWrapper.class)
	public NavigableMap<String, ? extends IncrementalDirectoryLocation> getModulePathLocations();

	public SakerPath putJavaFileForOutput(ExternalizableLocation location, String classname, String extension,
			OutputFileObject output);

	public SakerPath putFileForOutput(ExternalizableLocation location, String packagename, String relativename,
			OutputFileObject output);

	public void bulkPutJavaFilesForOutput(
			@RMIWrap(RMITreeMapWrapper.class) NavigableMap<OutputJavaFileData, ByteArrayRegion> data);

	@Deprecated
	public ByteArrayRegion getFileBytes(SakerPath path) throws IOException;

	public interface IncrementalDirectoryLocation {

		public NavigableSet<SakerPath> getDirectoryPaths();

		public IncrementalDirectoryFile getJavaFileAt(String classname, Kind kind);

		public IncrementalDirectoryFile getFileAtPackageRelative(String packagename, String relativename);

		@RMIWrap(RMIArrayListWrapper.class)
		public Iterable<? extends IncrementalDirectoryFile> list(String packagename,
				@RMIWrap(JavaFileObjectKindEnumSetRMIWrapper.class) Set<Kind> kinds, boolean recurse);

		public interface IncrementalDirectoryFile {
			public ByteArrayRegion getBytes() throws IOException;

			@RMICacheResult
			public SakerPath getPath();

			@RMICacheResult
			public String getInferredBinaryName();
		}
	}

	public static final class OutputJavaFileData implements Externalizable, Comparable<OutputJavaFileData> {
		private static final long serialVersionUID = 1L;

		protected ExternalizableLocation location;
		protected SakerPath path;
		protected transient String className;

		/**
		 * For {@link Externalizable}.
		 */
		public OutputJavaFileData() {
		}

		public OutputJavaFileData(ExternalizableLocation location, SakerPath path, String className) {
			this.location = location;
			this.path = path;
			this.className = className;
		}

		public ExternalizableLocation getLocation() {
			return location;
		}

		public SakerPath getPath() {
			return path;
		}

		public String getClassName() {
			return className;
		}

		@Override
		public int compareTo(OutputJavaFileData o) {
			int cmp = location.compareTo(o.location);
			if (cmp != 0) {
				return cmp;
			}
			return path.compareTo(o.path);
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(location);
			out.writeObject(path);
			out.writeObject(className);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			location = (ExternalizableLocation) in.readObject();
			path = (SakerPath) in.readObject();
			className = (String) in.readObject();
		}

	}
}
