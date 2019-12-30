package saker.java.compiler.impl.compile.signature.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.impl.signature.element.ModuleSignature.ProvidesDirectiveSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;

public class ProvidesDirectiveSignatureImpl implements ProvidesDirectiveSignature, Externalizable {
	private static final long serialVersionUID = 1L;

	private TypeSignature service;
	private List<? extends TypeSignature> implementationTypes;

	/**
	 * For {@link Externalizable}.
	 */
	public ProvidesDirectiveSignatureImpl() {
	}

	public ProvidesDirectiveSignatureImpl(TypeSignature service, List<? extends TypeSignature> implementationTypes) {
		this.service = service;
		this.implementationTypes = implementationTypes;
	}

	@Override
	public TypeSignature getService() {
		return service;
	}

	@Override
	public List<? extends TypeSignature> getImplementationTypes() {
		return implementationTypes;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(service);
		SerialUtils.writeExternalCollection(out, implementationTypes);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		service = (TypeSignature) in.readObject();
		implementationTypes = SerialUtils.readExternalImmutableList(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((implementationTypes == null) ? 0 : implementationTypes.hashCode());
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
		ProvidesDirectiveSignatureImpl other = (ProvidesDirectiveSignatureImpl) obj;
		if (implementationTypes == null) {
			if (other.implementationTypes != null)
				return false;
		} else if (!implementationTypes.equals(other.implementationTypes))
			return false;
		if (service == null) {
			if (other.service != null)
				return false;
		} else if (!service.equals(other.service))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "provides " + service + " with " + StringUtils.toStringJoin(", ", implementationTypes);
	}

}
