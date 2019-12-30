package saker.java.compiler.impl.compat.element;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.UnknownElementException;

public interface ElementVisitorCompat<R, P> extends ElementVisitor<R, P> {
	@Override
	public default R visit(Element e, P p) {
		return e.accept(this, p);
	}

	@Override
	public default R visit(Element e) {
		return visit(e, null);
	}

	@Override
	public default R visitUnknown(Element e, P p) {
		throw new UnknownElementException(e, p);
	}

	public default R visitModuleCompat(ModuleElementCompat moduleElement, P p) {
		return visitUnknown(moduleElement.getRealObject(), p);
	}
}
