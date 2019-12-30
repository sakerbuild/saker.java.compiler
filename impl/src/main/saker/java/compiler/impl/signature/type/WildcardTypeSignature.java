package saker.java.compiler.impl.signature.type;

import saker.build.thirdparty.saker.util.ObjectUtils;

public interface WildcardTypeSignature extends TypeSignature {
	public TypeSignature getUpperBounds();

	public TypeSignature getLowerBounds();

	@Override
	public default <R, P> R accept(TypeSignatureVisitor<R, P> v, P p) {
		return v.visitWildcard(this, p);
	}

	@Override
	public default boolean signatureEquals(TypeSignature other) {
		if (!(other instanceof WildcardTypeSignature)) {
			return false;
		}
		return signatureEquals((WildcardTypeSignature) other);
	}

	public default boolean signatureEquals(WildcardTypeSignature other) {
		if (!TypeSignature.super.signatureEquals(other)) {
			return false;
		}
		if (!ObjectUtils.objectsEquals(getUpperBounds(), other.getUpperBounds(), TypeSignature::signatureEquals)) {
			return false;
		}
		if (!ObjectUtils.objectsEquals(getLowerBounds(), other.getLowerBounds(), TypeSignature::signatureEquals)) {
			return false;
		}
		return true;
	}
}
