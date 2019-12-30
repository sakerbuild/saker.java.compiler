package saker.java.compiler.impl.compile.handler.incremental.model;

import java.util.Collection;

import javax.lang.model.element.Element;

import saker.java.compiler.impl.signature.element.AnnotatedSignature;
import saker.java.compiler.impl.signature.element.AnnotationSignature;

public class SignatureIncrementalAnnotatedConstruct extends IncrementalAnnotatedConstruct {
	private AnnotatedSignature signature;
	private Element enclosingElement;

	public SignatureIncrementalAnnotatedConstruct(IncrementalElementsTypesBase elemTypes, AnnotatedSignature signature,
			Element enclosingElement) {
		super(elemTypes);
		this.signature = signature;
		this.enclosingElement = enclosingElement;
	}

	@Override
	protected Element getEnclosingResolutionElement() {
		return enclosingElement;
	}

	@Override
	protected Collection<? extends AnnotationSignature> getSignatureAnnotations() {
		return signature.getAnnotations();
	}

	@Override
	public String toString() {
		return signature.toString();
	}
}
