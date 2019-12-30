package saker.java.compiler.impl.compat.type;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

public interface TypeVisitorCompat<R, P> extends TypeVisitor<R, P> {
	@Override
	public default R visit(TypeMirror t, P p) {
		return t.accept(this, p);
	}

	@Override
	public default R visit(TypeMirror t) {
		return visit(t, null);
	}
}
