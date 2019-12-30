package saker.java.compiler.impl.compile.handler.incremental.model.mirror;

import java.util.Collections;
import java.util.List;

import javax.lang.model.element.Element;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

import saker.java.compiler.impl.compile.handler.incremental.model.CommonTypeMirror;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementallyModelled;
import saker.java.compiler.impl.compile.handler.incremental.model.NonAnnotatedConstruct;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

public class IncrementalErrorType implements IncrementallyModelled, ErrorType, NonAnnotatedConstruct, CommonTypeMirror {
	private Object cause;
	private Element elem;
	private TypeMirror enclosingType;

	public IncrementalErrorType(IncrementalElementsTypesBase elemTypes, Object cause) {
		this(cause, null, IncrementalElementsTypes.getNoneTypeKind());
	}

	public IncrementalErrorType(IncrementalElementsTypesBase elemTypes, Element cause) {
		this(cause, cause, IncrementalElementsTypes.getNoneTypeKind());
	}

	public IncrementalErrorType(Object cause, Element elem, TypeMirror enclosingType) {
		this.cause = cause;
		this.elem = elem;
		this.enclosingType = enclosingType;
	}

	@Override
	public Element asElement() {
		return elem;
	}

	@Override
	public TypeMirror getEnclosingType() {
		return enclosingType;
	}

	@Override
	public List<? extends TypeMirror> getTypeArguments() {
		return Collections.emptyList();
	}

	@Override
	public TypeKind getKind() {
		return TypeKind.ERROR;
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		return v.visitError(this, p);
	}

	@Override
	public String toString() {
		return cause.toString();
	}

	@Override
	public TypeMirror getErasedType() {
		return this;
	}
}
