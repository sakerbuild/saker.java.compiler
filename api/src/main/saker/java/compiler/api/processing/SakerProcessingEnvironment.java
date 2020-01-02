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
package saker.java.compiler.api.processing;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.lang.model.SourceVersion;

import saker.java.compiler.api.processing.exc.SourceVersionNotFoundException;

/**
 * Describes the processing environment functionality provided by the incremental Java compiler.
 * <p>
 * The {@link ProcessingEnvironment} instance can be downcasted to {@link SakerProcessingEnvironment} during annotation
 * processing.
 * <p>
 * The interface provides access to the name of the current {@linkplain #getSourceVersion() source version} enumeration
 * that can avoid unexpected exceptions when the annotation processing and compilation are being done in a separate JVM.
 * 
 * @see Processor#init(ProcessingEnvironment)
 */
public interface SakerProcessingEnvironment extends ProcessingEnvironment {
	/**
	 * {@inheritDoc}
	 * 
	 * @throws SourceVersionNotFoundException
	 *             If the source version doesn't have an associated enum value in the current JVM. The name of the enum
	 *             will be the {@linkplain SourceVersionNotFoundException#getMessage() message} of the thrown exception.
	 *             <br>
	 *             It is recommended that callers attempt to catch an {@link IllegalArgumentException} instead, to avoid
	 *             any class loading related versioning issues.
	 * @see #getSourceVersionName()
	 */
	@Override
	public SourceVersion getSourceVersion() throws SourceVersionNotFoundException;

	/**
	 * Gets the name of the {@link SourceVersion} value returned by {@link #getSourceVersion()}.
	 * <p>
	 * This method serves the compatibility support when processing sources of a higher language model that can be
	 * represented in the current JVM.
	 * <p>
	 * E.g. if the compilation source version is <code>RELEASE_9</code>, and the current JVM has the version 8, then the
	 * <code>SourceVersion.RELEASE_9</code> doesn't exist in the current JVM, therefore
	 * {@link SourceVersion#valueOf(String)} would throw an {@link IllegalArgumentException}. This method returns the
	 * name of the {@link SourceVersion} enum that would be returned by {@link #getSourceVersion()}.
	 * 
	 * @return The name of the {@link SourceVersion} enum value.
	 * @see SourceVersion#name()
	 */
	public String getSourceVersionName();

	@Override
	public SakerElementsTypes getElementUtils();

	@Override
	public SakerElementsTypes getTypeUtils();

	@Override
	public SakerFiler getFiler();

	@Override
	public SakerMessager getMessager();

}
