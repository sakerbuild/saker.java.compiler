package saker.java.compiler.impl.signature.type;

import saker.java.compiler.impl.signature.Signature;

public interface NameSignature extends Signature {
	public String getName();

	@Override
	public int hashCode();

	@Override
	public boolean equals(Object o);
}
