package saker.java.compiler.impl.compile.handler.incremental.model.mirror;

import java.util.List;

import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

import saker.build.thirdparty.saker.util.StringUtils;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;

public class SimpleIntersectionType extends SimpleTypeMirror implements IntersectionType {
	private List<? extends TypeMirror> bounds;

	public SimpleIntersectionType(IncrementalElementsTypesBase elemTypes, List<? extends TypeMirror> bounds) {
		super(elemTypes);
		this.bounds = bounds;
	}

	@Override
	public TypeKind getKind() {
		return TypeKind.INTERSECTION;
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		return v.visitIntersection(this, p);
	}

	@Override
	public List<? extends TypeMirror> getBounds() {
		return bounds;
	}

	@Override
	public String toString() {
		return StringUtils.toStringJoin(" & ", bounds);
	}
}
