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

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.compat.ElementKindCompatUtils;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.signature.value.ConstantValueResolver;

public interface FieldSignature extends ClassMemberSignature {
	public TypeSignature getTypeSignature();

	public ConstantValueResolver getConstantValue();

	public default boolean isEnumConstant() {
		return getKindIndex() == ElementKindCompatUtils.ELEMENTKIND_INDEX_ENUM_CONSTANT;
	}

	public static boolean signatureEquals(FieldSignature first, FieldSignature other) {
		if (!isOnlyInitializerChanged(first, other)) {
			return false;
		}
		return !first.isInitializerChanged(other);
	}

	public static boolean isOnlyInitializerChanged(FieldSignature first, FieldSignature other) {
		if (!ClassMemberSignature.signatureEquals(first, other)) {
			return false;
		}
		if (!ObjectUtils.objectsEquals(first.getTypeSignature(), other.getTypeSignature(),
				TypeSignature::signatureEquals)) {
			return false;
		}
		if (first.isEnumConstant() != other.isEnumConstant()) {
			return false;
		}
		return true;
	}

	public default boolean isInitializerChanged(FieldSignature other) {
		return !ObjectUtils.objectsEquals(getConstantValue(), other.getConstantValue(),
				ConstantValueResolver::signatureEquals);
	}
}
