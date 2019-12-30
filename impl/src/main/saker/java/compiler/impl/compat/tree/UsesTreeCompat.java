package saker.java.compiler.impl.compat.tree;

import com.sun.source.tree.ExpressionTree;

public interface UsesTreeCompat extends DirectiveTreeCompat {
	public ExpressionTree getServiceName();
}
