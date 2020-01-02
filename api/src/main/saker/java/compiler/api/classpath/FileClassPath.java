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

import saker.std.api.file.location.FileLocation;

/**
 * Represents a classpath entry that is backed by a file location.
 * <p>
 * The backing file may locate a JAR or directory, the interface doesn't specify a requirement on its nature.
 * <p>
 * Clients shouldn't implement this interface.
 * 
 * @see JavaClassPath
 * @see ClassPathVisitor
 * @see JavaClassPathBuilder#addFileClassPath(FileLocation)
 */
public interface FileClassPath {
	/**
	 * Gets the file of this classpath.
	 * 
	 * @return The file location.
	 */
	public FileLocation getFileLocation();
}
