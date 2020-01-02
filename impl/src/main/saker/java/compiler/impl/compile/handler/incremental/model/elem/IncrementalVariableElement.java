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

import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalName;
import saker.java.compiler.impl.signature.element.FieldSignature;
import saker.java.compiler.impl.signature.value.ConstantValueResolver;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

public class IncrementalVariableElement extends IncrementalElement<FieldSignature>
		implements VariableElement, DocumentedIncrementalElement<FieldSignature> {
	private static final AtomicReferenceFieldUpdater<IncrementalVariableElement, TypeMirror> ARFU_asType = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalVariableElement.class, TypeMirror.class, "asType");

	private ElementKind kind;
	private IncrementalElement<?> enclosingElement;

	private volatile transient TypeMirror asType;

	public IncrementalVariableElement(IncrementalElementsTypesBase elemTypes, FieldSignature signature,
			ElementKind kind, IncrementalElement<?> enclosingElement) {
		super(elemTypes, signature);
		this.kind = kind;
		this.enclosingElement = enclosingElement;

		switch (kind) {
			case PARAMETER: {
				elementTypes = IncrementalElementsTypes.ELEMENT_TYPE_PARAMETER;
				break;
			}
			case RESOURCE_VARIABLE:
			case LOCAL_VARIABLE: {
				elementTypes = IncrementalElementsTypes.ELEMENT_TYPE_LOCAL_VARIABLE;
				break;
			}
			case FIELD: {
				elementTypes = IncrementalElementsTypes.ELEMENT_TYPE_FIELD;
				break;
			}
			case ENUM_CONSTANT: {
				//not gonna happen, but just in case
				elementTypes = IncrementalElementsTypes.ELEMENT_TYPE_FIELD;
				break;
			}
			default: {
				throw new AssertionError("Unrecognized element kind for VariableElement: " + kind);
			}
		}
	}

	@Override
	public TypeMirror asType() {
		TypeMirror thisastype = this.asType;
		if (thisastype != null) {
			return thisastype;
		}
		thisastype = elemTypes.getTypeMirror(signature.getTypeSignature(), this);
		if (ARFU_asType.compareAndSet(this, null, thisastype)) {
			return thisastype;
		}
		return this.asType;
	}

	@Override
	public ElementKind getKind() {
		return kind;
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
		ConstantValueResolver constval = signature.getConstantValue();
		if (constval != null) {
			return constval.resolve(elemTypes, this);
		}
		return null;
	}

	@Override
	public Name getSimpleName() {
		return new IncrementalName(signature.getSimpleName());
	}

	@Override
	public IncrementalElement<?> getEnclosingElement() {
		return enclosingElement;
	}

	@Override
	public String getDocComment() {
		return signature.getDocComment();
	}
}
