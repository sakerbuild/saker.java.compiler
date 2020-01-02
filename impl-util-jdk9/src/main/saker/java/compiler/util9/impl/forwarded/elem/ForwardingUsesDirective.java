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

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.ModuleElement.UsesDirective;
import javax.lang.model.element.TypeElement;

import saker.java.compiler.util9.impl.model.IncrementalElementsTypes9;

public class ForwardingUsesDirective extends ForwardingDirectiveBase<UsesDirective> implements UsesDirective {
	private static final AtomicReferenceFieldUpdater<ForwardingUsesDirective, TypeElement> ARFU_service = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingUsesDirective.class, TypeElement.class, "service");

	private volatile transient TypeElement service;

	public ForwardingUsesDirective(IncrementalElementsTypes9 elemTypes, UsesDirective subject) {
		super(elemTypes, subject);
	}

	@Override
	public TypeElement getService() {
		TypeElement thisval = this.service;
		if (thisval != null) {
			return thisval;
		}
		thisval = elemTypes.forwardElement(subject::getService);
		if (ARFU_service.compareAndSet(this, null, thisval)) {
			return thisval;
		}
		return this.service;
	}

}
