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
package saker.java.compiler.impl.signature.element;

import java.util.List;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.signature.type.TypeParameterTypeSignature;

public interface ParameterizedSignature {
	public List<? extends TypeParameterTypeSignature> getTypeParameters();

	public static boolean signatureEquals(ParameterizedSignature first, ParameterizedSignature other,
			SignatureNameChecker checkparameternames) {
		if (!ObjectUtils.collectionOrderedEquals(first.getTypeParameters(), other.getTypeParameters(),
				checkparameternames.typeParameterComparator)) {
			return false;
		}
		return true;
	}
}
