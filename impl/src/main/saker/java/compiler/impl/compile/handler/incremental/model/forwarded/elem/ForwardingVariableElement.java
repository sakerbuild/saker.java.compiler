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

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.VariableElement;

import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;

public class ForwardingVariableElement extends ForwardingElementBase<VariableElement> implements VariableElement {
	private static final AtomicReferenceFieldUpdater<ForwardingVariableElement, Object[]> ARFU_constantValue = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingVariableElement.class, Object[].class, "constantValue");

	private volatile transient Object[] constantValue;

	public ForwardingVariableElement(IncrementalElementsTypesBase elemTypes, VariableElement subject) {
		super(elemTypes, subject);
	}

	@Override
	public Object getConstantValue() {
		Object[] thisconstantvalue = this.constantValue;
		if (thisconstantvalue != null) {
			return thisconstantvalue[0];
		}
		thisconstantvalue = new Object[] { elemTypes.javac(subject::getConstantValue) };
		if (ARFU_constantValue.compareAndSet(this, null, thisconstantvalue)) {
			return thisconstantvalue[0];
		}
		return this.constantValue[0];
	}

}
