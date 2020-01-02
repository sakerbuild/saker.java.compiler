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
package saker.java.compiler.util9.impl.compat.element;

import javax.lang.model.element.ModuleElement.Directive;
import javax.lang.model.element.ModuleElement.DirectiveVisitor;
import javax.lang.model.element.ModuleElement.ExportsDirective;
import javax.lang.model.element.ModuleElement.OpensDirective;
import javax.lang.model.element.ModuleElement.ProvidesDirective;
import javax.lang.model.element.ModuleElement.RequiresDirective;
import javax.lang.model.element.ModuleElement.UsesDirective;

import saker.java.compiler.impl.compat.element.DirectiveCompat;

public class DirectiveCompatCreatorVisitor implements DirectiveVisitor<DirectiveCompat, Void> {
	public static final DirectiveCompatCreatorVisitor INSTANCE = new DirectiveCompatCreatorVisitor();

	public DirectiveCompat toDirectiveCompat(Directive directive) {
		return directive.accept(this, null);
	}

	@Override
	public DirectiveCompat visitRequires(RequiresDirective d, Void p) {
		return new RequiresDirectiveCompatImpl(d);
	}

	@Override
	public DirectiveCompat visitExports(ExportsDirective d, Void p) {
		return new ExportsDirectiveCompatImpl(d);
	}

	@Override
	public DirectiveCompat visitOpens(OpensDirective d, Void p) {
		return new OpensDirectiveCompatImpl(d);
	}

	@Override
	public DirectiveCompat visitUses(UsesDirective d, Void p) {
		return new UsesDirectiveCompatImpl(d);
	}

	@Override
	public DirectiveCompat visitProvides(ProvidesDirective d, Void p) {
		return new ProvidesDirectiveCompatImpl(d);
	}

}
