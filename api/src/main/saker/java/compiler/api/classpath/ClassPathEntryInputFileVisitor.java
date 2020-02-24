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

public interface ClassPathEntryInputFileVisitor {

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
