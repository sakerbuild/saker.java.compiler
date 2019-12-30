package saker.java.compiler.util9.impl.forwarded.elem;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.ModuleElement.UsesDirective;
import javax.lang.model.element.TypeElement;

import saker.java.compiler.util9.impl.model.IncrementalElementsTypes9;

public class ForwardingUsesDirective extends ForwardingDirectiveBase<UsesDirective> implements UsesDirective {
	private static final AtomicReferenceFieldUpdater<ForwardingUsesDirective, TypeElement> ARFU_service = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingUsesDirective.class, TypeElement.class, "service");

	private volatile transient TypeElement service;

	public ForwardingUsesDirective(IncrementalElementsTypes9 elemTypes, UsesDirective subject) {
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

}
