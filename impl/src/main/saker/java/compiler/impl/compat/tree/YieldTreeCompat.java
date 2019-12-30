package saker.java.compiler.impl.compat.tree;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.StatementTree;

public interface YieldTreeCompat {
	public StatementTree getRealObject();

	public ExpressionTree getValue();
}
