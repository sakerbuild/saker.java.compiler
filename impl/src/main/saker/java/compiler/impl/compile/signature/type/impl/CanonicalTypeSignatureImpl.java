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
import java.util.List;
import java.util.Objects;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.type.CanonicalTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.util.JavaSerialUtils;

public final class CanonicalTypeSignatureImpl extends AnnotatedCanonicalTypeSignature {
	private static final long serialVersionUID = 1L;

	private List<? extends TypeSignature> typeParameters;

	/**
	 * For {@link Externalizable}.
	 */
	public CanonicalTypeSignatureImpl() {
	}

	public static CanonicalTypeSignature create(String canonicalName) {
		return SimpleCanonicalTypeSignature.create(canonicalName);
	}

	public static CanonicalTypeSignature create(List<? extends AnnotationSignature> annotations, String canonicalName) {
		return AnnotatedCanonicalTypeSignature.create(annotations, canonicalName);
	}

	public static CanonicalTypeSignature create(String canonicalName, List<? extends TypeSignature> typeParameters) {
		if (ObjectUtils.isNullOrEmpty(typeParameters)) {
			return create(canonicalName);
		}
		return new SimpleParameterizedCanonicalTypeSignature(canonicalName, typeParameters);
	}

	public static CanonicalTypeSignature create(List<? extends AnnotationSignature> annotations, String canonicalName,
			List<? extends TypeSignature> typeParameters) {
		if (ObjectUtils.isNullOrEmpty(annotations)) {
			if (ObjectUtils.isNullOrEmpty(typeParameters)) {
				return create(canonicalName);
			}
			return create(canonicalName, typeParameters);
		}
		if (ObjectUtils.isNullOrEmpty(typeParameters)) {
			return create(annotations, canonicalName);
		}
		return new CanonicalTypeSignatureImpl(annotations, canonicalName, typeParameters);
	}

	public static CanonicalTypeSignature create(ParserCache cache, String canonicalName) {
		return cache.canonicalTypeSignature(canonicalName);
	}

	public static CanonicalTypeSignature create(ParserCache cache, List<? extends AnnotationSignature> annotations,
			String canonicalName) {
		return AnnotatedCanonicalTypeSignature.create(cache, annotations, canonicalName);
	}

	public static CanonicalTypeSignature create(ParserCache cache, String canonicalName,
			List<? extends TypeSignature> typeParameters) {
		if (ObjectUtils.isNullOrEmpty(typeParameters)) {
			return create(cache, canonicalName);
		}
		return new SimpleParameterizedCanonicalTypeSignature(canonicalName, typeParameters);
	}

	public static CanonicalTypeSignature create(ParserCache cache, List<? extends AnnotationSignature> annotations,
			String canonicalName, List<? extends TypeSignature> typeParameters) {
		if (ObjectUtils.isNullOrEmpty(annotations)) {
			if (ObjectUtils.isNullOrEmpty(typeParameters)) {
				return create(cache, canonicalName);
			}
			return create(cache, canonicalName, typeParameters);
		}
		if (ObjectUtils.isNullOrEmpty(typeParameters)) {
			return AnnotatedCanonicalTypeSignature.create(cache, annotations, canonicalName);
		}
		return new CanonicalTypeSignatureImpl(annotations, canonicalName, typeParameters);
	}

	private CanonicalTypeSignatureImpl(List<? extends AnnotationSignature> annotations, String canonicalName,
			List<? extends TypeSignature> typeParameters) {
		super(annotations, canonicalName);
		this.canonicalName = canonicalName;
		this.typeParameters = typeParameters;
	}

	@Override
	public List<? extends TypeSignature> getTypeParameters() {
		return typeParameters;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		JavaSerialUtils.writeOpenEndedList(annotations, out);
		JavaSerialUtils.writeOpenEndedList(typeParameters, out);
		out.writeObject(canonicalName);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		ArrayList<AnnotationSignature> annotations = new ArrayList<>();
		ArrayList<TypeSignature> typeparams = new ArrayList<>();
		this.annotations = annotations;
		this.typeParameters = typeparams;

		Object next = JavaSerialUtils.readOpenEndedList(AnnotationSignature.class, annotations, in);
		this.canonicalName = (String) JavaSerialUtils.readOpenEndedList(next, TypeSignature.class, typeparams, in);
	}

	@Override
	public int hashCode() {
		return super.hashCode() * 31 + Objects.hashCode(typeParameters);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		CanonicalTypeSignatureImpl other = (CanonicalTypeSignatureImpl) obj;
		if (typeParameters == null) {
			if (other.typeParameters != null)
				return false;
		} else if (!typeParameters.equals(other.typeParameters))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return super.toString()
				+ (getEnclosingSignature() == null ? getCanonicalName()
						: getEnclosingSignature().toString() + "." + getSimpleName())
				+ (ObjectUtils.isNullOrEmpty(typeParameters) ? ""
						: StringUtils.toStringJoin("<", ", ", typeParameters, ">"));
	}

}
