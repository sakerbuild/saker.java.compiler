package saker.java.compiler.impl.compat.element;

public interface RequiresDirectiveCompat extends DirectiveCompat {
	@Override
	public default <R, P> R accept(DirectiveVisitorCompat<R, P> v, P p) {
		return v.visitRequires(this, p);
	}

	public boolean isStatic();

	public boolean isTransitive();

	public ModuleElementCompat getDependency();
}
