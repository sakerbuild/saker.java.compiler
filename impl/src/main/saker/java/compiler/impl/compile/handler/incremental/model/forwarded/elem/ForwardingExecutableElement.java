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
package saker.java.compiler.impl.compile.handler.incremental.model.forwarded.elem;

import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import saker.java.compiler.impl.compile.handler.incremental.model.CommonExecutableElement;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;

public class ForwardingExecutableElement extends ForwardingElementBase<ExecutableElement>
		implements CommonExecutableElement {
	private static final AtomicReferenceFieldUpdater<ForwardingExecutableElement, TypeMirror> ARFU_returnType = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingExecutableElement.class, TypeMirror.class, "returnType");
	private static final AtomicReferenceFieldUpdater<ForwardingExecutableElement, TypeMirror> ARFU_receiverType = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingExecutableElement.class, TypeMirror.class, "receiverType");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<ForwardingExecutableElement, List> ARFU_typeParameters = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingExecutableElement.class, List.class, "typeParameters");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<ForwardingExecutableElement, List> ARFU_parameters = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingExecutableElement.class, List.class, "parameters");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<ForwardingExecutableElement, List> ARFU_thrownTypes = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingExecutableElement.class, List.class, "thrownTypes");

	private static final AtomicReferenceFieldUpdater<ForwardingExecutableElement, Boolean> ARFU_varags = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingExecutableElement.class, Boolean.class, "varags");
	private static final AtomicReferenceFieldUpdater<ForwardingExecutableElement, Boolean> ARFU_isDefault = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingExecutableElement.class, Boolean.class, "isDefault");
	private static final AtomicReferenceFieldUpdater<ForwardingExecutableElement, AnnotationValue> ARFU_defaultValue = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingExecutableElement.class, AnnotationValue.class, "defaultValue");
	private static final AtomicReferenceFieldUpdater<ForwardingExecutableElement, Boolean> ARFU_bridge = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingExecutableElement.class, Boolean.class, "bridge");

	private volatile transient TypeMirror returnType;
	private volatile transient TypeMirror receiverType;
	private volatile transient List<? extends TypeParameterElement> typeParameters;
	private volatile transient List<? extends VariableElement> parameters;
	private volatile transient List<? extends TypeMirror> thrownTypes;
	private volatile transient Boolean varags;
	private volatile transient Boolean isDefault;
	private volatile transient AnnotationValue defaultValue;
	private volatile transient Boolean bridge;

	public ForwardingExecutableElement(IncrementalElementsTypesBase elemTypes, ExecutableElement subject) {
		super(elemTypes, subject);
	}

	@Override
	public List<? extends TypeParameterElement> getTypeParameters() {
		List<? extends TypeParameterElement> thistypeparameters = this.typeParameters;
		if (thistypeparameters != null) {
			return thistypeparameters;
		}
		thistypeparameters = elemTypes.forwardElements(subject::getTypeParameters);
		if (ARFU_typeParameters.compareAndSet(this, null, thistypeparameters)) {
			return thistypeparameters;
		}
		return this.typeParameters;
	}

	@Override
	public TypeMirror getReturnType() {
		TypeMirror thisreturntype = this.returnType;
		if (thisreturntype != null) {
			return thisreturntype;
		}
		thisreturntype = elemTypes.forwardType(subject::getReturnType);
		if (ARFU_returnType.compareAndSet(this, null, thisreturntype)) {
			return thisreturntype;
		}
		return this.returnType;
	}

	@Override
	public List<? extends VariableElement> getParameters() {
		List<? extends VariableElement> thisparameters = this.parameters;
		if (thisparameters != null) {
			return thisparameters;
		}
		thisparameters = elemTypes.forwardElements(subject::getParameters);
		if (ARFU_parameters.compareAndSet(this, null, thisparameters)) {
			return thisparameters;
		}
		return this.parameters;
	}

	@Override
	public TypeMirror getReceiverType() {
		TypeMirror thisreceivertype = this.receiverType;
		if (thisreceivertype != null) {
			return thisreceivertype;
		}
		thisreceivertype = elemTypes.forwardTypeOrNone(subject::getReceiverType);
		if (ARFU_receiverType.compareAndSet(this, null, thisreceivertype)) {
			return thisreceivertype;
		}
		return this.receiverType;
	}

	@Override
	public boolean isVarArgs() {
		Boolean thisvarargs = this.varags;
		if (thisvarargs != null) {
			return thisvarargs;
		}
		thisvarargs = elemTypes.javac(subject::isVarArgs);
		if (ARFU_varags.compareAndSet(this, null, thisvarargs)) {
			return thisvarargs;
		}
		return this.varags;
	}

	@Override
	public boolean isDefault() {
		Boolean thisisdefault = this.isDefault;
		if (thisisdefault != null) {
			return thisisdefault;
		}
		thisisdefault = elemTypes.javac(subject::isDefault);
		if (ARFU_isDefault.compareAndSet(this, null, thisisdefault)) {
			return thisisdefault;
		}
		return this.isDefault;
	}

	@Override
	public List<? extends TypeMirror> getThrownTypes() {
		List<? extends TypeMirror> thisthrowntypes = this.thrownTypes;
		if (thisthrowntypes != null) {
			return thisthrowntypes;
		}
		thisthrowntypes = elemTypes.forwardTypes(subject::getThrownTypes);
		if (ARFU_thrownTypes.compareAndSet(this, null, thisthrowntypes)) {
			return thisthrowntypes;
		}
		return this.thrownTypes;
	}

	@Override
	public AnnotationValue getDefaultValue() {
		AnnotationValue thisdefaultvalue = this.defaultValue;
		if (thisdefaultvalue != null) {
			return thisdefaultvalue;
		}
		thisdefaultvalue = elemTypes.forward(elemTypes.javac(subject::getDefaultValue));
		if (ARFU_defaultValue.compareAndSet(this, null, thisdefaultvalue)) {
			return thisdefaultvalue;
		}
		return this.defaultValue;
	}

	@Override
	public boolean isBridge() {
		Boolean thisvarargs = this.bridge;
		if (thisvarargs != null) {
			return thisvarargs;
		}
		thisvarargs = elemTypes.isJavacElementBridge(subject);
		if (ARFU_bridge.compareAndSet(this, null, thisvarargs)) {
			return thisvarargs;
		}
		return this.bridge;
	}

}
