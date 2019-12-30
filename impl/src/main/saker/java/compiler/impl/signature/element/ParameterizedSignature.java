package saker.java.compiler.impl.signature.element;

import java.util.List;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.signature.type.TypeParameterTypeSignature;

public interface ParameterizedSignature {
	public List<? extends TypeParameterTypeSignature> getTypeParameters();

	public static boolean signatureEquals(ParameterizedSignature first, ParameterizedSignature other,
			SignatureNameChecker checkparameternames) {
		if (!ObjectUtils.collectionOrderedEquals(first.getTypeParameters(), other.getTypeParameters(),
				checkparameternames.typeParameterComparator)) {
			return false;
		}
		return true;
	}
}
