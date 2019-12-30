package saker.java.compiler.impl.signature.type;

import java.util.Objects;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.signature.element.AnnotatedSignature;

public interface TypeSignature extends AnnotatedSignature {
	public TypeSignature getEnclosingSignature();

	public String getSimpleName();

	public default String getName() {
		TypeSignature enclosing = getEnclosingSignature();
		if (enclosing == null) {
			return getSimpleName();
		}
		return enclosing.getName() + "$" + getSimpleName();
	}

	public default String getCanonicalName() {
		TypeSignature enclosing = getEnclosingSignature();
		if (enclosing == null) {
			return getSimpleName();
		}
		return enclosing.getCanonicalName() + "." + getSimpleName();
	}

	public <R, P> R accept(TypeSignatureVisitor<R, P> v, P p);

	public default boolean signatureEquals(TypeSignature other) {
		if (!Objects.equals(getSimpleName(), other.getSimpleName())) {
			return false;
		}
		if (!ObjectUtils.objectsEquals(getEnclosingSignature(), other.getEnclosingSignature(),
				TypeSignature::signatureEquals)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode();
}
