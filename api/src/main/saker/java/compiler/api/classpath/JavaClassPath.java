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
 * Represents a classpath configuration for the JVM.
 * <p>
 * The interface represents a collection of classpath configuration. It is a heterogeneous collection of classpath
 * objects.
 * <p>
 * The classpath entries may be enumerated by calling {@link #accept(ClassPathVisitor)} with a custom visitor
 * implementation. The possible classpath types and their properties can be accessed by overriding the appropriate
 * <code>visit</code> method.
 * <p>
 * Clients shouldn't implement this interface.
 * <p>
 * Create a new instance using {@link JavaClassPathBuilder}.
 * 
 * @see ClassPathVisitor
 */
public interface JavaClassPath {
	/**
	 * Checks if this classpath configuration contains any entries.
	 * 
	 * @return <code>true</code> if this classpath is empty.
	 */
	public boolean isEmpty();

	/**
	 * Calls the argument visitor for all the enclosed entries in this classpath.
	 * <p>
	 * If this classpath is {@linkplain #isEmpty() empty}, then the argument visitor will not be called at all.
	 * <p>
	 * The visitor may be called multiple times if this classpath contains multiple entries.
	 * 
	 * @param visitor
	 *            The visitor.
	 * @throws NullPointerException
	 *             If the visitor is <code>null</code>.
	 */
	public void accept(ClassPathVisitor visitor) throws NullPointerException;

	@Override
	public int hashCode();

	@Override
	public boolean equals(Object obj);

}