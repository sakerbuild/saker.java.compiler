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
import java.util.Collections;
import java.util.List;

import saker.java.compiler.impl.compile.signature.impl.AnnotatedSignatureImpl;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.signature.type.UnknownTypeSignature;
import saker.java.compiler.impl.util.JavaSerialUtils;

public final class UnknownTypeSignatureImpl extends AnnotatedSignatureImpl implements UnknownTypeSignature {
	private static final long serialVersionUID = 1L;

	private String typeDescription;

	public UnknownTypeSignatureImpl() {
	}

	public static UnknownTypeSignatureImpl create(String typedescription) {
		return new UnknownTypeSignatureImpl(typedescription);
	}

	public static UnknownTypeSignatureImpl create(List<AnnotationSignature> annotations, String typeDescription) {
		return new UnknownTypeSignatureImpl(annotations, typeDescription);
	}

	private UnknownTypeSignatureImpl(String typedescription) {
		super(Collections.emptyList());
		this.typeDescription = typedescription;
	}

	private UnknownTypeSignatureImpl(List<AnnotationSignature> annotations, String typeDescription) {
		super(annotations);
		this.typeDescription = typeDescription;
	}

	@Override
	public String getTypeDescription() {
		return typeDescription;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((typeDescription == null) ? 0 : typeDescription.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		UnknownTypeSignatureImpl other = (UnknownTypeSignatureImpl) obj;
		if (typeDescription == null) {
			if (other.typeDescription != null)
				return false;
		} else if (!typeDescription.equals(other.typeDescription))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return super.toString() + typeDescription;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		JavaSerialUtils.writeOpenEndedList(annotations, out);
		out.writeObject(typeDescription);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		ArrayList<AnnotationSignature> annotations = new ArrayList<>();
		this.annotations = annotations;
		this.typeDescription = (String) JavaSerialUtils.readOpenEndedList(AnnotationSignature.class, annotations, in);
	}

	@Override
	public String getSimpleName() {
		return null;
	}

	@Override
	public TypeSignature getEnclosingSignature() {
		return null;
	}
}
