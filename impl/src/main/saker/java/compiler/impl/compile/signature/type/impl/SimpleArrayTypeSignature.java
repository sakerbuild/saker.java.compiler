package saker.java.compiler.impl.compile.signature.type.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Collections;

import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.type.ArrayTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;

public class SimpleArrayTypeSignature implements ArrayTypeSignature, Externalizable {
	private static final long serialVersionUID = 1L;

	private TypeSignature componentType;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleArrayTypeSignature() {
	}

	public SimpleArrayTypeSignature(TypeSignature componentType) {
		this.componentType = componentType;
	}

	@Override
	public TypeSignature getEnclosingSignature() {
		return null;
	}

	@Override
	public String getSimpleName() {
		return null;
	}

	@Override
	public Collection<? extends AnnotationSignature> getAnnotations() {
		return Collections.emptyList();
	}

	@Override
	public TypeSignature getComponentType() {
		return componentType;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(componentType);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		componentType = (TypeSignature) in.readObject();
	}

	@Override
	public String toString() {
		return componentType + "[]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((componentType == null) ? 0 : componentType.hashCode());
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
		SimpleArrayTypeSignature other = (SimpleArrayTypeSignature) obj;
		if (componentType == null) {
			if (other.componentType != null)
				return false;
		} else if (!componentType.equals(other.componentType))
			return false;
		return true;
	}
}
