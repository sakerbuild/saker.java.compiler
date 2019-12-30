package saker.java.compiler.impl.compat.element;

public interface DirectiveVisitorCompat<R, P> {
	public default R visit(DirectiveCompat d) {
		return d.accept(this, null);
	}

	public default R visit(DirectiveCompat d, P p) {
		return d.accept(this, p);
	}

	public R visitRequires(RequiresDirectiveCompat d, P p);

	public R visitExports(ExportsDirectiveCompat d, P p);

	public R visitOpens(OpensDirectiveCompat d, P p);

	public R visitUses(UsesDirectiveCompat d, P p);

	public R visitProvides(ProvidesDirectiveCompat d, P p);
}
