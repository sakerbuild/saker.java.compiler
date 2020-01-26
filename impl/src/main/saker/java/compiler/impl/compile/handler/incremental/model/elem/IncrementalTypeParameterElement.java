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
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.compat.KindCompatUtils;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalName;
import saker.java.compiler.impl.compile.handler.incremental.model.mirror.IncrementalTypeVariable;
import saker.java.compiler.impl.compile.signature.type.impl.TypeVariableTypeSignatureImpl;
import saker.java.compiler.impl.signature.type.IntersectionTypeSignature;
import saker.java.compiler.impl.signature.type.TypeParameterTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.util.ImmutableModifierSet;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

public class IncrementalTypeParameterElement extends IncrementalElement<TypeParameterTypeSignature>
		implements TypeParameterElement {
	private static final AtomicReferenceFieldUpdater<IncrementalTypeParameterElement, TypeMirror> ARFU_asType = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalTypeParameterElement.class, TypeMirror.class, "asType");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<IncrementalTypeParameterElement, List> ARFU_bounds = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalTypeParameterElement.class, List.class, "bounds");

	private IncrementalElement<?> genericElement;

	private volatile transient TypeMirror asType;
	private volatile transient List<TypeMirror> bounds;

	public IncrementalTypeParameterElement(TypeParameterTypeSignature signature, IncrementalElementsTypesBase elemTypes,
			IncrementalElement<?> genericElement) {
		super(elemTypes, signature);
		this.genericElement = genericElement;
		elementTypes = IncrementalElementsTypes.ELEMENT_TYPE_TYPE_PARAMETER_TYPE_USE;
	}

	@Override
	public void setSignature(TypeParameterTypeSignature signature) {
		super.setSignature(signature);
		this.asType = null;
		this.bounds = null;
	}

	@Override
	public TypeMirror asType() {
		TypeMirror thisastype = this.asType;
		if (thisastype != null) {
			return thisastype;
		}
		//TODO check this if it includes the proper annotations and stuff
		IncrementalTypeVariable itv = new IncrementalTypeVariable(elemTypes,
				TypeVariableTypeSignatureImpl.create(signature.getVarName()), this);
		//XXX is this valid? the typemirror of a parameter element should not contain any annotation put on the type parameter itself
		itv.setElementTypes(Collections.emptySet());
		thisastype = itv;
		if (ARFU_asType.compareAndSet(this, null, thisastype)) {
			return thisastype;
		}
		return this.asType;
	}

	@Override
	public byte getKindIndex() {
		return KindCompatUtils.ELEMENTKIND_INDEX_TYPE_PARAMETER;
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
		return new IncrementalName(signature.getVarName());
	}

	public boolean simpleNameEquals(String name) {
		return name.equals(signature.getVarName());
	}

	@Override
	public List<? extends Element> getEnclosedElements() {
		return Collections.emptyList();
	}

	@Override
	public <R, P> R accept(ElementVisitor<R, P> v, P p) {
		return v.visitTypeParameter(this, p);
	}

	@Override
	public Element getGenericElement() {
		return genericElement;
	}

	@Override
	public IncrementalElement<?> getEnclosingElement() {
		return genericElement;
	}

	@Override
	public List<? extends TypeMirror> getBounds() {
		List<TypeMirror> thisbounds = this.bounds;
		if (thisbounds != null) {
			return thisbounds;
		}
		TypeSignature bounds = signature.getUpperBounds();
		if (bounds instanceof IntersectionTypeSignature) {
			IntersectionTypeSignature its = (IntersectionTypeSignature) bounds;
			List<? extends TypeSignature> itsbounds = its.getBounds();
			thisbounds = JavaTaskUtils.cloneImmutableList(itsbounds, bound -> elemTypes.getTypeMirror(bound, this));
		} else if (bounds == null) {
			thisbounds = ImmutableUtils.singletonList(elemTypes.getJavaLangObjectTypeMirror());
		} else {
			thisbounds = ImmutableUtils.singletonList(elemTypes.getTypeMirror(bounds, this));
		}
		if (ARFU_bounds.compareAndSet(this, null, thisbounds)) {
			return thisbounds;
		}
		return this.bounds;
	}

}
