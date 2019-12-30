package saker.java.compiler.impl.compile.handler.incremental.model;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;

import saker.build.thirdparty.saker.util.ReflectUtils;

public interface NonAnnotatedConstruct extends AnnotatedConstruct {
	public static final NonAnnotatedConstruct INSTANCE = new NonAnnotatedConstruct() {
	};

	@Override
	public default List<? extends AnnotationMirror> getAnnotationMirrors() {
		return Collections.emptyList();
	}

	@Override
	public default <A extends Annotation> A getAnnotation(Class<A> annotationType) {
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public default <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
		return (A[]) ReflectUtils.createEmptyArray(annotationType);
	}
}
