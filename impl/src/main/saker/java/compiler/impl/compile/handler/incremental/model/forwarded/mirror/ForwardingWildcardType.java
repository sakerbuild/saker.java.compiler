/*
 * Copyright (C) 2020 Bence Sipka
 *
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
