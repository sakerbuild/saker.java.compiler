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
package saker.java.compiler.util9.impl.model.elem;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.ModuleElement.DirectiveVisitor;
import javax.lang.model.element.ModuleElement.UsesDirective;
import javax.lang.model.element.TypeElement;

import saker.java.compiler.impl.signature.element.ModuleSignature.UsesDirectiveSignature;
import saker.java.compiler.util9.impl.model.IncrementalElementsTypes9;

public class IncrementalUsesDirective extends IncrementalDirectiveBase<UsesDirectiveSignature>
		implements UsesDirective {
	private static final AtomicReferenceFieldUpdater<IncrementalUsesDirective, TypeElement> ARFU_service = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalUsesDirective.class, TypeElement.class, "service");

	private volatile transient TypeElement service;

	public IncrementalUsesDirective(IncrementalElementsTypes9 elemTypes, UsesDirectiveSignature signature,
			IncrementalModuleElement module) {
		super(elemTypes, signature, module);
	}

	@Override
	public <R, P> R accept(DirectiveVisitor<R, P> v, P p) {
		return v.visitUses(this, p);
	}

	@Override
	public TypeElement getService() {
		TypeElement thisservice = this.service;
		if (thisservice != null) {
			return thisservice;
		}
		thisservice = elemTypes.getTypeElement(signature.getService(), module);
		if (ARFU_service.compareAndSet(this, null, thisservice)) {
			return thisservice;
		}
		return this.service;
	}

}
