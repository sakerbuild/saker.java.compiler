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
package saker.java.compiler.impl.compile.handler.incremental.model.forwarded.elem;

import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;

import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;

public class ForwardingTypeParameterElement extends ForwardingElementBase<TypeParameterElement>
		implements TypeParameterElement {
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<ForwardingTypeParameterElement, List> ARFU_bounds = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingTypeParameterElement.class, List.class, "bounds");
	private static final AtomicReferenceFieldUpdater<ForwardingTypeParameterElement, Element> ARFU_genericElement = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingTypeParameterElement.class, Element.class, "genericElement");

	private volatile transient List<? extends TypeMirror> bounds;
	private volatile transient Element genericElement;

	public ForwardingTypeParameterElement(IncrementalElementsTypesBase elemTypes, TypeParameterElement subject) {
		super(elemTypes, subject);
	}

	@Override
	public Element getGenericElement() {
		Element thisgenericelement = genericElement;
		if (thisgenericelement != null) {
			return thisgenericelement;
		}
		thisgenericelement = elemTypes.forwardElement(subject::getGenericElement);
		if (ARFU_genericElement.compareAndSet(this, null, thisgenericelement)) {
			return thisgenericelement;
		}
		return genericElement;
	}

	@Override
	public List<? extends TypeMirror> getBounds() {
		List<? extends TypeMirror> thisbounds = this.bounds;
		if (thisbounds != null) {
			return thisbounds;
		}
		thisbounds = elemTypes.forwardTypes(subject::getBounds);
		if (ARFU_bounds.compareAndSet(this, null, thisbounds)) {
			return thisbounds;
		}
		return this.bounds;
	}

}
