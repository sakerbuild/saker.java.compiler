package saker.java.compiler.impl.compile.handler.incremental.model.forwarded;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;

public class ForwardingJavacObjectBase<IET extends IncrementalElementsTypesBase, E> implements ForwardingObject<E> {
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<ForwardingJavacObjectBase, String> ARFU_toString = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingJavacObjectBase.class, String.class, "toString");

	protected IET elemTypes;
	protected E subject;

	private volatile transient String toString;

	public ForwardingJavacObjectBase(IET elemTypes, E subject) {
		this.elemTypes = elemTypes;
		this.subject = subject;
	}

	@Override
	public String toString() {
		String thistostring = this.toString;
		if (thistostring != null) {
			return thistostring;
		}
		thistostring = elemTypes.javac(subject::toString);
		if (ARFU_toString.compareAndSet(this, null, thistostring)) {
			return thistostring;
		}
		return this.toString;
	}

	@Override
	public E getForwardedSubject() {
		return subject;
	}
}
