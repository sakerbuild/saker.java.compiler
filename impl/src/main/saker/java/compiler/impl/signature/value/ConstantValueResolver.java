package saker.java.compiler.impl.signature.value;

import javax.lang.model.element.Element;

import saker.java.compiler.api.processing.SakerElementsTypes;

public interface ConstantValueResolver {
	public Object resolve(SakerElementsTypes elemtypes, Element resolutionelement);

	@Override
	public int hashCode();

	@Override
	public boolean equals(Object obj);

	@Override
	public String toString();

	public boolean signatureEquals(ConstantValueResolver other);

}
