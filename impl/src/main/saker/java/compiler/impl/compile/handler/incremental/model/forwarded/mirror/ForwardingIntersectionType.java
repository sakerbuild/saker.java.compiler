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
