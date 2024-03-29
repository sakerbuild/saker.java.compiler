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
package saker.java.compiler.impl.compile.signature.type.impl;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.compile.signature.impl.AnnotatedSignatureImpl;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.type.ArrayTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.util.JavaSerialUtils;

public final class ArrayTypeSignatureImpl extends AnnotatedSignatureImpl implements ArrayTypeSignature {
	private static final long serialVersionUID = 1L;

	private TypeSignature componentType;

	public ArrayTypeSignatureImpl() {
	}

	public static ArrayTypeSignature create(TypeSignature componentType) {
		return SimpleArrayTypeSignature.create(componentType);
	}

	public static ArrayTypeSignature create(List<? extends AnnotationSignature> annotations,
			TypeSignature componentType) {
		if (ObjectUtils.isNullOrEmpty(annotations)) {
			return SimpleArrayTypeSignature.create(componentType);
		}
		return new ArrayTypeSignatureImpl(annotations, componentType);
	}

	private ArrayTypeSignatureImpl(List<? extends AnnotationSignature> annotations, TypeSignature componentType) {
		super(annotations);
		this.componentType = componentType;
	}

	@Override
	public TypeSignature getComponentType() {
		return componentType;
	}

	@Override
	public int hashCode() {
		return super.hashCode() * 31 + Objects.hashCode(componentType);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ArrayTypeSignatureImpl other = (ArrayTypeSignatureImpl) obj;
		if (componentType == null) {
			if (other.componentType != null)
				return false;
		} else if (!componentType.equals(other.componentType))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return super.toString() + componentType + "[]";
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		JavaSerialUtils.writeOpenEndedList(annotations, out);
		out.writeObject(componentType);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		ArrayList<AnnotationSignature> annotations = new ArrayList<>();
		this.annotations = annotations;
		this.componentType = (TypeSignature) JavaSerialUtils.readOpenEndedList(AnnotationSignature.class, annotations,
				in);
	}

	@Override
	public TypeSignature getEnclosingSignature() {
		return null;
	}

	@Override
	public String getSimpleName() {
		return componentType.getSimpleName() + "[]";
	}

}
