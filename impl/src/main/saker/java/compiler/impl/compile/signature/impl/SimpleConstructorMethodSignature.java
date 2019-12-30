package saker.java.compiler.impl.compile.signature.impl;

import java.io.Externalizable;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

import saker.java.compiler.impl.compile.signature.type.impl.NoTypeSignatureImpl;
import saker.java.compiler.impl.signature.element.MethodParameterSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

public class SimpleConstructorMethodSignature extends MethodSignatureBase {
	private static final long serialVersionUID = 1L;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleConstructorMethodSignature() {
	}

	public SimpleConstructorMethodSignature(Set<Modifier> modifiers,
			List<? extends MethodParameterSignature> parameters) {
		super(modifiers, parameters);
	}

	@Override
	public String getSimpleName() {
		return IncrementalElementsTypes.CONSTRUCTOR_METHOD_NAME;
	}

	@Override
	public ElementKind getKind() {
		return ElementKind.CONSTRUCTOR;
	}

	@Override
	public TypeSignature getReturnType() {
		return NoTypeSignatureImpl.getVoid();
	}

}
