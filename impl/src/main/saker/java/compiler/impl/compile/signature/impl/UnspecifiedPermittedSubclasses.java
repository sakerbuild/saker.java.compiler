package saker.java.compiler.impl.compile.signature.impl;

import saker.java.compiler.impl.signature.element.ClassSignature.PermittedSubclassesList;

public enum UnspecifiedPermittedSubclasses implements PermittedSubclassesList {
	INSTANCE;

	@Override
	public void accept(Visitor visitor) {
		visitor.visitUnspecified();
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "." + name();
	}
}
