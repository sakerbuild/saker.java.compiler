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

import java.util.List;

import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.SwitchExpressionTree;

import saker.java.compiler.impl.compat.tree.BaseTreeCompatImpl;
import saker.java.compiler.impl.compat.tree.SwitchExpressionTreeCompat;

@SuppressWarnings("removal")
public class SwitchExpressionTreeCompatImpl extends BaseTreeCompatImpl<SwitchExpressionTree>
		implements SwitchExpressionTreeCompat {
	public SwitchExpressionTreeCompatImpl(SwitchExpressionTree real) {
		super(real);
	}

	@Override
	public ExpressionTree getExpression() {
		return real.getExpression();
	}

	@Override
	public List<? extends CaseTree> getCases() {
		return real.getCases();
	}
}
