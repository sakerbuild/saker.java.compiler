package saker.java.compiler.impl.compile.signature.type.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Collections;

import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.signature.type.WildcardTypeSignature;

public class SimpleExtendsWildcardTypeSignature implements WildcardTypeSignature, Externalizable {
	private static final long serialVersionUID = 1L;

	protected TypeSignature upperBounds;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleExtendsWildcardTypeSignature() {
	}

	public SimpleExtendsWildcardTypeSignature(TypeSignature upperBounds) {
		this.upperBounds = upperBounds;
	}

	@Override
	public TypeSignature getEnclosingSignature() {
		return null;
	}

	@Override
	public String getSimpleName() {
		return "?";
	}

	@Override
	public Collection<? extends AnnotationSignature> getAnnotations() {
		return Collections.emptyList();
	}

	@Override
	public TypeSignature getUpperBounds() {
		return upperBounds;
	}

	@Override
	public TypeSignature getLowerBounds() {
		return null;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(upperBounds);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		upperBounds = (TypeSignature) in.readObject();
	}

	@Override
	public String toString() {
		return "? extends " + upperBounds;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((upperBounds == null) ? 0 : upperBounds.hashCode());
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
		SimpleExtendsWildcardTypeSignature other = (SimpleExtendsWildcardTypeSignature) obj;
		if (upperBounds == null) {
			if (other.upperBounds != null)
				return false;
		} else if (!upperBounds.equals(other.upperBounds))
			return false;
		return true;
	}

}
