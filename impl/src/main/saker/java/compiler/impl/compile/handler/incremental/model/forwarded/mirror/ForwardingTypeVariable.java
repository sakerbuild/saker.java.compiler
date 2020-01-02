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
package saker.java.compiler.impl.compile.handler.incremental.model.forwarded.mirror;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;

public class ForwardingTypeVariable extends ForwardingTypeMirrorBase<TypeVariable> implements TypeVariable {
	private static final AtomicReferenceFieldUpdater<ForwardingTypeVariable, Element> ARFU_asElement = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingTypeVariable.class, Element.class, "asElement");
	private static final AtomicReferenceFieldUpdater<ForwardingTypeVariable, TypeMirror> ARFU_upperBound = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingTypeVariable.class, TypeMirror.class, "upperBound");
	private static final AtomicReferenceFieldUpdater<ForwardingTypeVariable, TypeMirror> ARFU_lowerBound = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingTypeVariable.class, TypeMirror.class, "lowerBound");

	private volatile transient Element asElement;
	private volatile transient TypeMirror upperBound;
	private volatile transient TypeMirror lowerBound;

	public ForwardingTypeVariable(IncrementalElementsTypesBase elemTypes, TypeVariable subject) {
		super(elemTypes, subject);
	}

	@Override
	public Element asElement() {
		Element thisaselement = this.asElement;
		if (thisaselement != null) {
			return thisaselement;
		}
		thisaselement = elemTypes.forwardElement(subject::asElement);
		if (ARFU_asElement.compareAndSet(this, null, thisaselement)) {
			return thisaselement;
		}
		return this.asElement;
	}

	@Override
	public TypeMirror getUpperBound() {
		TypeMirror thisupperbound = this.upperBound;
		if (thisupperbound != null) {
			return thisupperbound;
		}
		thisupperbound = elemTypes.forwardType(subject::getUpperBound);
		if (ARFU_upperBound.compareAndSet(this, null, thisupperbound)) {
			return thisupperbound;
		}
		return this.upperBound;
	}

	@Override
	public TypeMirror getLowerBound() {
		TypeMirror thislowerbound = this.lowerBound;
		if (thislowerbound != null) {
			return thislowerbound;
		}
		thislowerbound = elemTypes.forwardType(subject::getLowerBound);
		if (ARFU_lowerBound.compareAndSet(this, null, thislowerbound)) {
			return thislowerbound;
		}
		return this.lowerBound;
	}

}
