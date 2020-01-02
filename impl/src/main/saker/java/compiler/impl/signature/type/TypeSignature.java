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

import java.util.Objects;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.signature.element.AnnotatedSignature;

public interface TypeSignature extends AnnotatedSignature {
	public TypeSignature getEnclosingSignature();

	public String getSimpleName();

	public default String getName() {
		TypeSignature enclosing = getEnclosingSignature();
		if (enclosing == null) {
			return getSimpleName();
		}
		return enclosing.getName() + "$" + getSimpleName();
	}

	public default String getCanonicalName() {
		TypeSignature enclosing = getEnclosingSignature();
		if (enclosing == null) {
			return getSimpleName();
		}
		return enclosing.getCanonicalName() + "." + getSimpleName();
	}

	public <R, P> R accept(TypeSignatureVisitor<R, P> v, P p);

	public default boolean signatureEquals(TypeSignature other) {
		if (!Objects.equals(getSimpleName(), other.getSimpleName())) {
			return false;
		}
		if (!ObjectUtils.objectsEquals(getEnclosingSignature(), other.getEnclosingSignature(),
				TypeSignature::signatureEquals)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode();
}
