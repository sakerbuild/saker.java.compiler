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
package saker.java.compiler.impl.compat.type;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

public interface TypeVisitorCompat<R, P> extends TypeVisitor<R, P> {
	@Override
	public default R visit(TypeMirror t, P p) {
		return t.accept(this, p);
	}

	@Override
	public default R visit(TypeMirror t) {
		return visit(t, null);
	}
}
