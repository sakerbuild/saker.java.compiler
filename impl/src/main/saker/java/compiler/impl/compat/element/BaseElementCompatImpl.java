package saker.java.compiler.impl.compat.element;

import javax.lang.model.element.Element;

public class BaseElementCompatImpl<E extends Element> {
	protected final E real;

	public BaseElementCompatImpl(E real) {
		this.real = real;
	}

	public E getRealObject() {
		return real;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + real + "]";
	}
}
