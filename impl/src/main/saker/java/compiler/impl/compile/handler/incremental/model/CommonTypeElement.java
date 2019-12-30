package saker.java.compiler.impl.compile.handler.incremental.model;

import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;

public interface CommonTypeElement extends TypeElement {
	public Name getBinaryName();
}
