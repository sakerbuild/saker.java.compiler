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
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;

import saker.build.thirdparty.saker.util.StringUtils;
import saker.java.compiler.impl.compile.handler.incremental.model.CommonTypeMirror;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;

public class ErasedExecutableType implements ExecutableType, CommonTypeMirror {
	private static final AtomicReferenceFieldUpdater<ErasedExecutableType, TypeMirror> ARFU_returnType = AtomicReferenceFieldUpdater
			.newUpdater(ErasedExecutableType.class, TypeMirror.class, "returnType");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<ErasedExecutableType, List> ARFU_parameterTypes = AtomicReferenceFieldUpdater
			.newUpdater(ErasedExecutableType.class, List.class, "parameterTypes");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<ErasedExecutableType, List> ARFU_thrownTypes = AtomicReferenceFieldUpdater
			.newUpdater(ErasedExecutableType.class, List.class, "thrownTypes");

	private IncrementalElementsTypesBase elemTypes;
	private ExecutableType type;

	private volatile transient TypeMirror returnType;
	private volatile transient List<TypeMirror> parameterTypes;
	private volatile transient List<? extends TypeMirror> thrownTypes;

	public ErasedExecutableType(IncrementalElementsTypesBase elemTypes, ExecutableType type) {
		this.elemTypes = elemTypes;
		this.type = type;
	}

	@Override
	public TypeKind getKind() {
		return type.getKind();
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		return v.visitExecutable(this, p);
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
	public List<? extends TypeVariable> getTypeVariables() {
		//erasured type does not contain type variables
		return Collections.emptyList();
	}

	@Override
	public TypeMirror getReturnType() {
		TypeMirror thisreturntype = this.returnType;
		if (thisreturntype == null) {
			thisreturntype = elemTypes.erasure(type.getReturnType());
			if (ARFU_returnType.compareAndSet(this, null, thisreturntype)) {
				return thisreturntype;
			}
		}
		return this.returnType;
	}

	@Override
	public List<? extends TypeMirror> getParameterTypes() {
		List<? extends TypeMirror> thisparametertypes = this.parameterTypes;
		if (thisparametertypes == null) {
			thisparametertypes = elemTypes.erasure(type.getParameterTypes());
			if (ARFU_parameterTypes.compareAndSet(this, null, thisparametertypes)) {
				return thisparametertypes;
			}
		}
		return this.parameterTypes;
	}

	@Override
	public TypeMirror getReceiverType() {
		return type.getReceiverType();
	}

	@Override
	public List<? extends TypeMirror> getThrownTypes() {
		List<? extends TypeMirror> thisthrowntypes = this.thrownTypes;
		if (thisthrowntypes == null) {
			thisthrowntypes = elemTypes.erasure(type.getThrownTypes());
			if (ARFU_thrownTypes.compareAndSet(this, null, thisthrowntypes)) {
				return thisthrowntypes;
			}
		}
		return this.thrownTypes;
	}

	@Override
	public ExecutableType getErasedType() {
		return this;
	}

	@Override
	public String toString() {
		return getReturnType() + " (" + StringUtils.toStringJoin(", ", getParameterTypes()) + ")";
	}

}
