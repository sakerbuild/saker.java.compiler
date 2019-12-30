package saker.java.compiler.impl.signature.type;

public interface ArrayTypeSignature extends TypeSignature {
	public TypeSignature getComponentType();

	@Override
	public default <R, P> R accept(TypeSignatureVisitor<R, P> v, P p) {
		return v.visitArray(this, p);
	}

	@Override
	public default boolean signatureEquals(TypeSignature other) {
		if (!(other instanceof ArrayTypeSignature)) {
			return false;
		}
		return signatureEquals((ArrayTypeSignature) other);
	}

	public default boolean signatureEquals(ArrayTypeSignature other) {
		if (!TypeSignature.super.signatureEquals(other)) {
			return false;
		}
		if (!getComponentType().signatureEquals(other.getComponentType())) {
			return false;
		}
		return true;
	}
}
