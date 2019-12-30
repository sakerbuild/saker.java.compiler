package saker.java.compiler.impl.signature.type;

public interface NullTypeSignature extends TypeSignature {
	@Override
	public default <R, P> R accept(TypeSignatureVisitor<R, P> v, P p) {
		return v.visitNull(this, p);
	}

	@Override
	public default String getSimpleName() {
		return "null";
	}
}
