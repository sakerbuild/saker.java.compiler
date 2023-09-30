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

import java.io.Closeable;
import java.util.Collection;
import java.util.Map;
import java.util.NavigableMap;

import saker.build.file.SakerDirectory;
import saker.build.file.path.SakerPath;
import saker.java.compiler.api.compile.JavaAnnotationProcessor;

public interface JavaCompilerInvocationContext extends Closeable {
	public boolean isParallelProcessing();

	public Collection<String> getOptions();

	public Collection<String> getSuppressWarnings();

	public SakerDirectory getOutputClassDirectory();

	public SakerDirectory getOutputSourceDirectory();

	public SakerDirectory getOutputResourceDirectory();

	public SakerDirectory getOutputNativeHeaderDirectory();

	public NavigableMap<SakerPath, SakerDirectory> getClassPathDirectories();

	public NavigableMap<SakerPath, SakerDirectory> getBootClassPathDirectories();

	public Map<String, SakerDirectory> getModulePathDirectories();

	public Map<String, String> getGeneralProcessorOptions();

	public Map<ProcessorDetails, JavaAnnotationProcessor> getPassProcessorReferences();
}
