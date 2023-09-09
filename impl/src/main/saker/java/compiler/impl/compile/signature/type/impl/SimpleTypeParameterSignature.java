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
import saker.java.compiler.impl.signature.type.TypeParameterSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;

public class SimpleTypeParameterSignature implements TypeParameterSignature, Externalizable {
	private static final long serialVersionUID = 1L;

	//type variables are often a single character, so cache some of them
	private static final SimpleTypeParameterSignature[] SINGLE_CHAR_CACHE = new SimpleTypeParameterSignature['Z' - 'A'
			+ 1];
	static {
		for (char c = 'A'; c <= 'Z'; ++c) {
			SINGLE_CHAR_CACHE[c - 'A'] = new SimpleTypeParameterSignature(String.valueOf(c));
		}
	}

	protected String variableName;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleTypeParameterSignature() {
	}

	protected SimpleTypeParameterSignature(String variableName) {
		this.variableName = variableName;
	}

	public static SimpleTypeParameterSignature create(String variableName) {
		if (variableName != null && variableName.length() == 1) {
			char c = variableName.charAt(0);
			if (c >= 'A' && c <= 'Z') {
				return SINGLE_CHAR_CACHE[c - 'A'];
			}
		}
		return new SimpleTypeParameterSignature(variableName);
	}

	@Override
	public final String getVarName() {
		return variableName;
	}

	@Override
	public List<? extends AnnotationSignature> getAnnotations() {
		return Collections.emptyList();
	}

	@Override
	public TypeSignature getUpperBounds() {
		return null;
	}

	@Override
	public TypeSignature getLowerBounds() {
		return null;
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
		SimpleTypeParameterSignature other = (SimpleTypeParameterSignature) obj;
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
