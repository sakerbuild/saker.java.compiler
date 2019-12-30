package saker.java.compiler.impl.signature.type;

import java.util.Objects;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.signature.element.AnnotatedSignature;

public interface TypeParameterTypeSignature extends AnnotatedSignature {
	/**
	 * Returns the name of the type variable. <br>
	 * "?" in case of wildcard type, and user-specified name in case of a variable (Like T, E, etc...)
	 * 
	 * @return The name of the bounded variable.
	 */
	public String getVarName();

	public TypeSignature getUpperBounds();

	public TypeSignature getLowerBounds();

	public default boolean signatureEquals(TypeParameterTypeSignature other) {
		if (!Objects.equals(getVarName(), other.getVarName())) {
			return false;
		}
		return signatureEqualsWithoutName(other);
	}

	public default boolean signatureEqualsWithoutName(TypeParameterTypeSignature other) {
		if (!AnnotatedSignature.annotationSignaturesEqual(this, other)) {
			return false;
		}
		if (!ObjectUtils.objectsEquals(getUpperBounds(), other.getUpperBounds(), TypeSignature::signatureEquals)) {
			return false;
		}
		if (!ObjectUtils.objectsEquals(getLowerBounds(), other.getLowerBounds(), TypeSignature::signatureEquals)) {
			return false;
		}
		return true;
//		return signatureEquals(other, true);
	}

//	public default boolean signatureEquals(TypeSignature other, boolean includebounds) {
//		if (!(other instanceof BoundedTypeSignature)) {
//			return false;
//		}
//		return signatureEquals((BoundedTypeSignature) other, includebounds);
//	}
//
//	public default boolean signatureEquals(BoundedTypeSignature other, boolean includebounds) {
//		if (!TypeSignature.super.signatureEquals(other)) {
//			return false;
//		}
//		if (!Objects.equals(getVarName(), other.getVarName())) {
//			return false;
//		}
//		if (includebounds) {
//			if (!ObjectUtils.equals(getUpperBounds(), other.getUpperBounds(), BoundedTypeSignature::boundlessSignatureEquals)) {
//				return false;
//			}
//			if (!ObjectUtils.equals(getLowerBounds(), other.getLowerBounds(), BoundedTypeSignature::boundlessSignatureEquals)) {
//				return false;
//			}
//		}
//		return true;
//	}
//
//	public static boolean boundlessSignatureEquals(TypeSignature left, TypeSignature right) {
//		if (left instanceof BoundedTypeSignature) {
//			return ((BoundedTypeSignature) left).signatureEquals(right, false);
//		}
//		return left.signatureEquals(right);
//	}
}
