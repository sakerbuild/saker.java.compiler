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

import java.util.Objects;

import saker.build.task.identifier.TaskIdentifier;

/**
 * Represents a task identifier for a Java compilation worker task.
 * <p>
 * The Java compiler worker task is the one that actually compiles the Java source files. The task has an output that is
 * an instance of {@link JavaCompilerWorkerTaskOutput}. If you retrieve the result of a task that bears a task
 * identifier that is the instance of this interface, then you can cast the result to
 * {@link JavaCompilerWorkerTaskOutput}.
 * <p>
 * clients shouldn't implement this interface.
 */
public interface JavaCompilationWorkerTaskIdentifier extends TaskIdentifier {
	/**
	 * Gets the pass identifier of the compiler task.
	 * <p>
	 * The identifier is usually the one specified by the user, or automatically generated based on the input
	 * configuration.
	 * 
	 * @return The identifier.
	 */
	public String getPassIdentifier();

	/**
	 * Creates a new instance with the given pass identifier.
	 * 
	 * @param passidentifier
	 *            The pass identifier.
	 * @return The created task identifier.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 */
	public static JavaCompilationWorkerTaskIdentifier create(String passidentifier) throws NullPointerException {
		Objects.requireNonNull(passidentifier, "pass id");
		return new JavaCompilationWorkerTaskIdentifierImpl(passidentifier);
	}
}