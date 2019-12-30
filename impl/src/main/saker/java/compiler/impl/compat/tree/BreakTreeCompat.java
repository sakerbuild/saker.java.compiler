package saker.java.compiler.impl.compat.tree;

import com.sun.source.tree.BreakTree;
import com.sun.source.tree.ExpressionTree;

public interface BreakTreeCompat {
	public BreakTree getRealObject();

	// From JDK 12:

	public ExpressionTree getValue();
}
