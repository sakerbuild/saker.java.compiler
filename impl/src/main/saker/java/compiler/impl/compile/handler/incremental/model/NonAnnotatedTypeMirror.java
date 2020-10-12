package saker.java.compiler.impl.compile.handler.incremental.model;

import java.lang.annotation.Annotation;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeMirror;

public interface NonAnnotatedTypeMirror extends NonAnnotatedConstruct, TypeMirror {
	@Override
	public default <A extends Annotation> A getAnnotation(Class<A> annotationType) {
		//override due to TypeMirror also declaring this method
		return NonAnnotatedConstruct.super.getAnnotation(annotationType);
	}

	@Override
	public default List<? extends AnnotationMirror> getAnnotationMirrors() {
		//override due to TypeMirror also declaring this method
		return NonAnnotatedConstruct.super.getAnnotationMirrors();
	}

	@Override
	public default <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
		//override due to TypeMirror also declaring this method
		return NonAnnotatedConstruct.super.getAnnotationsByType(annotationType);
	}
}
