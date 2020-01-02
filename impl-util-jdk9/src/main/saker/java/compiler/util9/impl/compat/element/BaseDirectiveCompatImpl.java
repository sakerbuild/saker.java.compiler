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
package saker.java.compiler.util9.impl.compat.element;

import javax.lang.model.element.ModuleElement.Directive;

import saker.java.compiler.impl.compat.element.DirectiveCompat;

public abstract class BaseDirectiveCompatImpl<D extends Directive> implements DirectiveCompat {
	protected final D real;

	public BaseDirectiveCompatImpl(D real) {
		this.real = real;
	}

	@Override
	public D getRealObject() {
		return real;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + real + "]";
	}

	@Override
	public String getKind() {
		return real.getKind().toString();
	}
}
