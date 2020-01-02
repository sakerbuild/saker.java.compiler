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

import saker.java.compiler.impl.signature.element.AnnotationSignature.Value;
import saker.java.compiler.impl.signature.type.TypeSignature;

public class DocumentedAnnotationAttributeMethodSignature extends AnnotationAttributeMethodSignature {
	private static final long serialVersionUID = 1L;

	protected String docComment;

	/**
	 * For {@link Externalizable}.
	 */
	public DocumentedAnnotationAttributeMethodSignature() {
	}

	public DocumentedAnnotationAttributeMethodSignature(TypeSignature returnType, String name, Value defaultValue,
			String docComment) {
		super(returnType, name, defaultValue);
		this.docComment = docComment;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeUTF(docComment);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		docComment = in.readUTF();
	}

	@Override
	public String getDocComment() {
		return docComment;
	}
}
