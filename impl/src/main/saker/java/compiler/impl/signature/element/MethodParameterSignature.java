package saker.java.compiler.impl.signature.element;

import java.util.Objects;
import java.util.Set;

import javax.lang.model.element.Modifier;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.signature.type.TypeSignature;

public interface MethodParameterSignature extends AnnotatedSignature {
	public String getSimpleName();

	public TypeSignature getTypeSignature();

	public Set<Modifier> getModifiers();

	public static boolean signatureEquals(MethodParameterSignature first, MethodParameterSignature other) {
		if (!Objects.equals(first.getSimpleName(), other.getSimpleName())) {
			return false;
		}
		return signatureEqualsWithoutName(first, other);
	}

	public static boolean signatureEqualsWithoutName(MethodParameterSignature first, MethodParameterSignature other) {
		if (!AnnotatedSignature.annotationSignaturesEqual(first, other)) {
			return false;
		}
		if (!ObjectUtils.objectsEquals(first.getTypeSignature(), other.getTypeSignature(),
				TypeSignature::signatureEquals)) {
			return false;
		}
		if (!Objects.equals(first.getModifiers(), other.getModifiers())) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode();
}
