package saker.java.compiler.impl.compile.signature.type.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.List;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.compile.signature.impl.AnnotatedSignatureImpl;
import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.type.CanonicalTypeSignature;
import saker.java.compiler.impl.signature.type.ParameterizedTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;

public class AnnotatedCanonicalTypeSignature extends AnnotatedSignatureImpl implements CanonicalTypeSignature {
	private static final long serialVersionUID = 1L;

	protected String canonicalName;

	/**
	 * For {@link Externalizable}.
	 */
	public AnnotatedCanonicalTypeSignature() {
	}

	protected AnnotatedCanonicalTypeSignature(List<? extends AnnotationSignature> annotations, String canonicalName) {
		super(annotations);
		this.canonicalName = canonicalName;
	}

	public static CanonicalTypeSignature create(List<? extends AnnotationSignature> annotations, String canonicalName) {
		if (ObjectUtils.isNullOrEmpty(annotations)) {
			return new SimpleCanonicalTypeSignature(canonicalName);
		}
		return new AnnotatedCanonicalTypeSignature(annotations, canonicalName);
	}

	public static CanonicalTypeSignature create(ParserCache cache, List<? extends AnnotationSignature> annotations,
			String canonicalName) {
		if (ObjectUtils.isNullOrEmpty(annotations)) {
			return cache.canonicalTypeSignature(canonicalName);
		}
		return new AnnotatedCanonicalTypeSignature(annotations, canonicalName);
	}

	@Override
	public List<? extends TypeSignature> getTypeParameters() {
		return Collections.emptyList();
	}

	@Override
	public ParameterizedTypeSignature getEnclosingSignature() {
		return null;
	}

	@Override
	public String getSimpleName() {
		return canonicalName.substring(canonicalName.indexOf('.') + 1);
	}

	@Override
	public String getCanonicalName() {
		return canonicalName;
	}

	@Override
	public String getName() {
		return canonicalName;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(canonicalName);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		canonicalName = (String) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((canonicalName == null) ? 0 : canonicalName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AnnotatedCanonicalTypeSignature other = (AnnotatedCanonicalTypeSignature) obj;
		if (canonicalName == null) {
			if (other.canonicalName != null)
				return false;
		} else if (!canonicalName.equals(other.canonicalName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return super.toString() + (getEnclosingSignature() == null ? getCanonicalName()
				: getEnclosingSignature().toString() + "." + getSimpleName());
	}
}
