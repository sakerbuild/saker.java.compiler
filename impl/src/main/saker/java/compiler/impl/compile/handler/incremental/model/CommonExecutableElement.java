package saker.java.compiler.impl.compile.handler.incremental.model;

import javax.lang.model.element.ExecutableElement;

public interface CommonExecutableElement extends ExecutableElement, CommonElement {
	public boolean isBridge();
}
