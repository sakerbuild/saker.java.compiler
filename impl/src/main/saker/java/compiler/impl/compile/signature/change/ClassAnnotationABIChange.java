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
package saker.java.compiler.impl.compile.signature.change;

import java.util.function.Consumer;

import saker.java.compiler.impl.compile.handler.usage.TopLevelAbiUsage;
import saker.java.compiler.impl.signature.element.ClassSignature;

public class ClassAnnotationABIChange implements AbiChange {

	private String classCanonicalName;

	public ClassAnnotationABIChange(ClassSignature clazz) {
		this.classCanonicalName = clazz.getCanonicalName();
	}

	@Override
	public boolean affects(TopLevelAbiUsage usage, Consumer<AbiChange> foundchanges) {
		// affect, if the usage references the class in any way.
		// java compiler might give warnings based on annotations
		// or an annotation type retention changed
		// or annotation type target changed
		return ClassChangedABIChange.affectsReferencedClass(classCanonicalName, usage, foundchanges);
	}

	@Override
	public String toString() {
		return "Class annotations changed: " + classCanonicalName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((classCanonicalName == null) ? 0 : classCanonicalName.hashCode());
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
		ClassAnnotationABIChange other = (ClassAnnotationABIChange) obj;
		if (classCanonicalName == null) {
			if (other.classCanonicalName != null)
				return false;
		} else if (!classCanonicalName.equals(other.classCanonicalName))
			return false;
		return true;
	}

}
