package saker.java.compiler.impl.signature.type;

import java.util.List;

public interface UnresolvedTypeSignature extends ParameterizedTypeSignature {

	public String getUnresolvedName();

	@Override
	public default <R, P> R accept(TypeSignatureVisitor<R, P> v, P p) {
		return v.visitUnresolved(this, p);
	}

	@Override
	public List<? extends TypeSignature> getTypeParameters();

	@Override
	public default boolean signatureEquals(TypeSignature other) {
		if (!(other instanceof UnresolvedTypeSignature)) {
			return false;
		}
		return signatureEquals((UnresolvedTypeSignature) other);
	}

	@Override
	public default boolean signatureEquals(ParameterizedTypeSignature other) {
		if (!(other instanceof UnresolvedTypeSignature)) {
			return false;
		}
		return signatureEquals((UnresolvedTypeSignature) other);
	}

	public default boolean signatureEquals(UnresolvedTypeSignature other) {
		if (!ParameterizedTypeSignature.super.signatureEquals(other)) {
			return false;
		}
		//XXX do we have to include the scope?
		if (!getUnresolvedName().equals(other.getUnresolvedName())) {
			return false;
		}
		return true;
	}

}
