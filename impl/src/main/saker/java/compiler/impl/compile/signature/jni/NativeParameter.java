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
package saker.java.compiler.impl.compile.signature.jni;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public final class NativeParameter implements Externalizable {
	private static final long serialVersionUID = 1L;

	private NativeType type;
	private String name;

	/**
	 * For {@link Externalizable}.
	 */
	public NativeParameter() {
	}

	public NativeParameter(NativeType type, String name) {
		this.type = type;
		this.name = name;
	}

	public NativeParameter(VariableElement param, Types types, Elements elements) {
		type = new NativeType(param.asType(), types, elements, false);
		name = param.getSimpleName().toString();
	}

	public NativeParameter(String typeString, String nativeType, String descriptorString, String name) {
		type = new NativeType(typeString, nativeType, descriptorString);
		this.name = name;
	}

	public NativeType getType() {
		return type;
	}

	public final String getName() {
		return name;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[type=" + type + ", name=" + name + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		NativeParameter other = (NativeParameter) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(type);
		out.writeUTF(name);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		type = (NativeType) in.readObject();
		name = in.readUTF();
	}

}