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
package saker.java.compiler.impl.compile.handler.incremental.model;

import javax.lang.model.type.TypeMirror;

public class IsAssignableTypeVisitor extends IsSubTypeVisitor {

	public IsAssignableTypeVisitor(IncrementalElementsTypesBase incrementalElementsTypes) {
		super(incrementalElementsTypes);
	}

	@Override
	protected boolean isRawDeclaredTypesCompatible(boolean leftraw, boolean rightraw) {
		//if any of the types are raw, consider them assignable
		return true;
	}

	@Override
	protected boolean isDeclaredEnclosingTypesCompatible(TypeMirror leftenclosing, TypeMirror rightenclosing) {
		return elemTypes.isAssignable(leftenclosing, rightenclosing);
	}

	@Override
	protected boolean callWithArguments(TypeMirror t, TypeMirror p) {
		return elemTypes.isAssignable(t, p);
	}
}