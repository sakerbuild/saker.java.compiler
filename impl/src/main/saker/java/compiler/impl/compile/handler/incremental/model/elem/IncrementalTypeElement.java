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

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.compile.handler.incremental.model.CommonTypeElement;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalName;
import saker.java.compiler.impl.compile.handler.incremental.model.mirror.IncrementalDeclaredType;
import saker.java.compiler.impl.signature.element.ClassMemberSignature;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.FieldSignature;
import saker.java.compiler.impl.signature.element.MethodSignature;
import saker.java.compiler.impl.signature.type.TypeParameterTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

public class IncrementalTypeElement extends IncrementalElement<ClassSignature>
		implements CommonTypeElement, DocumentedIncrementalElement<ClassSignature> {
	private static final AtomicReferenceFieldUpdater<IncrementalTypeElement, TypeMirror> ARFU_asType = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalTypeElement.class, TypeMirror.class, "asType");
	private static final AtomicReferenceFieldUpdater<IncrementalTypeElement, TypeMirror> ARFU_superClass = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalTypeElement.class, TypeMirror.class, "superClass");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<IncrementalTypeElement, List> ARFU_typeParameters = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalTypeElement.class, List.class, "typeParameters");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<IncrementalTypeElement, List> ARFU_enclosedElements = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalTypeElement.class, List.class, "enclosedElements");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<IncrementalTypeElement, List> ARFU_superInterfaces = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalTypeElement.class, List.class, "superInterfaces");
	private static final AtomicReferenceFieldUpdater<IncrementalTypeElement, Element> ARFU_enclosingElement = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalTypeElement.class, Element.class, "enclosingElement");

	private volatile transient TypeMirror asType;
	private volatile transient TypeMirror superClass;
	private volatile transient List<IncrementalTypeParameterElement> typeParameters;
	private volatile transient List<IncrementalElement<?>> enclosedElements;
	private volatile transient List<TypeMirror> superInterfaces;
	private volatile transient Element enclosingElement;

	public IncrementalTypeElement(ClassSignature signature, IncrementalElementsTypesBase elemTypes) {
		super(elemTypes, signature);
		if (signature.getKind() == ElementKind.ANNOTATION_TYPE) {
			elementTypes = IncrementalElementsTypes.ELEMENT_TYPE_ANNOTATION_TYPE_TYPE_TYPE_USE;
		} else {
			elementTypes = IncrementalElementsTypes.ELEMENT_TYPE_TYPE_TYPE_USE;
		}
	}

	public void setEnclosingElement(Element enclosingElement) {
		this.enclosingElement = enclosingElement;
	}

	@Override
	public void invalidate() {
		super.invalidate();
		//fine to use direct assignment as this will not be called from multi-threaded code
		this.typeParameters = null;
		this.enclosedElements = null;
		this.asType = null;
		this.superClass = null;
		this.superInterfaces = null;
		this.enclosingElement = null;
	}

	@Override
	public TypeMirror asType() {
		TypeMirror thisastype = this.asType;
		if (thisastype != null) {
			return thisastype;
		}
		IncrementalDeclaredType ntype = new IncrementalDeclaredType(elemTypes, signature.getTypeSignature(), this,
				this);
		//disable TYPE_USE annotations as this is not really a "type use". Javac doesnt give this either.
		ntype.setElementTypes(Collections.emptySet());
		if (ARFU_asType.compareAndSet(this, null, ntype)) {
			return ntype;
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
	public <R, P> R accept(ElementVisitor<R, P> v, P p) {
		return v.visitType(this, p);
	}

	public IncrementalExecutableElement getMethodWithSignature(MethodSignature signature) {
		for (IncrementalElement<?> ie : getEnclosedElements()) {
			if (ie.getSignature() == signature) {
				return (IncrementalExecutableElement) ie;
			}
		}
		return null;
	}

	@Override
	public List<? extends IncrementalElement<?>> getEnclosedElements() {
		List<IncrementalElement<?>> thisenclosedelements = enclosedElements;
		if (thisenclosedelements != null) {
			return thisenclosedelements;
		}
		List<? extends ClassMemberSignature> members = signature.getMembers();
		thisenclosedelements = JavaTaskUtils.cloneImmutableList(members, m -> {
			if (m instanceof ClassSignature) {
				IncrementalTypeElement gottype = elemTypes.getLocalPackagesTypesContainer()
						.getTypeElement((ClassSignature) m);
				return gottype;
			}
			if (m instanceof MethodSignature) {
				return new IncrementalExecutableElement((MethodSignature) m, this, elemTypes);
			}
			if (m instanceof FieldSignature) {
				FieldSignature fs = (FieldSignature) m;
				return new IncrementalVariableElement(elemTypes, fs,
						fs.isEnumConstant() ? ElementKind.ENUM_CONSTANT : ElementKind.FIELD, this);
			}
			throw new IllegalArgumentException(m.toString());
		});
		if (ARFU_enclosedElements.compareAndSet(this, null, thisenclosedelements)) {
			return thisenclosedelements;
		}
		return this.enclosedElements;
	}

	@Override
	public NestingKind getNestingKind() {
		return signature.getNestingKind();
	}

	@Override
	public Name getQualifiedName() {
		return new IncrementalName(signature.getCanonicalName());
	}

	@Override
	public Name getSimpleName() {
		return new IncrementalName(signature.getSimpleName());
	}

	public boolean simpleNameEquals(String name) {
		return name.equals(signature.getSimpleName());
	}

	@Override
	public TypeMirror getSuperclass() {
		TypeMirror thissuperclass = this.superClass;
		if (thissuperclass != null) {
			return thissuperclass;
		}
		ElementKind kind = getKind();
		switch (kind) {
			case INTERFACE:
			case ANNOTATION_TYPE: {
				thissuperclass = IncrementalElementsTypes.getNoneTypeKind();
				break;
			}
			case ENUM: {
				thissuperclass = elemTypes.getDeclaredType(elemTypes.getJavaLangEnumTypeElement(), asType());
				break;
			}
			case CLASS: {
				TypeSignature extending = signature.getSuperClass();
				if (extending != null) {
					thissuperclass = elemTypes.getTypeMirror(extending, this);
				} else {
					thissuperclass = elemTypes.getJavaLangObjectTypeMirror();
				}
				break;
			}
			default: {
				throw new IllegalStateException("Unknown TypeElement kind: " + kind);
			}
		}
		if (ARFU_superClass.compareAndSet(this, null, thissuperclass)) {
			return thissuperclass;
		}
		return this.superClass;
	}

	@Override
	public List<? extends TypeMirror> getInterfaces() {
		List<TypeMirror> thissuperinterfaces = this.superInterfaces;
		if (thissuperinterfaces != null) {
			return thissuperinterfaces;
		}
		List<? extends TypeSignature> itfs = signature.getSuperInterfaces();
		thissuperinterfaces = JavaTaskUtils.cloneImmutableList(itfs, ts -> elemTypes.getTypeMirror(ts, this));
		if (ARFU_superInterfaces.compareAndSet(this, null, thissuperinterfaces)) {
			return thissuperinterfaces;
		}
		return this.superInterfaces;
	}

	@Override
	public List<? extends IncrementalTypeParameterElement> getTypeParameters() {
		List<IncrementalTypeParameterElement> thistypeparameters = this.typeParameters;
		if (thistypeparameters != null) {
			return thistypeparameters;
		}
		List<? extends TypeParameterTypeSignature> params = signature.getTypeParameters();
		thistypeparameters = JavaTaskUtils.cloneImmutableList(params,
				p -> new IncrementalTypeParameterElement(p, elemTypes, this));
		if (ARFU_typeParameters.compareAndSet(this, null, thistypeparameters)) {
			return thistypeparameters;
		}
		return this.typeParameters;
	}

	@Override
	public Element getEnclosingElement() {
		Element thisenclosingelement = this.enclosingElement;
		if (thisenclosingelement != null) {
			return thisenclosingelement;
		}
		ClassSignature enclosing = signature.getEnclosingSignature();
		if (enclosing != null) {
			thisenclosingelement = elemTypes.getTypeElement(enclosing.getCanonicalName());
		} else {
			thisenclosingelement = elemTypes.getPackageElement(signature.getPackageName());
		}
		if (ARFU_enclosingElement.compareAndSet(this, null, thisenclosingelement)) {
			return thisenclosingelement;
		}
		return this.enclosingElement;
	}

	@Override
	protected <A extends Annotation> A[] getInheritedAnnotations(Class<A> annoType) {
		if (getKind() == ElementKind.CLASS) {
			TypeMirror superclass = getSuperclass();
			if (superclass.getKind() == TypeKind.DECLARED) {
				return ((DeclaredType) superclass).asElement().getAnnotationsByType(annoType);
			}
		}
		return super.getInheritedAnnotations(annoType);
	}

	@Override
	protected <A extends Annotation> A getInheritedAnnotation(Class<A> annoType) {
		if (getKind() == ElementKind.CLASS) {
			TypeMirror superclass = getSuperclass();
			if (superclass.getKind() == TypeKind.DECLARED) {
				return ((DeclaredType) superclass).asElement().getAnnotation(annoType);
			}
			return null;
		}
		return super.getInheritedAnnotation(annoType);
	}

	@Override
	public String toString() {
		return signature.getBinaryName();
	}

	@Override
	public Name getBinaryName() {
		return new IncrementalName(signature.getBinaryName());
	}

	@Override
	public String getDocComment() {
		return signature.getDocComment();
	}

}
