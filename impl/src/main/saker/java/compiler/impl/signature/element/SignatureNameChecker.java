package saker.java.compiler.impl.signature.element;

import java.util.function.BiPredicate;

import saker.java.compiler.impl.signature.type.TypeParameterTypeSignature;

public enum SignatureNameChecker {
	COMPARE_WITH_NAMES(MethodParameterSignature::signatureEquals, TypeParameterTypeSignature::signatureEquals),
	COMPARE_WITHOUT_NAMES(MethodParameterSignature::signatureEqualsWithoutName,
			TypeParameterTypeSignature::signatureEqualsWithoutName);

	public final BiPredicate<? super MethodParameterSignature, ? super MethodParameterSignature> methodParameterComparator;
	public final BiPredicate<? super TypeParameterTypeSignature, ? super TypeParameterTypeSignature> typeParameterComparator;

	private SignatureNameChecker(
			BiPredicate<? super MethodParameterSignature, ? super MethodParameterSignature> methodParameterComparator,
			BiPredicate<? super TypeParameterTypeSignature, ? super TypeParameterTypeSignature> typeParameterComparator) {
		this.methodParameterComparator = methodParameterComparator;
		this.typeParameterComparator = typeParameterComparator;
	}

}