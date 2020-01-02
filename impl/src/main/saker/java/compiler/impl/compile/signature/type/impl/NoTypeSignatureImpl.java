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
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;

import javax.lang.model.type.TypeKind;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.compile.signature.impl.AnnotatedSignatureImpl;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.type.NoTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;

public class NoTypeSignatureImpl extends AnnotatedSignatureImpl implements NoTypeSignature {
	private static final long serialVersionUID = 1L;

	private static final NoTypeSignatureImpl INSTANCE_NONE = new NoTypeSignatureImpl(Collections.emptyList(),
			TypeKind.NONE);
	private static final NoTypeSignatureImpl INSTANCE_VOID = new NoTypeSignatureImpl(Collections.emptyList(),
			TypeKind.VOID);

	private static final EnumMap<TypeKind, NoTypeSignature> SIMPLE_NOTYPE_SIGNATURES = new EnumMap<>(TypeKind.class);
	static {
		SIMPLE_NOTYPE_SIGNATURES.put(TypeKind.VOID, INSTANCE_VOID);
		SIMPLE_NOTYPE_SIGNATURES.put(TypeKind.NONE, INSTANCE_NONE);
	}

	private TypeKind kind;

	public NoTypeSignatureImpl() {
	}

	public static NoTypeSignature getVoid() {
		return INSTANCE_VOID;
	}

	public static NoTypeSignature getNone() {
		return INSTANCE_NONE;
	}

	public static NoTypeSignature create(TypeKind kind) {
		return SIMPLE_NOTYPE_SIGNATURES.get(kind);
	}

	public static NoTypeSignature create(List<? extends AnnotationSignature> annotations, TypeKind kind) {
		if (ObjectUtils.isNullOrEmpty(annotations)) {
			return create(kind);
		}
		return new NoTypeSignatureImpl(annotations, kind);
	}

	private NoTypeSignatureImpl(List<? extends AnnotationSignature> annotations, TypeKind kind) {
		super(annotations);
		this.kind = kind;
	}

	@Override
	public TypeKind getKind() {
		return kind;
	}

	@Override
	public String toString() {
		return super.toString() + kind.toString().toLowerCase(Locale.ENGLISH);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((kind == null) ? 0 : kind.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		NoTypeSignatureImpl other = (NoTypeSignatureImpl) obj;
		if (kind != other.kind) {
			return false;
		}
		return true;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);

		out.writeObject(kind);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);

		kind = (TypeKind) in.readObject();
	}

	@Override
	public TypeSignature getEnclosingSignature() {
		return null;
	}

	@Override
	public String getSimpleName() {
		return kind.toString().toLowerCase(Locale.ENGLISH);
	}
}
