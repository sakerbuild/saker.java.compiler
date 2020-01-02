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
package saker.java.compiler.util9.impl.model.mirror;

import javax.lang.model.element.ModuleElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;

import saker.java.compiler.impl.compile.handler.incremental.model.mirror.SimpleTypeMirror;
import saker.java.compiler.util9.impl.model.IncrementalElementsTypes9;

public class SimpleModuleType extends SimpleTypeMirror implements CommonModuleType {
	private ModuleElement moduleElement;

	public SimpleModuleType(IncrementalElementsTypes9 elemTypes, ModuleElement moduleElement) {
		super(elemTypes);
		this.moduleElement = moduleElement;
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		return v.visitNoType(this, p);
	}

	@Override
	public TypeKind getKind() {
		return TypeKind.MODULE;
	}

	@Override
	public ModuleElement getModuleElement() {
		return moduleElement;
	}

}
