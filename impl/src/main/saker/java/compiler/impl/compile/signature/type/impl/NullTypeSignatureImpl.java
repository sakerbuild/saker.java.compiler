package saker.java.compiler.impl.compile.signature.type.impl;

import java.io.Externalizable;
import java.util.List;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.compile.signature.impl.AnnotatedSignatureImpl;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.type.NullTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;

public class NullTypeSignatureImpl extends AnnotatedSignatureImpl implements NullTypeSignature {
	private static final long serialVersionUID = 1L;

	private static final NullTypeSignature INSTANCE = new NullTypeSignatureImpl();

	/**
	 * For {@link Externalizable}.
	 */
	public NullTypeSignatureImpl() {
	}

	public static NullTypeSignature create() {
		return INSTANCE;
	}

	public static NullTypeSignature create(List<AnnotationSignature> annotations) {
		if (ObjectUtils.isNullOrEmpty(annotations)) {
			return INSTANCE;
		}
		return new NullTypeSignatureImpl(annotations);
	}

	private NullTypeSignatureImpl(List<AnnotationSignature> annotations) {
		super(annotations);
	}

	@Override
	public TypeSignature getEnclosingSignature() {
		return null;
	}

}
