package saker.java.compiler.impl.compile.handler.incremental.model.elem;

import javax.lang.model.element.Element;

import saker.java.compiler.impl.signature.Signature;

public interface SignaturedElement<Sig extends Signature> extends Element {
	public Sig getSignature();
}
