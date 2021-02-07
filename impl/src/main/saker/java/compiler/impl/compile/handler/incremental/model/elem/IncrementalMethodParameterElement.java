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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import saker.java.compiler.impl.compat.ElementKindCompatUtils;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalName;
import saker.java.compiler.impl.signature.element.MethodParameterSignature;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

public class IncrementalMethodParameterElement extends IncrementalElement<MethodParameterSignature>
		implements VariableElement {
	private static final AtomicReferenceFieldUpdater<IncrementalMethodParameterElement, TypeMirror> ARFU_asType = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalMethodParameterElement.class, TypeMirror.class, "asType");

	private IncrementalExecutableElement enclosingElement;

	private volatile transient TypeMirror asType;

	public IncrementalMethodParameterElement(IncrementalElementsTypesBase elemTypes, MethodParameterSignature signature,
			IncrementalExecutableElement enclosingElement) {
		super(elemTypes, signature);
		this.enclosingElement = enclosingElement;
		elementTypes = IncrementalElementsTypes.ELEMENT_TYPE_PARAMETER;
	}

	@Override
	public void invalidate() {
		super.invalidate();
		//fine to use direct assignment as this will not be called from multi-threaded code
		this.asType = null;
	}

	@Override
	public TypeMirror asType() {
		TypeMirror thisastype = this.asType;
		if (thisastype == null) {
			thisastype = elemTypes.getTypeMirror(signature.getTypeSignature(), enclosingElement);
			if (ARFU_asType.compareAndSet(this, null, thisastype)) {
				return thisastype;
			}
		}
		return this.asType;
	}

	@Override
	public byte getKindIndex() {
		return ElementKindCompatUtils.ELEMENTKIND_INDEX_PARAMETER;
	}

	@Override
	public ElementKind getKind() {
		return ElementKind.PARAMETER;
	}

	@Override
	public Set<Modifier> getModifiers() {
		return signature.getModifiers();
	}

	@Override
	public List<? extends Element> getEnclosedElements() {
		return Collections.emptyList();
	}

	@Override
	public <R, P> R accept(ElementVisitor<R, P> v, P p) {
		return v.visitVariable(this, p);
	}

	@Override
	public Object getConstantValue() {
		return null;
	}

	@Override
	public Name getSimpleName() {
		return new IncrementalName(signature.getSimpleName());
	}

	@Override
	public IncrementalExecutableElement getEnclosingElement() {
		return enclosingElement;
	}

}
