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
package saker.java.compiler.api.option;

import java.util.Collection;
import java.util.Set;

import saker.java.compiler.api.compile.SakerJavaCompilerUtils;
import saker.java.compiler.impl.options.SimpleAddReadsPath;

/**
 * Represents an add-reads configuration.
 * <p>
 * The interface corresponds to the <code>--add-reads</code> command line option for the JVM. It is used by the Java
 * compiler task, but can also be used for other JVM related use-cases.
 * <p>
 * Clients shouldn't implement this interface.
 * <p>
 * Use {@link #create(String, Collection) create} to create a new instance.
 * 
 * @see SakerJavaCompilerUtils#toAddReadsCommandLineString(JavaAddReads)
 * @since saker.java.compiler 0.8.8
 */
public interface JavaAddReads {
	/**
	 * Gets the module that requires the associated modules.
	 * 
	 * @return The module name.
	 */
	public String getModule();

	/**
	 * Gets the names of the required modules.
	 * <p>
	 * Always contains at least 1 element.
	 * 
	 * @return The required module names.
	 */
	public Set<String> getRequires();

	@Override
	public int hashCode();

	@Override
	public boolean equals(Object obj);

	/**
	 * Creates a new instance that requires the specified modules by the given module.
	 * 
	 * @param module
	 *            The module name.
	 * @param requiredmodules
	 *            The collection of module names that is required by the module.
	 * @return The created instance.
	 * @throws NullPointerException
	 *             If any of the argument is <code>null</code>.
	 * @throws IllegalArgumentException
	 *             If the required modules is empty.
	 */
	public static JavaAddReads create(String module, Collection<String> requiredmodules)
			throws NullPointerException, IllegalArgumentException {
		return new SimpleAddReadsPath(module, requiredmodules);
	}
}
