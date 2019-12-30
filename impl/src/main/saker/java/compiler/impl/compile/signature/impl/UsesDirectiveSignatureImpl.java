package saker.java.compiler.impl.compile.signature.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.java.compiler.impl.signature.element.ModuleSignature.UsesDirectiveSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;

public class UsesDirectiveSignatureImpl implements UsesDirectiveSignature, Externalizable {
	private static final long serialVersionUID = 1L;

	private TypeSignature service;

	/**
	 * For {@link Externalizable}.
	 */
	public UsesDirectiveSignatureImpl() {
	}

	public UsesDirectiveSignatureImpl(TypeSignature service) {
		this.service = service;
	}

	@Override
	public TypeSignature getService() {
		return service;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(service);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		service = (TypeSignature) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((service == null) ? 0 : service.hashCode());
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
		UsesDirectiveSignatureImpl other = (UsesDirectiveSignatureImpl) obj;
		if (service == null) {
			if (other.service != null)
				return false;
		} else if (!service.equals(other.service))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "uses " + service;
	}

}