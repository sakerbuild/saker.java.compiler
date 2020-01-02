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
import saker.java.compiler.impl.signature.type.ArrayTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;

public class SimpleArrayTypeSignature implements ArrayTypeSignature, Externalizable {
	private static final long serialVersionUID = 1L;

	private TypeSignature componentType;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleArrayTypeSignature() {
	}

	public SimpleArrayTypeSignature(TypeSignature componentType) {
		this.componentType = componentType;
	}

	@Override
	public TypeSignature getEnclosingSignature() {
		return null;
	}

	@Override
	public String getSimpleName() {
		return null;
	}

	@Override
	public Collection<? extends AnnotationSignature> getAnnotations() {
		return Collections.emptyList();
	}

	@Override
	public TypeSignature getComponentType() {
		return componentType;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(componentType);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		componentType = (TypeSignature) in.readObject();
	}

	@Override
	public String toString() {
		return componentType + "[]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((componentType == null) ? 0 : componentType.hashCode());
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
		SimpleArrayTypeSignature other = (SimpleArrayTypeSignature) obj;
		if (componentType == null) {
			if (other.componentType != null)
				return false;
		} else if (!componentType.equals(other.componentType))
			return false;
		return true;
	}
}
