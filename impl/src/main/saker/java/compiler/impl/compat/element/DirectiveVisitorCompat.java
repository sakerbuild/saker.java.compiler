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
package saker.java.compiler.impl.compat.element;

public interface DirectiveVisitorCompat<R, P> {
	public default R visit(DirectiveCompat d) {
		return d.accept(this, null);
	}

	public default R visit(DirectiveCompat d, P p) {
		return d.accept(this, p);
	}

	public R visitRequires(RequiresDirectiveCompat d, P p);

	public R visitExports(ExportsDirectiveCompat d, P p);

	public R visitOpens(OpensDirectiveCompat d, P p);

	public R visitUses(UsesDirectiveCompat d, P p);

	public R visitProvides(ProvidesDirectiveCompat d, P p);
}
