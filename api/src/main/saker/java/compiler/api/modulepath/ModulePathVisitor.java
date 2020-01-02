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

/**
 * Visitor interface for examining modulepath entries.
 * <p>
 * The visitor declares methods for visiting various types of modulepath. It can be used by calling
 * {@link JavaModulePath#accept(ModulePathVisitor)} with a custom visitor implementation.
 * <p>
 * All of the declared methods in this interface are <code>default</code> and throw an
 * {@link UnsupportedOperationException} by default. Additional <code>visit</code> methods may be added to this
 * interface with similar default implementations.
 * <p>
 * Clients are recommended to implement this interface.
 */
public interface ModulePathVisitor {
	/**
	 * Visits a compilation modulepath.
	 * 
	 * @param modulepath
	 *            The modulepath entry.
	 */
	public default void visit(CompilationModulePath modulepath) {
		throw new UnsupportedOperationException("Unsupported module path: " + modulepath);
	}

	/**
	 * Visits a file modulepath.
	 * 
	 * @param modulepath
	 *            The modulepath entry.
	 */
	public default void visit(FileModulePath modulepath) {
		throw new UnsupportedOperationException("Unsupported module path: " + modulepath);
	}

	/**
	 * Visits an SDK modulepath.
	 * 
	 * @param modulepath
	 *            The modulepath entry.
	 */
	public default void visit(SDKModulePath modulepath) {
		throw new UnsupportedOperationException("Unsupported module path: " + modulepath);
	}
}
