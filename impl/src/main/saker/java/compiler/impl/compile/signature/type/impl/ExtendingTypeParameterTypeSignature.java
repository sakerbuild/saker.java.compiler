package saker.java.compiler.impl.compile.signature.type.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.java.compiler.impl.signature.type.TypeSignature;

public class ExtendingTypeParameterTypeSignature extends SimpleTypeParameterTypeSignature {
	private static final long serialVersionUID = 1L;

	protected TypeSignature upperBounds;

	/**
	 * For {@link Externalizable};
	 */
	public ExtendingTypeParameterTypeSignature() {
	}

	public ExtendingTypeParameterTypeSignature(String varName, TypeSignature upperBounds) {
		super(varName);
		this.upperBounds = upperBounds;
	}

	@Override
	public final TypeSignature getUpperBounds() {
		return upperBounds;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(upperBounds);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		upperBounds = (TypeSignature) in.readObject();
	}

	@Override
	public String toString() {
		if (upperBounds == null) {
			return super.toString();
		}
		return super.toString() + " extends " + upperBounds;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((upperBounds == null) ? 0 : upperBounds.hashCode());
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
		ExtendingTypeParameterTypeSignature other = (ExtendingTypeParameterTypeSignature) obj;
		if (upperBounds == null) {
			if (other.upperBounds != null)
				return false;
		} else if (!upperBounds.equals(other.upperBounds))
			return false;
		return true;
	}

}
