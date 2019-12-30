package saker.java.compiler.impl.compile.handler.incremental.model.elem;

import java.util.Collection;

import javax.lang.model.element.Element;

import saker.java.compiler.impl.compile.handler.incremental.model.CommonElement;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalAnnotatedConstruct;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;
import saker.java.compiler.impl.signature.element.AnnotatedSignature;
import saker.java.compiler.impl.signature.element.AnnotationSignature;

public abstract class IncrementalElement<Sig extends AnnotatedSignature> extends IncrementalAnnotatedConstruct
		implements CommonElement, SignaturedElement<Sig> {
	protected Sig signature;

	public IncrementalElement(IncrementalElementsTypesBase elemTypes, Sig signature) {
		super(elemTypes);
		this.signature = signature;
	}

	@Override
	protected Collection<? extends AnnotationSignature> getSignatureAnnotations() {
		return signature.getAnnotations();
	}

	@Override
	protected Element getEnclosingResolutionElement() {
		return this;
	}

	@Override
	public Sig getSignature() {
		return signature;
	}

	public void setSignature(Sig signature) {
		super.invalidate();
		this.signature = signature;
	}

	@Override
	public String toString() {
		return signature.toString();
	}

	@Override
	public boolean isDeprecated() {
		return hasDeprecatedAnnotation();
	}

//	
//	hashCode and equals inherited from Object
//	
}
