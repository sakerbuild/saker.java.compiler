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
import saker.sdk.support.api.SDKPathReference;
import saker.std.api.file.location.FileLocation;

/**
 * Builder interface for {@link JavaModulePath}.
 * <p>
 * Clients shouldn't implement this interface.
 * <p>
 * Create a new instance using {@link #newBuilder()}.
 */
public interface JavaModulePathBuilder {
	/**
	 * Adds a compilation modulepath to the builder.
	 * 
	 * @param compilationworkertaskid
	 *            The task identifier of the compilation worker build task.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 * @see CompilationModulePath
	 */
	public void addCompileModulePath(JavaCompilationWorkerTaskIdentifier compilationworkertaskid)
			throws NullPointerException;

	/**
	 * Adds a file modulepath to the builder.
	 * 
	 * @param filelocation
	 *            The file location of the modulepath.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 * @see FileModulePath
	 */
	public void addFileModulePath(FileLocation filelocation) throws NullPointerException;

	/**
	 * Adds an SDK modulepath to the builder.
	 * 
	 * @param sdkpathreference
	 *            The SDK path reference.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 * @see SDKModulePath
	 */
	public void addSDKModulePath(SDKPathReference sdkpathreference) throws NullPointerException;

	/**
	 * Builds the modulepath.
	 * <p>
	 * The builder can be reused after this call.
	 * 
	 * @return The created modulepath.
	 */
	public JavaModulePath build();

	/**
	 * Creates a new builder.
	 * 
	 * @return The builder.
	 */
	public static JavaModulePathBuilder newBuilder() {
		return new JavaModulePathBuilderImpl();
	}

}