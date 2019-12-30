package saker.java.compiler.impl.compile.signature.impl;

import java.io.Externalizable;
import java.util.Set;

import javax.lang.model.element.Modifier;

import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

public class FinalMethodParameterSignatureImpl extends MethodParameterSignatureImpl {
	private static final long serialVersionUID = 1L;

	/**
	 * For {@link Externalizable}.
	 */
	public FinalMethodParameterSignatureImpl() {
	}

	public FinalMethodParameterSignatureImpl(TypeSignature type, String name) {
		super(type, name);
	}

	@Override
	public Set<Modifier> getModifiers() {
		return IncrementalElementsTypes.MODIFIERS_FINAL;
	}
}
