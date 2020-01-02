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
package saker.java.compiler.api.processor;

import java.io.Externalizable;

import javax.annotation.processing.Processor;

/**
 * Stateless factory class for instantiating an {@linkplain Processor annotation processor}.
 * <p>
 * The {@link #create(ProcessorCreationContext)} method is called when the annotation processing is started, and the
 * resulting processor is used to perform the processing.
 * <p>
 * Clients should implement this interface. When doing so, make sure to adhere to the {@link #hashCode()} and
 * {@link #equals(Object)} contract. Implementations should also implement {@link Externalizable}.
 */
public interface ProcessorCreator {
	/**
	 * Gets the name of this processor creator.
	 * <p>
	 * The name plays no significance in the compilation process, it is only for display purposes for the user.
	 * 
	 * @return The name of this processor creator.
	 */
	public String getName();

	/**
	 * Creates the annotation processor.
	 * <p>
	 * The method should always return different instances of a processor, and shouldn't reuse implementations.
	 * <p>
	 * The processor creation context provides access to functionality to help the creation of classloaders and other
	 * runtime resources.
	 * 
	 * @param creationcontext
	 *            The processor creation context.
	 * @return The processor to use during annotation processing.
	 * @throws Exception
	 *             If the operation failed.
	 */
	public Processor create(ProcessorCreationContext creationcontext) throws Exception;

	@Override
	public int hashCode();

	/**
	 * Checks if this processor creator is the same as the argument.
	 * <p>
	 * Two processor creators are considered to be the same if they create the same processors given the same
	 * circumstances, and the created processors will produce the same outputs and operations.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj);
}