/*
 * Copyright (C) 2020 Bence Sipka
 *
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
