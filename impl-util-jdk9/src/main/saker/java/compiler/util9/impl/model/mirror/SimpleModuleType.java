package saker.java.compiler.util9.impl.model.mirror;

import javax.lang.model.element.ModuleElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeVisitor;

import saker.java.compiler.impl.compile.handler.incremental.model.mirror.SimpleTypeMirror;
import saker.java.compiler.util9.impl.model.IncrementalElementsTypes9;

public class SimpleModuleType extends SimpleTypeMirror implements CommonModuleType {
	private ModuleElement moduleElement;

	public SimpleModuleType(IncrementalElementsTypes9 elemTypes, ModuleElement moduleElement) {
		super(elemTypes);
		this.moduleElement = moduleElement;
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
		return moduleElement;
	}

}
