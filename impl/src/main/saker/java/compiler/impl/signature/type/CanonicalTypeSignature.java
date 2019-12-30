package saker.java.compiler.impl.signature.type;

import java.util.Objects;

public interface CanonicalTypeSignature extends ParameterizedTypeSignature {
	@Override
	public default boolean signatureEquals(TypeSignature other) {
		if (!(other instanceof CanonicalTypeSignature)) {
			return false;
		}
		return signatureEquals((CanonicalTypeSignature) other);
	}

	public default boolean signatureEquals(CanonicalTypeSignature other) {
		if (!ParameterizedTypeSignature.super.signatureEquals(other)) {
			return false;
		}
		if (!Objects.equals(getCanonicalName(), other.getCanonicalName())) {
			return false;
		}
		return true;
	}
}
