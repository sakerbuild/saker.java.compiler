package saker.java.compiler.impl.compile.handler.incremental.model.forwarded.elem;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

import saker.java.compiler.impl.compile.handler.incremental.model.CommonTypeElement;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalName;

public class ForwardingTypeElement extends ForwardingElementBase<TypeElement> implements CommonTypeElement {
	private static final AtomicReferenceFieldUpdater<ForwardingTypeElement, TypeMirror> ARFU_superClass = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingTypeElement.class, TypeMirror.class, "superClass");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<ForwardingTypeElement, List> ARFU_typeParameters = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingTypeElement.class, List.class, "typeParameters");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<ForwardingTypeElement, List> ARFU_superInterfaces = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingTypeElement.class, List.class, "superInterfaces");
	private static final AtomicReferenceFieldUpdater<ForwardingTypeElement, NestingKind> ARFU_nestingKind = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingTypeElement.class, NestingKind.class, "nestingKind");
	private static final AtomicReferenceFieldUpdater<ForwardingTypeElement, Name> ARFU_qualifiedName = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingTypeElement.class, Name.class, "qualifiedName");
	private static final AtomicReferenceFieldUpdater<ForwardingTypeElement, Name> ARFU_binaryName = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingTypeElement.class, Name.class, "binaryName");

	private volatile transient TypeMirror superClass;
	private volatile transient List<? extends TypeMirror> superInterfaces;
	private volatile transient List<? extends TypeParameterElement> typeParameters;
	private volatile transient NestingKind nestingKind;
	private volatile transient Name qualifiedName;
	private volatile transient Name binaryName;

	public ForwardingTypeElement(IncrementalElementsTypesBase elemTypes, TypeElement subject) {
		super(elemTypes, subject);
	}

	@Override
	public NestingKind getNestingKind() {
		NestingKind thisnestingkind = this.nestingKind;
		if (thisnestingkind != null) {
			return thisnestingkind;
		}
		thisnestingkind = elemTypes.javac(subject::getNestingKind);
		if (ARFU_nestingKind.compareAndSet(this, null, thisnestingkind)) {
			return thisnestingkind;
		}
		return this.nestingKind;
	}

	@Override
	public Name getQualifiedName() {
		Name thisqualifiedname = this.qualifiedName;
		if (thisqualifiedname != null) {
			return thisqualifiedname;
		}
		thisqualifiedname = elemTypes.javac(subject::getQualifiedName);
		if (ARFU_qualifiedName.compareAndSet(this, null, thisqualifiedname)) {
			return thisqualifiedname;
		}
		return this.qualifiedName;
	}

	@Override
	public Name getBinaryName() {
		Name thisbinaryname = this.binaryName;
		if (thisbinaryname != null) {
			return thisbinaryname;
		}
		thisbinaryname = new IncrementalName(elemTypes.getJavacTypeBinaryName(subject).toString());
		if (ARFU_binaryName.compareAndSet(this, null, thisbinaryname)) {
			return thisbinaryname;
		}
		return this.binaryName;
	}

	@Override
	public TypeMirror getSuperclass() {
		TypeMirror thissuperclass = this.superClass;
		if (thissuperclass != null) {
			return thissuperclass;
		}
		thissuperclass = elemTypes.forwardType(subject::getSuperclass);
		if (ARFU_superClass.compareAndSet(this, null, thissuperclass)) {
			return thissuperclass;
		}
		return this.superClass;
	}

	@Override
	public List<? extends TypeMirror> getInterfaces() {
		List<? extends TypeMirror> thissuperinterfaces = this.superInterfaces;
		if (thissuperinterfaces != null) {
			return thissuperinterfaces;
		}
		thissuperinterfaces = elemTypes.forwardTypes(subject::getInterfaces);
		if (ARFU_superInterfaces.compareAndSet(this, null, thissuperinterfaces)) {
			return thissuperinterfaces;
		}
		return this.superInterfaces;
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

}