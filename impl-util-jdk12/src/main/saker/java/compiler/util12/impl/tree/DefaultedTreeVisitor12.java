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
