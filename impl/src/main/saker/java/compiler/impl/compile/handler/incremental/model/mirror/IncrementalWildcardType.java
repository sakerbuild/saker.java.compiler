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
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

import saker.java.compiler.impl.compile.handler.incremental.model.CommonWildcardType;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.signature.type.WildcardTypeSignature;

public class IncrementalWildcardType extends IncrementalTypeMirror<WildcardTypeSignature>
		implements CommonWildcardType {
	private static final AtomicReferenceFieldUpdater<IncrementalWildcardType, TypeMirror> ARFU_extendsBound = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalWildcardType.class, TypeMirror.class, "extendsBound");
	private static final AtomicReferenceFieldUpdater<IncrementalWildcardType, TypeMirror> ARFU_superBound = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalWildcardType.class, TypeMirror.class, "superBound");

	private Element enclosingElement;
	private TypeParameterElement correspondingTypeParameter;

	private volatile transient TypeMirror extendsBound;
	private volatile transient TypeMirror superBound;

	public IncrementalWildcardType(IncrementalElementsTypesBase elemTypes, WildcardTypeSignature signature,
			Element enclosingElement, TypeParameterElement correspondingTypeParameter) {
		super(elemTypes, signature);
		this.enclosingElement = enclosingElement;
		this.correspondingTypeParameter = correspondingTypeParameter;
	}

	@Override
	protected Element getEnclosingResolutionElement() {
		return enclosingElement;
	}

	@Override
	public void invalidate() {
		super.invalidate();
		//fine to use direct assignment as this will not be called from multi-threaded code
		this.extendsBound = null;
		this.superBound = null;
	}

	@Override
	public TypeKind getKind() {
		return TypeKind.WILDCARD;
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		return v.visitWildcard(this, p);
	}

	@Override
	public TypeMirror getExtendsBound() {
		TypeMirror thisextendsbound = this.extendsBound;
		if (thisextendsbound != null) {
			return thisextendsbound;
		}
		TypeSignature upper = signature.getUpperBounds();
		if (upper == null) {
			return null;
		}
		thisextendsbound = elemTypes.getTypeMirror(upper, enclosingElement);
		if (ARFU_extendsBound.compareAndSet(this, null, thisextendsbound)) {
			return thisextendsbound;
		}
		return this.extendsBound;
	}

	@Override
	public TypeMirror getSuperBound() {
		TypeMirror thissuperbound = superBound;
		if (thissuperbound != null) {
			return thissuperbound;
		}
		TypeSignature lower = signature.getLowerBounds();
		if (lower == null) {
			return null;
		}
		thissuperbound = elemTypes.getTypeMirror(lower, enclosingElement);
		if (ARFU_superBound.compareAndSet(this, null, thissuperbound)) {
			return thissuperbound;
		}
		return this.superBound;
	}

	@Override
	public TypeParameterElement getCorrespondingTypeParameter() {
		return correspondingTypeParameter;
	}

}
