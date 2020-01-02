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

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.Element;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;
import saker.java.compiler.impl.signature.type.ArrayTypeSignature;

public class IncrementalArrayType extends IncrementalTypeMirror<ArrayTypeSignature> implements ArrayType {
	private static final AtomicReferenceFieldUpdater<IncrementalArrayType, TypeMirror> ARFU_typeParameters = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalArrayType.class, TypeMirror.class, "componentType");

	private Element enclosingElement;

	private volatile transient TypeMirror componentType;

	public IncrementalArrayType(IncrementalElementsTypesBase elemTypes, ArrayTypeSignature signature,
			Element enclosingElement) {
		super(elemTypes, signature);
		this.enclosingElement = enclosingElement;
	}

	@Override
	protected Element getEnclosingResolutionElement() {
		return enclosingElement;
	}

	@Override
	public void invalidate() {
		super.invalidate();
		//fine to use direct assignment as this will not be called from multi-threaded code
		this.componentType = null;
	}

	@Override
	public void setSignature(ArrayTypeSignature signature) {
		super.setSignature(signature);
		this.componentType = null;
	}

	@Override
	public TypeKind getKind() {
		return TypeKind.ARRAY;
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		return v.visitArray(this, p);
	}

	@Override
	public TypeMirror getComponentType() {
		TypeMirror thiscomponenttype = componentType;
		if (thiscomponenttype == null) {
			thiscomponenttype = elemTypes.getTypeMirror(signature.getComponentType(), enclosingElement);
			if (ARFU_typeParameters.compareAndSet(this, null, thiscomponenttype)) {
				return thiscomponenttype;
			}
		}
		return this.componentType;
	}

}
