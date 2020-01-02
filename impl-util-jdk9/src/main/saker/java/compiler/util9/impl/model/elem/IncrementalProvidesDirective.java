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

import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.ModuleElement.DirectiveVisitor;
import javax.lang.model.element.ModuleElement.ProvidesDirective;
import javax.lang.model.element.TypeElement;

import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.signature.element.ModuleSignature.ProvidesDirectiveSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.util9.impl.model.IncrementalElementsTypes9;

public class IncrementalProvidesDirective extends IncrementalDirectiveBase<ProvidesDirectiveSignature>
		implements ProvidesDirective {
	private static final AtomicReferenceFieldUpdater<IncrementalProvidesDirective, TypeElement> ARFU_service = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalProvidesDirective.class, TypeElement.class, "service");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<IncrementalProvidesDirective, List> ARFU_implementations = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalProvidesDirective.class, List.class, "implementations");

	private volatile transient TypeElement service;
	private volatile transient List<TypeElement> implementations;

	public IncrementalProvidesDirective(IncrementalElementsTypes9 elemTypes, ProvidesDirectiveSignature signature,
			IncrementalModuleElement module) {
		super(elemTypes, signature, module);
	}

	@Override
	public <R, P> R accept(DirectiveVisitor<R, P> v, P p) {
		return v.visitProvides(this, p);
	}

	@Override
	public List<? extends TypeElement> getImplementations() {
		List<TypeElement> thisimplementations = this.implementations;
		if (thisimplementations != null) {
			return thisimplementations;
		}
		List<? extends TypeSignature> impltypes = signature.getImplementationTypes();
		thisimplementations = JavaTaskUtils.cloneImmutableList(impltypes, elemTypes::getCurrentModuleTypeElement);
		if (ARFU_implementations.compareAndSet(this, null, thisimplementations)) {
			return thisimplementations;
		}
		return this.implementations;
	}

	@Override
	public TypeElement getService() {
		TypeElement thisservice = this.service;
		if (thisservice != null) {
			return thisservice;
		}
		thisservice = elemTypes.getCurrentModuleTypeElement(signature.getService());
		if (ARFU_service.compareAndSet(this, null, thisservice)) {
			return thisservice;
		}
		return this.service;
	}

}
