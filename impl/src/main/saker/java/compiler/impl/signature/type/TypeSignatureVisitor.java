package saker.java.compiler.impl.signature.type;

public interface TypeSignatureVisitor<R, P> {
	public R visitArray(ArrayTypeSignature type, P p);

	public R visitWildcard(WildcardTypeSignature type, P p);

	public R visitTypeVariable(TypeVariableTypeSignature type, P p);

	public R visitIntersection(IntersectionTypeSignature type, P p);

	public R visitNoType(NoTypeSignature type, P p);

	public R visitPrimitive(PrimitiveTypeSignature type, P p);

	public R visitUnion(UnionTypeSignature type, P p);

	public R visitUnknown(UnknownTypeSignature type, P p);

	public R visitUnresolved(UnresolvedTypeSignature type, P p);

	public R visitParameterized(ParameterizedTypeSignature type, P p);

	public R visitEncloser(ParameterizedTypeSignature type, P p);

	public R visitNull(NullTypeSignature type, P p);
}
