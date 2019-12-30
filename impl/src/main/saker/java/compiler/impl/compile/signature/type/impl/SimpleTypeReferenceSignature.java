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

public class SimpleTypeReferenceSignature implements ParameterizedTypeSignature, Externalizable {
	private static final long serialVersionUID = 1L;

	protected ParameterizedTypeSignature enclosingSignature;
	protected String simpleName;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleTypeReferenceSignature() {
	}

	public SimpleTypeReferenceSignature(ParameterizedTypeSignature enclosingSignature, String simpleName) {
		this.enclosingSignature = enclosingSignature;
		this.simpleName = simpleName;
	}

	@Override
	public String getSimpleName() {
		return simpleName;
	}

	@Override
	public Collection<? extends AnnotationSignature> getAnnotations() {
		return Collections.emptyList();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(enclosingSignature);
		out.writeUTF(simpleName);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		enclosingSignature = (ParameterizedTypeSignature) in.readObject();
		simpleName = in.readUTF();
	}

	@Override
	public List<? extends TypeSignature> getTypeParameters() {
		return Collections.emptyList();
	}

	@Override
	public ParameterizedTypeSignature getEnclosingSignature() {
		return enclosingSignature;
	}

	@Override
	public String toString() {
		return enclosingSignature + "." + simpleName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((enclosingSignature == null) ? 0 : enclosingSignature.hashCode());
		result = prime * result + ((simpleName == null) ? 0 : simpleName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleTypeReferenceSignature other = (SimpleTypeReferenceSignature) obj;
		if (enclosingSignature == null) {
			if (other.enclosingSignature != null)
				return false;
		} else if (!enclosingSignature.equals(other.enclosingSignature))
			return false;
		if (simpleName == null) {
			if (other.simpleName != null)
				return false;
		} else if (!simpleName.equals(other.simpleName))
			return false;
		return true;
	}

}
