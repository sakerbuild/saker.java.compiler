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
package saker.java.compiler.impl.signature.type;

import java.util.List;

public interface UnresolvedTypeSignature extends ParameterizedTypeSignature {

	public String getUnresolvedName();

	@Override
	public default <R, P> R accept(TypeSignatureVisitor<R, P> v, P p) {
		return v.visitUnresolved(this, p);
	}

	@Override
	public List<? extends TypeSignature> getTypeParameters();

	@Override
	public default boolean signatureEquals(TypeSignature other) {
		if (!(other instanceof UnresolvedTypeSignature)) {
			return false;
		}
		return signatureEquals((UnresolvedTypeSignature) other);
	}

	@Override
	public default boolean signatureEquals(ParameterizedTypeSignature other) {
		if (!(other instanceof UnresolvedTypeSignature)) {
			return false;
		}
		return signatureEquals((UnresolvedTypeSignature) other);
	}

	public default boolean signatureEquals(UnresolvedTypeSignature other) {
		if (!ParameterizedTypeSignature.super.signatureEquals(other)) {
			return false;
		}
		//XXX do we have to include the scope?
		if (!getUnresolvedName().equals(other.getUnresolvedName())) {
			return false;
		}
		return true;
	}

}
