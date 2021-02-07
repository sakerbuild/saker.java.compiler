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

/**
 * Common exception superclass for the scenario when an enumeration value cannot be found in the current JVM.
 * <p>
 * Exceptions of this type can be thrown if the enumeration value that is needed is not found in the current JVM. This
 * can happen when some code is running on a more recent Java version and needs to communicate with an older version. In
 * cases when new enumeration values are introduced, they won't be found in the older JVM, therefore this exception is
 * thrown by the handling code.
 * <p>
 * The {@linkplain #getMessage() message} returns the name of the enumeration that was not found.
 * 
 * @since saker.java.compiler 0.8.8
 * @see ElementKindNotFoundException
 * @see ModifierNotFoundException
 * @see SourceVersionNotFoundException
 */
public class EnumerationNotFoundException extends IllegalArgumentException {
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new instance with the specified enum name and the {@link IllegalArgumentException} that caused it.
	 * 
	 * @param enumname
	 *            The name of the {@link Enum} that was not found.
	 * @param cause
	 *            The exception that was thrown when the enumeration was not found. It is the one thrown by
	 *            {@link Enum#valueOf(Class, String)} or other more specific <code>Enum.valueOf</code> methods.
	 * @throws NullPointerException
	 *             If any of the arguments are <code>null</code>.
	 */
	public EnumerationNotFoundException(String enumname, IllegalArgumentException cause) throws NullPointerException {
		super(Objects.requireNonNull(enumname, "enum name"), Objects.requireNonNull(cause, "cause"));
	}

	/**
	 * Gets the enumeration name that was not found.
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
	 * The exception is directly thrown by {@link Enum#valueOf(Class, String)} or other more specific
	 * <code>Enum.valueOf</code> methods.
	 * <p>
	 * {@inheritDoc}
	 */
	@Override
	public IllegalArgumentException getCause() {
		return (IllegalArgumentException) super.getCause();
	}
}
