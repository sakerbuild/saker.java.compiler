package saker.java.compiler.impl.compat;

public class CompatObjectImpl<T> {
	protected final T real;

	public CompatObjectImpl(T real) {
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
