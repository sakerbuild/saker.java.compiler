package saker.java.compiler.impl.compile.signature.type.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.List;

import saker.java.compiler.impl.compile.signature.impl.AnnotatedSignatureImpl;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.type.ParameterizedTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.signature.type.UnresolvedTypeSignature;

public class AnnotatedUnresolvedTypeSignature extends AnnotatedSignatureImpl
		implements UnresolvedTypeSignature, Externalizable {
	private static final long serialVersionUID = 1L;

	protected String qualifiedName;

	/**
	 * For {@link Externalizable}.
	 */
	public AnnotatedUnresolvedTypeSignature() {
	}

	public AnnotatedUnresolvedTypeSignature(List<? extends AnnotationSignature> annotations, String qualifiedName) {
		super(annotations);
		this.qualifiedName = qualifiedName;
	}

	@Override
	public ParameterizedTypeSignature getEnclosingSignature() {
		return null;
	}

	@Override
	public final String getUnresolvedName() {
		return qualifiedName;
	}

	@Override
	public final String getSimpleName() {
		return qualifiedName;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeUTF(qualifiedName);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		qualifiedName = in.readUTF();
	}

	@Override
	public List<? extends TypeSignature> getTypeParameters() {
		return Collections.emptyList();
	}

	@Override
	public final int hashCode() {
		return qualifiedName.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AnnotatedUnresolvedTypeSignature other = (AnnotatedUnresolvedTypeSignature) obj;
		if (!qualifiedName.equals(other.qualifiedName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		ParameterizedTypeSignature enclosing = getEnclosingSignature();
		return super.toString()
				+ (enclosing == null ? getCanonicalName() : enclosing.toString() + "." + getSimpleName());
	}
}
