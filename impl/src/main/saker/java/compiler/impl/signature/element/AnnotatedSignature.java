package saker.java.compiler.impl.signature.element;

import java.util.Collection;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.signature.Signature;

public interface AnnotatedSignature extends Signature {
	public Collection<? extends AnnotationSignature> getAnnotations();

	public static boolean annotationSignaturesEqual(AnnotatedSignature first, AnnotatedSignature other) {
		return ObjectUtils.collectionOrderedEquals(first.getAnnotations(), other.getAnnotations(),
				AnnotationSignature::signatureEquals);
	}

	@Override
	public int hashCode();
}
