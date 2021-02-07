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

import javax.lang.model.element.ElementKind;

/**
 * Exception thrown when an {@link ElementKind} was not found in the current JVM.
 * <p>
 * Exceptions of this type can be thrown if the compilation is being done by a more recent JVM version than the one that
 * tries to retrieve the {@link ElementKind} enumeration value.
 * <p>
 * The {@linkplain #getMessage() message} returns the name of the {@link ElementKind} that was not found.
 * 
 * @since saker.java.compiler 0.8.7
 */
public final class ElementKindNotFoundException extends EnumerationNotFoundException {
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new instance with the specified enum name and the {@link IllegalArgumentException} that caused it.
	 * 
	 * @param enumname
	 *            The name of the {@link ElementKind} that was not found.
	 * @param cause
	 *            The exception that determined that the enumeration was not found. It is the one thrown by
	 *            {@link ElementKind#valueOf(String)}.
	 * @throws NullPointerException
	 *             If any of the arguments are <code>null</code>.
	 */
	public ElementKindNotFoundException(String enumname, IllegalArgumentException cause) throws NullPointerException {
		super(Objects.requireNonNull(enumname, "enum name"), Objects.requireNonNull(cause, "cause"));
	}

}
