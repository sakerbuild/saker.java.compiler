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
import java.util.ArrayList;
import java.util.List;

import saker.build.thirdparty.saker.util.StringUtils;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.util.JavaSerialUtils;

public final class SimpleParameterizedUnresolvedTypeSignature extends SimpleUnresolvedTypeSignature {
	private static final long serialVersionUID = 1L;

	protected List<? extends TypeSignature> typeParameters;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleParameterizedUnresolvedTypeSignature() {
	}

	public SimpleParameterizedUnresolvedTypeSignature(String qualifiedName,
			List<? extends TypeSignature> typeParameters) {
		super(qualifiedName);
		this.typeParameters = typeParameters;
	}

	@Override
	public List<? extends TypeSignature> getTypeParameters() {
		return typeParameters;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		JavaSerialUtils.writeOpenEndedList(typeParameters, out);
		out.writeObject(qualifiedName);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		ArrayList<TypeSignature> typeparams = new ArrayList<>();
		this.typeParameters = typeparams;

		this.qualifiedName = (String) JavaSerialUtils.readOpenEndedList(TypeSignature.class, typeparams, in);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleParameterizedUnresolvedTypeSignature other = (SimpleParameterizedUnresolvedTypeSignature) obj;
		if (!typeParameters.equals(other.typeParameters))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return super.toString() + StringUtils.toStringJoin("<", ", ", typeParameters, ">");
	}
}
