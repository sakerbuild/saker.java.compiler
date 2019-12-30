package saker.java.compiler.impl.compile.handler.incremental.model;

import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;

public interface CommonDeclaredType extends DeclaredType, CommonTypeMirror {
	public DeclaredType getCapturedType();

	@Override
	public Element asElement();
}
