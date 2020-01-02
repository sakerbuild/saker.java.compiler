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
package saker.java.compiler.impl.compile.handler.incremental.model.scope;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;

import saker.java.compiler.api.processing.SakerElementsTypes;
import saker.java.compiler.impl.compile.signature.type.impl.TypeVariableTypeSignatureImpl;
import saker.java.compiler.impl.signature.type.ParameterizedTypeSignature;
import saker.java.compiler.impl.signature.type.ResolutionScope;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

public class ClassHeaderResolutionScope extends BasicResolutionScope {

	private ResolutionScope enclosingScope;
	private TypeElement type;

	public ClassHeaderResolutionScope(ResolutionScope enclosingScope, TypeElement signature) {
		this.enclosingScope = enclosingScope;
		this.type = signature;
	}

	@Override
	public ResolutionScope getEnclosingScope() {
		return enclosingScope;
	}

	@Override
	public Element asElement(SakerElementsTypes elemtypes) {
		return asType(elemtypes);
	}

	@Override
	public TypeElement asType(SakerElementsTypes elemtypes) {
		return type;
	}

	@Override
	public ExecutableElement asExecutable(SakerElementsTypes elemtypes) {
		return null;
	}

	@Override
	protected Element resolveTypeImpl(IncrementalElementsTypes elemtypes, String simplename) {
		TypeElement astype = asType(elemtypes);
		if (astype == null) {
			return null;
		}
		if (astype.getSimpleName().contentEquals(simplename)) {
			return astype;
		}
		//try type variables
		for (TypeParameterElement tp : astype.getTypeParameters()) {
			if (tp.getSimpleName().contentEquals(simplename)) {
				return tp;
			}
		}
		return null;
	}

	@Override
	protected VariableElement resolveVariableImpl(IncrementalElementsTypes elemtypes, String simplename) {
		return null;
	}

	@Override
	public TypeSignature resolveTypeSignature(SakerElementsTypes elemtypes, String qualifiedname,
			List<? extends TypeSignature> typeparameters) {
		TypeElement astype = asType(elemtypes);
		if (astype == null) {
			return enclosingScope.resolveTypeSignature(elemtypes, qualifiedname, typeparameters);
		}

		QualifiedNameIterator it = new QualifiedNameIterator(qualifiedname);
		String first = it.next();
		if (!it.hasNext()) {
			//simple name
			for (TypeParameterElement tp : type.getTypeParameters()) {
				if (tp.getSimpleName().contentEquals(qualifiedname)) {
					return TypeVariableTypeSignatureImpl.create(qualifiedname);
				}
			}
			//not type variable
			if (astype.getSimpleName().contentEquals(first)) {
				return IncrementalElementsTypes.createTypeElementSignature(astype, typeparameters);
			}
		} else {
			if (astype.getSimpleName().contentEquals(first)) {
				IncrementalElementsTypes iet = (IncrementalElementsTypes) elemtypes;
				ParameterizedTypeSignature resolved = iet.resolveSignatureInScope(astype, it, typeparameters);
				if (resolved != null) {
					return resolved;
				}
				return null;
			}
		}
		return enclosingScope.resolveTypeSignature(elemtypes, qualifiedname, typeparameters);
	}

	@Override
	public String toString() {
		return "Class header: " + type.getQualifiedName();
	}
}
