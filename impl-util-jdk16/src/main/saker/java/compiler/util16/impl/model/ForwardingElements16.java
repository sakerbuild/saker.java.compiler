package saker.java.compiler.util16.impl.model;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.RecordComponentElement;

import saker.java.compiler.util9.impl.model.ForwardingElements9;

public abstract class ForwardingElements16 extends ForwardingElements9 {

	protected ForwardingElements16() {
		super();
	}

	@Override
	public RecordComponentElement recordComponentFor(ExecutableElement accessor) {
		return getForwardedElements().recordComponentFor(accessor);
	}
}
