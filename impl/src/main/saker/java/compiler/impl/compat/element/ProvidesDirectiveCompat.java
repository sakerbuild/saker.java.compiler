package saker.java.compiler.impl.compat.element;

import java.util.List;

import javax.lang.model.element.TypeElement;

public interface ProvidesDirectiveCompat extends DirectiveCompat {
	@Override
	public default <R, P> R accept(DirectiveVisitorCompat<R, P> v, P p) {
		return v.visitProvides(this, p);
	}

	public TypeElement getService();

	public List<? extends TypeElement> getImplementations();
}
