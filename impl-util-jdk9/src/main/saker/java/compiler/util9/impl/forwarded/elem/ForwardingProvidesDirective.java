package saker.java.compiler.util9.impl.forwarded.elem;

import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.ModuleElement.ProvidesDirective;
import javax.lang.model.element.TypeElement;

import saker.java.compiler.util9.impl.model.IncrementalElementsTypes9;

public class ForwardingProvidesDirective extends ForwardingDirectiveBase<ProvidesDirective> implements ProvidesDirective {
	private static final AtomicReferenceFieldUpdater<ForwardingProvidesDirective, TypeElement> ARFU_service = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingProvidesDirective.class, TypeElement.class, "service");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<ForwardingProvidesDirective, List> ARFU_implementations = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingProvidesDirective.class, List.class, "implementations");

	private volatile transient TypeElement service;
	private volatile transient List<? extends TypeElement> implementations;

	public ForwardingProvidesDirective(IncrementalElementsTypes9 elemTypes, ProvidesDirective subject) {
		super(elemTypes, subject);
	}

	@Override
	public TypeElement getService() {
		TypeElement thisval = this.service;
		if (thisval != null) {
			return thisval;
		}
		thisval = elemTypes.forwardElement(subject::getService);
		if (ARFU_service.compareAndSet(this, null, thisval)) {
			return thisval;
		}
		return this.service;
	}

	@Override
	public List<? extends TypeElement> getImplementations() {
		List<? extends TypeElement> thisval = this.implementations;
		if (thisval != null) {
			return thisval;
		}
		thisval = elemTypes.forwardElements(subject::getImplementations);
		if (ARFU_implementations.compareAndSet(this, null, thisval)) {
			return thisval;
		}
		return this.implementations;
	}

}
