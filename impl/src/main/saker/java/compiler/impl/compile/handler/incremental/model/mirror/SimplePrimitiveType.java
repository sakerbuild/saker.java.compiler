package saker.java.compiler.impl.compile.handler.incremental.model.mirror;

import java.util.Locale;

import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

import saker.java.compiler.impl.compile.handler.incremental.model.CommonTypeMirror;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementallyModelled;
import saker.java.compiler.impl.compile.handler.incremental.model.NonAnnotatedConstruct;

public class SimplePrimitiveType
		implements PrimitiveType, IncrementallyModelled, CommonTypeMirror, NonAnnotatedConstruct {
	private TypeKind kind;

	public SimplePrimitiveType(TypeKind kind) {
		this.kind = kind;
	}

	@Override
	public TypeKind getKind() {
		return kind;
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		return v.visitPrimitive(this, p);
	}

	@Override
	public String toString() {
		return kind.toString().toLowerCase(Locale.ENGLISH);
	}

	@Override
	public TypeMirror getErasedType() {
		return this;
	}

}
