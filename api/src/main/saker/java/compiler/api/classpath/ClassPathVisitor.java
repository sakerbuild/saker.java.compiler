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
package saker.java.compiler.api.classpath;

/**
 * Visitor interface for examining classpath entries.
 * <p>
 * The visitor declares methods for visiting various types of classpath. It can be used by calling
 * {@link JavaClassPath#accept(ClassPathVisitor)} with a custom visitor implementation.
 * <p>
 * All of the declared methods in this interface are <code>default</code> and throw an
 * {@link UnsupportedOperationException} by default. Additional <code>visit</code> methods may be added to this
 * interface with similar default implementations.
 * <p>
 * Clients are recommended to implement this interface.
 */
public interface ClassPathVisitor {
	/**
	 * Visits a class path reference.
	 * 
	 * @param classpath
	 *            The classpath entry.
	 */
	public default void visit(ClassPathReference classpath) {
		throw new UnsupportedOperationException("Unsupported class path: " + classpath);
	}

	/**
	 * Visits a compilation classpath.
	 * 
	 * @param classpath
	 *            The classpath entry.
	 */
	public default void visit(CompilationClassPath classpath) {
		throw new UnsupportedOperationException("Unsupported class path: " + classpath);
	}

	/**
	 * Visits a file classpath.
	 * 
	 * @param classpath
	 *            The classpath entry.
	 */
	public default void visit(FileClassPath classpath) {
		throw new UnsupportedOperationException("Unsupported class path: " + classpath);
	}

	/**
	 * Visits an SDK classpath.
	 * 
	 * @param classpath
	 *            The classpath entry.
	 */
	public default void visit(SDKClassPath classpath) {
		throw new UnsupportedOperationException("Unsupported class path: " + classpath);
	}
}
