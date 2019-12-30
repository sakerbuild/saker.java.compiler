package saker.java.compiler.impl.compile.handler.incremental.model.forwarded.mirror;

import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.TypeMirror;

import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;

public class ForwardingIntersectionType extends ForwardingTypeMirrorBase<IntersectionType> implements IntersectionType {
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<ForwardingIntersectionType, List> ARFU_bounds = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingIntersectionType.class, List.class, "bounds");

	private volatile transient List<? extends TypeMirror> bounds;

	public ForwardingIntersectionType(IncrementalElementsTypesBase elemTypes, IntersectionType subject) {
		super(elemTypes, subject);
	}

	@Override
	public List<? extends TypeMirror> getBounds() {
		List<? extends TypeMirror> thisbounds = this.bounds;
		if (thisbounds != null) {
			return thisbounds;
		}
		thisbounds = elemTypes.forwardTypes(subject::getBounds);
		if (ARFU_bounds.compareAndSet(this, null, thisbounds)) {
			return thisbounds;
		}
		return this.bounds;
	}

}
