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
package saker.java.compiler.api.compile;

import saker.build.file.path.SakerPath;
import saker.sdk.support.api.SDKDescription;

/**
 * Interface providing access to the configuration related results of a Java compilation.
 * <p>
 * Clients shouldn't implement this interface.
 */
public interface JavaCompilationConfigurationOutput {
	/**
	 * Gets the worker task identifier that performs the compilation.
	 * 
	 * @return The task identifier.
	 */
	public JavaCompilationWorkerTaskIdentifier getCompilationTaskIdentifier();

	/**
	 * Gets the class output directory of the compilation.
	 * 
	 * @return The class output directory build execution path.
	 * @see SakerJavaCompilerUtils#DIR_CLASS_OUTPUT
	 */
	public SakerPath getClassDirectory();

	/**
	 * Gets the header output directory of the compilation.
	 * <p>
	 * Note that if the native header generation was not specified for the compilation task, the denoted directory may
	 * be empty.
	 * 
	 * @return The header output directory build execution path.
	 * @see JavaCompilationTaskBuilder#setGenerateNativeHeaders(Boolean)
	 * @see SakerJavaCompilerUtils#DIR_NATIVE_HEADER_OUTPUT
	 */
	public SakerPath getHeaderDirectory();

	/**
	 * Gets the resource output directory of the compilation.
	 * 
	 * @return The resource output directory build execution path.
	 * @see SakerJavaCompilerUtils#DIR_RESOURCE_OUTPUT
	 */
	public SakerPath getResourceDirectory();

	/**
	 * Gets the source output directory of the compilation.
	 * 
	 * @return The source output directory build execution path.
	 * @see SakerJavaCompilerUtils#DIR_SOURCE_OUTPUT
	 */
	public SakerPath getSourceGenDirectory();

	/**
	 * Gets the name of the module that was compiled.
	 * <p>
	 * It is only non-<code>null</code> if the compilation is being done targetting Java 9 or later, and there's a
	 * <code>module-info</code> with a valid module declaration.
	 * 
	 * @return The module name or <code>null</code> if no module was compiled.
	 */
	public String getModuleName();

	/**
	 * Gets the SDK description that is used to perform the compilation.
	 * <p>
	 * This is the {@link SDKDescription} that is associated with the <code>Java</code> name.
	 * 
	 * @return The SDK description.
	 */
	public SDKDescription getJavaSDK();

	@Override
	public int hashCode();

	@Override
	public boolean equals(Object obj);
}
