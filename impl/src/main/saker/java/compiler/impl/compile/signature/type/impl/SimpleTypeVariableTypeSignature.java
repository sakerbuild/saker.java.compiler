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
import java.util.Collection;
import java.util.Collections;

import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignatureVisitor;
import saker.java.compiler.impl.signature.type.TypeVariableTypeSignature;

public class SimpleTypeVariableTypeSignature implements TypeVariableTypeSignature, Externalizable {
	private static final long serialVersionUID = 1L;

	private String variableName;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleTypeVariableTypeSignature() {
	}

	public SimpleTypeVariableTypeSignature(String variableName) {
		this.variableName = variableName;
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
	public Collection<? extends AnnotationSignature> getAnnotations() {
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + variableName.hashCode();
		return result;
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
		if (!this.variableName.equals(other.variableName)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return variableName;
	}
}
