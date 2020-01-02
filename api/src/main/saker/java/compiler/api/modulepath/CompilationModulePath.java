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
package saker.java.compiler.api.modulepath;

import saker.java.compiler.api.compile.JavaCompilationWorkerTaskIdentifier;

/**
 * Represents a modulepath entry that is backed by the output of a Java compilation task.
 * <p>
 * The interface encloses the task identifier of the compiler task that can be used to retrieve the result of the
 * compilation.
 * <p>
 * The consumers of the compilation modulepath may or may not include transitive modulepaths that are configured as an
 * input to the compilation. Usually, transitive modulepaths are included unless otherwise noted.
 * <p>
 * Clients shouldn't implement this interface.
 * 
 * @see JavaModulePath
 * @see ModulePathVisitor
 * @see JavaModulePathBuilder#addCompileModulePath(JavaCompilationWorkerTaskIdentifier)
 */
public interface CompilationModulePath {
	/**
	 * Gets the task identifier of the Java compiler task that performs the compilation.
	 * 
	 * @return The task identifier.
	 */
	public JavaCompilationWorkerTaskIdentifier getCompilationWorkerTaskIdentifier();
}
