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
package saker.java.compiler.impl.compile.handler.incremental.model.elem;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;

import saker.build.thirdparty.saker.util.ReflectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.java.compiler.impl.compile.handler.incremental.model.CommonElement;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;
import saker.java.compiler.impl.compile.handler.incremental.model.mirror.CapturedTypeVariable;
import saker.java.compiler.impl.util.ImmutableModifierSet;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

public class CapturedWildcardTypeParameterElement implements TypeParameterElement, CommonElement {
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<CapturedWildcardTypeParameterElement, List> ARFU_bounds = AtomicReferenceFieldUpdater
			.newUpdater(CapturedWildcardTypeParameterElement.class, List.class, "bounds");

	private IncrementalElementsTypesBase elemTypes;
	private CapturedTypeVariable asType;

	private volatile transient List<? extends TypeMirror> bounds;

	public CapturedWildcardTypeParameterElement(IncrementalElementsTypesBase elemTypes,
			CapturedTypeVariable capturedTypeVariable) {
		this.elemTypes = elemTypes;
		this.asType = capturedTypeVariable;
	}

	@Override
	public CapturedTypeVariable asType() {
		return asType;
	}

	@Override
	public ElementKind getKind() {
		return ElementKind.TYPE_PARAMETER;
	}

	@Override
	public Set<Modifier> getModifiers() {
		return ImmutableModifierSet.empty();
	}

	@Override
	public Name getSimpleName() {
		return IncrementalElementsTypes.getCapturedWildcardName();
	}

	@Override
	public List<? extends Element> getEnclosedElements() {
		return Collections.emptyList();
	}

	@Override
	public List<? extends AnnotationMirror> getAnnotationMirrors() {
		return Collections.emptyList();
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
		return null;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
		return (A[]) ReflectUtils.createEmptyArray(annotationType);
	}

	@Override
	public <R, P> R accept(ElementVisitor<R, P> v, P p) {
		return v.visitTypeParameter(this, p);
	}

	@Override
	public Element getGenericElement() {
		return getEnclosingElement();
	}

	@Override
	public List<? extends TypeMirror> getBounds() {
		List<? extends TypeMirror> thisbounds = bounds;
		if (thisbounds == null) {
			thisbounds = elemTypes.createCaptureWildcardElementBounds(asType.getUpperBound());
			if (ARFU_bounds.compareAndSet(this, null, thisbounds)) {
				return thisbounds;
			}
		}
		return this.bounds;
	}

	@Override
	public Element getEnclosingElement() {
		return elemTypes.getCapturesEnclosingElement();
	}

	@Override
	public String toString() {
		return getSimpleName()
				+ (getBounds().isEmpty() ? "" : " extends " + StringUtils.toStringJoin(" & ", getBounds()));
	}

	@Override
	public boolean isDeprecated() {
		return false;
	}

//	
//	hashCode and equals inherited from Object
//	
}
