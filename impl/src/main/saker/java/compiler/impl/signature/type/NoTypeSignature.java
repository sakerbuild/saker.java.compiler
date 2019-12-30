package saker.java.compiler.impl.signature.type;

import java.util.Objects;

import javax.lang.model.type.TypeKind;

public interface NoTypeSignature extends TypeSignature {
	public TypeKind getKind();

	@Override
	public default <R, P> R accept(TypeSignatureVisitor<R, P> v, P p) {
		return v.visitNoType(this, p);
	}

	@Override
	public default boolean signatureEquals(TypeSignature other) {
		if (!(other instanceof NoTypeSignature)) {
			return false;
		}
		return signatureEquals((NoTypeSignature) other);
	}

	public default boolean signatureEquals(NoTypeSignature other) {
		if (!TypeSignature.super.signatureEquals(other)) {
			return false;
		}
		if (!Objects.equals(getKind(), other.getKind())) {
			return false;
		}
		return true;
	}
}
