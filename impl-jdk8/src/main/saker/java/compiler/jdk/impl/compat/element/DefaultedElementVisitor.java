package saker.java.compiler.jdk.impl.compat.element;

import javax.lang.model.element.Element;
import javax.lang.model.element.UnknownElementException;

import saker.java.compiler.impl.compat.element.ElementVisitorCompat;

public interface DefaultedElementVisitor<R, P> extends ElementVisitorCompat<R, P> {
	@Override
	public default R visitUnknown(Element e, P p) {
		throw new UnknownElementException(e, p);
	}
}
