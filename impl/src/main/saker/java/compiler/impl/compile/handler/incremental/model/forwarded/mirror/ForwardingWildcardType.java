package saker.java.compiler.impl.compile.handler.incremental.model.forwarded.mirror;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;

import saker.java.compiler.impl.compile.handler.incremental.model.CommonWildcardType;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;

public class ForwardingWildcardType extends ForwardingTypeMirrorBase<WildcardType> implements CommonWildcardType {
	private static final AtomicReferenceFieldUpdater<ForwardingWildcardType, TypeMirror[]> ARFU_extendsBounds = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingWildcardType.class, TypeMirror[].class, "extendsBounds");
	private static final AtomicReferenceFieldUpdater<ForwardingWildcardType, TypeMirror[]> ARFU_superBounds = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingWildcardType.class, TypeMirror[].class, "superBounds");

	private TypeParameterElement correspondingTypeParameter;

	private transient volatile TypeMirror[] extendsBounds;
	private transient volatile TypeMirror[] superBounds;

	public ForwardingWildcardType(IncrementalElementsTypesBase elemTypes, WildcardType subject,
			TypeParameterElement correspondingTypeParameter) {
		super(elemTypes, subject);
		this.correspondingTypeParameter = correspondingTypeParameter;
	}

	@Override
	public TypeMirror getExtendsBound() {
		TypeMirror[] thisextendsbound = this.extendsBounds;
		if (thisextendsbound != null) {
			return thisextendsbound[0];
		}
		TypeMirror eb = elemTypes.javac(subject::getExtendsBound);
		if (eb != null) {
			eb = elemTypes.forwardType(eb);
		}
		thisextendsbound = new TypeMirror[] { eb };
		if (ARFU_extendsBounds.compareAndSet(this, null, thisextendsbound)) {
			return thisextendsbound[0];
		}
		return this.extendsBounds[0];
	}

	@Override
	public TypeMirror getSuperBound() {
		TypeMirror[] thissuperbound = this.superBounds;
		if (thissuperbound != null) {
			return thissuperbound[0];
		}
		TypeMirror sb = elemTypes.javac(subject::getSuperBound);
		if (sb != null) {
			sb = elemTypes.forwardType(sb);
		}
		thissuperbound = new TypeMirror[] { sb };
		if (ARFU_superBounds.compareAndSet(this, null, thissuperbound)) {
			return thissuperbound[0];
		}
		return this.superBounds[0];
	}

	@Override
	public TypeParameterElement getCorrespondingTypeParameter() {
		return correspondingTypeParameter;
	}

}
