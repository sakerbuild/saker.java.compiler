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
package saker.java.compiler.impl.compile.handler.invoker;

import java.io.IOException;
import java.util.Map;
import java.util.NavigableMap;

import saker.build.file.FileHandle;
import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.rmi.annot.transfer.RMIWrap;
import saker.build.thirdparty.saker.util.rmi.wrap.RMIArrayListWrapper;
import saker.build.thirdparty.saker.util.rmi.wrap.RMITreeMapSerializeKeySerializeValueWrapper;
import saker.java.compiler.api.compile.exc.JavaCompilationFailedException;
import saker.java.compiler.impl.compile.handler.diagnostic.DiagnosticEntry;
import saker.java.compiler.impl.compile.handler.info.ClassHoldingFileData;
import saker.java.compiler.impl.compile.handler.info.CompilationInfo;

public interface JavaCompilerInvocationDirector {

	public CompilationInfo invokeCompilation(NavigableMap<SakerPath, ? extends FileHandle> units,
			NavigableMap<SakerPath, ? extends ClassHoldingFileData> removedsourcefiles)
			throws JavaCompilationFailedException, IOException;

	public void addGeneratedClassFilesForSourceFiles(
			@RMIWrap(RMITreeMapSerializeKeySerializeValueWrapper.class) Map<String, SakerPath> classbinarynamesourcefilepaths);

	public void runCompilationRounds();

	public boolean isAnyErrorRaised();

	public void reportDiagnostic(DiagnosticEntry entry);

	public default void reportDiagnostics(
			@RMIWrap(RMIArrayListWrapper.class) Iterable<? extends DiagnosticEntry> entries) {
		for (DiagnosticEntry de : entries) {
			reportDiagnostic(de);
		}
	}

}
