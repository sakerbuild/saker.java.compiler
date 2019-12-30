package saker.java.compiler.impl.compile.handler.invoker;

import javax.lang.model.element.NestingKind;

import saker.java.compiler.impl.signature.element.ClassSignatureHeader;

class SimpleClassSignatureHeader implements ClassSignatureHeader {
	private ClassSignatureHeader enclosingSignature;
	private String simpleName;
	private String binaryName;
	private NestingKind nestingKind;

	public SimpleClassSignatureHeader(ClassSignatureHeader enclosingSignature, String simpleName, String binaryName,
			NestingKind nestingKind) {
		this.enclosingSignature = enclosingSignature;
		this.simpleName = simpleName;
		this.binaryName = binaryName;
		this.nestingKind = nestingKind;
	}

	@Override
	public String getSimpleName() {
		return simpleName;
	}

	@Override
	public String getBinaryName() {
		return binaryName;
	}

	@Override
	public NestingKind getNestingKind() {
		return nestingKind;
	}

	@Override
	public ClassSignatureHeader getEnclosingSignature() {
		return enclosingSignature;
	}

	@Override
	public String toString() {
		return binaryName;
	}

}