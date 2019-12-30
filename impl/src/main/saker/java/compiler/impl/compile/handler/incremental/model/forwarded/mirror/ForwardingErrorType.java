package saker.java.compiler.impl.compile.handler.incremental.model.forwarded.mirror;

import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeMirror;

import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;

public class ForwardingErrorType extends ForwardingTypeMirrorBase<ErrorType> implements ErrorType {

	public ForwardingErrorType(IncrementalElementsTypesBase elemTypes, ErrorType subject) {
		super(elemTypes, subject);
	}

	@Override
	public Element asElement() {
		return elemTypes.forwardElement(elemTypes.javac(subject::asElement));
	}

	@Override
	public TypeMirror getEnclosingType() {
		return elemTypes.forwardType(subject::getEnclosingType);
	}

	@Override
	public List<? extends TypeMirror> getTypeArguments() {
		return elemTypes.forwardTypes(subject::getTypeArguments);
	}

}
