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

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.compile.handler.incremental.model.CommonExecutableElement;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalAnnotatedConstruct;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalName;
import saker.java.compiler.impl.compile.handler.incremental.model.mirror.IncrementalExecutableType;
import saker.java.compiler.impl.signature.element.AnnotationSignature.Value;
import saker.java.compiler.impl.signature.element.MethodParameterSignature;
import saker.java.compiler.impl.signature.element.MethodSignature;
import saker.java.compiler.impl.signature.type.TypeParameterTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

public class IncrementalExecutableElement extends IncrementalElement<MethodSignature>
		implements CommonExecutableElement, DocumentedIncrementalElement<MethodSignature> {
	private static final AtomicReferenceFieldUpdater<IncrementalExecutableElement, TypeMirror> ARFU_returnType = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalExecutableElement.class, TypeMirror.class, "returnType");
	private static final AtomicReferenceFieldUpdater<IncrementalExecutableElement, TypeMirror> ARFU_receiverType = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalExecutableElement.class, TypeMirror.class, "receiverType");
	private static final AtomicReferenceFieldUpdater<IncrementalExecutableElement, TypeMirror> ARFU_asType = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalExecutableElement.class, TypeMirror.class, "asType");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<IncrementalExecutableElement, List> ARFU_typeParameters = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalExecutableElement.class, List.class, "typeParameters");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<IncrementalExecutableElement, List> ARFU_parameters = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalExecutableElement.class, List.class, "parameters");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<IncrementalExecutableElement, List> ARFU_thrownTypes = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalExecutableElement.class, List.class, "thrownTypes");

	private IncrementalElement<?> enclosingElement;

	private volatile transient TypeMirror returnType;
	private volatile transient TypeMirror receiverType;
	private volatile transient TypeMirror asType;
	private volatile transient List<TypeParameterElement> typeParameters;
	private volatile transient List<IncrementalMethodParameterElement> parameters;
	private volatile transient List<TypeMirror> thrownTypes;

	public IncrementalExecutableElement(MethodSignature signature, IncrementalElement<?> enclosingElement,
			IncrementalElementsTypesBase elemTypes) {
		super(elemTypes, signature);
		this.enclosingElement = enclosingElement;
		if (signature.getKind() == ElementKind.CONSTRUCTOR) {
			elementTypes = IncrementalElementsTypes.ELEMENT_TYPE_CONSTRUCTOR;
		} else {
			elementTypes = IncrementalElementsTypes.ELEMENT_TYPE_METHOD;
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		//fine to use direct assignment as this will not be called from multi-threaded code
		this.typeParameters = null;
		this.parameters = null;
		this.asType = null;
		this.receiverType = null;
		this.thrownTypes = null;
		this.receiverType = null;
	}

	@Override
	public TypeMirror asType() {
		TypeMirror thisastype = this.asType;
		if (thisastype == null) {
			thisastype = new IncrementalExecutableType(signature, elemTypes, this);
			if (ARFU_asType.compareAndSet(this, null, thisastype)) {
				return thisastype;
			}
		}
		return this.asType;
	}

	@Override
	public ElementKind getKind() {
		return signature.getKind();
	}

	@Override
	public Set<Modifier> getModifiers() {
		return signature.getModifiers();
	}

	@Override
	public IncrementalElement<?> getEnclosingElement() {
		return enclosingElement;
	}

	@Override
	public List<? extends Element> getEnclosedElements() {
		return Collections.emptyList();
	}

	@Override
	public List<? extends TypeParameterElement> getTypeParameters() {
		List<TypeParameterElement> thistypeparameters = this.typeParameters;
		if (thistypeparameters == null) {
			List<? extends TypeParameterTypeSignature> params = signature.getTypeParameters();
			thistypeparameters = JavaTaskUtils.cloneImmutableList(params,
					p -> new IncrementalTypeParameterElement(p, elemTypes, this));
			if (ARFU_typeParameters.compareAndSet(this, null, thistypeparameters)) {
				return thistypeparameters;
			}
		}
		return this.typeParameters;
	}

	@Override
	public TypeMirror getReturnType() {
		TypeMirror thisreturntype = this.returnType;
		if (thisreturntype != null) {
			return thisreturntype;
		}
		if (getKind() != ElementKind.METHOD) {
			thisreturntype = IncrementalElementsTypes.getVoidTypeKind();
		} else {
			thisreturntype = elemTypes.getTypeMirror(signature.getReturnType(), this);
			if (thisreturntype instanceof IncrementalAnnotatedConstruct) {
				IncrementalAnnotatedConstruct iac = (IncrementalAnnotatedConstruct) thisreturntype;
				//keep the TYPE_USE annotations for the return type
				iac.setElementTypes(IncrementalElementsTypes.ELEMENT_TYPE_TYPE_USE);
			}
		}
		if (ARFU_returnType.compareAndSet(this, null, thisreturntype)) {
			return thisreturntype;
		}
		return this.returnType;
	}

	@Override
	public TypeMirror getReceiverType() {
		TypeMirror thisreceivertype = this.receiverType;
		if (thisreceivertype != null) {
			return thisreceivertype;
		}
		TypeSignature sigreceiverparam = signature.getReceiverParameter();
		if (sigreceiverparam == null) {
			thisreceivertype = IncrementalElementsTypes.getNoneTypeKind();
		} else {
			thisreceivertype = elemTypes.getTypeMirror(sigreceiverparam, this);
		}
		if (ARFU_receiverType.compareAndSet(this, null, thisreceivertype)) {
			return thisreceivertype;
		}
		return this.receiverType;
	}

	@Override
	public List<? extends VariableElement> getParameters() {
		List<IncrementalMethodParameterElement> thisparameters = this.parameters;
		if (thisparameters != null) {
			return thisparameters;
		}
		List<? extends MethodParameterSignature> params = signature.getParameters();
		thisparameters = JavaTaskUtils.cloneImmutableList(params,
				p -> new IncrementalMethodParameterElement(elemTypes, p, this));
		if (ARFU_parameters.compareAndSet(this, null, thisparameters)) {
			return thisparameters;
		}
		return this.parameters;
	}

	@Override
	public boolean isVarArgs() {
		return signature.isVarArg();
	}

	@Override
	public boolean isDefault() {
		return getModifiers().contains(Modifier.DEFAULT);
	}

	@Override
	public List<? extends TypeMirror> getThrownTypes() {
		List<TypeMirror> thisthrowntypes = this.thrownTypes;
		if (thisthrowntypes != null) {
			return thisthrowntypes;
		}
		List<? extends TypeSignature> thrown = signature.getThrowingTypes();
		thisthrowntypes = JavaTaskUtils.cloneImmutableList(thrown, t -> elemTypes.getTypeMirror(t, this));
		if (ARFU_thrownTypes.compareAndSet(this, null, thisthrowntypes)) {
			return thisthrowntypes;
		}
		return this.thrownTypes;
	}

	@Override
	public AnnotationValue getDefaultValue() {
		Value defval = signature.getDefaultValue();
		if (defval != null) {
			return new IncrementalAnnotationValue(elemTypes, defval, getReturnType(), this);
		}
		return null;
	}

	@Override
	public Name getSimpleName() {
		return new IncrementalName(signature.getSimpleName());
	}

	@Override
	public <R, P> R accept(ElementVisitor<R, P> v, P p) {
		return v.visitExecutable(this, p);
	}

	@Override
	public boolean isBridge() {
		return false;
	}

	@Override
	public String getDocComment() {
		return signature.getDocComment();
	}
}
