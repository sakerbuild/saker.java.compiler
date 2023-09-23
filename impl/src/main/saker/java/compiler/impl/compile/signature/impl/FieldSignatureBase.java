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
import java.util.Objects;

import saker.java.compiler.impl.JavaUtil;
import saker.java.compiler.impl.signature.element.FieldSignature;
import saker.java.compiler.impl.signature.value.ConstantValueResolver;

public abstract class FieldSignatureBase implements FieldSignature, Externalizable {
	private static final long serialVersionUID = 1L;

	protected String name;

	/**
	 * For {@link Externalizable}.
	 */
	public FieldSignatureBase() {
	}

	public FieldSignatureBase(String name) {
		this.name = name;
	}

	@Override
	public final String getSimpleName() {
		return name;
	}

	@Override
	public String getDocComment() {
		return null;
	}

	@Override
	public ConstantValueResolver getConstantValue() {
		return null;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(name);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		name = in.readUTF();
	}

	@Override
	public final int hashCode() {
		return Objects.hashCode(getSimpleName());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FieldSignatureBase other = (FieldSignatureBase) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return JavaUtil.modifiersToStringWithSpace(getModifiers()) + getTypeSignature() + " " + name;
	}
}
