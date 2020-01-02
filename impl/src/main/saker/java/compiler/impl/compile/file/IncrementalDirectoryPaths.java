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

import java.io.IOException;
import java.util.NavigableMap;
import java.util.Set;

import javax.tools.JavaFileObject.Kind;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.rmi.annot.invoke.RMICacheResult;
import saker.build.thirdparty.saker.rmi.annot.transfer.RMISerialize;
import saker.build.thirdparty.saker.rmi.annot.transfer.RMIWrap;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import saker.build.thirdparty.saker.util.rmi.wrap.RMIArrayListRemoteElementWrapper;
import saker.build.thirdparty.saker.util.rmi.wrap.RMITreeMapSerializeKeyRemoteValueWrapper;
import saker.java.compiler.impl.compile.handler.ExternalizableLocation;
import saker.java.compiler.impl.compile.handler.invoker.rmi.JavaFileObjectKindEnumSetRMIWrapper;

public interface IncrementalDirectoryPaths {
	public IncrementalDirectoryLocation getDirectoryLocation(ExternalizableLocation location);

	@RMICacheResult
	public boolean isNoCommandLineClassPath();

	@RMICacheResult
	public boolean isAllowCommandLineBootClassPath();

	@RMISerialize
	@RMICacheResult
	public Set<ExternalizableLocation> getPresentLocations();

	@RMICacheResult
	@RMIWrap(RMITreeMapSerializeKeyRemoteValueWrapper.class)
	public NavigableMap<String, IncrementalDirectoryLocation> getModulePathLocations();

	public SakerPath putJavaFileForOutput(ExternalizableLocation location, String classname, String extension,
			OutputFileObject output);

	public SakerPath putFileForOutput(ExternalizableLocation location, String packagename, String relativename,
			OutputFileObject output);

	@Deprecated
	public ByteArrayRegion getFileBytes(SakerPath path) throws IOException;

	public interface IncrementalDirectoryLocation {
		public IncrementalDirectoryFile getJavaFileAt(String classname, Kind kind);

		public IncrementalDirectoryFile getFileAtPackageRelative(String packagename, String relativename);

		@RMIWrap(RMIArrayListRemoteElementWrapper.class)
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
}
