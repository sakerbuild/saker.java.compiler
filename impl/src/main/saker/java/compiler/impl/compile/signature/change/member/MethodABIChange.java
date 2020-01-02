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

import javax.lang.model.element.ElementKind;

import saker.java.compiler.impl.compile.signature.change.AbiChange;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.MethodSignature;
import saker.java.compiler.impl.signature.element.SignatureNameChecker;

public abstract class MethodABIChange implements AbiChange {
	protected ElementKind enclosingClassKind;
	protected String classCanonicalName;
	protected MethodSignature method;

	protected transient ClassSignature enclosingClass;

	public MethodABIChange(ClassSignature enclosingclass, MethodSignature method) {
		this.enclosingClass = enclosingclass;
		this.enclosingClassKind = enclosingclass.getKind();
		this.classCanonicalName = enclosingclass.getCanonicalName();
		this.method = method;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((classCanonicalName == null) ? 0 : classCanonicalName.hashCode());
		result = prime * result + ((enclosingClassKind == null) ? 0 : enclosingClassKind.hashCode());
		result = prime * result + ((method == null) ? 0 : method.hashCode());
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
		MethodABIChange other = (MethodABIChange) obj;
		if (classCanonicalName == null) {
			if (other.classCanonicalName != null)
				return false;
		} else if (!classCanonicalName.equals(other.classCanonicalName))
			return false;
		if (enclosingClassKind != other.enclosingClassKind)
			return false;
		if (method == null) {
			if (other.method != null)
				return false;
		} else if (!MethodSignature.signatureEquals(method, other.method, SignatureNameChecker.COMPARE_WITHOUT_NAMES))
			return false;
		return true;
	}
}
