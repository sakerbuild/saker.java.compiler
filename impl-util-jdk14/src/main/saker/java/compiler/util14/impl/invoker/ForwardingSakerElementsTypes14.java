package saker.java.compiler.util14.impl.invoker;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.RecordComponentElement;

import saker.java.compiler.api.processing.SakerElementsTypes;
import saker.java.compiler.util9.impl.invoker.ForwardingSakerElementsTypes9;

public class ForwardingSakerElementsTypes14 extends ForwardingSakerElementsTypes9 {

	public ForwardingSakerElementsTypes14(SakerElementsTypes elementsTypes) {
		super(elementsTypes);
	}

	@Override
	public RecordComponentElement recordComponentFor(ExecutableElement accessor) {
		return elementsTypes.recordComponentFor(accessor);
	}
}
