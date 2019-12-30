package saker.java.compiler.util9.impl.model.elem;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.ModuleElement.DirectiveVisitor;
import javax.lang.model.element.ModuleElement.RequiresDirective;

import saker.java.compiler.impl.signature.element.ModuleSignature.RequiresDirectiveSignature;
import saker.java.compiler.util9.impl.model.IncrementalElementsTypes9;

public class IncrementalRequiresDirective extends IncrementalDirectiveBase<RequiresDirectiveSignature> implements RequiresDirective {
	private static final AtomicReferenceFieldUpdater<IncrementalRequiresDirective, ModuleElement> ARFU_dependency = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalRequiresDirective.class, ModuleElement.class, "dependency");

	private transient volatile ModuleElement dependency;

	public IncrementalRequiresDirective(IncrementalElementsTypes9 elemTypes, RequiresDirectiveSignature signature, IncrementalModuleElement module) {
		super(elemTypes, signature, module);
	}

	@Override
	public <R, P> R accept(DirectiveVisitor<R, P> v, P p) {
		return v.visitRequires(this, p);
	}

	@Override
	public ModuleElement getDependency() {
		ModuleElement thisdependency = this.dependency;
		if (thisdependency != null) {
			return thisdependency;
		}
		thisdependency = elemTypes.getModuleElement(signature.getDependencyModule().getName());
		if (ARFU_dependency.compareAndSet(this, null, thisdependency)) {
			return thisdependency;
		}
		return this.dependency;
	}

	@Override
	public boolean isStatic() {
		return signature.isStatic();
	}

	@Override
	public boolean isTransitive() {
		return signature.isTransitive();
	}

}
