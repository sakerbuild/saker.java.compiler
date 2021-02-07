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

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

import saker.java.compiler.impl.compat.ElementKindCompatUtils;

public interface ClassMemberSignature extends AnnotatedSignature, DocumentedSignature {
	public String getSimpleName();

	public Set<Modifier> getModifiers();

	public ElementKind getKind();

	/**
	 * @see ElementKindCompatUtils
	 */
	public default byte getKindIndex() {
		return ElementKindCompatUtils.getElementKindIndex(getKind());
	}

	public static boolean signatureEquals(ClassMemberSignature first, ClassMemberSignature other) {
		if (!AnnotatedSignature.annotationSignaturesEqual(first, other)) {
			return false;
		}
		if (first.getKindIndex() != other.getKindIndex()) {
			return false;
		}
		if (!Objects.equals(first.getSimpleName(), other.getSimpleName())) {
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
