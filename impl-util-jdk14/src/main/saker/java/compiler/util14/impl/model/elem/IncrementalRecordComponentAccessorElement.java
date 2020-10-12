package saker.java.compiler.util14.impl.model.elem;

import javax.lang.model.element.Element;

import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;
import saker.java.compiler.impl.compile.handler.incremental.model.elem.IncrementalElement;
import saker.java.compiler.impl.compile.handler.incremental.model.elem.IncrementalExecutableElement;
import saker.java.compiler.impl.signature.element.MethodSignature;

public class IncrementalRecordComponentAccessorElement extends IncrementalExecutableElement {
	private IncrementalRecordComponentElement recordElement;

	public IncrementalRecordComponentAccessorElement(MethodSignature signature, IncrementalElement<?> enclosingElement,
			IncrementalElementsTypesBase elemTypes, IncrementalRecordComponentElement record) {
		super(signature, enclosingElement, elemTypes);
		this.recordElement = record;
	}

	public IncrementalRecordComponentElement getRecordComponentElement() {
		return recordElement;
	}

	@Override
	public Element getRecordComponentForAccessor() {
		return recordElement;
	}
}
