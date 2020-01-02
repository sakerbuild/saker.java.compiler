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

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

import saker.java.compiler.impl.compile.handler.incremental.model.CommonDeclaredType;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;

public class ErasedDeclaredType implements CommonDeclaredType {
	private static final AtomicReferenceFieldUpdater<ErasedDeclaredType, TypeMirror> ARFU_enclosingType = AtomicReferenceFieldUpdater
			.newUpdater(ErasedDeclaredType.class, TypeMirror.class, "enclosingType");

	private IncrementalElementsTypesBase elemTypes;
	private DeclaredType type;

	private volatile transient TypeMirror enclosingType;

	public ErasedDeclaredType(IncrementalElementsTypesBase elemTypes, DeclaredType type) {
		this.elemTypes = elemTypes;
		this.type = type;
	}

	@Override
	public TypeKind getKind() {
		return type.getKind();
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		return v.visitDeclared(this, p);
	}

	@Override
	public List<? extends AnnotationMirror> getAnnotationMirrors() {
		return type.getAnnotationMirrors();
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
		return type.getAnnotation(annotationType);
	}

	@Override
	public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
		return type.getAnnotationsByType(annotationType);
	}

	@Override
	public Element asElement() {
		return type.asElement();
	}

	@Override
	public TypeMirror getEnclosingType() {
		TypeMirror thisenclosingtype = this.enclosingType;
		if (thisenclosingtype == null) {
			thisenclosingtype = elemTypes.erasure(type.getEnclosingType());
			if (ARFU_enclosingType.compareAndSet(this, null, thisenclosingtype)) {
				return thisenclosingtype;
			}
		}
		return this.enclosingType;
	}

	@Override
	public List<? extends TypeMirror> getTypeArguments() {
		return Collections.emptyList();
	}

	@Override
	public DeclaredType getCapturedType() {
		//needs no capture as there are no type parameters present.
		return this;
	}

	@Override
	public DeclaredType getErasedType() {
		return this;
	}

	@Override
	public String toString() {
		return (getEnclosingType().getKind() == TypeKind.NONE ? "" : getEnclosingType() + ".") + type;
	}
}
