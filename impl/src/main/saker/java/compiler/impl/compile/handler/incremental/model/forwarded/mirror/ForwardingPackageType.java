package saker.java.compiler.impl.compile.handler.incremental.model.forwarded.mirror;

import javax.lang.model.element.PackageElement;

import saker.java.compiler.impl.compile.handler.incremental.model.CommonPackageType;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;

public class ForwardingPackageType extends ForwardingTypeMirrorBase<CommonPackageType> implements CommonPackageType {

	public ForwardingPackageType(IncrementalElementsTypesBase elemTypes, CommonPackageType subject) {
		super(elemTypes, subject);
	}

	@Override
	public PackageElement getPackageElement() {
		return subject.getPackageElement();
	}

}
