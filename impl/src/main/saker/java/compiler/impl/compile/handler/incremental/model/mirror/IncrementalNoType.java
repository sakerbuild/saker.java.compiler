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

import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

import saker.java.compiler.impl.compile.handler.incremental.model.CommonTypeMirror;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementallyModelled;
import saker.java.compiler.impl.compile.handler.incremental.model.NonAnnotatedTypeMirror;

public class IncrementalNoType implements IncrementallyModelled, NoType, NonAnnotatedTypeMirror, CommonTypeMirror {
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
