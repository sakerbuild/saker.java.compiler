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
import java.util.Set;

import javax.lang.model.element.Modifier;

import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.util.ImmutableModifierSet;

public final class FullMethodParameterSignatureImpl extends MethodParameterSignatureImpl {
	private static final long serialVersionUID = 1L;

	private short modifierFlags;

	/**
	 * For {@link Externalizable}.
	 */
	public FullMethodParameterSignatureImpl() {
	}

	public FullMethodParameterSignatureImpl(Set<Modifier> modifiers, TypeSignature type, String name) {
		super(type, name);
		this.modifierFlags = ImmutableModifierSet.getFlag(modifiers);
	}

	@Override
	public Set<Modifier> getModifiers() {
		return ImmutableModifierSet.forFlags(modifierFlags);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		ImmutableModifierSet.writeExternalFlag(out, modifierFlags);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		modifierFlags = ImmutableModifierSet.readExternalFlag(in);
	}

	//hashCode inherited

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		FullMethodParameterSignatureImpl other = (FullMethodParameterSignatureImpl) obj;
		if (modifierFlags != other.modifierFlags)
			return false;
		return true;
	}

}
