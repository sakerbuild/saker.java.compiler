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

import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

import saker.build.thirdparty.saker.util.StringUtils;
import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.compile.handler.incremental.model.CommonDeclaredType;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;

public class CapturedDeclaredType extends SimpleTypeMirror implements CommonDeclaredType {
	private static final AtomicReferenceFieldUpdater<CapturedDeclaredType, TypeMirror> ARFU_enclosingType = AtomicReferenceFieldUpdater
			.newUpdater(CapturedDeclaredType.class, TypeMirror.class, "enclosingType");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<CapturedDeclaredType, List> ARFU_typeArguments = AtomicReferenceFieldUpdater
			.newUpdater(CapturedDeclaredType.class, List.class, "typeArguments");

	private IncrementalElementsTypesBase elemTypes;
	private DeclaredType type;

	private volatile transient TypeMirror enclosingType;
	private volatile transient List<TypeMirror> typeArguments;

	public CapturedDeclaredType(IncrementalElementsTypesBase elemTypes, DeclaredType type) {
		super(elemTypes);
		this.elemTypes = elemTypes;
		this.type = type;
	}

	@Override
	public TypeKind getKind() {
		return TypeKind.DECLARED;
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		return v.visitDeclared(this, p);
	}

	@Override
	public Element asElement() {
		return type.asElement();
	}

	@Override
	public TypeMirror getEnclosingType() {
		TypeMirror thisenclosingtype = this.enclosingType;
		if (thisenclosingtype == null) {
			thisenclosingtype = elemTypes.capture(type.getEnclosingType());
			if (ARFU_enclosingType.compareAndSet(this, null, thisenclosingtype)) {
				return thisenclosingtype;
			}
		}
		return this.enclosingType;
	}

	@Override
	public DeclaredType getCapturedType() {
		return this;
	}

	@Override
	public List<? extends TypeMirror> getTypeArguments() {
		List<TypeMirror> thistypearguments = this.typeArguments;
		if (thistypearguments != null) {
			return thistypearguments;
		}
		List<? extends TypeMirror> args = type.getTypeArguments();
		thistypearguments = JavaTaskUtils.cloneImmutableList(args, elemTypes::captureTypeParameter);
		if (ARFU_typeArguments.compareAndSet(this, null, thistypearguments)) {
			return thistypearguments;
		}
		return this.typeArguments;
	}

	@Override
	public String toString() {
		return (getEnclosingType().getKind() == TypeKind.NONE ? "" : getEnclosingType() + ".") + asElement()
				+ (getTypeArguments().isEmpty() ? "" : "<" + StringUtils.toStringJoin(", ", getTypeArguments()) + ">");
	}
}
