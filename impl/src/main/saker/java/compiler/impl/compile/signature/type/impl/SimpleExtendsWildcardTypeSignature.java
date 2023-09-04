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
import saker.java.compiler.impl.signature.type.WildcardTypeSignature;

public class SimpleExtendsWildcardTypeSignature implements WildcardTypeSignature, Externalizable {
	private static final long serialVersionUID = 1L;

	protected TypeSignature upperBounds;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleExtendsWildcardTypeSignature() {
	}

	public SimpleExtendsWildcardTypeSignature(TypeSignature upperBounds) {
		this.upperBounds = upperBounds;
	}

	@Override
	public TypeSignature getEnclosingSignature() {
		return null;
	}

	@Override
	public String getSimpleName() {
		return "?";
	}

	@Override
	public List<? extends AnnotationSignature> getAnnotations() {
		return Collections.emptyList();
	}

	@Override
	public TypeSignature getUpperBounds() {
		return upperBounds;
	}

	@Override
	public TypeSignature getLowerBounds() {
		return null;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(upperBounds);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		upperBounds = (TypeSignature) in.readObject();
	}

	@Override
	public String toString() {
		return "? extends " + upperBounds;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((upperBounds == null) ? 0 : upperBounds.hashCode());
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
		SimpleExtendsWildcardTypeSignature other = (SimpleExtendsWildcardTypeSignature) obj;
		if (upperBounds == null) {
			if (other.upperBounds != null)
				return false;
		} else if (!upperBounds.equals(other.upperBounds))
			return false;
		return true;
	}

}
