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

import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import saker.java.compiler.impl.compile.handler.incremental.model.CommonDeclaredType;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;

public class ForwardingDeclaredType extends ForwardingTypeMirrorBase<DeclaredType> implements CommonDeclaredType {
	private static final AtomicReferenceFieldUpdater<ForwardingDeclaredType, DeclaredType> ARFU_capturedType = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingDeclaredType.class, DeclaredType.class, "capturedType");
	private static final AtomicReferenceFieldUpdater<ForwardingDeclaredType, Element> ARFU_asElement = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingDeclaredType.class, Element.class, "asElement");
	private static final AtomicReferenceFieldUpdater<ForwardingDeclaredType, TypeMirror> ARFU_enclosingType = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingDeclaredType.class, TypeMirror.class, "enclosingType");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<ForwardingDeclaredType, List> ARFU_typeArguments = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingDeclaredType.class, List.class, "typeArguments");

	private volatile transient DeclaredType capturedType;
	private volatile transient Element asElement;
	private volatile transient TypeMirror enclosingType;
	private volatile transient List<? extends TypeMirror> typeArguments;

	public ForwardingDeclaredType(IncrementalElementsTypesBase elemTypes, DeclaredType subject) {
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
	public TypeMirror getEnclosingType() {
		TypeMirror thisenclosingtype = this.enclosingType;
		if (thisenclosingtype != null) {
			return thisenclosingtype;
		}
		thisenclosingtype = elemTypes.forwardType(subject::getEnclosingType);
		if (ARFU_enclosingType.compareAndSet(this, null, thisenclosingtype)) {
			return thisenclosingtype;
		}
		return this.enclosingType;
	}

	@Override
	public List<? extends TypeMirror> getTypeArguments() {
		List<? extends TypeMirror> thistypearguments = this.typeArguments;
		if (thistypearguments != null) {
			return thistypearguments;
		}
		thistypearguments = elemTypes.forwardTypeArguments((subject::getTypeArguments), this);
		if (ARFU_typeArguments.compareAndSet(this, null, thistypearguments)) {
			return thistypearguments;
		}
		return this.typeArguments;
	}

	@Override
	public DeclaredType getCapturedType() {
		DeclaredType thiscapturedtype = capturedType;
		if (thiscapturedtype != null) {
			return thiscapturedtype;
		}
		thiscapturedtype = elemTypes.captureImpl(this);
		if (ARFU_capturedType.compareAndSet(this, null, thiscapturedtype)) {
			return thiscapturedtype;
		}
		return this.capturedType;
	}

}
