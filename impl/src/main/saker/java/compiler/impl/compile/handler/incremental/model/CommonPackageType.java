package saker.java.compiler.impl.compile.handler.incremental.model;

import javax.lang.model.element.PackageElement;
import javax.lang.model.type.NoType;

public interface CommonPackageType extends NoType {
	public PackageElement getPackageElement();
}
