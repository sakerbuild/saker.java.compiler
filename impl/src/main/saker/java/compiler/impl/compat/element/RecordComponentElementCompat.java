package saker.java.compiler.impl.compat.element;

import javax.lang.model.element.ExecutableElement;

public interface RecordComponentElementCompat extends ElementCompat {

	public ExecutableElement getAccessor();

	//getEnclosingElement and getSimpleName are from getRealObject
}
