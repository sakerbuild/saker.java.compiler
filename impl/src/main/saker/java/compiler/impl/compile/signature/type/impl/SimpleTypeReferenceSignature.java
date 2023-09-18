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
import java.util.Objects;

import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.type.ParameterizedTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;

public class SimpleTypeReferenceSignature implements ParameterizedTypeSignature, Externalizable {
	private static final long serialVersionUID = 1L;

	protected ParameterizedTypeSignature enclosingSignature;
	protected String simpleName;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleTypeReferenceSignature() {
	}

	public SimpleTypeReferenceSignature(ParameterizedTypeSignature enclosingSignature, String simpleName) {
		this.enclosingSignature = enclosingSignature;
		this.simpleName = simpleName;
	}

	@Override
	public String getSimpleName() {
		return simpleName;
	}

	@Override
	public List<? extends AnnotationSignature> getAnnotations() {
		return Collections.emptyList();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(enclosingSignature);
		out.writeUTF(simpleName);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		enclosingSignature = (ParameterizedTypeSignature) in.readObject();
		simpleName = in.readUTF();
	}

	@Override
	public List<? extends TypeSignature> getTypeParameters() {
		return Collections.emptyList();
	}

	@Override
	public ParameterizedTypeSignature getEnclosingSignature() {
		return enclosingSignature;
	}

	@Override
	public String toString() {
		return enclosingSignature + "." + simpleName;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(enclosingSignature) * 31 + Objects.hashCode(simpleName);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleTypeReferenceSignature other = (SimpleTypeReferenceSignature) obj;
		if (enclosingSignature == null) {
			if (other.enclosingSignature != null)
				return false;
		} else if (!enclosingSignature.equals(other.enclosingSignature))
			return false;
		if (simpleName == null) {
			if (other.simpleName != null)
				return false;
		} else if (!simpleName.equals(other.simpleName))
			return false;
		return true;
	}

}
