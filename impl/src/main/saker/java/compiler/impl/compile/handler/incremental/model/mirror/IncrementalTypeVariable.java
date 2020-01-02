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

import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;

import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.signature.type.TypeVariableTypeSignature;

public class IncrementalTypeVariable extends IncrementalTypeMirror<TypeSignature> implements TypeVariable {
	private static final AtomicReferenceFieldUpdater<IncrementalTypeVariable, TypeMirror> ARFU_upperBound = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalTypeVariable.class, TypeMirror.class, "upperBound");
	private static final AtomicReferenceFieldUpdater<IncrementalTypeVariable, TypeMirror> ARFU_lowerBound = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalTypeVariable.class, TypeMirror.class, "lowerBound");

	private TypeParameterElement element;
	private Element genericElement;

	private volatile transient TypeMirror upperBound;
	private volatile transient TypeMirror lowerBound;

	public IncrementalTypeVariable(IncrementalElementsTypesBase elemTypes, TypeVariableTypeSignature sig,
			TypeParameterElement element) {
		super(elemTypes, sig);
		this.element = element;
		if (element != null) {
			genericElement = element.getGenericElement();
		}
	}

	@Override
	protected Element getEnclosingResolutionElement() {
		return element;
	}

	@Override
	public void invalidate() {
		super.invalidate();
		//fine to use direct assignment as this will not be called from multi-threaded code
		this.upperBound = null;
		this.lowerBound = null;
	}

	@Override
	public TypeKind getKind() {
		return TypeKind.TYPEVAR;
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		return v.visitTypeVariable(this, p);
	}

	@Override
	public Element asElement() {
		return element;
	}

	@Override
	public TypeMirror getUpperBound() {
		TypeMirror thisupperbound = this.upperBound;
		if (thisupperbound != null) {
			return thisupperbound;
		}
		if (element == null) {
			thisupperbound = elemTypes.getJavaLangObjectTypeMirror();
		} else {
			List<? extends TypeMirror> bounds = element.getBounds();
			switch (bounds.size()) {
				case 0: {
					thisupperbound = elemTypes.getJavaLangObjectTypeMirror();
					break;
				}
				case 1: {
					thisupperbound = bounds.get(0);
					break;
				}
				default: {
					thisupperbound = new SimpleIntersectionType(elemTypes, bounds);
					break;
				}
			}
		}
//			TypeSignature upper = signature.getUpperBounds();
//			if (upper == null) {
//				thisupperbound = elemTypes.getJavaLangObjectTypeMirror();
//			} else {
//				thisupperbound = elemTypes.getTypeMirror(upper, genericElement);
//			}
		if (ARFU_upperBound.compareAndSet(this, null, thisupperbound)) {
			return thisupperbound;
		}
		return this.upperBound;
//		TypeMirror thisupperbound = this.upperBound;
//		if (thisupperbound == null) {
//			TypeSignature upper = signature.getUpperBounds();
//			if (upper == null) {
//				thisupperbound = elemTypes.getJavaLangObjectTypeMirror();
//			} else {
//				thisupperbound = elemTypes.getTypeMirror(upper, genericElement);
//			}
//			if (ARFU_upperBound.compareAndSet(this, null, thisupperbound)) {
//				return thisupperbound;
//			}
//		}
//		return this.upperBound;
	}

	@Override
	public TypeMirror getLowerBound() {
		return elemTypes.getNullType();
//		TypeMirror thislowerbound = this.lowerBound;
//		if (thislowerbound == null) {
//			TypeSignature lower = signature.getLowerBounds();
//			if (lower == null) {
//				thislowerbound = elemTypes.getNullType();
//			} else {
//				thislowerbound = elemTypes.getTypeMirror(lower, genericElement);
//			}
//			if (ARFU_lowerBound.compareAndSet(this, null, thislowerbound)) {
//				return thislowerbound;
//			}
//		}
//		return this.lowerBound;
	}

}
