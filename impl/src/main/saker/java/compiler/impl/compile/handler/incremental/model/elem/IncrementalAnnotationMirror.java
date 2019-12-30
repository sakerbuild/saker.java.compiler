package saker.java.compiler.impl.compile.handler.incremental.model.elem;

import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;

import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementallyModelled;
import saker.java.compiler.impl.signature.element.AnnotationSignature;

public class IncrementalAnnotationMirror implements AnnotationMirror, IncrementallyModelled {
	private IncrementalElementsTypesBase elemTypes;
	private AnnotationSignature signature;
	private Element enclosingElement;

	public IncrementalAnnotationMirror(IncrementalElementsTypesBase elementTypes, AnnotationSignature signature,
			Element enclosingElement) {
		this.elemTypes = elementTypes;
		this.signature = signature;
		this.enclosingElement = enclosingElement;
	}

	public Element getResolutionEnclosingElement() {
		return enclosingElement;
	}

	public AnnotationSignature getSignature() {
		return signature;
	}

	@Override
	public DeclaredType getAnnotationType() {
		return elemTypes.getAnnotationDeclaredType(signature, enclosingElement);
	}

	@Override
	public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValues() {
		return elemTypes.getAnnotationValues(this, false, enclosingElement);
	}

	@Override
	public String toString() {
		return signature.toString();
	}
}
