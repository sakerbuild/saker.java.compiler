package saker.java.compiler.util9.impl.model.elem;

import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

import saker.java.compiler.impl.compile.handler.incremental.model.IncrementallyModelled;

public interface CommonModuleElement extends ModuleElement, IncrementallyModelled {
	public TypeElement getTypeElement(String name);

	public PackageElement getPackageElement(String name);
}
