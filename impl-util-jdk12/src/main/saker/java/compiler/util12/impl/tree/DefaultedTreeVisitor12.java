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
package saker.java.compiler.util12.impl.tree;

import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.SwitchExpressionTree;

import saker.java.compiler.util9.impl.compat.tree.DefaultedTreeVisitor9;

@SuppressWarnings("removal")
public interface DefaultedTreeVisitor12<R, P> extends DefaultedTreeVisitor9<R, P> {
	@Override
	public default R visitSwitchExpression(SwitchExpressionTree node, P p) {
		return visitSwitchExpressionCompat(new SwitchExpressionTreeCompatImpl(node), p);
	}

	@Override
	public default R visitCase(CaseTree node, P p) {
		return visitCaseCompat(new CaseTreeCompatImpl(node), p);
	}

	@Override
	public default R visitBreak(BreakTree node, P p) {
		return visitBreakCompat(new BreakTreeCompatImpl(node), p);
	}

}
