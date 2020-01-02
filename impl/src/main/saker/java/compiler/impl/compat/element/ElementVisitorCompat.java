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
package saker.java.compiler.impl.compat.element;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.UnknownElementException;

public interface ElementVisitorCompat<R, P> extends ElementVisitor<R, P> {
	@Override
	public default R visit(Element e, P p) {
		return e.accept(this, p);
	}

	@Override
	public default R visit(Element e) {
		return visit(e, null);
	}

	@Override
	public default R visitUnknown(Element e, P p) {
		throw new UnknownElementException(e, p);
	}

	public default R visitModuleCompat(ModuleElementCompat moduleElement, P p) {
		return visitUnknown(moduleElement.getRealObject(), p);
	}
}
