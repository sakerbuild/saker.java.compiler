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
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;

import saker.java.compiler.api.processing.SakerElementsTypes;
import saker.java.compiler.impl.signature.type.ParameterizedTypeSignature;
import saker.java.compiler.impl.signature.type.ResolutionScope;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

public class EnclosingTypeResolutionScope extends BasicResolutionScope {

	private TypeSignature type;
	private Element resolutionElement;

	public EnclosingTypeResolutionScope(TypeSignature type, Element resolutionElement) {
		this.type = type;
		this.resolutionElement = resolutionElement;
	}

	@Override
	public ResolutionScope getEnclosingScope() {
		return null;
	}

	@Override
	public Element asElement(SakerElementsTypes elemtypes) {
		return asType(elemtypes);
	}

	@Override
	public TypeElement asType(SakerElementsTypes elemtypes) {
		IncrementalElementsTypes iet = (IncrementalElementsTypes) elemtypes;
		return iet.getTypeElement(type, resolutionElement);
	}

	@Override
	public ExecutableElement asExecutable(SakerElementsTypes elemtypes) {
		return null;
	}

	@Override
	protected Element resolveTypeImpl(IncrementalElementsTypes elemtypes, String simplename) {
		return IncrementalElementsTypes.findInHierarchy(asType(elemtypes),
				t -> IncrementalElementsTypes.findDirectlyEnclosedType(t, simplename));
	}

	@Override
	protected VariableElement resolveVariableImpl(IncrementalElementsTypes elemtypes, String simplename) {
		return IncrementalElementsTypes.findInHierarchy(asType(elemtypes),
				t -> IncrementalElementsTypes.findDirectlyEnclosedVariable(t, simplename));
	}

	@Override
	public TypeSignature resolveTypeSignature(SakerElementsTypes elemtypes, String qualifiedname,
			List<? extends TypeSignature> typeparameters) {
		IncrementalElementsTypes iet = (IncrementalElementsTypes) elemtypes;
		DeclaredType enclosingmirror = (DeclaredType) iet.getTypeMirror(type, resolutionElement);
		if (enclosingmirror == null) {
			return null;
		}
		TypeElement encmirroraselem = (TypeElement) enclosingmirror.asElement();
		if (encmirroraselem == null) {
			return null;
		}
		ParameterizedTypeSignature result = iet.resolveSignatureInScope(encmirroraselem, enclosingmirror,
				new QualifiedNameIterator(qualifiedname), typeparameters);
		return result;
	}

	@Override
	public String toString() {
		return type.toString();
	}
}
