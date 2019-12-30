package saker.java.compiler.impl.compile.signature.value;

import javax.lang.model.element.Element;

import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.signature.value.ConstantValueResolver;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

public interface EnumOrConstantValueResolver extends ConstantValueResolver {
	public String getIdentifier();

	public TypeSignature getType(IncrementalElementsTypes elemtypes, Element resolutionelement);
}
