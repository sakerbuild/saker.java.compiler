package saker.java.compiler.impl.compile.signature.type.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.type.ParameterizedTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.signature.type.UnresolvedTypeSignature;

public class SimpleUnresolvedTypeSignature implements UnresolvedTypeSignature, Externalizable {
	private static final long serialVersionUID = 1L;

	protected String qualifiedName;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleUnresolvedTypeSignature() {
	}

	public SimpleUnresolvedTypeSignature(String qualifiedName) {
		this.qualifiedName = qualifiedName;
	}

	@Override
	public ParameterizedTypeSignature getEnclosingSignature() {
		return null;
	}

	@Override
	public String getUnresolvedName() {
		return qualifiedName;
	}

	@Override
	public String getSimpleName() {
		return qualifiedName;
	}

	@Override
	public Collection<? extends AnnotationSignature> getAnnotations() {
		return Collections.emptyList();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(qualifiedName);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
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
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleUnresolvedTypeSignature other = (SimpleUnresolvedTypeSignature) obj;
		if (!qualifiedName.equals(other.qualifiedName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return qualifiedName;
	}
}
