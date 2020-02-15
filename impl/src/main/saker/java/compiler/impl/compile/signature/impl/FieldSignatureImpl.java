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

import saker.build.thirdparty.saker.util.StringUtils;
import saker.java.compiler.impl.JavaUtil;
import saker.java.compiler.impl.signature.element.FieldSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.signature.value.ConstantValueResolver;
import saker.java.compiler.jdk.impl.JavaCompilationUtils;

public class FieldSignatureImpl extends SimpleFieldSignature {
	private static final long serialVersionUID = 1L;

	protected ConstantValueResolver constantValue;
	protected String docComment;

	/**
	 * For {@link Externalizable}.
	 */
	public FieldSignatureImpl() {
	}

	public static FieldSignature create(ElementKind kind, Set<Modifier> modifiers, TypeSignature type, String name,
			ConstantValueResolver constantValue, String docComment) {
		if (kind == ElementKind.ENUM_CONSTANT) {
			if (docComment == null) {
				return new SimpleEnumConstantFieldSignature(type, name);
			}
			return new DocumentedSimpleEnumConstantFieldSignature(type, name, docComment);
		}
		if (JavaCompilationUtils.isRecordComponentElementKind(kind)) {
			return new RecordComponentSignatureImpl(modifiers, type, name, docComment);
		}
		if (docComment == null && constantValue == null) {
			return new SimpleFieldSignature(modifiers, type, name);
		}

		return new FieldSignatureImpl(modifiers, type, name, constantValue, docComment);
	}

	public static FieldSignature createField(Set<Modifier> modifiers, TypeSignature type, String name,
			ConstantValueResolver constantValue, String docComment) {
		if (constantValue == null && docComment == null) {
			return new SimpleFieldSignature(modifiers, type, name);
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
		super(modifiers, type, name);
		this.constantValue = constantValue;
		this.docComment = docComment;
	}

	@Override
	public String getDocComment() {
		return docComment;
	}

	@Override
	public ConstantValueResolver getConstantValue() {
		return constantValue;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);

		out.writeObject(constantValue);
		out.writeObject(docComment);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);

		constantValue = (ConstantValueResolver) in.readObject();
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
		if (constantValue == null) {
			if (other.constantValue != null)
				return false;
		} else if (!constantValue.equals(other.constantValue))
			return false;
		if (docComment == null) {
			if (other.docComment != null)
				return false;
		} else if (!docComment.equals(other.docComment))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return (getAnnotations().isEmpty() ? ""
				: String.join(" ", StringUtils.asStringIterable(getAnnotations())) + " ")
				+ JavaUtil.modifiersToStringWithSpace(getModifiers()) + type + " " + name
				+ (constantValue == null ? "" : " = " + constantValue);
	}

}