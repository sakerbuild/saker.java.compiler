package saker.java.compiler.impl.compile.signature.type.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.List;

import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.impl.signature.type.TypeSignature;

public class SimpleParameterizedUnresolvedTypeSignature extends SimpleUnresolvedTypeSignature {
	private static final long serialVersionUID = 1L;

	protected List<? extends TypeSignature> typeParameters = Collections.emptyList();

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleParameterizedUnresolvedTypeSignature() {
	}

	public SimpleParameterizedUnresolvedTypeSignature(String qualifiedName,
			List<? extends TypeSignature> typeParameters) {
		super(qualifiedName);
		this.typeParameters = typeParameters;
	}

	@Override
	public List<? extends TypeSignature> getTypeParameters() {
		return typeParameters;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		SerialUtils.writeExternalCollection(out, typeParameters);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		typeParameters = SerialUtils.readExternalImmutableList(in);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleParameterizedUnresolvedTypeSignature other = (SimpleParameterizedUnresolvedTypeSignature) obj;
		if (!typeParameters.equals(other.typeParameters))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return super.toString() + StringUtils.toStringJoin("<", ", ", typeParameters, ">");
	}
}
