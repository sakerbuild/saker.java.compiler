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

import java.util.List;

import javax.lang.model.element.PackageElement;

public interface ExportsDirectiveCompat extends DirectiveCompat {
	@Override
	public default <R, P> R accept(DirectiveVisitorCompat<R, P> v, P p) {
		return v.visitExports(this, p);
	}

	public PackageElement getPackage();

	public List<? extends ModuleElementCompat> getTargetModules();
}
