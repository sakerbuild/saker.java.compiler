package saker.java.compiler.impl.compat.tree;

import com.sun.source.tree.ExpressionTree;

public interface RequiresTreeCompat extends DirectiveTreeCompat {
	public boolean isStatic();

	public boolean isTransitive();

	public ExpressionTree getModuleName();
}
