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

import java.util.function.BiPredicate;

import saker.java.compiler.impl.signature.type.TypeParameterSignature;

public enum SignatureNameChecker {
	COMPARE_WITH_NAMES(MethodParameterSignature::signatureEquals, TypeParameterSignature::signatureEquals),
	COMPARE_WITHOUT_NAMES(MethodParameterSignature::signatureEqualsWithoutName,
			TypeParameterSignature::signatureEqualsWithoutName);

	public final BiPredicate<? super MethodParameterSignature, ? super MethodParameterSignature> methodParameterComparator;
	public final BiPredicate<? super TypeParameterSignature, ? super TypeParameterSignature> typeParameterComparator;

	private SignatureNameChecker(
			BiPredicate<? super MethodParameterSignature, ? super MethodParameterSignature> methodParameterComparator,
			BiPredicate<? super TypeParameterSignature, ? super TypeParameterSignature> typeParameterComparator) {
		this.methodParameterComparator = methodParameterComparator;
		this.typeParameterComparator = typeParameterComparator;
	}

}