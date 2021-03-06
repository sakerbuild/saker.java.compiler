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
import saker.java.compiler.impl.options.SimpleAddExportsPath;

/**
 * Represents an add-exports configuration.
 * <p>
 * The interface corresponds to the <code>--add-exports</code> command line option for the JVM. It is used by the Java
 * compiler task, but can also be used for other JVM related use-cases.
 * <p>
 * Clients shouldn't implement this interface.
 * <p>
 * Use {@link #create(String, Collection, Collection) create} to create a new instance.
 * 
 * @see SakerJavaCompilerUtils#toAddExportsCommandLineStrings(JavaAddExports)
 */
public interface JavaAddExports {
	/**
	 * Gets the module that contains the exported packages.
	 * 
	 * @return The module name.
	 */
	public String getModule();

	/**
	 * Gets the names of the exported package.
	 * 
	 * @return The exported packages.
	 */
	public Set<String> getPackage();

	/**
	 * Gets the names of the modules to which the packages are exported to.
	 * 
	 * @return The module names, or empty collection if the packages are exported to <code>ALL-UNNAMED</code> modules.
	 */
	public Set<String> getTarget();

	@Override
	public int hashCode();

	@Override
	public boolean equals(Object obj);

	/**
	 * Creates a new add exports configuration.
	 * 
	 * @param module
	 *            The module from which the packages are exported.
	 * @param packages
	 *            The packages that are exported.
	 * @param target
	 *            The target module names for which the packages are exported. If <code>null</code> or empty, the
	 *            packages are exported to <code>ALL-UNNAMED</code> modules.
	 * @return The created add export configuration.
	 * @throws NullPointerException
	 *             If the <code>module</code> or <code>packages</code> are <code>null</code>.
	 * @throws IllegalArgumentException
	 *             If the <code>packages</code> collection is empty.
	 */
	public static JavaAddExports create(String module, Collection<String> packages, Collection<String> target)
			throws NullPointerException, IllegalArgumentException {
		return new SimpleAddExportsPath(module, packages, target);
	}
}
