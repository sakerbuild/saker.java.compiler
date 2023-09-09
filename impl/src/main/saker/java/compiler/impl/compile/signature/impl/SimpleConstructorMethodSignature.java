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
import java.util.List;
import java.util.Set;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

import saker.java.compiler.impl.compat.ElementKindCompatUtils;
import saker.java.compiler.impl.compile.signature.type.impl.NoTypeSignatureImpl;
import saker.java.compiler.impl.signature.element.MethodParameterSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

public class SimpleConstructorMethodSignature extends MethodSignatureBase {
	private static final long serialVersionUID = 1L;

	//Note: subclasses may have their own serialization functions, 
	//      so take care when adding new fields

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleConstructorMethodSignature() {
	}

	public SimpleConstructorMethodSignature(Set<Modifier> modifiers,
			List<? extends MethodParameterSignature> parameters) {
		super(modifiers, parameters);
	}

	@Override
	public String getSimpleName() {
		return IncrementalElementsTypes.CONSTRUCTOR_METHOD_NAME;
	}

	@Override
	public ElementKind getKind() {
		return ElementKind.CONSTRUCTOR;
	}

	@Override
	public final byte getKindIndex() {
		return ElementKindCompatUtils.ELEMENTKIND_INDEX_CONSTRUCTOR;
	}

	@Override
	public TypeSignature getReturnType() {
		return NoTypeSignatureImpl.getVoid();
	}

}
