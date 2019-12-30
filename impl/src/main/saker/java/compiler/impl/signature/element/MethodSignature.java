package saker.java.compiler.impl.signature.element;

import java.util.List;
import java.util.Objects;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.signature.type.TypeSignature;

public interface MethodSignature extends ClassMemberSignature, ParameterizedSignature {
	public TypeSignature getReturnType();

	public List<? extends MethodParameterSignature> getParameters();

	public List<? extends TypeSignature> getThrowingTypes();

	public AnnotationSignature.Value getDefaultValue();

	public TypeSignature getReceiverParameter();

	public boolean isVarArg();

	public static boolean signatureEquals(MethodSignature first, MethodSignature other,
			SignatureNameChecker checkparameternames) {
		if (!ClassMemberSignature.signatureEquals(first, other)) {
			return false;
		}
		if (!ParameterizedSignature.signatureEquals(first, other, checkparameternames)) {
			return false;
		}
		if (!Objects.equals(first.getSimpleName(), other.getSimpleName())) {
			return false;
		}
		if (!Objects.equals(first.getModifiers(), other.getModifiers())) {
			return false;
		}
		if (!ObjectUtils.objectsEquals(first.getReturnType(), other.getReturnType(), TypeSignature::signatureEquals)) {
			return false;
		}
		if (!ObjectUtils.collectionOrderedEquals(first.getParameters(), other.getParameters(),
				checkparameternames.methodParameterComparator)) {
			return false;
		}
		if (!ObjectUtils.collectionOrderedEquals(first.getThrowingTypes(), other.getThrowingTypes(),
				TypeSignature::signatureEquals)) {
			return false;
		}
		if (!ObjectUtils.objectsEquals(first.getDefaultValue(), other.getDefaultValue(),
				AnnotationSignature.Value::signatureEquals)) {
			return false;
		}
		if (!ObjectUtils.objectsEquals(first.getReceiverParameter(), other.getReceiverParameter(),
				TypeSignature::signatureEquals)) {
			return false;
		}
		if (!Objects.equals(first.isVarArg(), other.isVarArg())) {
			return false;
		}
		return true;
	}
}
