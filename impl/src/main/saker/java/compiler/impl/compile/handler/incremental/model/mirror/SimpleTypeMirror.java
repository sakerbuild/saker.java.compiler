package saker.java.compiler.impl.compile.handler.incremental.model.mirror;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.type.TypeMirror;

import saker.java.compiler.impl.compile.handler.incremental.model.CommonTypeMirror;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementallyModelled;
import saker.java.compiler.impl.compile.handler.incremental.model.NonAnnotatedConstruct;

public abstract class SimpleTypeMirror implements IncrementallyModelled, CommonTypeMirror, NonAnnotatedConstruct {
	private static final AtomicReferenceFieldUpdater<SimpleTypeMirror, TypeMirror> ARFU_erasedType = AtomicReferenceFieldUpdater
			.newUpdater(SimpleTypeMirror.class, TypeMirror.class, "erasedType");

	protected IncrementalElementsTypesBase elemTypes;

	protected volatile transient TypeMirror erasedType;

	public SimpleTypeMirror(IncrementalElementsTypesBase elemTypes) {
		this.elemTypes = elemTypes;
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
