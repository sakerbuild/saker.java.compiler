package saker.java.compiler.impl.compile.handler.incremental.model.mirror;

import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

import saker.java.compiler.impl.compile.handler.incremental.model.CommonWildcardType;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;

public class SimpleWildcardType extends SimpleTypeMirror implements CommonWildcardType {
	private TypeMirror extendsBound;
	private TypeMirror superBound;
	private TypeParameterElement correspondingTypeParameter;

	public SimpleWildcardType(IncrementalElementsTypesBase elemTypes, TypeMirror extendsBound, TypeMirror superBound,
			TypeParameterElement correspondingTypeParameter) {
		super(elemTypes);
		this.extendsBound = extendsBound;
		this.superBound = superBound;
		this.correspondingTypeParameter = correspondingTypeParameter;
	}

	@Override
	public TypeKind getKind() {
		return TypeKind.WILDCARD;
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		return v.visitWildcard(this, p);
	}

	@Override
	public TypeMirror getExtendsBound() {
		return extendsBound;
	}

	@Override
	public TypeMirror getSuperBound() {
		return superBound;
	}

	@Override
	public TypeParameterElement getCorrespondingTypeParameter() {
		return correspondingTypeParameter;
	}

	@Override
	public String toString() {
		return "?" + (superBound != null ? " super " + superBound : "")
				+ (extendsBound != null ? " extends " + extendsBound : "");
	}

}
