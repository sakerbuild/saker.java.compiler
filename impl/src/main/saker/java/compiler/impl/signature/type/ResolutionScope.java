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

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;

import saker.java.compiler.api.processing.SakerElementsTypes;

public interface ResolutionScope {
	public ResolutionScope getEnclosingScope();

	public Element asElement(SakerElementsTypes elemtypes);

	public TypeElement asType(SakerElementsTypes elemtypes);

	public ExecutableElement asExecutable(SakerElementsTypes elemtypes);

	/**
	 * Returns the resolved {@link TypeElement} or {@link TypeParameterElement}. Null if not found.
	 * 
	 * @param elemtypes
	 * @param simplename
	 * @return
	 */
	public Element resolveType(SakerElementsTypes elemtypes, String simplename);

	public VariableElement resolveVariable(SakerElementsTypes elemtypes, String simplename);

	public TypeSignature resolveTypeSignature(SakerElementsTypes elemtypes, String qualifiedname,
			List<? extends TypeSignature> typeparameters);
}
