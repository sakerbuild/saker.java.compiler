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

import java.util.Objects;

import saker.java.compiler.impl.options.FileLocationClassAndModulePathReferenceOption;
import saker.java.compiler.impl.options.SDKClassAndModulePathReferenceOption;
import saker.sdk.support.api.SDKPathReference;
import saker.std.api.file.location.FileLocation;

/**
 * Represents an input file configuration for {@link ClassPathEntry}.
 * <p>
 * The interface encloses a classpath file specified with a given configuration. Users of this interface can examine the
 * input file by implementing a custom {@link ClassPathEntryInputFileVisitor} and calling
 * {@link #accept(ClassPathEntryInputFileVisitor) accept}.
 * <p>
 * This interface shouldn't be implemented by users. Use the <code>create</code> static methods to create a new instance
 * for a given kind.
 * 
 * @see ClassPathEntry
 * @see ClassPathEntry#getInputFile()
 * @since 0.8.4
 */
public interface ClassPathEntryInputFile {
	/**
	 * Accepts the argument visitor.
	 * <p>
	 * The method will call an appropriate <code>visit</code> method based on the input file configuration.
	 * 
	 * @param visitor
	 *            The visitor.
	 * @throws NullPointerException
	 *             If the visitor is <code>null</code>.
	 */
	public void accept(ClassPathEntryInputFileVisitor visitor) throws NullPointerException;

	@Override
	public int hashCode();

	@Override
	public boolean equals(Object obj);

	/**
	 * Creates a new input file for the argument file location.
	 * 
	 * @param filelocation
	 *            The file location.
	 * @return The created classpath input file.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 * @see ClassPathEntryInputFileVisitor#visit(FileClassPath)
	 */
	public static ClassPathEntryInputFile create(FileLocation filelocation) throws NullPointerException {
		Objects.requireNonNull(filelocation, "file location");
		return new FileLocationClassAndModulePathReferenceOption(filelocation);
	}

	/**
	 * Creates a new input file for the argument SDK path reference.
	 * 
	 * @param sdkpathreference
	 *            The SDK path reference.
	 * @return The created classpath input file.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 * @see ClassPathEntryInputFileVisitor#visit(SDKClassPath)
	 */
	public static ClassPathEntryInputFile create(SDKPathReference sdkpathreference) throws NullPointerException {
		Objects.requireNonNull(sdkpathreference, "sdk path reference");
		return new SDKClassAndModulePathReferenceOption(sdkpathreference);
	}
}
