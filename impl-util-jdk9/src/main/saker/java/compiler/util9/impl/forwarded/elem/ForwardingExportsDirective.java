package saker.java.compiler.util9.impl.forwarded.elem;

import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.ModuleElement.ExportsDirective;
import javax.lang.model.element.PackageElement;

import saker.java.compiler.util9.impl.model.IncrementalElementsTypes9;

public class ForwardingExportsDirective extends ForwardingDirectiveBase<ExportsDirective> implements ExportsDirective {
	private static final AtomicReferenceFieldUpdater<ForwardingExportsDirective, PackageElement> ARFU_packageElement = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingExportsDirective.class, PackageElement.class, "packageElement");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<ForwardingExportsDirective, List> ARFU_targetModules = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingExportsDirective.class, List.class, "targetModules");

	private volatile transient PackageElement packageElement;
	private volatile transient List<? extends ModuleElement> targetModules;

	public ForwardingExportsDirective(IncrementalElementsTypes9 elemTypes, ExportsDirective subject) {
		super(elemTypes, subject);
	}

	@Override
	public PackageElement getPackage() {
		PackageElement thisval = this.packageElement;
		if (thisval != null) {
			return thisval;
		}
		thisval = elemTypes.forwardElement(subject::getPackage);
		if (ARFU_packageElement.compareAndSet(this, null, thisval)) {
			return thisval;
		}
		return this.packageElement;
	}

	@Override
	public List<? extends ModuleElement> getTargetModules() {
		List<? extends ModuleElement> thisval = this.targetModules;
		if (thisval != null) {
			return thisval;
		}
		thisval = elemTypes.forwardElements(subject::getTargetModules);
		if (ARFU_targetModules.compareAndSet(this, null, thisval)) {
			return thisval;
		}
		return this.targetModules;
	}

}
