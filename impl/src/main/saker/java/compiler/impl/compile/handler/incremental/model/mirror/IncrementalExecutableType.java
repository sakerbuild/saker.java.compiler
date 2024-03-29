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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.compat.ImmutableElementTypeSet;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;
import saker.java.compiler.impl.signature.element.MethodSignature;
import saker.java.compiler.impl.signature.type.TypeParameterSignature;

public class IncrementalExecutableType extends IncrementalTypeMirror<MethodSignature> implements ExecutableType {
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<IncrementalExecutableType, List> ARFU_typeVariables = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalExecutableType.class, List.class, "typeVariables");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<IncrementalExecutableType, List> ARFU_parameterTypes = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalExecutableType.class, List.class, "parameterTypes");

	private ExecutableElement executableElement;

	private volatile transient List<TypeVariable> typeVariables;
	private volatile transient List<TypeMirror> parameterTypes;

	public IncrementalExecutableType(MethodSignature signature, IncrementalElementsTypesBase elemTypes,
			ExecutableElement executableElement) {
		super(elemTypes, signature);
		this.executableElement = executableElement;
		//no TYPE_USE annotations on an executable, leave them to the return type
		elementTypes = ImmutableElementTypeSet.empty();
	}

	@Override
	protected Element getEnclosingResolutionElement() {
		return executableElement;
	}

	@Override
	public void invalidate() {
		super.invalidate();
		//fine to use direct assignment as this will not be called from multi-threaded code
		this.typeVariables = null;
		this.parameterTypes = null;
	}

	public String getSimpleName() {
		return signature.getSimpleName();
	}

	@Override
	public TypeKind getKind() {
		return TypeKind.EXECUTABLE;
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		return v.visitExecutable(this, p);
	}

	@Override
	public List<? extends TypeVariable> getTypeVariables() {
		List<TypeVariable> thistypevariables = this.typeVariables;
		if (thistypevariables != null) {
			return thistypevariables;
		}
		List<? extends TypeParameterSignature> params = signature.getTypeParameters();
		if (ObjectUtils.isNullOrEmpty(params)) {
			thistypevariables = Collections.emptyList();
		} else {
			List<? extends TypeParameterElement> typeparams = executableElement.getTypeParameters();
			if (typeparams.size() != params.size()) {
				throw new IllegalStateException("Type variable counts doesnt match: " + this + " - " + executableElement
						+ " - " + params.size() + " - " + typeparams.size());
			}
			TypeVariable[] tvars = new TypeVariable[typeparams.size()];
			Iterator<? extends TypeParameterSignature> pit = params.iterator();
			Iterator<? extends TypeParameterElement> tpit = typeparams.iterator();
			int i = 0;
			while (pit.hasNext()) {
				TypeParameterSignature p = pit.next();
				TypeParameterElement ntpe = tpit.next();
				if ("?".equals(p.getVarName())) {
					throw new IllegalArgumentException(
							"Wildcard type parameter found for type parameter. " + signature);
				}
				tvars[i++] = (TypeVariable) ntpe.asType();
			}
			thistypevariables = ImmutableUtils.unmodifiableArrayList(tvars);
		}
		if (ARFU_typeVariables.compareAndSet(this, null, thistypevariables)) {
			return thistypevariables;
		}
		return this.typeVariables;
	}

	@Override
	public TypeMirror getReturnType() {
		return executableElement.getReturnType();
	}

	@Override
	public List<? extends TypeMirror> getParameterTypes() {
		List<TypeMirror> thisparametertypes = this.parameterTypes;
		if (thisparametertypes != null) {
			return thisparametertypes;
		}
		List<? extends VariableElement> params = executableElement.getParameters();
		thisparametertypes = JavaTaskUtils.cloneImmutableList(params, VariableElement::asType);
		if (ARFU_parameterTypes.compareAndSet(this, null, thisparametertypes)) {
			return thisparametertypes;
		}
		return this.parameterTypes;
	}

	@Override
	public TypeMirror getReceiverType() {
		return executableElement.getReceiverType();
	}

	@Override
	public List<? extends TypeMirror> getThrownTypes() {
		return executableElement.getThrownTypes();
	}

}
