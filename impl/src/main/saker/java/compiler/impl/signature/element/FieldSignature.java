package saker.java.compiler.impl.signature.element;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.signature.value.ConstantValueResolver;

public interface FieldSignature extends ClassMemberSignature {
	public TypeSignature getTypeSignature();

	public ConstantValueResolver getConstantValue();

	public boolean isEnumConstant();

	public static boolean signatureEquals(FieldSignature first, FieldSignature other) {
		if (!isOnlyInitializerChanged(first, other)) {
			return false;
		}
		return !first.isInitializerChanged(other);
	}

	public static boolean isOnlyInitializerChanged(FieldSignature first, FieldSignature other) {
		if (!ClassMemberSignature.signatureEquals(first, other)) {
			return false;
		}
		if (!ObjectUtils.objectsEquals(first.getTypeSignature(), other.getTypeSignature(),
				TypeSignature::signatureEquals)) {
			return false;
		}
		if (first.isEnumConstant() != other.isEnumConstant()) {
			return false;
		}
		return true;
	}

	public default boolean isInitializerChanged(FieldSignature other) {
		return !ObjectUtils.objectsEquals(getConstantValue(), other.getConstantValue(),
				ConstantValueResolver::signatureEquals);
	}
}
