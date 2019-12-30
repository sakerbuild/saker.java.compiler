package saker.java.compiler.util9.impl.compat.element;

import javax.lang.model.element.ModuleElement;

import saker.java.compiler.impl.compat.element.ElementVisitorCompat;

public interface DefaultedElementVisitor9<R, P> extends ElementVisitorCompat<R, P> {
	@Override
	public default R visitModule(ModuleElement e, P p) {
		return visitModuleCompat(new ModuleElementCompatImpl(e), p);
	}
}
