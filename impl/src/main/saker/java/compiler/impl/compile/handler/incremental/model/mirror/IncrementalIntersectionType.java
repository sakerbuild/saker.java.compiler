package saker.java.compiler.impl.compile.handler.incremental.model.mirror;

import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.Element;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;
import saker.java.compiler.impl.signature.type.IntersectionTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;

public class IncrementalIntersectionType extends IncrementalTypeMirror<IntersectionTypeSignature>
		implements IntersectionType {
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<IncrementalIntersectionType, List> ARFU_bounds = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalIntersectionType.class, List.class, "bounds");

	private Element enclosingElement;

	private volatile transient List<TypeMirror> bounds;

	public IncrementalIntersectionType(IncrementalElementsTypesBase elemTypes, IntersectionTypeSignature signature,
			Element enclosingElement) {
		super(elemTypes, signature);
		this.enclosingElement = enclosingElement;
	}

	@Override
	protected Element getEnclosingResolutionElement() {
		return enclosingElement;
	}

	@Override
	public void invalidate() {
		super.invalidate();
		//fine to use direct assignment as this will not be called from multi-threaded code
		this.bounds = null;
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
		List<TypeMirror> thisbounds = this.bounds;
		if (thisbounds != null) {
			return thisbounds;
		}
		List<? extends TypeSignature> bounds = signature.getBounds();
		thisbounds = JavaTaskUtils.cloneImmutableList(bounds,
				bound -> elemTypes.getTypeMirror(bound, enclosingElement));
		if (ARFU_bounds.compareAndSet(this, null, thisbounds)) {
			return thisbounds;
		}
		return this.bounds;
	}

}
