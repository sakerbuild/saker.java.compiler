package saker.java.compiler.impl.compat.element;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

public interface RecordComponentElementCompat {
	public Element getRealObject();

	public ExecutableElement getAccessor();

	//getEnclosingElement and getSimpleName are from getRealObject
}
