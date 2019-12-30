package saker.java.compiler.impl.compile.handler.incremental.model.mirror;

import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

import saker.java.compiler.impl.compile.handler.incremental.model.CommonTypeMirror;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementallyModelled;
import saker.java.compiler.impl.compile.handler.incremental.model.NonAnnotatedConstruct;

public class IncrementalNoType implements IncrementallyModelled, NoType, NonAnnotatedConstruct, CommonTypeMirror {
	public static final IncrementalNoType INSTANCE_ERROR = new IncrementalNoType(TypeKind.ERROR);
	public static final IncrementalNoType INSTANCE_VOID = new IncrementalNoType(TypeKind.VOID);
	public static final IncrementalNoType INSTANCE_NONE = new IncrementalNoType(TypeKind.NONE);

	private TypeKind kind;

	private IncrementalNoType(TypeKind kind) {
		this.kind = kind;
	}

	@Override
	public TypeKind getKind() {
		return kind;
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		return v.visitNoType(this, p);
	}

	@Override
	public String toString() {
		return kind.toString().toLowerCase();
	}

	@Override
	public TypeMirror getErasedType() {
		return this;
	}

}
