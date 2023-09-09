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
import saker.java.compiler.impl.signature.type.ParameterizedTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.signature.type.UnresolvedTypeSignature;
import saker.java.compiler.impl.util.JavaSerialUtils;

public final class UnresolvedTypeSignatureImpl extends AnnotatedUnresolvedTypeSignature {
	private static final long serialVersionUID = 1L;

	private List<? extends TypeSignature> typeParameters;
	private ParameterizedTypeSignature enclosing;

	/**
	 * For {@link Externalizable}.
	 */
	public UnresolvedTypeSignatureImpl() {
	}

	public static UnresolvedTypeSignature create(String qualifiedName) {
		return SimpleUnresolvedTypeSignature.create(qualifiedName);
	}

	public static UnresolvedTypeSignature create(ParserCache cache, String qualifiedName) {
		return cache.unresolved(qualifiedName);
	}

	public static UnresolvedTypeSignature create(List<? extends AnnotationSignature> annotations,
			ParameterizedTypeSignature enclosing, String qualifiedName, List<? extends TypeSignature> typeParameters) {
		if (enclosing == null) {
			if (ObjectUtils.isNullOrEmpty(annotations)) {
				if (ObjectUtils.isNullOrEmpty(typeParameters)) {
					return create(qualifiedName);
				}
				return new SimpleParameterizedUnresolvedTypeSignature(qualifiedName, typeParameters);
			}
			if (ObjectUtils.isNullOrEmpty(typeParameters)) {
				return new AnnotatedUnresolvedTypeSignature(annotations, qualifiedName);
			}
		}
		return new UnresolvedTypeSignatureImpl(annotations, enclosing, qualifiedName, typeParameters);
	}

	public static UnresolvedTypeSignature create(ParserCache cache, List<? extends AnnotationSignature> annotations,
			ParameterizedTypeSignature enclosing, String qualifiedName, List<? extends TypeSignature> typeParameters) {
		if (enclosing == null) {
			if (ObjectUtils.isNullOrEmpty(annotations)) {
				if (ObjectUtils.isNullOrEmpty(typeParameters)) {
					return create(cache, qualifiedName);
				}
				return new SimpleParameterizedUnresolvedTypeSignature(qualifiedName, typeParameters);
			}
			if (ObjectUtils.isNullOrEmpty(typeParameters)) {
				return new AnnotatedUnresolvedTypeSignature(annotations, qualifiedName);
			}
		}
		return new UnresolvedTypeSignatureImpl(annotations, enclosing, qualifiedName, typeParameters);
	}

	private UnresolvedTypeSignatureImpl(List<? extends AnnotationSignature> annotations,
			ParameterizedTypeSignature enclosing, String qualifiedName, List<? extends TypeSignature> typeParameters) {
		super(annotations, qualifiedName);
		this.enclosing = enclosing;
		this.typeParameters = typeParameters;
	}

	@Override
	public ParameterizedTypeSignature getEnclosingSignature() {
		return enclosing;
	}

	@Override
	public List<? extends TypeSignature> getTypeParameters() {
		return typeParameters;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		JavaSerialUtils.writeOpenEndedList(annotations, out);
		JavaSerialUtils.writeOpenEndedList(typeParameters, out);
		out.writeObject(qualifiedName);
		out.writeObject(enclosing);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		ArrayList<AnnotationSignature> annotations = new ArrayList<>();
		ArrayList<TypeSignature> typeparams = new ArrayList<>();
		this.annotations = annotations;
		this.typeParameters = typeparams;

		Object next = JavaSerialUtils.readOpenEndedList(AnnotationSignature.class, annotations, in);
		this.qualifiedName = (String) JavaSerialUtils.readOpenEndedList(next, TypeSignature.class, typeparams, in);
		this.enclosing = (ParameterizedTypeSignature) in.readObject();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		UnresolvedTypeSignatureImpl other = (UnresolvedTypeSignatureImpl) obj;
		if (!Objects.equals(typeParameters, other.typeParameters))
			return false;
		if (!Objects.equals(enclosing, other.enclosing))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return super.toString() + (ObjectUtils.isNullOrEmpty(typeParameters) ? ""
				: StringUtils.toStringJoin("<", ", ", typeParameters, ">"));
	}
}
