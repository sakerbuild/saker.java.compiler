package saker.java.compiler.impl.signature.type;

import java.util.Objects;

public interface TypeVariableTypeSignature extends TypeSignature {

	public String getVariableName();

	@Override
	public default boolean signatureEquals(TypeSignature other) {
		if (!(other instanceof TypeVariableTypeSignature)) {
			return false;
		}
		return signatureEquals((TypeVariableTypeSignature) other);
	}

	public default boolean signatureEquals(TypeVariableTypeSignature other) {
		if (!TypeSignature.super.signatureEquals(other)) {
			return false;
		}
		if (!Objects.equals(getVariableName(), other.getVariableName())) {
			return false;
		}
		return true;
	}
}
