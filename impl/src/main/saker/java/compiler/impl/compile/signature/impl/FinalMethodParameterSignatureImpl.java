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
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

public final class FinalMethodParameterSignatureImpl extends MethodParameterSignatureImpl {
	private static final long serialVersionUID = 1L;

	/**
	 * For {@link Externalizable}.
	 */
	public FinalMethodParameterSignatureImpl() {
	}

	public FinalMethodParameterSignatureImpl(TypeSignature type, String name) {
		super(type, name);
	}

	@Override
	public Set<Modifier> getModifiers() {
		return IncrementalElementsTypes.MODIFIERS_FINAL;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(type);
		out.writeObject(name);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		type = (TypeSignature) in.readObject();
		name = (String) in.readObject();
	}
}
