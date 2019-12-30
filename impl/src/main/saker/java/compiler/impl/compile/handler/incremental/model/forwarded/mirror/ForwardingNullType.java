package saker.java.compiler.impl.compile.handler.incremental.model.forwarded.mirror;

import javax.lang.model.type.NullType;

import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;

public class ForwardingNullType extends ForwardingTypeMirrorBase<NullType> implements NullType {

	public ForwardingNullType(IncrementalElementsTypesBase elemTypes, NullType subject) {
		super(elemTypes, subject);
	}

}
