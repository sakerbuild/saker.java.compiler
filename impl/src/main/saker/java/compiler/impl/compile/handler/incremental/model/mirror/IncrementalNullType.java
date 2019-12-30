package saker.java.compiler.impl.compile.handler.incremental.model.mirror;

import javax.lang.model.type.NullType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

import saker.java.compiler.impl.compile.handler.incremental.model.CommonTypeMirror;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementallyModelled;
import saker.java.compiler.impl.compile.handler.incremental.model.NonAnnotatedConstruct;

public class IncrementalNullType implements IncrementallyModelled, NullType, NonAnnotatedConstruct, CommonTypeMirror {
	public static final IncrementalNullType INSTANCE = new IncrementalNullType();

	public IncrementalNullType() {
	}

	@Override
	public TypeKind getKind() {
		return TypeKind.NULL;
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		return v.visitNull(this, p);
	}

	@Override
	public String toString() {
		return "null";
	}

	@Override
	public TypeMirror getErasedType() {
		return this;
	}
}
