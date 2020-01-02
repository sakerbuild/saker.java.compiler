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
package saker.java.compiler.api.processing.exc;

import java.util.Objects;

import javax.lang.model.SourceVersion;

/**
 * Exception describing the scenario when a {@link SourceVersion} enumeration cannot be retrieved due to it not being
 * available in the current JVM.
 * <p>
 * This scenario can happen when the compilation is running in a forked mode, where the compilation is being done in a
 * separate JVM, but the annotation processing is done in the build JVM. If the forked JVM is compiling for a more
 * recent {@link SourceVersion} that cannot be represented in the JVM that is running the annotation processing, then
 * this exception is thrown.
 * <p>
 * The actual name of the enumeration that was not found can be retrieved by calling {@link #getMessage()}. It is in the
 * format of <code>RELEASE_*</code>.
 */
public final class SourceVersionNotFoundException extends IllegalArgumentException {
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new instance with the specified enum name and the {@link IllegalArgumentException} that caused this.
	 * 
	 * @param enumname
	 *            The name of the {@link SourceVersion} that was not found.
	 * @param cause
	 *            The exception that determined that the enumeration was not found. It is the one thrown by
	 *            {@link SourceVersion#valueOf(String)}.
	 * @throws NullPointerException
	 *             If any of the arguments are <code>null</code>.
	 */
	public SourceVersionNotFoundException(String enumname, IllegalArgumentException cause) throws NullPointerException {
		super(Objects.requireNonNull(enumname, "enum name"), Objects.requireNonNull(cause, "cause"));
	}

	/**
	 * Gets the enumeration name of the {@link SourceVersion} that was not found.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public String getMessage() {
		return super.getMessage();
	}

	/**
	 * Gets the exception that was received when the enumeration was tried to be looked up.
	 * <p>
	 * The exception is directly thrown by {@link SourceVersion#valueOf(String)}.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public IllegalArgumentException getCause() {
		return (IllegalArgumentException) super.getCause();
	}
}
