package saker.java.compiler.impl.compile.handler.incremental.model;

import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

public interface CommonTypeMirror extends TypeMirror {
	public TypeMirror getErasedType();

	public default Element asElement() {
		return null;
	}
}
