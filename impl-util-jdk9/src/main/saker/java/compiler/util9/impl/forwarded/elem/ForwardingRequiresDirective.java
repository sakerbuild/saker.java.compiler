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
package saker.java.compiler.util9.impl.forwarded.elem;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.ModuleElement.RequiresDirective;

import saker.java.compiler.util9.impl.model.IncrementalElementsTypes9;

public class ForwardingRequiresDirective extends ForwardingDirectiveBase<RequiresDirective> implements RequiresDirective {
	private static final AtomicReferenceFieldUpdater<ForwardingRequiresDirective, ModuleElement> ARFU_dependency = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingRequiresDirective.class, ModuleElement.class, "dependency");
	private static final AtomicIntegerFieldUpdater<ForwardingRequiresDirective> AIFU_static = AtomicIntegerFieldUpdater
			.newUpdater(ForwardingRequiresDirective.class, "staticValue");
	private static final AtomicIntegerFieldUpdater<ForwardingRequiresDirective> AIFU_transient = AtomicIntegerFieldUpdater
			.newUpdater(ForwardingRequiresDirective.class, "transientValue");

	private volatile ModuleElement dependency;
	private volatile transient int staticValue = -1;
	private volatile transient int transientValue = -1;

	public ForwardingRequiresDirective(IncrementalElementsTypes9 elemTypes, RequiresDirective subject) {
		super(elemTypes, subject);
	}

	@Override
	public ModuleElement getDependency() {
		ModuleElement thisval = this.dependency;
		if (thisval != null) {
			return thisval;
		}
		thisval = elemTypes.forwardElement(subject::getDependency);
		if (ARFU_dependency.compareAndSet(this, null, thisval)) {
			return thisval;
		}
		return this.dependency;
	}

	@Override
	public boolean isStatic() {
		int thisval = this.staticValue;
		if (thisval >= 0) {
			return thisval != 0;
		}
		thisval = elemTypes.javac(subject::isStatic) ? 1 : 0;
		if (AIFU_static.compareAndSet(this, -1, thisval)) {
			return thisval != 0;
		}
		return this.staticValue != 0;
	}

	@Override
	public boolean isTransitive() {
		int thisval = this.transientValue;
		if (thisval >= 0) {
			return thisval != 0;
		}
		thisval = elemTypes.javac(subject::isTransitive) ? 1 : 0;
		if (AIFU_transient.compareAndSet(this, -1, thisval)) {
			return thisval != 0;
		}
		return this.transientValue != 0;
	}

}
