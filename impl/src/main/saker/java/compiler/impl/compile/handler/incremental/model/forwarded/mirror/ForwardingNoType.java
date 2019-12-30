package saker.java.compiler.impl.compile.handler.incremental.model.forwarded.mirror;

import javax.lang.model.type.NoType;

import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

public class ForwardingNoType extends ForwardingTypeMirrorBase<NoType> implements NoType {

	public ForwardingNoType(IncrementalElementsTypes elemTypes, NoType subject) {
		super(elemTypes, subject);
	}

}
