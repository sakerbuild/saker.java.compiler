package saker.java.compiler.impl.compile.handler.incremental.model.forwarded.mirror;

import javax.lang.model.type.TypeMirror;

import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;

public class ForwardingUnknownType extends ForwardingTypeMirrorBase<TypeMirror> {

	public ForwardingUnknownType(IncrementalElementsTypesBase elemTypes, TypeMirror subject) {
		super(elemTypes, subject);
	}

}
