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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.java.compiler.impl.compile.signature.impl.AnnotatedSignatureImpl;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.type.ParameterizedTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.util.JavaSerialUtils;

public final class TypeReferenceSignatureImpl extends AnnotatedSignatureImpl implements ParameterizedTypeSignature {
	private static final long serialVersionUID = 1L;

	private ParameterizedTypeSignature enclosingSignature;
	private String simpleName;
	private List<? extends TypeSignature> typeParameters = Collections.emptyList();

	/**
	 * For {@link Externalizable}.
	 */
	public TypeReferenceSignatureImpl() {
	}

	public static ParameterizedTypeSignature create(ParameterizedTypeSignature enclosingSignature, String simpleName) {
		if (enclosingSignature == null) {
			return CanonicalTypeSignatureImpl.create(simpleName);
		}
		return new SimpleTypeReferenceSignature(enclosingSignature, simpleName);
	}

	public static ParameterizedTypeSignature create(ParameterizedTypeSignature enclosingSignature, String simpleName,
			List<? extends TypeSignature> typeParameters) {
		if (ObjectUtils.isNullOrEmpty(typeParameters)) {
			return create(enclosingSignature, simpleName);
		}
		if (enclosingSignature == null) {
			return CanonicalTypeSignatureImpl.create(simpleName, typeParameters);
		}
		return new SimpleParameterizedTypeReferenceSignature(enclosingSignature, simpleName, typeParameters);
	}

	public static ParameterizedTypeSignature create(List<? extends AnnotationSignature> annotations,
			ParameterizedTypeSignature enclosingSignature, String simpleName,
			List<? extends TypeSignature> typeParameters) {
		if (ObjectUtils.isNullOrEmpty(annotations)) {
			return create(enclosingSignature, simpleName, typeParameters);
		}
		return new TypeReferenceSignatureImpl(annotations, enclosingSignature, simpleName, typeParameters);
	}

	public static ParameterizedTypeSignature create(List<? extends AnnotationSignature> annotations,
			ParameterizedTypeSignature enclosingSignature, String simpleName) {
		if (ObjectUtils.isNullOrEmpty(annotations)) {
			return create(enclosingSignature, simpleName);
		}
		return new TypeReferenceSignatureImpl(annotations, enclosingSignature, simpleName, Collections.emptyList());
	}

	private TypeReferenceSignatureImpl(List<? extends AnnotationSignature> annotations,
			ParameterizedTypeSignature enclosingSignature, String simpleName,
			List<? extends TypeSignature> typeParameters) {
		super(annotations);
		this.enclosingSignature = enclosingSignature;
		this.simpleName = simpleName;
		this.typeParameters = typeParameters;
	}

	@Override
	public ParameterizedTypeSignature getEnclosingSignature() {
		return enclosingSignature;
	}

	@Override
	public List<? extends TypeSignature> getTypeParameters() {
		return typeParameters;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		JavaSerialUtils.writeOpenEndedList(annotations, out);
		JavaSerialUtils.writeOpenEndedList(typeParameters, out);

		out.writeObject(simpleName);
		out.writeObject(enclosingSignature);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		ArrayList<AnnotationSignature> annotations = new ArrayList<>();
		ArrayList<TypeSignature> typeparams = new ArrayList<>();
		this.annotations = annotations;
		this.typeParameters = typeparams;

		Object next = JavaSerialUtils.readOpenEndedList(AnnotationSignature.class, annotations, in);
		this.simpleName = (String) JavaSerialUtils.readOpenEndedList(next, TypeSignature.class, typeparams, in);
		this.enclosingSignature = (ParameterizedTypeSignature) in.readObject();
	}

	@Override
	public String getSimpleName() {
		return simpleName;
	}

	@Override
	public String toString() {
		return super.toString()
				+ (getEnclosingSignature() == null ? getCanonicalName()
						: getEnclosingSignature().toString() + "." + getSimpleName())
				+ (ObjectUtils.isNullOrEmpty(typeParameters) ? ""
						: StringUtils.toStringJoin("<", ", ", typeParameters, ">"));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((enclosingSignature == null) ? 0 : enclosingSignature.hashCode());
		result = prime * result + ((simpleName == null) ? 0 : simpleName.hashCode());
		result = prime * result + ((typeParameters == null) ? 0 : typeParameters.hashCode());
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
		TypeReferenceSignatureImpl other = (TypeReferenceSignatureImpl) obj;
		if (enclosingSignature == null) {
			if (other.enclosingSignature != null)
				return false;
		} else if (!enclosingSignature.equals(other.enclosingSignature))
			return false;
		if (simpleName == null) {
			if (other.simpleName != null)
				return false;
		} else if (!simpleName.equals(other.simpleName))
			return false;
		if (typeParameters == null) {
			if (other.typeParameters != null)
				return false;
		} else if (!typeParameters.equals(other.typeParameters))
			return false;
		return true;
	}

}
