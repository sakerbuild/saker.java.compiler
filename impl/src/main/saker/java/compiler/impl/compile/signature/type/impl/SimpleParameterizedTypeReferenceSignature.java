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
import java.util.List;
import java.util.Objects;

import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.impl.signature.type.ParameterizedTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;

public class SimpleParameterizedTypeReferenceSignature extends SimpleTypeReferenceSignature {
	private static final long serialVersionUID = 1L;

	private List<? extends TypeSignature> typeParameters;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleParameterizedTypeReferenceSignature() {
	}

	public SimpleParameterizedTypeReferenceSignature(ParameterizedTypeSignature enclosingSignature, String simpleName,
			List<? extends TypeSignature> typeParameters) {
		super(enclosingSignature, simpleName);
		this.typeParameters = typeParameters;
	}

	@Override
	public List<? extends TypeSignature> getTypeParameters() {
		return typeParameters;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		SerialUtils.writeExternalCollection(out, typeParameters);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		typeParameters = SerialUtils.readExternalImmutableList(in);
	}

	@Override
	public String toString() {
		return super.toString() + StringUtils.toStringJoin("<", ", ", typeParameters, ">");
	}

	@Override
	public int hashCode() {
		return super.hashCode() * 31 + Objects.hashCode(typeParameters);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleParameterizedTypeReferenceSignature other = (SimpleParameterizedTypeReferenceSignature) obj;
		if (typeParameters == null) {
			if (other.typeParameters != null)
				return false;
		} else if (!typeParameters.equals(other.typeParameters))
			return false;
		return true;
	}
}
