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

import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

import saker.java.compiler.impl.compile.handler.incremental.model.CommonWildcardType;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;

public class SimpleWildcardType extends SimpleTypeMirror implements CommonWildcardType {
	private TypeMirror extendsBound;
	private TypeMirror superBound;
	private TypeParameterElement correspondingTypeParameter;

	public SimpleWildcardType(IncrementalElementsTypesBase elemTypes, TypeMirror extendsBound, TypeMirror superBound,
			TypeParameterElement correspondingTypeParameter) {
		super(elemTypes);
		this.extendsBound = extendsBound;
		this.superBound = superBound;
		this.correspondingTypeParameter = correspondingTypeParameter;
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
		return extendsBound;
	}

	@Override
	public TypeMirror getSuperBound() {
		return superBound;
	}

	@Override
	public TypeParameterElement getCorrespondingTypeParameter() {
		return correspondingTypeParameter;
	}

	@Override
	public String toString() {
		return "?" + (superBound != null ? " super " + superBound : "")
				+ (extendsBound != null ? " extends " + extendsBound : "");
	}

}
