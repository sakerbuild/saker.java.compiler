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

import java.util.Objects;
import java.util.Set;

import javax.lang.model.element.Modifier;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.signature.type.TypeSignature;

public interface MethodParameterSignature extends AnnotatedSignature {
	public String getSimpleName();

	public TypeSignature getTypeSignature();

	public Set<Modifier> getModifiers();

	public static boolean signatureEquals(MethodParameterSignature first, MethodParameterSignature other) {
		if (!Objects.equals(first.getSimpleName(), other.getSimpleName())) {
			return false;
		}
		return signatureEqualsWithoutName(first, other);
	}

	public static boolean signatureEqualsWithoutName(MethodParameterSignature first, MethodParameterSignature other) {
		if (!AnnotatedSignature.annotationSignaturesEqual(first, other)) {
			return false;
		}
		if (!ObjectUtils.objectsEquals(first.getTypeSignature(), other.getTypeSignature(),
				TypeSignature::signatureEquals)) {
			return false;
		}
		if (!Objects.equals(first.getModifiers(), other.getModifiers())) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode();
}
