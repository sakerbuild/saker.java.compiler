package saker.java.compiler.jdk.impl.invoker;

import saker.java.compiler.api.processing.SakerElementsTypes;
import saker.java.compiler.util8.impl.invoker.ForwardingSakerElementsTypes8;

public class ForwardingSakerElementsTypes extends ForwardingSakerElementsTypes8 {
	public ForwardingSakerElementsTypes(SakerElementsTypes elementsTypes) {
		super(elementsTypes);
	}
}
