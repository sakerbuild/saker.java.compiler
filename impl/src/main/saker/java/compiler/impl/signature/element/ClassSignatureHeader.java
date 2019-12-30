package saker.java.compiler.impl.signature.element;

import javax.lang.model.element.NestingKind;

import saker.java.compiler.impl.signature.Signature;

public interface ClassSignatureHeader extends Signature {
	public String getSimpleName();

	public String getBinaryName();

	public NestingKind getNestingKind();

	public ClassSignatureHeader getEnclosingSignature();
}
