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

import javax.lang.model.element.Modifier;

import saker.java.compiler.impl.signature.type.TypeSignature;

public class DocumentedFieldSignature extends SimpleFieldSignature {
	private static final long serialVersionUID = 1L;

	protected String docComment;

	/**
	 * For {@link Externalizable}.
	 */
	public DocumentedFieldSignature() {
	}

	public DocumentedFieldSignature(Set<Modifier> modifiers, TypeSignature type, String name, String docComment) {
		super(modifiers, type, name);
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
		DocumentedFieldSignature other = (DocumentedFieldSignature) obj;
		if (docComment == null) {
			if (other.docComment != null)
				return false;
		} else if (!docComment.equals(other.docComment))
			return false;
		return true;
	}

}