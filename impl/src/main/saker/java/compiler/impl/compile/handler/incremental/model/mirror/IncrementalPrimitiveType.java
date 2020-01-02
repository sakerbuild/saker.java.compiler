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
package saker.java.compiler.impl.compile.handler.incremental.model.mirror;

import javax.lang.model.element.Element;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;

import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;
import saker.java.compiler.impl.signature.type.PrimitiveTypeSignature;

public class IncrementalPrimitiveType extends IncrementalTypeMirror<PrimitiveTypeSignature> implements PrimitiveType {
	private Element enclosingElement;

	public IncrementalPrimitiveType(IncrementalElementsTypesBase elemTypes, PrimitiveTypeSignature signature,
			Element enclosingelement) {
		super(elemTypes, signature);
		this.enclosingElement = enclosingelement;
	}

	@Override
	protected Element getEnclosingResolutionElement() {
		return enclosingElement;
	}

	@Override
	public TypeKind getKind() {
		return signature.getTypeKind();
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		return v.visitPrimitive(this, p);
	}
}
