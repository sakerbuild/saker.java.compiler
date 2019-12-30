package saker.java.compiler.impl.compile.handler.incremental.model.forwarded.mirror;

import javax.lang.model.type.PrimitiveType;

import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;

public class ForwardingPrimitiveType extends ForwardingTypeMirrorBase<PrimitiveType> implements PrimitiveType {

	public ForwardingPrimitiveType(IncrementalElementsTypesBase elemTypes, PrimitiveType subject) {
		super(elemTypes, subject);
	}

}
