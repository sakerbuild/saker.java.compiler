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
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.signature.type.WildcardTypeSignature;

public final class SimpleSuperWildcardTypeSignature implements WildcardTypeSignature, Externalizable {
	private static final long serialVersionUID = 1L;

	protected TypeSignature lowerBounds;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleSuperWildcardTypeSignature() {
	}

	public SimpleSuperWildcardTypeSignature(TypeSignature lowerBounds) {
		this.lowerBounds = lowerBounds;
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
		return null;
	}

	@Override
	public TypeSignature getLowerBounds() {
		return lowerBounds;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(lowerBounds);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		lowerBounds = (TypeSignature) in.readObject();
	}

	@Override
	public String toString() {
		return "? super " + lowerBounds;
	}

	@Override
	public int hashCode() {
		return Objects.hash(lowerBounds);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleSuperWildcardTypeSignature other = (SimpleSuperWildcardTypeSignature) obj;
		if (lowerBounds == null) {
			if (other.lowerBounds != null)
				return false;
		} else if (!lowerBounds.equals(other.lowerBounds))
			return false;
		return true;
	}

}
