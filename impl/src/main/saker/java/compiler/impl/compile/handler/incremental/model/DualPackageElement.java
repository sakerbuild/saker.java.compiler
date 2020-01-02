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
package saker.java.compiler.impl.compile.handler.incremental.model;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.TypeMirror;

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.java.compiler.impl.compile.handler.incremental.model.elem.DocumentedElement;
import saker.java.compiler.impl.compile.handler.incremental.model.forwarded.ForwardingAnnotatedConstruct;
import saker.java.compiler.impl.compile.handler.incremental.model.mirror.SimplePackageType;
import saker.java.compiler.impl.signature.element.PackageSignature;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

public class DualPackageElement implements PackageElement, CommonElement, DocumentedElement<PackageSignature> {
	private static final AtomicReferenceFieldUpdater<DualPackageElement, TypeMirror> ARFU_asType = AtomicReferenceFieldUpdater
			.newUpdater(DualPackageElement.class, TypeMirror.class, "asType");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<DualPackageElement, List> ARFU_enclosedElements = AtomicReferenceFieldUpdater
			.newUpdater(DualPackageElement.class, List.class, "enclosedElements");

	public static final AtomicIntegerFieldUpdater<DualPackageElement> AIFU_deprecated = AtomicIntegerFieldUpdater
			.newUpdater(DualPackageElement.class, "deprecated");

	private final IncrementalElementsTypesBase elemTypes;
	private final PackagesTypesContainer packagesTypesContainer;
	private final Name qualifiedName;
	private final Name simpleName;

	private PackageSignature parsedSignature;
	private PackageElement javacElement;
	private AnnotatedConstruct annotatedConstruct;
	private Element enclosingElement;

	private volatile transient TypeMirror asType;
	private volatile transient List<Element> enclosedElements;
	private volatile transient int deprecated = -1;

	public DualPackageElement(IncrementalElementsTypesBase elemTypes, PackagesTypesContainer packagesTypesContainer,
			String qualifiedname) {
		this.elemTypes = elemTypes;
		this.packagesTypesContainer = packagesTypesContainer;
		if (qualifiedname.isEmpty()) {
			this.qualifiedName = IncrementalName.EMPTY_NAME;
			this.simpleName = IncrementalName.EMPTY_NAME;
		} else {
			this.qualifiedName = new IncrementalName(qualifiedname);
			this.simpleName = new IncrementalName(qualifiedname.substring(qualifiedname.lastIndexOf('.') + 1));
		}
		annotatedConstruct = NonAnnotatedConstruct.INSTANCE;
	}

	public DualPackageElement(IncrementalElementsTypesBase elemTypes, PackagesTypesContainer packagesTypesContainer,
			PackageSignature signature, PackageElement javacelement, String qualifiedname) {
		this.elemTypes = elemTypes;
		this.packagesTypesContainer = packagesTypesContainer;
		if (qualifiedname.isEmpty()) {
			this.qualifiedName = IncrementalName.EMPTY_NAME;
			this.simpleName = IncrementalName.EMPTY_NAME;
		} else {
			this.qualifiedName = new IncrementalName(qualifiedname);
			this.simpleName = new IncrementalName(qualifiedname.substring(qualifiedname.lastIndexOf('.') + 1));
		}
		setSignatureImpl(signature);
		setJavacElementImpl(javacelement);
	}

	public void setEnclosingElement(Element enclosingElement) {
		this.enclosingElement = enclosingElement;
	}

	public void invalidate() {
		this.asType = null;
		this.enclosedElements = null;
		this.deprecated = -1;
	}

	public void setSignature(PackageSignature signature) {
		if (this.parsedSignature == signature) {
			return;
		}
		invalidate();
		setSignatureImpl(signature);
	}

	private void setSignatureImpl(PackageSignature signature) {
		this.parsedSignature = signature;
		SignatureIncrementalAnnotatedConstruct nac = new SignatureIncrementalAnnotatedConstruct(elemTypes,
				parsedSignature, this);
		nac.setElementTypes(IncrementalElementsTypes.ELEMENT_TYPE_PACKAGE);
		this.annotatedConstruct = nac;
	}

	public void setJavacElement(PackageElement javacelement) {
		if (this.javacElement == javacelement) {
			return;
		}
		invalidate();
		setJavacElementImpl(javacelement);
	}

	private void setJavacElementImpl(PackageElement javacelement) {
		this.javacElement = javacelement;
		if (this.parsedSignature == null) {
			this.annotatedConstruct = new ForwardingAnnotatedConstruct<>(elemTypes, javacelement);
		}
	}

	@Override
	public PackageSignature getSignature() {
		return parsedSignature;
	}

	@Override
	public String getDocComment() {
		PackageSignature psig = parsedSignature;
		return psig == null ? null : psig.getDocComment();
	}

	public PackageElement getJavacElement() {
		return javacElement;
	}

	@Override
	public TypeMirror asType() {
		TypeMirror thisastype = this.asType;
		if (thisastype == null) {
			thisastype = new SimplePackageType(this);
			if (ARFU_asType.compareAndSet(this, null, thisastype)) {
				return thisastype;
			}
		}
		return this.asType;
	}

	@Override
	public ElementKind getKind() {
		return ElementKind.PACKAGE;
	}

	@Override
	public Set<Modifier> getModifiers() {
		//no modifiers on a package
		return Collections.emptySet();
	}

	@Override
	public List<? extends AnnotationMirror> getAnnotationMirrors() {
		return annotatedConstruct.getAnnotationMirrors();
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
		return annotatedConstruct.getAnnotation(annotationType);
	}

	@Override
	public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
		return annotatedConstruct.getAnnotationsByType(annotationType);
	}

	@Override
	public <R, P> R accept(ElementVisitor<R, P> v, P p) {
		return v.visitPackage(this, p);
	}

	@Override
	public Name getQualifiedName() {
		return qualifiedName;
	}

	@Override
	public Name getSimpleName() {
		return simpleName;
	}

	@Override
	public List<? extends Element> getEnclosedElements() {
		List<Element> thisenclosedelements = this.enclosedElements;
		if (thisenclosedelements != null) {
			return thisenclosedelements;
		}
		ArrayList<Element> nenclosedelements = new ArrayList<>(
				packagesTypesContainer.getPackageEnclosedNonJavacElements(this.qualifiedName.toString()));
		PackageElement javacelem = javacElement;
		if (javacelem != null) {
			nenclosedelements.addAll(elemTypes.forwardElements(javacelem::getEnclosedElements));
		}
		thisenclosedelements = ImmutableUtils.makeImmutableList(nenclosedelements);
		if (ARFU_enclosedElements.compareAndSet(this, null, thisenclosedelements)) {
			return thisenclosedelements;
		}
		return this.enclosedElements;
	}

	@Override
	public boolean isUnnamed() {
		return getSimpleName().length() == 0;
	}

	@Override
	public Element getEnclosingElement() {
		return enclosingElement;
	}

	@Override
	public boolean isDeprecated() {
		int thisval = this.deprecated;
		if (thisval >= 0) {
			return thisval != 0;
		}
		if (parsedSignature != null) {
			thisval = getAnnotation(Deprecated.class) != null ? 1 : 0;
		} else if (javacElement != null) {
			thisval = elemTypes.isJavacElementDeprecated(javacElement) ? 1 : 0;
		} else {
			thisval = 0;
		}
		if (AIFU_deprecated.compareAndSet(this, -1, thisval)) {
			return thisval != 0;
		}
		return this.deprecated != 0;
	}

	@Override
	public String toString() {
		return qualifiedName.length() == 0 ? "unnamed package" : qualifiedName.toString();
	}
}
