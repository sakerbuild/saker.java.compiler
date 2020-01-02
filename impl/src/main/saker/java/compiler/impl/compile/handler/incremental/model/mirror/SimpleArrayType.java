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

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;

public class SimpleArrayType extends SimpleTypeMirror implements ArrayType {
	private TypeMirror componentType;

	public SimpleArrayType(IncrementalElementsTypesBase elemTypes, TypeMirror componentType) {
		super(elemTypes);
		this.componentType = componentType;
	}

	public static SimpleArrayType erasured(IncrementalElementsTypesBase elemTypes, TypeMirror erasedcomponent) {
		SimpleArrayType result = new SimpleArrayType(elemTypes, erasedcomponent);
		result.erasedType = result;
		return result;
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
		return componentType;
	}

	@Override
	public String toString() {
		return componentType + "[]";
	}
}
