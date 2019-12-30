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

public class SimpleSuperWildcardTypeSignature implements WildcardTypeSignature, Externalizable {
	private static final long serialVersionUID = 1L;

	protected TypeSignature lowerBounds;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleSuperWildcardTypeSignature() {
	}

	public SimpleSuperWildcardTypeSignature(TypeSignature lowerBounds) {
		this.lowerBounds = lowerBounds;
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
		return null;
	}

	@Override
	public TypeSignature getLowerBounds() {
		return lowerBounds;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(lowerBounds);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		lowerBounds = (TypeSignature) in.readObject();
	}

	@Override
	public String toString() {
		return "? super " + lowerBounds;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((lowerBounds == null) ? 0 : lowerBounds.hashCode());
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
		SimpleSuperWildcardTypeSignature other = (SimpleSuperWildcardTypeSignature) obj;
		if (lowerBounds == null) {
			if (other.lowerBounds != null)
				return false;
		} else if (!lowerBounds.equals(other.lowerBounds))
			return false;
		return true;
	}

}
