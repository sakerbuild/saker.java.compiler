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

import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;

import saker.java.compiler.api.processing.SakerElementsTypes;
import saker.java.compiler.impl.signature.type.ResolutionScope;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

public abstract class BasicResolutionScope implements ResolutionScope {

	public BasicResolutionScope() {
	}

	@Override
	public Element resolveType(SakerElementsTypes elemtypes, String simplename) {
		Element resolved = resolveTypeImpl((IncrementalElementsTypes) elemtypes, simplename);
		if (resolved != null) {
			return resolved;
		}
		ResolutionScope enclosing = getEnclosingScope();
		if (enclosing != null) {
			return enclosing.resolveType(elemtypes, simplename);
		}
		return null;
	}

	@Override
	public VariableElement resolveVariable(SakerElementsTypes elemtypes, String simplename) {
		VariableElement resolved = resolveVariableImpl((IncrementalElementsTypes) elemtypes, simplename);
		if (resolved != null) {
			return resolved;
		}
		ResolutionScope enclosing = getEnclosingScope();
		if (enclosing != null) {
			return enclosing.resolveVariable(elemtypes, simplename);
		}
		return null;
	}

	protected abstract Element resolveTypeImpl(IncrementalElementsTypes elemtypes, String simplename);

	protected abstract VariableElement resolveVariableImpl(IncrementalElementsTypes elemtypes, String simplename);

}
