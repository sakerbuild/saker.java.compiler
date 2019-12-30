package saker.java.compiler.impl.compat.element;

public interface DirectiveCompat {
	public Object getRealObject();

	public String getKind();

	public <R, P> R accept(DirectiveVisitorCompat<R, P> v, P p);
}
