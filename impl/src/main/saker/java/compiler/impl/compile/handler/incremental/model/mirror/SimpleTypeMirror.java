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
package saker.java.compiler.impl.compile.handler.incremental.model.mirror;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.type.TypeMirror;

import saker.java.compiler.impl.compile.handler.incremental.model.CommonTypeMirror;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementallyModelled;
import saker.java.compiler.impl.compile.handler.incremental.model.NonAnnotatedTypeMirror;

public abstract class SimpleTypeMirror implements IncrementallyModelled, CommonTypeMirror, NonAnnotatedTypeMirror {
	private static final AtomicReferenceFieldUpdater<SimpleTypeMirror, TypeMirror> ARFU_erasedType = AtomicReferenceFieldUpdater
			.newUpdater(SimpleTypeMirror.class, TypeMirror.class, "erasedType");

	protected IncrementalElementsTypesBase elemTypes;

	protected volatile transient TypeMirror erasedType;

	public SimpleTypeMirror(IncrementalElementsTypesBase elemTypes) {
		this.elemTypes = elemTypes;
	}

	@Override
	public TypeMirror getErasedType() {
		TypeMirror thiserasedtype = this.erasedType;
		if (thiserasedtype == null) {
			thiserasedtype = elemTypes.erasureImpl(this);
			if (ARFU_erasedType.compareAndSet(this, null, thiserasedtype)) {
				return thiserasedtype;
			}
		}
		return this.erasedType;
	}
}
