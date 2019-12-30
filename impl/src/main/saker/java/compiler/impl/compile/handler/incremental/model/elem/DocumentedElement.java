package saker.java.compiler.impl.compile.handler.incremental.model.elem;

import saker.java.compiler.impl.signature.element.DocumentedSignature;

public interface DocumentedElement<Sig extends DocumentedSignature> extends SignaturedElement<Sig> {
	public String getDocComment();
}
