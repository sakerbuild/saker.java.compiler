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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.type.TypeParameterTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;

public class TypeParameterTypeSignatureImpl extends ExtendingTypeParameterTypeSignature {
	private static final long serialVersionUID = 1L;

	protected List<AnnotationSignature> annotations;
	protected TypeSignature lowerBounds;

	public TypeParameterTypeSignatureImpl() {
	}

	public static TypeParameterTypeSignature create(String varName, TypeSignature lowerBounds,
			TypeSignature upperBounds) {
		if (lowerBounds == null) {
			if (upperBounds == null) {
				//just a simple name as a type parameter
				return new SimpleTypeParameterTypeSignature(varName);
			}
			//T extends Something format
			return new ExtendingTypeParameterTypeSignature(varName, upperBounds);
		}
		return new TypeParameterTypeSignatureImpl(varName, upperBounds, Collections.emptyList(), lowerBounds);
	}

	public static TypeParameterTypeSignature create(List<AnnotationSignature> annotations, String varName,
			TypeSignature lowerBounds, TypeSignature upperBounds) {
		if (ObjectUtils.isNullOrEmpty(annotations)) {
			return create(varName, lowerBounds, upperBounds);
		}
		return new TypeParameterTypeSignatureImpl(varName, upperBounds, annotations, lowerBounds);
	}

	public TypeParameterTypeSignatureImpl(String varName, TypeSignature upperBounds,
			List<AnnotationSignature> annotations, TypeSignature lowerBounds) {
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
		result = prime * result + ((lowerBounds == null) ? 0 : lowerBounds.hashCode());
		result = prime * result + ((upperBounds == null) ? 0 : upperBounds.hashCode());
		result = prime * result + ((varName == null) ? 0 : varName.hashCode());
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
		TypeParameterTypeSignatureImpl other = (TypeParameterTypeSignatureImpl) obj;
		if (lowerBounds == null) {
			if (other.lowerBounds != null)
				return false;
		} else if (!lowerBounds.equals(other.lowerBounds))
			return false;
		if (upperBounds == null) {
			if (other.upperBounds != null)
				return false;
		} else if (!upperBounds.equals(other.upperBounds))
			return false;
		if (varName == null) {
			if (other.varName != null)
				return false;
		} else if (!varName.equals(other.varName))
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

		out.writeObject(lowerBounds);
		SerialUtils.writeExternalCollection(out, annotations);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);

		lowerBounds = (TypeSignature) in.readObject();
		annotations = SerialUtils.readExternalImmutableList(in);
	}

}
