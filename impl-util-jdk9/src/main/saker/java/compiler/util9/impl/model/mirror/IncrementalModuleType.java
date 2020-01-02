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

import javax.lang.model.element.Element;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;

import saker.java.compiler.impl.compile.handler.incremental.model.mirror.IncrementalTypeMirror;
import saker.java.compiler.impl.signature.element.ModuleSignature;
import saker.java.compiler.util9.impl.model.IncrementalElementsTypes9;
import saker.java.compiler.util9.impl.model.elem.IncrementalModuleElement;

public class IncrementalModuleType extends IncrementalTypeMirror<ModuleSignature> implements CommonModuleType {

	private IncrementalModuleElement module;

	public IncrementalModuleType(IncrementalElementsTypes9 elemTypes, IncrementalModuleElement module) {
		super(elemTypes, module.getSignature());
		this.module = module;
		setElementTypes(IncrementalElementsTypes9.ELEMENT_TYPE_MODULE);
	}

	@Override
	protected Element getEnclosingResolutionElement() {
		return module;
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
		return module;
	}

}
