package saker.java.compiler.jdk.impl.compat.type;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.UnknownTypeException;

import saker.java.compiler.impl.compat.type.TypeVisitorCompat;

public interface DefaultedTypeVisitor<R, P> extends TypeVisitorCompat<R, P> {
	@Override
	public default R visitUnknown(TypeMirror t, P p) {
		throw new UnknownTypeException(t, p);
	}
}
