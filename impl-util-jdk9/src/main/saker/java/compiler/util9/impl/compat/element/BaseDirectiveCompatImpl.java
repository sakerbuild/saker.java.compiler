package saker.java.compiler.util9.impl.compat.element;

import javax.lang.model.element.ModuleElement.Directive;

import saker.java.compiler.impl.compat.element.DirectiveCompat;

public abstract class BaseDirectiveCompatImpl<D extends Directive> implements DirectiveCompat {
	protected final D real;

	public BaseDirectiveCompatImpl(D real) {
		this.real = real;
	}

	@Override
	public D getRealObject() {
		return real;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + real + "]";
	}

	@Override
	public String getKind() {
		return real.getKind().toString();
	}
}
