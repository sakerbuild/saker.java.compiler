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

import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import saker.java.compiler.impl.JavaUtil;

public final class NativeType implements Externalizable {
	private static final long serialVersionUID = 1L;

	private String typeString;
	private String nativeType;
	private String descriptorString;

	/**
	 * Support for Externalizable.
	 */
	public NativeType() {
	}

	public NativeType(String typeString, String nativeType, String descriptorString) {
		this.typeString = typeString;
		this.nativeType = nativeType;
		this.descriptorString = descriptorString;
	}

	public NativeType(TypeMirror type, Types types, Elements elements, boolean withtypevarbounds) {
		TypeMirror erasuredtype = types.erasure(type);
		this.typeString = JavaUtil.toTypeString(type, elements, withtypevarbounds);
		this.nativeType = JavaUtil.getNativeType(type, types, elements);
		this.descriptorString = JavaUtil.getDescriptorString(erasuredtype, elements);
	}

	public final String getNativeType() {
		return nativeType;
	}

	public final String getDescriptorString() {
		return descriptorString;
	}

	public final String getTypeString() {
		return typeString;
	}

	@Override
	public String toString() {
		return "NativeType [typeString=" + typeString + ", nativeType=" + nativeType + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((nativeType == null) ? 0 : nativeType.hashCode());
		result = prime * result + ((descriptorString == null) ? 0 : descriptorString.hashCode());
		result = prime * result + ((typeString == null) ? 0 : typeString.hashCode());
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
		NativeType other = (NativeType) obj;
		if (nativeType == null) {
			if (other.nativeType != null)
				return false;
		} else if (!nativeType.equals(other.nativeType))
			return false;
		if (descriptorString == null) {
			if (other.descriptorString != null)
				return false;
		} else if (!descriptorString.equals(other.descriptorString))
			return false;
		if (typeString == null) {
			if (other.typeString != null)
				return false;
		} else if (!typeString.equals(other.typeString))
			return false;
		return true;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(typeString);
		out.writeUTF(nativeType);
		out.writeUTF(descriptorString);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		typeString = in.readUTF();
		nativeType = in.readUTF();
		descriptorString = in.readUTF();
	}

}