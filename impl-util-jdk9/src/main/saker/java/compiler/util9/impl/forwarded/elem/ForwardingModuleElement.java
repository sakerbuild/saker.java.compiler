package saker.java.compiler.util9.impl.forwarded.elem;

import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

import saker.java.compiler.impl.compile.handler.incremental.model.forwarded.elem.ForwardingElementBase;
import saker.java.compiler.util9.impl.model.IncrementalElementsTypes9;
import saker.java.compiler.util9.impl.model.elem.CommonModuleElement;

public class ForwardingModuleElement extends ForwardingElementBase<ModuleElement> implements CommonModuleElement {
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<ForwardingModuleElement, List> ARFU_directives = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingModuleElement.class, List.class, "directives");
	private static final AtomicReferenceFieldUpdater<ForwardingModuleElement, Name> ARFU_qualifiedName = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingModuleElement.class, Name.class, "qualifiedName");
	private static final AtomicIntegerFieldUpdater<ForwardingModuleElement> AIFU_open = AtomicIntegerFieldUpdater
			.newUpdater(ForwardingModuleElement.class, "open");

	private volatile transient List<? extends Directive> directives;
	private volatile transient Name qualifiedName;
	private volatile int open = -1;

	public ForwardingModuleElement(IncrementalElementsTypes9 elemTypes, ModuleElement subject) {
		super(elemTypes, subject);
		setElementKind(ElementKind.MODULE);
	}

	public void setQualifiedName(Name qualifiedName) {
		this.qualifiedName = qualifiedName;
	}

	@Override
	public List<? extends Directive> getDirectives() {
		List<? extends Directive> thisdirectives = this.directives;
		if (thisdirectives != null) {
			return thisdirectives;
		}
		thisdirectives = ((IncrementalElementsTypes9) elemTypes).forwardDirectives(subject::getDirectives);
		if (ARFU_directives.compareAndSet(this, null, thisdirectives)) {
			return thisdirectives;
		}
		return this.directives;
	}

	@Override
	public Name getQualifiedName() {
		Name thisname = this.qualifiedName;
		if (thisname != null) {
			return thisname;
		}
		thisname = elemTypes.javac(subject::getQualifiedName);
		if (ARFU_qualifiedName.compareAndSet(this, null, thisname)) {
			return thisname;
		}
		return this.qualifiedName;
	}

	@Override
	public boolean isOpen() {
		int thisval = this.open;
		if (thisval >= 0) {
			return thisval != 0;
		}
		thisval = elemTypes.javac(subject::isOpen) ? 1 : 0;
		if (AIFU_open.compareAndSet(this, -1, thisval)) {
			return thisval != 0;
		}
		return this.open != 0;
	}

	@Override
	public boolean isUnnamed() {
		return getQualifiedName().length() == 0;
	}

	@Override
	public TypeElement getTypeElement(String name) {
		return elemTypes.forwardElementElems(el -> el.getTypeElement(subject, name));
	}

	@Override
	public PackageElement getPackageElement(String name) {
		return elemTypes.forwardElementElems(el -> el.getPackageElement(subject, name));
	}

}
