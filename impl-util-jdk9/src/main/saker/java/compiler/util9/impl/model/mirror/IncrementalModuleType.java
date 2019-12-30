package saker.java.compiler.util9.impl.model.mirror;

import javax.lang.model.element.Element;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;

import saker.java.compiler.impl.compile.handler.incremental.model.mirror.IncrementalTypeMirror;
import saker.java.compiler.impl.signature.element.ModuleSignature;
import saker.java.compiler.util9.impl.model.IncrementalElementsTypes9;
import saker.java.compiler.util9.impl.model.elem.IncrementalModuleElement;

public class IncrementalModuleType extends IncrementalTypeMirror<ModuleSignature> implements CommonModuleType {

	private IncrementalModuleElement module;

	public IncrementalModuleType(IncrementalElementsTypes9 elemTypes, IncrementalModuleElement module) {
		super(elemTypes, module.getSignature());
		this.module = module;
		setElementTypes(IncrementalElementsTypes9.ELEMENT_TYPE_MODULE);
	}

	@Override
	protected Element getEnclosingResolutionElement() {
		return module;
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		return v.visitNoType(this, p);
	}

	@Override
	public TypeKind getKind() {
		return TypeKind.MODULE;
	}

	@Override
	public ModuleElement getModuleElement() {
		return module;
	}

}
