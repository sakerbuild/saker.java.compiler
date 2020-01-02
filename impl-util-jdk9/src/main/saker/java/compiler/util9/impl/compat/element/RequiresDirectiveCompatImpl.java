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

import javax.lang.model.element.ModuleElement.RequiresDirective;

import saker.java.compiler.impl.compat.element.ModuleElementCompat;
import saker.java.compiler.impl.compat.element.RequiresDirectiveCompat;

public class RequiresDirectiveCompatImpl extends BaseDirectiveCompatImpl<RequiresDirective>
		implements RequiresDirectiveCompat {

	public RequiresDirectiveCompatImpl(RequiresDirective real) {
		super(real);
	}

	@Override
	public boolean isStatic() {
		return real.isStatic();
	}

	@Override
	public boolean isTransitive() {
		return real.isTransitive();
	}

	@Override
	public ModuleElementCompat getDependency() {
		return new ModuleElementCompatImpl(real.getDependency());
	}

}
