package saker.java.compiler.impl.compile.signature.type.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Collections;

import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.type.TypeParameterTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;

public class SimpleTypeParameterTypeSignature implements TypeParameterTypeSignature, Externalizable {
	private static final long serialVersionUID = 1L;

	protected String varName;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleTypeParameterTypeSignature() {
	}

	public SimpleTypeParameterTypeSignature(String varName) {
		this.varName = varName;
	}

	@Override
	public final String getVarName() {
		return varName;
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
		return null;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(varName);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		varName = in.readUTF();
	}

	@Override
	public String toString() {
		return varName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((varName == null) ? 0 : varName.hashCode());
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
		SimpleTypeParameterTypeSignature other = (SimpleTypeParameterTypeSignature) obj;
		if (varName == null) {
			if (other.varName != null)
				return false;
		} else if (!varName.equals(other.varName))
			return false;
		return true;
	}
}
