package saker.java.compiler.impl.signature.type;

import java.util.List;

import saker.build.thirdparty.saker.util.ObjectUtils;

public interface IntersectionTypeSignature extends TypeSignature {
	public List<? extends TypeSignature> getBounds();

	@Override
	public default <R, P> R accept(TypeSignatureVisitor<R, P> v, P p) {
		return v.visitIntersection(this, p);
	}

	@Override
	public default boolean signatureEquals(TypeSignature other) {
		if (!(other instanceof IntersectionTypeSignature)) {
			return false;
		}
		return signatureEquals((IntersectionTypeSignature) other);
	}

	public default boolean signatureEquals(IntersectionTypeSignature other) {
		if (!TypeSignature.super.signatureEquals(other)) {
			return false;
		}
		if (!ObjectUtils.collectionOrderedEquals(getBounds(), other.getBounds(), TypeSignature::signatureEquals)) {
			return false;
		}
		return true;
	}
}
