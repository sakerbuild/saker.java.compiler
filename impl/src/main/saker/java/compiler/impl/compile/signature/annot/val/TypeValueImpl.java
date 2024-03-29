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
package saker.java.compiler.impl.compile.signature.annot.val;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

import saker.java.compiler.impl.signature.element.AnnotationSignature.TypeValue;
import saker.java.compiler.impl.signature.type.TypeSignature;

public final class TypeValueImpl implements TypeValue, Externalizable {
	private static final long serialVersionUID = 1L;

	private TypeSignature type;

	public TypeValueImpl() {
	}

	public TypeValueImpl(TypeSignature type) {
		this.type = type;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(type);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		type = (TypeSignature) in.readObject();
	}

	@Override
	public TypeSignature getType() {
		return type;
	}

	public void setType(TypeSignature type) {
		this.type = type;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(type);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TypeValueImpl other = (TypeValueImpl) obj;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return type.toString() + ".class";
	}

}
