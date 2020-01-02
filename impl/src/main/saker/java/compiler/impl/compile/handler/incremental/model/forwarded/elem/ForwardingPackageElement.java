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
package saker.java.compiler.impl.compile.handler.incremental.model.forwarded.elem;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;

import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;

public class ForwardingPackageElement extends ForwardingElementBase<PackageElement> implements PackageElement {
	private static final AtomicReferenceFieldUpdater<ForwardingPackageElement, Name> ARFU_qualifiedName = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingPackageElement.class, Name.class, "qualifiedName");

	private volatile transient Name qualifiedName;

	public ForwardingPackageElement(IncrementalElementsTypesBase elemTypes, PackageElement subject) {
		this(elemTypes, subject, null);
	}

	public ForwardingPackageElement(IncrementalElementsTypesBase elemTypes, PackageElement subject, Name qualifiedName) {
		super(elemTypes, subject);
		this.qualifiedName = qualifiedName;
		setElementKind(ElementKind.PACKAGE);
	}

	public void setQualifiedName(Name qualifiedName) {
		this.qualifiedName = qualifiedName;
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
	public boolean isUnnamed() {
		return getQualifiedName().length() == 0;
	}

	@Override
	public String toString() {
		Name qname = getQualifiedName();
		return qname.length() == 0 ? "unnamed package" : qname.toString();
	}
}
