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
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

import saker.build.thirdparty.saker.util.StringUtils;
import saker.java.compiler.impl.compile.handler.incremental.model.CommonDeclaredType;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;

public class SimpleDeclaredType extends SimpleTypeMirror implements CommonDeclaredType {
	private static final AtomicReferenceFieldUpdater<SimpleDeclaredType, DeclaredType> ARFU_capturedType = AtomicReferenceFieldUpdater
			.newUpdater(SimpleDeclaredType.class, DeclaredType.class, "capturedType");

	private TypeMirror enclosing;
	private TypeElement element;
	private List<? extends TypeMirror> typeArguments;

	private volatile transient DeclaredType capturedType;

	public SimpleDeclaredType(IncrementalElementsTypesBase elemTypes, TypeMirror enclosing, TypeElement element,
			List<? extends TypeMirror> typeArguments) {
		super(elemTypes);
		this.enclosing = enclosing;
		this.element = element;
		this.typeArguments = typeArguments;
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
		return element;
	}

	@Override
	public TypeMirror getEnclosingType() {
		return enclosing;
	}

	@Override
	public List<? extends TypeMirror> getTypeArguments() {
		return typeArguments;
	}

	@Override
	public DeclaredType getCapturedType() {
		DeclaredType thiscapturedtype = capturedType;
		if (thiscapturedtype == null) {
			thiscapturedtype = elemTypes.captureImpl(this);
			if (ARFU_capturedType.compareAndSet(this, null, thiscapturedtype)) {
				return thiscapturedtype;
			}
		}
		return this.capturedType;
	}

	@Override
	public String toString() {
		return (getEnclosingType().getKind() == TypeKind.NONE ? "" : getEnclosingType() + ".")
				+ element.getQualifiedName()
				+ (typeArguments.isEmpty() ? "" : "<" + StringUtils.toStringJoin(", ", typeArguments) + ">");
	}
}
