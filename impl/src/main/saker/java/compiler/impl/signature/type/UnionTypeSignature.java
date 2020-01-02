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

import saker.build.thirdparty.saker.util.ObjectUtils;

public interface UnionTypeSignature extends TypeSignature {
	public List<? extends TypeSignature> getAlternatives();

	@Override
	public default <R, P> R accept(TypeSignatureVisitor<R, P> v, P p) {
		return v.visitUnion(this, p);
	}

	@Override
	public default boolean signatureEquals(TypeSignature other) {
		if (!(other instanceof UnionTypeSignature)) {
			return false;
		}
		return signatureEquals((UnionTypeSignature) other);
	}

	public default boolean signatureEquals(UnionTypeSignature other) {
		if (!TypeSignature.super.signatureEquals(other)) {
			return false;
		}
		if (!ObjectUtils.collectionOrderedEquals(getAlternatives(), other.getAlternatives(),
				TypeSignature::signatureEquals)) {
			return false;
		}
		return true;
	}
}
