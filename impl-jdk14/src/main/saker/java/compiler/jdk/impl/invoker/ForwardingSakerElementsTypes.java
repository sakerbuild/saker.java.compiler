package saker.java.compiler.jdk.impl.invoker;

import saker.java.compiler.api.processing.SakerElementsTypes;
import saker.java.compiler.util14.impl.invoker.ForwardingSakerElementsTypes14;

public class ForwardingSakerElementsTypes extends ForwardingSakerElementsTypes14 {
	public ForwardingSakerElementsTypes(SakerElementsTypes elementsTypes) {
		super(elementsTypes);
	}
}
