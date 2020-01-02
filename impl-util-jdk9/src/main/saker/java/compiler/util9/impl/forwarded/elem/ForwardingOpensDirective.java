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

import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.ModuleElement.OpensDirective;
import javax.lang.model.element.PackageElement;

import saker.java.compiler.util9.impl.model.IncrementalElementsTypes9;

public class ForwardingOpensDirective extends ForwardingDirectiveBase<OpensDirective> implements OpensDirective {
	private static final AtomicReferenceFieldUpdater<ForwardingOpensDirective, PackageElement> ARFU_packageElement = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingOpensDirective.class, PackageElement.class, "packageElement");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<ForwardingOpensDirective, List> ARFU_targetModules = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingOpensDirective.class, List.class, "targetModules");

	private volatile transient PackageElement packageElement;
	private volatile transient List<? extends ModuleElement> targetModules;

	public ForwardingOpensDirective(IncrementalElementsTypes9 elemTypes, OpensDirective subject) {
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
