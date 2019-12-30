package saker.java.compiler.impl.compile.handler.incremental.model.forwarded.mirror;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeMirror;

import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;

public class ForwardingArrayType extends ForwardingTypeMirrorBase<ArrayType> implements ArrayType {
	private static final AtomicReferenceFieldUpdater<ForwardingArrayType, TypeMirror> ARFU_typeParameters = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingArrayType.class, TypeMirror.class, "componentType");

	private volatile transient TypeMirror componentType;

	public ForwardingArrayType(IncrementalElementsTypesBase elemTypes, ArrayType subject) {
		super(elemTypes, subject);
	}

	@Override
	public TypeMirror getComponentType() {
		TypeMirror thiscomponenttype = componentType;
		if (thiscomponenttype != null) {
			return thiscomponenttype;
		}
		thiscomponenttype = elemTypes.forwardType(subject::getComponentType);
		if (ARFU_typeParameters.compareAndSet(this, null, thiscomponenttype)) {
			return thiscomponenttype;
		}
		return this.componentType;
	}

}
