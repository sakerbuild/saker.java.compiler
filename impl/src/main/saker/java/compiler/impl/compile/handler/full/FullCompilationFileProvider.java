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

import java.util.Collection;
import java.util.Set;

import javax.tools.JavaFileObject.Kind;

import saker.build.file.SakerFile;
import saker.build.thirdparty.saker.rmi.annot.transfer.RMISerialize;
import saker.build.thirdparty.saker.rmi.annot.transfer.RMIWrap;
import saker.build.thirdparty.saker.util.rmi.wrap.RMIArrayListRemoteElementWrapper;
import saker.java.compiler.impl.compile.handler.invoker.SakerPathBytes;

public interface FullCompilationFileProvider {
	public interface LocationProvider {
		public SakerPathBytes getJavaInputFile(String classname, Kind kind);

		public SakerPathBytes getInputFile(String packagename, String relativename);

		@RMIWrap(RMIArrayListRemoteElementWrapper.class)
		public Collection<? extends SakerFile> list(String packagename, @RMISerialize Set<Kind> kinds, boolean recurse);
	}

	public LocationProvider getLocation(String name, boolean outputlocation);
}
