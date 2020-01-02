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

import java.io.Externalizable;
import java.util.Collection;

/**
 * Represents a classpath entry that is provided by an external agent.
 * <p>
 * This interface is the extension point for defining custom classpath entries.
 * <p>
 * The class path reference is split up into multiple entries that define the location and nature of the classpath. The
 * sub-entries may have other transitive classpaths, and may also define other properties to help integration with the
 * build system.
 * <p>
 * Clients should implement this interface. When doing so, make sure to adhere to the {@link #hashCode()} and
 * {@link #equals(Object)} contract. Implementations should also implement the {@link Externalizable} interface.
 * 
 * @see JavaClassPath
 * @see ClassPathVisitor
 * @see JavaClassPathBuilder#addClassPath(ClassPathReference)
 */
public interface ClassPathReference {
	/**
	 * Gets the sub-entries in this classpath reference.
	 * 
	 * @return The entries.
	 */
	public Collection<? extends ClassPathEntry> getEntries();

	@Override
	public boolean equals(Object obj);

	@Override
	public int hashCode();
}
