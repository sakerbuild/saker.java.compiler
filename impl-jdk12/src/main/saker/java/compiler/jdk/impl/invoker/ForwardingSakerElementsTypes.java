package saker.java.compiler.jdk.impl.invoker;

import saker.java.compiler.api.processing.SakerElementsTypes;
import saker.java.compiler.util9.impl.invoker.ForwardingSakerElementsTypes9;

public class ForwardingSakerElementsTypes extends ForwardingSakerElementsTypes9 {
	public ForwardingSakerElementsTypes(SakerElementsTypes elementsTypes) {
		super(elementsTypes);
	}
}
