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

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.type.TypeParameterSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.util.JavaSerialUtils;

public class TypeParameterSignatureImpl extends ExtendingTypeParameterSignature {
	private static final long serialVersionUID = 1L;

	protected List<AnnotationSignature> annotations;
	protected TypeSignature lowerBounds;

	public TypeParameterSignatureImpl() {
	}

	public static TypeParameterSignature create(String varName, TypeSignature lowerBounds, TypeSignature upperBounds) {
		if (lowerBounds == null) {
			if (upperBounds == null) {
				//just a simple name as a type parameter
				return SimpleTypeParameterSignature.create(varName);
			}
			//T extends Something format
			return new ExtendingTypeParameterSignature(varName, upperBounds);
		}
		return new TypeParameterSignatureImpl(varName, upperBounds, Collections.emptyList(), lowerBounds);
	}

	public static TypeParameterSignature create(List<AnnotationSignature> annotations, String varName,
			TypeSignature lowerBounds, TypeSignature upperBounds) {
		if (ObjectUtils.isNullOrEmpty(annotations)) {
			return create(varName, lowerBounds, upperBounds);
		}
		return new TypeParameterSignatureImpl(varName, upperBounds, annotations, lowerBounds);
	}

	public TypeParameterSignatureImpl(String varName, TypeSignature upperBounds, List<AnnotationSignature> annotations,
			TypeSignature lowerBounds) {
		super(varName, upperBounds);
		this.annotations = annotations;
		this.lowerBounds = lowerBounds;
	}

	@Override
	public List<? extends AnnotationSignature> getAnnotations() {
		return annotations;
	}

	@Override
	public TypeSignature getLowerBounds() {
		return lowerBounds;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((annotations == null) ? 0 : annotations.hashCode());
		result = prime * result + ((lowerBounds == null) ? 0 : lowerBounds.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof TypeParameterSignatureImpl))
			return false;
		TypeParameterSignatureImpl other = (TypeParameterSignatureImpl) obj;
		if (annotations == null) {
			if (other.annotations != null)
				return false;
		} else if (!annotations.equals(other.annotations))
			return false;
		if (lowerBounds == null) {
			if (other.lowerBounds != null)
				return false;
		} else if (!lowerBounds.equals(other.lowerBounds))
			return false;
		return true;
	}

	@Override
	public String toString() {
		String result = ObjectUtils.isNullOrEmpty(annotations) ? ""
				: (StringUtils.toStringJoin(" ", annotations) + " ") + super.toString();
		if (lowerBounds != null) {
			result += " super " + lowerBounds;
		}
		return result;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);

		JavaSerialUtils.writeOpenEndedList(annotations, out);
		out.writeObject(lowerBounds);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);

		ArrayList<AnnotationSignature> annotations = new ArrayList<>();
		this.annotations = annotations;
		this.lowerBounds = (TypeSignature) JavaSerialUtils.readOpenEndedList(AnnotationSignature.class, annotations,
				in);
	}

}
