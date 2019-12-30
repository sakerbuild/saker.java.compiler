package saker.java.compiler.impl.compat.tree;

import com.sun.source.tree.Tree;

public class BaseTreeCompatImpl<T extends Tree> {
	protected final T real;

	public BaseTreeCompatImpl(T real) {
		this.real = real;
	}

	public T getRealObject() {
		return real;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + real + "]";
	}
}
