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

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeMirror;

import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;

public class ForwardingArrayType extends ForwardingTypeMirrorBase<ArrayType> implements ArrayType {
	private static final AtomicReferenceFieldUpdater<ForwardingArrayType, TypeMirror> ARFU_typeParameters = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingArrayType.class, TypeMirror.class, "componentType");

	private volatile transient TypeMirror componentType;

	public ForwardingArrayType(IncrementalElementsTypesBase elemTypes, ArrayType subject) {
		super(elemTypes, subject);
	}

	@Override
	public TypeMirror getComponentType() {
		TypeMirror thiscomponenttype = componentType;
		if (thiscomponenttype != null) {
			return thiscomponenttype;
		}
		thiscomponenttype = elemTypes.forwardType(subject::getComponentType);
		if (ARFU_typeParameters.compareAndSet(this, null, thiscomponenttype)) {
			return thiscomponenttype;
		}
		return this.componentType;
	}

}
