package saker.java.compiler.impl.compile.handler.incremental.model.mirror;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

import saker.java.compiler.impl.compile.handler.incremental.model.CommonPackageType;
import saker.java.compiler.impl.compile.handler.incremental.model.CommonTypeMirror;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementallyModelled;

public class SimplePackageType implements CommonPackageType, IncrementallyModelled, CommonTypeMirror {
	private PackageElement packageElement;

	public SimplePackageType(PackageElement packageElement) {
		this.packageElement = packageElement;
	}

	@Override
	public TypeKind getKind() {
		return TypeKind.PACKAGE;
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		return v.visitNoType(this, p);
	}

	@Override
	public PackageElement getPackageElement() {
		return packageElement;
	}

	@Override
	public TypeMirror getErasedType() {
		return this;
	}

	@Override
	public String toString() {
		return getPackageElement().toString();
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> a) {
		return packageElement.getAnnotation(a);
	}

	@Override
	public List<? extends AnnotationMirror> getAnnotationMirrors() {
		return packageElement.getAnnotationMirrors();
	}

	@Override
	public <A extends Annotation> A[] getAnnotationsByType(Class<A> a) {
		return packageElement.getAnnotationsByType(a);
	}
}
