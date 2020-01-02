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
package saker.java.compiler.impl.compile.handler.incremental.model.mirror;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.type.TypeMirror;

import saker.java.compiler.impl.compile.handler.incremental.model.CommonTypeMirror;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalAnnotatedConstruct;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;
import saker.java.compiler.impl.signature.element.AnnotatedSignature;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

public abstract class IncrementalTypeMirror<Sig extends AnnotatedSignature> extends IncrementalAnnotatedConstruct
		implements CommonTypeMirror {
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<IncrementalTypeMirror, TypeMirror> ARFU_erasedType = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalTypeMirror.class, TypeMirror.class, "erasedType");

	protected Sig signature;

	private volatile transient TypeMirror erasedType;

	public IncrementalTypeMirror(IncrementalElementsTypesBase elemTypes, Sig signature) {
		super(elemTypes);
		this.signature = signature;
		elementTypes = IncrementalElementsTypes.ELEMENT_TYPE_TYPE_USE;
	}

	public Sig getSignature() {
		return signature;
	}

	@Override
	public void invalidate() {
		super.invalidate();
		//fine to use direct assignment as this will not be called from multi-threaded code
		this.erasedType = null;
	}

	public void setSignature(Sig signature) {
		invalidate();
		this.signature = signature;
	}

	@Override
	protected Collection<? extends AnnotationSignature> getSignatureAnnotations() {
		return signature.getAnnotations();
	}

	@Override
	public String toString() {
		return signature.toString();
	}

	@Override
	public TypeMirror getErasedType() {
		TypeMirror thiserasedtype = this.erasedType;
		if (thiserasedtype == null) {
			thiserasedtype = elemTypes.erasureImpl(this);
			if (ARFU_erasedType.compareAndSet(this, null, thiserasedtype)) {
				return thiserasedtype;
			}
		}
		return this.erasedType;
	}
}
