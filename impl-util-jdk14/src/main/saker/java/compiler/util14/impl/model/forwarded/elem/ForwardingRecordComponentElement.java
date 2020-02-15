package saker.java.compiler.util14.impl.model.forwarded.elem;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.RecordComponentElement;

import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;
import saker.java.compiler.impl.compile.handler.incremental.model.forwarded.elem.ForwardingElementBase;

public class ForwardingRecordComponentElement extends ForwardingElementBase<RecordComponentElement>
		implements RecordComponentElement {

	private static final AtomicReferenceFieldUpdater<ForwardingRecordComponentElement, ExecutableElement> ARFU_accessor = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingRecordComponentElement.class, ExecutableElement.class, "accessor");

	private volatile transient ExecutableElement accessor;

	public ForwardingRecordComponentElement(IncrementalElementsTypesBase elemTypes, RecordComponentElement subject) {
		super(elemTypes, subject);
	}

	@Override
	public ExecutableElement getAccessor() {
		ExecutableElement accessor = this.accessor;
		if (accessor != null) {
			return accessor;
		}
		accessor = elemTypes.forwardElement(subject::getAccessor);
		if (ARFU_accessor.compareAndSet(this, null, accessor)) {
			return accessor;
		}
		return this.accessor;
	}

}
