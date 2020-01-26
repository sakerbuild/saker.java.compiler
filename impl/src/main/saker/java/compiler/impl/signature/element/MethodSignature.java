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
import java.util.Objects;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.signature.type.TypeSignature;

public interface MethodSignature extends ClassMemberSignature, ParameterizedSignature {
	public TypeSignature getReturnType();

	public List<? extends MethodParameterSignature> getParameters();
	
	public default int getParameterCount() {
		List<? extends MethodParameterSignature> params = getParameters();
		if (params == null) {
			return 0;
		}
		return params.size();
	}

	public List<? extends TypeSignature> getThrowingTypes();

	public AnnotationSignature.Value getDefaultValue();

	public TypeSignature getReceiverParameter();

	public boolean isVarArg();

	public static boolean signatureEquals(MethodSignature first, MethodSignature other,
			SignatureNameChecker checkparameternames) {
		if (!ClassMemberSignature.signatureEquals(first, other)) {
			return false;
		}
		if (!ParameterizedSignature.signatureEquals(first, other, checkparameternames)) {
			return false;
		}
		if (!Objects.equals(first.getSimpleName(), other.getSimpleName())) {
			return false;
		}
		if (!Objects.equals(first.getModifiers(), other.getModifiers())) {
			return false;
		}
		if (!ObjectUtils.objectsEquals(first.getReturnType(), other.getReturnType(), TypeSignature::signatureEquals)) {
			return false;
		}
		if (!ObjectUtils.collectionOrderedEquals(first.getParameters(), other.getParameters(),
				checkparameternames.methodParameterComparator)) {
			return false;
		}
		if (!ObjectUtils.collectionOrderedEquals(first.getThrowingTypes(), other.getThrowingTypes(),
				TypeSignature::signatureEquals)) {
			return false;
		}
		if (!ObjectUtils.objectsEquals(first.getDefaultValue(), other.getDefaultValue(),
				AnnotationSignature.Value::signatureEquals)) {
			return false;
		}
		if (!ObjectUtils.objectsEquals(first.getReceiverParameter(), other.getReceiverParameter(),
				TypeSignature::signatureEquals)) {
			return false;
		}
		if (!Objects.equals(first.isVarArg(), other.isVarArg())) {
			return false;
		}
		return true;
	}
}
