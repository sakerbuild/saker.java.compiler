package saker.java.compiler.impl.compile.handler.incremental.model.forwarded.mirror;

import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.UnionType;

import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;

public class ForwardingUnionType extends ForwardingTypeMirrorBase<UnionType> implements UnionType {
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<ForwardingUnionType, List> ARFU_alternatives = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingUnionType.class, List.class, "alternatives");

	private volatile transient List<? extends TypeMirror> alternatives;

	public ForwardingUnionType(IncrementalElementsTypesBase elemTypes, UnionType subject) {
		super(elemTypes, subject);
	}

	@Override
	public List<? extends TypeMirror> getAlternatives() {
		List<? extends TypeMirror> thisalternatives = this.alternatives;
		if (thisalternatives != null) {
			return thisalternatives;
		}
		thisalternatives = elemTypes.forwardTypes(subject::getAlternatives);
		if (ARFU_alternatives.compareAndSet(this, null, thisalternatives)) {
			return thisalternatives;
		}
		return this.alternatives;
	}

}
