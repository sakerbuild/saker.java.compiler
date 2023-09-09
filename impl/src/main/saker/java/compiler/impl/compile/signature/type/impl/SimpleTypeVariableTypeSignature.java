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
import java.util.Collections;
import java.util.List;

import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignatureVisitor;
import saker.java.compiler.impl.signature.type.TypeVariableTypeSignature;

public final class SimpleTypeVariableTypeSignature implements TypeVariableTypeSignature, Externalizable {
	private static final long serialVersionUID = 1L;

	//type variables are often a single character, so cache some of them
	private static final SimpleTypeVariableTypeSignature[] SINGLE_CHAR_CACHE = new SimpleTypeVariableTypeSignature['Z'
			- 'A' + 1];
	static {
		for (char c = 'A'; c <= 'Z'; ++c) {
			SINGLE_CHAR_CACHE[c - 'A'] = new SimpleTypeVariableTypeSignature(String.valueOf(c));
		}
	}

	private String variableName;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleTypeVariableTypeSignature() {
	}

	private SimpleTypeVariableTypeSignature(String variableName) {
		this.variableName = variableName;
	}

	public static SimpleTypeVariableTypeSignature create(String variableName) {
		if (variableName != null && variableName.length() == 1) {
			char c = variableName.charAt(0);
			if (c >= 'A' && c <= 'Z') {
				return SINGLE_CHAR_CACHE[c - 'A'];
			}
		}
		return new SimpleTypeVariableTypeSignature(variableName);
	}

	@Override
	public TypeSignature getEnclosingSignature() {
		return null;
	}

	@Override
	public String getSimpleName() {
		return variableName;
	}

	@Override
	public <R, P> R accept(TypeSignatureVisitor<R, P> v, P p) {
		return v.visitTypeVariable(this, p);
	}

	@Override
	public List<? extends AnnotationSignature> getAnnotations() {
		return Collections.emptyList();
	}

	@Override
	public String getVariableName() {
		return variableName;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(variableName);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		variableName = in.readUTF();
	}

	private Object readResolve() {
		String varname = this.variableName;
		if (varname != null && varname.length() == 1) {
			char c = varname.charAt(0);
			if (c >= 'A' && c <= 'Z') {
				return SINGLE_CHAR_CACHE[c - 'A'];
			}
		}
		return this;
	}

	@Override
	public int hashCode() {
		return variableName == null ? 0 : variableName.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleTypeVariableTypeSignature other = (SimpleTypeVariableTypeSignature) obj;
		if (variableName == null) {
			if (other.variableName != null)
				return false;
		} else if (!variableName.equals(other.variableName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return variableName;
	}
}
