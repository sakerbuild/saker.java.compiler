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

import java.util.Collection;
import java.util.Map;

import saker.java.compiler.api.processor.ProcessorConfiguration;

/**
 * Builder interface for {@link JavaAnnotationProcessor}.
 * <p>
 * Clients shouldn't implement this interface.
 * <p>
 * Create a new instance using {@link #newBuilder()}.
 */
public interface JavaAnnotationProcessorBuilder {
	/**
	 * Sets the processor configuration.
	 * 
	 * @param configuration
	 *            The configuration.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 * @see JavaAnnotationProcessor#getProcessor()
	 */
	public void setProcessor(ProcessorConfiguration configuration) throws NullPointerException;

	/**
	 * Sets the aggregating property of the processor.
	 * <p>
	 * If set to <code>null</code>, {@link ProcessorConfiguration#getAggregating()} is used.
	 * 
	 * @param aggregating
	 *            The aggregating property or <code>null</code> to use the processor provided value.
	 * @see JavaAnnotationProcessor#getAggregating()
	 */
	public void setAggregating(Boolean aggregating);

	/**
	 * Sets the consistent property of the processor.
	 * <p>
	 * If set to <code>null</code>, {@link ProcessorConfiguration#getConsistent()} is used.
	 * 
	 * @param consistent
	 *            The consistent property or <code>null</code> to use the processor provided value.
	 * @see JavaAnnotationProcessor#getConsistent()
	 */
	public void setConsistent(Boolean consistent);

	/**
	 * Sets the always run property of the processor.
	 * <p>
	 * If set to <code>null</code>, {@link ProcessorConfiguration#getAlwaysRun()} is used.
	 * 
	 * @param alwaysRun
	 *            The always run property or <code>null</code> to use the processor provided value.
	 * @see JavaAnnotationProcessor#getAlwaysRun()
	 */
	public void setAlwaysRun(Boolean alwaysRun);

	/**
	 * Sets the processor key-value options.
	 * 
	 * @param options
	 *            The options or <code>null</code> to use none.
	 * @see JavaAnnotationProcessor#getOptions()
	 */
	public void setOptions(Map<String, String> options);

	/**
	 * Sets the warning types to suppress for the processor.
	 * 
	 * @param suppressWarnings
	 *            The warning types or <code>null</code> for no additional suppression.
	 * @see JavaAnnotationProcessor#getSuppressWarnings()
	 * @see JavaCompilerWarningType
	 */
	public void setSuppressWarnings(Collection<String> suppressWarnings);

	/**
	 * Builds the annotation processor configuration.
	 * <p>
	 * The builder can be reused after this call.
	 * 
	 * @return The annotation processor configuration.
	 * @throws IllegalStateException
	 *             If the {@linkplain #setProcessor(ProcessorConfiguration) processor} hasn't been set.
	 */
	public JavaAnnotationProcessor build() throws IllegalStateException;

	/**
	 * Creates a new builder instance.
	 * 
	 * @return The builder.
	 */
	public static JavaAnnotationProcessorBuilder newBuilder() {
		return new JavaAnnotationProcessorBuilderImpl();
	}

}