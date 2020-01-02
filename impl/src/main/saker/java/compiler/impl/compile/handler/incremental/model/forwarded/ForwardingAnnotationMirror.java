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
package saker.java.compiler.impl.compile.handler.incremental.model.forwarded;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.DeclaredType;

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;

public class ForwardingAnnotationMirror implements ForwardingObject<AnnotationMirror>, AnnotationMirror {
	private static final AtomicReferenceFieldUpdater<ForwardingAnnotationMirror, DeclaredType> ARFU_annotationType = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingAnnotationMirror.class, DeclaredType.class, "annotationType");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<ForwardingAnnotationMirror, Map> ARFU_elementValues = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingAnnotationMirror.class, Map.class, "elementValues");

	protected IncrementalElementsTypesBase elemTypes;
	protected AnnotationMirror subject;

	private volatile transient DeclaredType annotationType;
	private volatile transient Map<ExecutableElement, AnnotationValue> elementValues;

	public ForwardingAnnotationMirror(IncrementalElementsTypesBase elemTypes, AnnotationMirror subject) {
		this.elemTypes = elemTypes;
		this.subject = subject;
	}

	@Override
	public AnnotationMirror getForwardedSubject() {
		return subject;
	}

	@Override
	public DeclaredType getAnnotationType() {
		DeclaredType thisannotationtype = this.annotationType;
		if (thisannotationtype != null) {
			return thisannotationtype;
		}
		thisannotationtype = elemTypes.forwardType(subject::getAnnotationType);
		if (ARFU_annotationType.compareAndSet(this, null, thisannotationtype)) {
			return thisannotationtype;
		}
		return this.annotationType;
	}

	@Override
	public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValues() {
		Map<ExecutableElement, AnnotationValue> thiselementvalues = this.elementValues;
		if (thiselementvalues != null) {
			return thiselementvalues;
		}
		Map<? extends ExecutableElement, ? extends AnnotationValue> vals = elemTypes.javac(subject::getElementValues);
		LinkedHashMap<ExecutableElement, AnnotationValue> nelementvalues = new LinkedHashMap<>();
		for (Entry<? extends ExecutableElement, ? extends AnnotationValue> e : vals.entrySet()) {
			nelementvalues.put(elemTypes.forwardElement(e.getKey()), elemTypes.forward(e.getValue()));
		}
		thiselementvalues = ImmutableUtils.unmodifiableMap(nelementvalues);
		if (ARFU_elementValues.compareAndSet(this, null, thiselementvalues)) {
			return thiselementvalues;
		}
		return this.elementValues;
	}

	@Override
	public String toString() {
		return elemTypes.javac(subject::toString);
	}

}
