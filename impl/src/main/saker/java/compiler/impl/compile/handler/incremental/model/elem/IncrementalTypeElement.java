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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
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

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.compat.KindCompatUtils;
import saker.java.compiler.impl.compile.handler.incremental.model.CommonTypeElement;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalName;
import saker.java.compiler.impl.compile.handler.incremental.model.mirror.IncrementalDeclaredType;
import saker.java.compiler.impl.compile.signature.impl.ClassSignatureImpl;
import saker.java.compiler.impl.compile.signature.impl.FullMethodSignature;
import saker.java.compiler.impl.compile.signature.impl.MethodParameterSignatureImpl;
import saker.java.compiler.impl.compile.signature.type.impl.CanonicalTypeSignatureImpl;
import saker.java.compiler.impl.compile.signature.type.impl.PrimitiveTypeSignatureImpl;
import saker.java.compiler.impl.signature.element.ClassMemberSignature;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.FieldSignature;
import saker.java.compiler.impl.signature.element.MethodParameterSignature;
import saker.java.compiler.impl.signature.element.MethodSignature;
import saker.java.compiler.impl.signature.type.TypeParameterTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.util.ImmutableModifierSet;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

public class IncrementalTypeElement extends IncrementalElement<ClassSignature>
		implements CommonTypeElement, DocumentedIncrementalElement<ClassSignature> {

	private static final MethodSignature RECORD_IMPLICIT_HASHCODE_SIGNATURE = FullMethodSignature.create("hashCode",
			IncrementalElementsTypes.MODIFIERS_PUBLIC_FINAL, null, null,
			PrimitiveTypeSignatureImpl.create(TypeKind.INT), null, ElementKind.METHOD, null, null, false, null);
	private static final MethodSignature RECORD_IMLICIT_TOSTRING_SIGNATURE = FullMethodSignature.create("toString",
			IncrementalElementsTypes.MODIFIERS_PUBLIC, null, null, CanonicalTypeSignatureImpl.INSTANCE_JAVA_LANG_STRING,
			null, ElementKind.METHOD, null, null, false, null);
	private static final MethodSignature RECORD_IMLICIT_EQUALS_SIGNATURE = FullMethodSignature.create("equals",
			IncrementalElementsTypes.MODIFIERS_PUBLIC_FINAL,
			Collections.singletonList(MethodParameterSignatureImpl.create(ImmutableModifierSet.empty(),
					CanonicalTypeSignatureImpl.INSTANCE_JAVA_LANG_OBJECT, "o")),
			null, PrimitiveTypeSignatureImpl.create(TypeKind.BOOLEAN), null, ElementKind.METHOD, null, null, false,
			null);

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
		// fine to use direct assignment as this will not be called from multi-threaded
		// code
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
		// disable TYPE_USE annotations as this is not really a "type use". Javac doesnt
		// give this either.
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

	private boolean isAllTypesSame(List<? extends TypeMirror> types, List<? extends TypeMirror> otypes) {
		Iterator<? extends TypeMirror> it = types.iterator();
		Iterator<? extends TypeMirror> oit = otypes.iterator();
		while (it.hasNext()) {
			if (!oit.hasNext()) {
				return false;
			}
			TypeMirror tm = it.next();
			TypeMirror otm = oit.next();
			if (!elemTypes.isSameType(tm, otm)) {
				return false;
			}
		}
		if (oit.hasNext()) {
			return false;
		}
		return true;
	}

	private boolean hasCanonicalConstructorWithParameterTypes(List<TypeMirror> paramtypes,
			List<? extends IncrementalElement<?>> elements) {
		int paramcount = paramtypes.size();
		for (IncrementalElement<?> e : elements) {
			if (e.getKindIndex() != KindCompatUtils.ELEMENTKIND_INDEX_CONSTRUCTOR) {
				continue;
			}
			MethodSignature ms = (MethodSignature) e.getSignature();
			if (ms.getParameterCount() != paramcount) {
				continue;
			}
			List<TypeMirror> eparamtypes = new ArrayList<>(paramcount);
			for (MethodParameterSignature p : ms.getParameters()) {
				//it is okay to use this as the enclosing element
				// as the canonical constructors of a record cannot declare type
				// variables
				eparamtypes.add(elemTypes.getTypeMirror(p.getTypeSignature(), this));
			}

			if (isAllTypesSame(eparamtypes, paramtypes)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public List<? extends IncrementalElement<?>> getEnclosedElements() {
		List<IncrementalElement<?>> thisenclosedelements = enclosedElements;
		if (thisenclosedelements != null) {
			return thisenclosedelements;
		}
		List<? extends ClassMemberSignature> members = signature.getMembers();
		thisenclosedelements = new ArrayList<>(members.size());
		if (!ObjectUtils.isNullOrEmpty(members)) {
			for (ClassMemberSignature m : members) {
				switch (m.getKindIndex()) {
					case KindCompatUtils.ELEMENTKIND_INDEX_ANNOTATION_TYPE:
					case KindCompatUtils.ELEMENTKIND_INDEX_INTERFACE:
					case KindCompatUtils.ELEMENTKIND_INDEX_CLASS:
					case KindCompatUtils.ELEMENTKIND_INDEX_ENUM:
					case KindCompatUtils.ELEMENTKIND_INDEX_RECORD: {
						IncrementalTypeElement gottype = elemTypes.getLocalPackagesTypesContainer()
								.getTypeElement((ClassSignature) m);
						thisenclosedelements.add(gottype);
						break;
					}
					case KindCompatUtils.ELEMENTKIND_INDEX_CONSTRUCTOR:
					case KindCompatUtils.ELEMENTKIND_INDEX_METHOD: {
						IncrementalExecutableElement constructorelem = new IncrementalExecutableElement(
								(MethodSignature) m, this, elemTypes);
						thisenclosedelements.add(constructorelem);
						break;
					}
					case KindCompatUtils.ELEMENTKIND_INDEX_FIELD:
					case KindCompatUtils.ELEMENTKIND_INDEX_ENUM_CONSTANT: {
						FieldSignature fs = (FieldSignature) m;
						thisenclosedelements.add(new IncrementalVariableElement(elemTypes, fs,
								fs.isEnumConstant() ? ElementKind.ENUM_CONSTANT : ElementKind.FIELD, this));
						break;
					}
					case KindCompatUtils.ELEMENTKIND_INDEX_RECORD_COMPONENT: {
						FieldSignature fs = (FieldSignature) m;
						thisenclosedelements
								.add(new IncrementalVariableElement(elemTypes, fs, ElementKind.FIELD, this));
						thisenclosedelements.add(elemTypes.createRecordComponentElement(this, fs));
						break;
					}
					default: {
						throw new IllegalArgumentException(m.toString());
					}
				}
			}
		}
		if (signature.getKindIndex() == KindCompatUtils.ELEMENTKIND_INDEX_RECORD) {
			// handle implicit constructor, add only if not already declared
			Collection<? extends FieldSignature> fields = signature.getFields();
			List<MethodParameterSignature> paramsignatures = new ArrayList<>(fields.size());
			int fieldcount = fields.size();
			List<TypeMirror> paramtypes = new ArrayList<>(fieldcount);
			for (FieldSignature f : fields) {
				paramsignatures.add(MethodParameterSignatureImpl.create(ImmutableModifierSet.empty(),
						f.getTypeSignature(), f.getSimpleName()));
				paramtypes.add(elemTypes.getTypeMirror(f.getTypeSignature(), this));
			}
			if (!hasCanonicalConstructorWithParameterTypes(paramtypes, thisenclosedelements)) {
				thisenclosedelements.add(new IncrementalExecutableElement(
						FullMethodSignature.create(IncrementalElementsTypes.CONSTRUCTOR_METHOD_NAME,
								IncrementalElementsTypes.MODIFIERS_PUBLIC, paramsignatures, null, null, null,
								ElementKind.CONSTRUCTOR, null, null, false, null),
						this, elemTypes));
			}

			//handle implicit members
			if (!ClassSignatureImpl.hasSimpleNoArgMethodWithName("toString", members)) {
				thisenclosedelements
						.add(new IncrementalExecutableElement(RECORD_IMLICIT_TOSTRING_SIGNATURE, this, elemTypes));
			}
			if (!ClassSignatureImpl.hasSimpleNoArgMethodWithName("hashCode", members)) {
				thisenclosedelements
						.add(new IncrementalExecutableElement(RECORD_IMPLICIT_HASHCODE_SIGNATURE, this, elemTypes));
			}
			if (getEqualsMethod(thisenclosedelements) == null) {
				thisenclosedelements
						.add(new IncrementalExecutableElement(RECORD_IMLICIT_EQUALS_SIGNATURE, this, elemTypes));
			}
			for (FieldSignature f : fields) {
				if (!ClassSignatureImpl.hasSimpleNoArgMethodWithName(f.getSimpleName(), members)) {
					MethodSignature msignature = FullMethodSignature.create(f.getSimpleName(),
							IncrementalElementsTypes.MODIFIERS_PUBLIC, null, null, f.getTypeSignature(), null,
							ElementKind.METHOD, null, null, false, null);
					thisenclosedelements.add(new IncrementalExecutableElement(msignature, this, elemTypes));
				}
			}
		}
		List<IncrementalElement<?>> immutableenclosedelements = ImmutableUtils.unmodifiableList(thisenclosedelements);
		if (ARFU_enclosedElements.compareAndSet(this, null, immutableenclosedelements)) {
			return immutableenclosedelements;
		}
		return this.enclosedElements;
	}

	private IncrementalElement<?> getEqualsMethod(List<IncrementalElement<?>> thisenclosedelements) {
		for (IncrementalElement<?> e : thisenclosedelements) {
			if (e.getKindIndex() != KindCompatUtils.ELEMENTKIND_INDEX_METHOD) {
				continue;
			}
			MethodSignature s = (MethodSignature) e.getSignature();
			if (!"equals".equals(s.getSimpleName())) {
				continue;
			}
			if (s.getParameterCount() != 1) {
				continue;
			}
			MethodParameterSignature paramsignature = s.getParameters().get(0);
			TypeMirror paramte = elemTypes.getTypeMirror(paramsignature.getTypeSignature(), e);
			if (elemTypes.isSameType(paramte, elemTypes.getJavaLangObjectTypeMirror())) {
				return e;
			}
		}
		return null;
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
		switch (signature.getKindIndex()) {
			case KindCompatUtils.ELEMENTKIND_INDEX_INTERFACE:
			case KindCompatUtils.ELEMENTKIND_INDEX_ANNOTATION_TYPE: {
				thissuperclass = IncrementalElementsTypes.getNoneTypeKind();
				break;
			}
			case KindCompatUtils.ELEMENTKIND_INDEX_ENUM: {
				thissuperclass = elemTypes.getDeclaredType(elemTypes.getJavaLangEnumTypeElement(), asType());
				break;
			}
			case KindCompatUtils.ELEMENTKIND_INDEX_CLASS: {
				TypeSignature extending = signature.getSuperClass();
				if (extending != null) {
					thissuperclass = elemTypes.getTypeMirror(extending, this);
				} else {
					thissuperclass = elemTypes.getJavaLangObjectTypeMirror();
				}
				break;
			}
			case KindCompatUtils.ELEMENTKIND_INDEX_RECORD: {
				thissuperclass = elemTypes.getJavaLangRecordTypeMirror();
				break;
			}
			default: {
				break;
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
