package saker.java.compiler.impl.compat.element;

import javax.lang.model.element.TypeElement;

public interface UsesDirectiveCompat extends DirectiveCompat {
	@Override
	public default <R, P> R accept(DirectiveVisitorCompat<R, P> v, P p) {
		return v.visitUses(this, p);
	}

	public TypeElement getService();
}
