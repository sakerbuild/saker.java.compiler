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
import saker.java.compiler.impl.signature.type.ResolutionScope;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

public class MethodResolutionScope extends BasicResolutionScope {

	protected ResolutionScope enclosingScope;
	protected ExecutableElement executable;

	public MethodResolutionScope(ResolutionScope enclosingScope, ExecutableElement executable) {
		this.enclosingScope = enclosingScope;
		this.executable = executable;
	}

	@Override
	public ResolutionScope getEnclosingScope() {
		return enclosingScope;
	}

	@Override
	public Element asElement(SakerElementsTypes elemtypes) {
		return asExecutable(elemtypes);
	}

	@Override
	public TypeElement asType(SakerElementsTypes elemtypes) {
		return null;
	}

	@Override
	public ExecutableElement asExecutable(SakerElementsTypes elemtypes) {
		return executable;
	}

	@Override
	protected Element resolveTypeImpl(IncrementalElementsTypes elemtypes, String simplename) {
		ExecutableElement asexecutable = asExecutable(elemtypes);
		if (asexecutable == null) {
			return null;
		}
		//try type variables
		for (TypeParameterElement tp : asexecutable.getTypeParameters()) {
			if (tp.getSimpleName().contentEquals(simplename)) {
				return tp;
			}
		}
		return null;
	}

	@Override
	protected VariableElement resolveVariableImpl(IncrementalElementsTypes elemtypes, String simplename) {
		ExecutableElement asexecutable = asExecutable(elemtypes);
		if (asexecutable == null) {
			return null;
		}
		List<? extends VariableElement> param = asexecutable.getParameters();
		for (VariableElement ve : param) {
			if (ve.getSimpleName().contentEquals(simplename)) {
				return ve;
			}
		}
		return null;
	}

	@Override
	public TypeSignature resolveTypeSignature(SakerElementsTypes elemtypes, String qualifiedname,
			List<? extends TypeSignature> typeparameters) {
		for (TypeParameterElement tp : executable.getTypeParameters()) {
			if (tp.getSimpleName().contentEquals(qualifiedname)) {
				return TypeVariableTypeSignatureImpl.create(qualifiedname);
			}
		}
		return enclosingScope.resolveTypeSignature(elemtypes, qualifiedname, typeparameters);
	}

	@Override
	public String toString() {
		return executable.toString();
	}
}
