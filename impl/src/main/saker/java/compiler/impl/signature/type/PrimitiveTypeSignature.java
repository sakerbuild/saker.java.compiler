package saker.java.compiler.impl.signature.type;

import java.util.Objects;

import javax.lang.model.type.TypeKind;

public interface PrimitiveTypeSignature extends TypeSignature {
	public TypeKind getTypeKind();

	@Override
	public default <R, P> R accept(TypeSignatureVisitor<R, P> v, P p) {
		return v.visitPrimitive(this, p);
	}

	@Override
	public default boolean signatureEquals(TypeSignature other) {
		if (!(other instanceof PrimitiveTypeSignature)) {
			return false;
		}
		return signatureEquals((PrimitiveTypeSignature) other);
	}

	public default boolean signatureEquals(PrimitiveTypeSignature other) {
		if (!TypeSignature.super.signatureEquals(other)) {
			return false;
		}
		if (!Objects.equals(getTypeKind(), other.getTypeKind())) {
			return false;
		}
		return true;
	}
}
