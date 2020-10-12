package saker.java.compiler.util14.impl.model.elem;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.RecordComponentElement;

import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;
import saker.java.compiler.impl.compile.handler.incremental.model.forwarded.elem.ForwardingExecutableElement;

public class ForwardingRecordComponentAccessorElement extends ForwardingExecutableElement {

	private RecordComponentElement recordComponent;

	public ForwardingRecordComponentAccessorElement(IncrementalElementsTypesBase elemTypes, ExecutableElement subject,
			RecordComponentElement recordComponent) {
		super(elemTypes, subject);
		this.recordComponent = recordComponent;
	}

	@Override
	public Element getRecordComponentForAccessor() {
		return recordComponent;
	}
}
