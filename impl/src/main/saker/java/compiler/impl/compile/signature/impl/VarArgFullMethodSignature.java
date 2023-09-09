package saker.java.compiler.impl.compile.signature.impl;

import java.io.Externalizable;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

import saker.java.compiler.impl.signature.element.MethodParameterSignature;
import saker.java.compiler.impl.signature.type.TypeParameterSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;

public final class VarArgFullMethodSignature extends FullMethodSignature {
	private static final long serialVersionUID = 1L;

	/**
	 * For {@link Externalizable}.
	 */
	public VarArgFullMethodSignature() {
	}

	public VarArgFullMethodSignature(Set<Modifier> modifiers, List<MethodParameterSignature> parameters,
			TypeSignature returnType, String name, List<TypeParameterSignature> typeParameters,
			List<TypeSignature> throwsTypes, ElementKind methodKind, TypeSignature receiverParameter,
			String docComment) {
		super(modifiers, parameters, returnType, name, typeParameters, throwsTypes, methodKind, receiverParameter,
				docComment);
	}

	@Override
	public boolean isVarArg() {
		return true;
	}

}
