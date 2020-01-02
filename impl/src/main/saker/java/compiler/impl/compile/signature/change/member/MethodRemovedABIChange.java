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

import java.util.function.Consumer;

import saker.java.compiler.impl.compile.handler.usage.TopLevelAbiUsage;
import saker.java.compiler.impl.compile.signature.change.AbiChange;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.MethodSignature;

public class MethodRemovedABIChange extends MethodABIChange {

	public MethodRemovedABIChange(ClassSignature enclosingclass, MethodSignature method) {
		super(enclosingclass, method);
	}

	@Override
	public boolean affects(TopLevelAbiUsage usage, Consumer<AbiChange> foundchanges) {
		if (!AbiChange.isVisibleFrom(usage, method, enclosingClass)) {
			return false;
		}
		if (usage.isInheritesFromClass(classCanonicalName)) {
			return true;
		}
		if (usage.isReferencesMethod(classCanonicalName, method.getSimpleName())) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "Class method removed: " + classCanonicalName + ": " + method;
	}
}
