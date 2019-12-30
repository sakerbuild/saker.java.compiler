package saker.java.compiler.impl.compile.handler.incremental.model.forwarded.elem;

import javax.lang.model.element.Element;

import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;

public class ForwardingUnknownElement extends ForwardingElementBase<Element> {

	public ForwardingUnknownElement(IncrementalElementsTypesBase elemTypes, Element subject) {
		super(elemTypes, subject);
	}

}
