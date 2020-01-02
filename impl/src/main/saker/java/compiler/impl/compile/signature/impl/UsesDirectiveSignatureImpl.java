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

import saker.java.compiler.impl.signature.element.ModuleSignature.UsesDirectiveSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;

public class UsesDirectiveSignatureImpl implements UsesDirectiveSignature, Externalizable {
	private static final long serialVersionUID = 1L;

	private TypeSignature service;

	/**
	 * For {@link Externalizable}.
	 */
	public UsesDirectiveSignatureImpl() {
	}

	public UsesDirectiveSignatureImpl(TypeSignature service) {
		this.service = service;
	}

	@Override
	public TypeSignature getService() {
		return service;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(service);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		service = (TypeSignature) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((service == null) ? 0 : service.hashCode());
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
		UsesDirectiveSignatureImpl other = (UsesDirectiveSignatureImpl) obj;
		if (service == null) {
			if (other.service != null)
				return false;
		} else if (!service.equals(other.service))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "uses " + service;
	}

}