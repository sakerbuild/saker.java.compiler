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

public class ClassAddedABIChange implements AbiChange {
	private ClassSignature clazz;
	private String enclosingPath;
	private String classSimpleName;

	public ClassAddedABIChange(ClassSignature clazz) {
		this.clazz = clazz;
		ClassSignature enclosing = clazz.getEnclosingSignature();
		if (enclosing != null) {
			enclosingPath = enclosing.getCanonicalName();
		} else {
			enclosingPath = clazz.getPackageName();
		}
		this.classSimpleName = clazz.getSimpleName();
	}

	@Override
	public boolean affects(TopLevelAbiUsage usage, Consumer<AbiChange> foundchanges) {
		if (!AbiChange.isVisibleFrom(usage, clazz)) {
			return false;
		}
		boolean wcimport = usage.hasWildcardTypeImportPath(enclosingPath);
		boolean inheritance = usage.isInheritesFromClass(enclosingPath);

		String canonicalname = clazz.getCanonicalName();
		if (usage.addABIChangeForEachMember(u -> ((wcimport || inheritance) && u.isSimpleTypePresent(classSimpleName))
				|| u.isReferencesClass(canonicalname), foundchanges)) {
			return true;
		}

		if (usage.isReferencesClass(canonicalname)) {
			return true;
		}
		if ((wcimport || inheritance) && usage.isSimpleTypePresent(classSimpleName)) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "Class added: " + clazz.getCanonicalName();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((classSimpleName == null) ? 0 : classSimpleName.hashCode());
		result = prime * result + ((enclosingPath == null) ? 0 : enclosingPath.hashCode());
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
		ClassAddedABIChange other = (ClassAddedABIChange) obj;
		if (classSimpleName == null) {
			if (other.classSimpleName != null)
				return false;
		} else if (!classSimpleName.equals(other.classSimpleName))
			return false;
		if (enclosingPath == null) {
			if (other.enclosingPath != null)
				return false;
		} else if (!enclosingPath.equals(other.enclosingPath))
			return false;
		return true;
	}

}
