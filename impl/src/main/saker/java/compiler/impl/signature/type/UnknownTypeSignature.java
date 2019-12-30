package saker.java.compiler.impl.signature.type;

import java.util.Objects;

public interface UnknownTypeSignature extends TypeSignature {
	@Override
	public default <R, P> R accept(TypeSignatureVisitor<R, P> v, P p) {
		return v.visitUnknown(this, p);
	}

	public String getTypeDescription();

	@Override
	public default boolean signatureEquals(TypeSignature other) {
		if (!(other instanceof UnknownTypeSignature)) {
			return false;
		}
		return signatureEquals((UnknownTypeSignature) other);
	}

	public default boolean signatureEquals(UnknownTypeSignature other) {
		if (!TypeSignature.super.signatureEquals(other)) {
			return false;
		}
		if (!Objects.equals(getTypeDescription(), other.getTypeDescription())) {
			return false;
		}
		return true;
	}
}
