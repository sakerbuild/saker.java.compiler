package saker.java.compiler.impl.compile.handler.incremental.model;

import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.WildcardType;

public interface CommonWildcardType extends WildcardType {
	public TypeParameterElement getCorrespondingTypeParameter();
}
