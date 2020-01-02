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

import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;
import saker.java.compiler.impl.compile.handler.incremental.model.KindBasedTypeVisitor;
import saker.java.compiler.impl.compile.handler.incremental.model.forwarded.ForwardingAnnotatedConstruct;

public abstract class ForwardingTypeMirrorBase<T extends TypeMirror> extends ForwardingAnnotatedConstruct<T>
		implements ForwardingTypeMirror<T> {
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<ForwardingTypeMirrorBase, TypeMirror> ARFU_erasedType = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingTypeMirrorBase.class, TypeMirror.class, "erasedType");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<ForwardingTypeMirrorBase, TypeKind> ARFU_typeKind = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingTypeMirrorBase.class, TypeKind.class, "typeKind");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<ForwardingTypeMirrorBase, String> ARFU_toString = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingTypeMirrorBase.class, String.class, "toString");

	private volatile transient TypeMirror erasedType;
	private volatile transient TypeKind typeKind;
	private volatile transient String toString;

	public ForwardingTypeMirrorBase(IncrementalElementsTypesBase elemTypes, T subject) {
		super(elemTypes, subject);
	}

	public void setTypeKind(TypeKind typeKind) {
		this.typeKind = typeKind;
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		return KindBasedTypeVisitor.visit(getKind(), this, v, p);
	}

	@Override
	public T getForwardedSubject() {
		return subject;
	}

	@Override
	public TypeKind getKind() {
		TypeKind thistypekind = this.typeKind;
		if (thistypekind != null) {
			return thistypekind;
		}
		thistypekind = elemTypes.javac(subject::getKind);
		if (ARFU_typeKind.compareAndSet(this, null, thistypekind)) {
			return thistypekind;
		}
		return this.typeKind;
	}

	@Override
	public String toString() {
		String thistostring = this.toString;
		if (thistostring != null) {
			return thistostring;
		}
		thistostring = elemTypes.javac(subject::toString);
		if (ARFU_toString.compareAndSet(this, null, thistostring)) {
			return thistostring;
		}
		return this.toString;
	}

	@Override
	public TypeMirror getErasedType() {
		TypeMirror thiserasedtype = this.erasedType;
		if (thiserasedtype != null) {
			return thiserasedtype;
		}
		thiserasedtype = elemTypes.erasureImpl(this);
		if (ARFU_erasedType.compareAndSet(this, null, thiserasedtype)) {
			return thiserasedtype;
		}
		return this.erasedType;
	}

}
