package saker.java.compiler.impl.compile.handler.incremental.model;

import javax.lang.model.element.Element;

public interface CommonElement extends Element {
	public boolean isDeprecated();
}
