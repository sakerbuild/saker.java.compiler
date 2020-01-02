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
package saker.java.compiler.impl.compile.signature.change.member;

import java.util.Set;
import java.util.function.Consumer;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

import saker.java.compiler.impl.compile.handler.usage.TopLevelAbiUsage;
import saker.java.compiler.impl.compile.signature.change.AbiChange;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.MethodSignature;

public class MethodAddedABIChange extends MethodABIChange {

	public MethodAddedABIChange(ClassSignature enclosingclass, MethodSignature method) {
		super(enclosingclass, method);
	}

	@Override
	public boolean affects(TopLevelAbiUsage usage, Consumer<AbiChange> foundchanges) {
		if (!AbiChange.isVisibleFrom(usage, method, enclosingClass)) {
			return false;
		}
		Set<Modifier> modifiers = method.getModifiers();
		boolean isstatic = modifiers.contains(Modifier.STATIC);
		if (isstatic && usage.hasWildcardStaticImportPath(classCanonicalName)) {
			//TODO check if usage has a simple method identifier reference?
			return true;
		}
		if (usage.isInheritesFromClass(classCanonicalName)) {
			return true;
		}
		if (usage.isReferencesMethod(classCanonicalName, method.getSimpleName())) {
			return true;
		}
		if (enclosingClassKind == ElementKind.ANNOTATION_TYPE) {
			if (method.getDefaultValue() == null) {
				//annotation method addition only triggers modification if it has no default value, as then it must be specified
				if (usage.isReferencesClass(classCanonicalName)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "Class method added: " + classCanonicalName + ": " + method;
	}

}
