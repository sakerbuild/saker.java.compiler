package saker.java.compiler.util9.impl.forwarded.mirror;

import javax.lang.model.element.ModuleElement;

import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;
import saker.java.compiler.impl.compile.handler.incremental.model.forwarded.mirror.ForwardingTypeMirrorBase;
import saker.java.compiler.util9.impl.model.mirror.CommonModuleType;

public class ForwardedModuleType extends ForwardingTypeMirrorBase<CommonModuleType> implements CommonModuleType {

	public ForwardedModuleType(IncrementalElementsTypesBase elemTypes, CommonModuleType subject) {
		super(elemTypes, subject);
	}

	@Override
	public ModuleElement getModuleElement() {
		return subject.getModuleElement();
	}

}
