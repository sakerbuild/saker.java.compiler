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

import java.util.List;

import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.Name;

import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.compat.element.BaseElementCompatImpl;
import saker.java.compiler.impl.compat.element.DirectiveCompat;
import saker.java.compiler.impl.compat.element.ModuleElementCompat;

public class ModuleElementCompatImpl extends BaseElementCompatImpl<ModuleElement> implements ModuleElementCompat {
	public ModuleElementCompatImpl(ModuleElement real) {
		super(real);
	}

	@Override
	public Name getQualifiedName() {
		return real.getQualifiedName();
	}

	@Override
	public boolean isOpen() {
		return real.isOpen();
	}

	@Override
	public boolean isUnnamed() {
		return real.isUnnamed();
	}

	@Override
	public List<? extends DirectiveCompat> getDirectives() {
		return JavaTaskUtils.cloneImmutableList(real.getDirectives(),
				DirectiveCompatCreatorVisitor.INSTANCE::toDirectiveCompat);
	}

}
