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

public class MethodChangedABIChange implements AbiChange {
	private String classCanonicalName;
	private String methodName;

	public MethodChangedABIChange(String classCanonicalName, String methodName) {
		this.classCanonicalName = classCanonicalName;
		this.methodName = methodName;
	}

	@Override
	public boolean affects(TopLevelAbiUsage usage, Consumer<AbiChange> foundchanges) {
		if (usage.isInheritesFromClass(classCanonicalName)) {
			//TODO shouldn't detect ABI change if the changed method is private and is not visible from the subclass
			return true;
		}
		if (usage.isReferencesMethod(classCanonicalName, methodName)) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "Class method changed: " + classCanonicalName + ": " + methodName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((classCanonicalName == null) ? 0 : classCanonicalName.hashCode());
		result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MethodChangedABIChange other = (MethodChangedABIChange) obj;
		if (classCanonicalName == null) {
			if (other.classCanonicalName != null)
				return false;
		} else if (!classCanonicalName.equals(other.classCanonicalName))
			return false;
		if (methodName == null) {
			if (other.methodName != null)
				return false;
		} else if (!methodName.equals(other.methodName))
			return false;
		return true;
	}

}
