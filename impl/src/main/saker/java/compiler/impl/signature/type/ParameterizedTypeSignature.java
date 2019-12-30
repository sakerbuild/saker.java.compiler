package saker.java.compiler.impl.signature.type;

import java.util.List;

import saker.build.thirdparty.saker.util.ObjectUtils;

public interface ParameterizedTypeSignature extends TypeSignature {
	public List<? extends TypeSignature> getTypeParameters();

	@Override
	public ParameterizedTypeSignature getEnclosingSignature();

	@Override
	public default <R, P> R accept(TypeSignatureVisitor<R, P> v, P p) {
		ParameterizedTypeSignature enclosing = getEnclosingSignature();
		if (enclosing != null) {
			return v.visitEncloser(this, p);
		}
		return v.visitParameterized(this, p);
	}

	@Override
	public default boolean signatureEquals(TypeSignature other) {
		if (!(other instanceof ParameterizedTypeSignature)) {
			return false;
		}
		return signatureEquals((ParameterizedTypeSignature) other);
	}

	public default boolean signatureEquals(ParameterizedTypeSignature other) {
		if (!TypeSignature.super.signatureEquals(other)) {
			return false;
		}
		if (!ObjectUtils.collectionOrderedEquals(getTypeParameters(), other.getTypeParameters(),
				TypeSignature::signatureEquals)) {
			return false;
		}
		return true;
	}
}
