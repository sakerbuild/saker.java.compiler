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

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.type.TypeMirror;

import saker.build.thirdparty.saker.util.ReflectUtils;
import saker.java.compiler.impl.compile.handler.incremental.model.CommonElement;
import saker.java.compiler.impl.util.ImmutableModifierSet;

public class CaptureEnclosingElement implements CommonElement {
	private Element enclosingElement;
	private TypeMirror asType;
	private Name name;

	public CaptureEnclosingElement(Element enclosingElement, TypeMirror nonetype, Name name) {
		this.enclosingElement = enclosingElement;
		this.asType = nonetype;
		this.name = name;
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
	public TypeMirror asType() {
		return asType;
	}

	@Override
	public ElementKind getKind() {
		return ElementKind.OTHER;
	}

	@Override
	public Set<Modifier> getModifiers() {
		return ImmutableModifierSet.empty();
	}

	@Override
	public Name getSimpleName() {
		return name;
	}

	@Override
	public Element getEnclosingElement() {
		return enclosingElement;
	}

	@Override
	public List<? extends Element> getEnclosedElements() {
		return Collections.emptyList();
	}

	@Override
	public <R, P> R accept(ElementVisitor<R, P> v, P p) {
		return v.visitUnknown(this, p);
	}

	@Override
	public boolean isDeprecated() {
		return false;
	}

	@Override
	public String toString() {
		return "";
	}

//	
//	hashCode and equals inherited from Object
//	
}
