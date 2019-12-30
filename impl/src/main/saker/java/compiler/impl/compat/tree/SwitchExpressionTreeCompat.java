package saker.java.compiler.impl.compat.tree;

import java.util.List;

import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ExpressionTree;

public interface SwitchExpressionTreeCompat {
	public ExpressionTree getRealObject();

	public ExpressionTree getExpression();

	public List<? extends CaseTree> getCases();
}
