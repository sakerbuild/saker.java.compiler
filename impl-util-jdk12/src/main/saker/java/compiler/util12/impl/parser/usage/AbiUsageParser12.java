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
package saker.java.compiler.util12.impl.parser.usage;

import java.util.List;

import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.SwitchExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.jdk.impl.compat.tree.TreeCompatUtil;
import saker.java.compiler.util9.impl.parser.usage.AbiUsageParser9;

@SuppressWarnings({ "removal", "deprecation" })
public class AbiUsageParser12 extends AbiUsageParser9 {

	public AbiUsageParser12(Trees trees, String sourceversion, ParserCache cache) {
		super(trees, sourceversion, cache);
	}

	@Override
	public Void visitCase(CaseTree tree, ParseContext p) {
		visitCasePre12(tree, p);
		Tree body = tree.getBody();
		if (body != null) {
			descend(body, p);
		}
		List<? extends ExpressionTree> expressions = tree.getExpressions();
		if (!ObjectUtils.isNullOrEmpty(expressions)) {
			for (ExpressionTree exptree : expressions) {
				descend(exptree, p);
			}
		}
		return null;
	}

	@Override
	public Void visitSwitchExpression(SwitchExpressionTree node, ParseContext p) {
		List<? extends CaseTree> cases = node.getCases();
		if (!ObjectUtils.isNullOrEmpty(cases)) {
			for (CaseTree ct : cases) {
				descend(ct, p);
			}
		}
		ExpressionTree expression = node.getExpression();
		if (expression != null) {
			TreePath expressionpath = descend(expression, p);
			withExpressionType(expressionpath, p.typeElementAddUsedTypeConsumer);
		}
		return null;
	}

	@Override
	public Void visitBreak(BreakTree tree, ParseContext p) {
		super.visitBreak(tree, p);
		ExpressionTree expr = TreeCompatUtil.getBreakTreeValue(tree);
		if (expr != null) {
			descend(expr, p);
		}
		return null;
	}

	protected final void visitBreakPre12(BreakTree tree, ParseContext p) {
		super.visitBreak(tree, p);
	}

	protected final void visitCasePre12(CaseTree tree, ParseContext p) {
		super.visitCase(tree, p);
	}
}
