package saker.java.compiler.impl.signature.type;

import java.util.List;

import saker.build.thirdparty.saker.util.ObjectUtils;

public interface UnionTypeSignature extends TypeSignature {
	public List<? extends TypeSignature> getAlternatives();

	@Override
	public default <R, P> R accept(TypeSignatureVisitor<R, P> v, P p) {
		return v.visitUnion(this, p);
	}

	@Override
	public default boolean signatureEquals(TypeSignature other) {
		if (!(other instanceof UnionTypeSignature)) {
			return false;
		}
		return signatureEquals((UnionTypeSignature) other);
	}

	public default boolean signatureEquals(UnionTypeSignature other) {
		if (!TypeSignature.super.signatureEquals(other)) {
			return false;
		}
		if (!ObjectUtils.collectionOrderedEquals(getAlternatives(), other.getAlternatives(),
				TypeSignature::signatureEquals)) {
			return false;
		}
		return true;
	}
}
