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
package saker.java.compiler.util14.impl.tree;

import com.sun.source.tree.BindingPatternTree;

import saker.java.compiler.util13.impl.tree.DefaultedTreeVisitor13;

@SuppressWarnings("removal")
public interface DefaultedTreeVisitor14<R, P> extends DefaultedTreeVisitor13<R, P> {
	@Override
	public default R visitBindingPattern(BindingPatternTree node, P p) {
		return visitBindingPatternCompat(new BindingPatternTreeCompatImpl(node), p);
	}
}
