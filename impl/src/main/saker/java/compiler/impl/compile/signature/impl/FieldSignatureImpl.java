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
package saker.java.compiler.impl.compile.signature.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Set;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

import saker.java.compiler.impl.compat.ElementKindCompatUtils;
import saker.java.compiler.impl.signature.element.FieldSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.signature.value.ConstantValueResolver;

public class FieldSignatureImpl extends ConstantFieldSignature {
	private static final long serialVersionUID = 1L;

	protected String docComment;

	/**
	 * For {@link Externalizable}.
	 */
	public FieldSignatureImpl() {
	}

	public static FieldSignature create(ElementKind kind, Set<Modifier> modifiers, TypeSignature type, String name,
			ConstantValueResolver constantValue, String docComment) {
		if (kind == ElementKind.ENUM_CONSTANT) {
			return createEnumSignature(type, name, docComment);
		}
		if (ElementKindCompatUtils.isRecordComponentElementKind(kind)) {
			return createRecordComponent(modifiers, type, name, docComment);
		}
		return createField(modifiers, type, name, constantValue, docComment);
	}

	public static FieldSignature createField(Set<Modifier> modifiers, TypeSignature type, String name,
			ConstantValueResolver constantValue, String docComment) {
		if (docComment == null) {
			if (constantValue == null) {
				return new SimpleFieldSignature(modifiers, type, name);
			}
			return new ConstantFieldSignature(modifiers, type, name, constantValue);
		}
		if (constantValue == null) {
			return new DocumentedFieldSignature(modifiers, type, name, docComment);
		}
		return new FieldSignatureImpl(modifiers, type, name, constantValue, docComment);
	}

	public static FieldSignature createEnumSignature(TypeSignature type, String name, String docComment) {
		if (docComment == null) {
			return new SimpleEnumConstantFieldSignature(type, name);
		}
		return new DocumentedSimpleEnumConstantFieldSignature(type, name, docComment);
	}

	public static FieldSignature createRecordComponent(Set<Modifier> modifiers, TypeSignature type, String name,
			String doccomment) {
		return new RecordComponentSignatureImpl(modifiers, type, name, doccomment);
	}

	private FieldSignatureImpl(Set<Modifier> modifiers, TypeSignature type, String name,
			ConstantValueResolver constantValue, String docComment) {
		super(modifiers, type, name, constantValue);
		this.docComment = docComment;
	}

	@Override
	public String getDocComment() {
		return docComment;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);

		out.writeObject(docComment);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);

		docComment = (String) in.readObject();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		FieldSignatureImpl other = (FieldSignatureImpl) obj;
		if (docComment == null) {
			if (other.docComment != null)
				return false;
		} else if (!docComment.equals(other.docComment))
			return false;
		return true;
	}

}