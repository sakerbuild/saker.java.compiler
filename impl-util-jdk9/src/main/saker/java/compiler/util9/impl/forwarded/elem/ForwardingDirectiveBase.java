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
package saker.java.compiler.util9.impl.forwarded.elem;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.ModuleElement.Directive;
import javax.lang.model.element.ModuleElement.DirectiveKind;
import javax.lang.model.element.ModuleElement.DirectiveVisitor;

import saker.java.compiler.impl.compile.handler.incremental.model.forwarded.ForwardingJavacObjectBase;
import saker.java.compiler.util9.impl.model.IncrementalElementsTypes9;
import saker.java.compiler.util9.impl.model.KindBasedDirectiveVisitor9;

public class ForwardingDirectiveBase<E extends Directive> extends ForwardingJavacObjectBase<IncrementalElementsTypes9, E> implements Directive {
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<ForwardingDirectiveBase, String> ARFU_toString = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingDirectiveBase.class, String.class, "toString");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<ForwardingDirectiveBase, DirectiveKind> ARFU_kind = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingDirectiveBase.class, DirectiveKind.class, "kind");

	private volatile transient String toString;
	private volatile transient DirectiveKind kind;

	public ForwardingDirectiveBase(IncrementalElementsTypes9 elemTypes, E subject) {
		super(elemTypes, subject);
	}

	public void setKind(DirectiveKind kind) {
		this.kind = kind;
	}

	@Override
	public <R, P> R accept(DirectiveVisitor<R, P> v, P p) {
		return KindBasedDirectiveVisitor9.visit(getKind(), this, v, p);
	}

	@Override
	public DirectiveKind getKind() {
		DirectiveKind thiskind = this.kind;
		if (thiskind != null) {
			return thiskind;
		}
		thiskind = elemTypes.javac(subject::getKind);
		if (ARFU_kind.compareAndSet(this, null, thiskind)) {
			return thiskind;
		}
		return this.kind;
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
}
