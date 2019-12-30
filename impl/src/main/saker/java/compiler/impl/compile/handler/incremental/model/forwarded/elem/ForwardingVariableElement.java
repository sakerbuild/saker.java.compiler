package saker.java.compiler.impl.compile.handler.incremental.model.forwarded.elem;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.VariableElement;

import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;

public class ForwardingVariableElement extends ForwardingElementBase<VariableElement> implements VariableElement {
	private static final AtomicReferenceFieldUpdater<ForwardingVariableElement, Object[]> ARFU_constantValue = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingVariableElement.class, Object[].class, "constantValue");

	private volatile transient Object[] constantValue;

	public ForwardingVariableElement(IncrementalElementsTypesBase elemTypes, VariableElement subject) {
		super(elemTypes, subject);
	}

	@Override
	public Object getConstantValue() {
		Object[] thisconstantvalue = this.constantValue;
		if (thisconstantvalue != null) {
			return thisconstantvalue[0];
		}
		thisconstantvalue = new Object[] { elemTypes.javac(subject::getConstantValue) };
		if (ARFU_constantValue.compareAndSet(this, null, thisconstantvalue)) {
			return thisconstantvalue[0];
		}
		return this.constantValue[0];
	}

}
