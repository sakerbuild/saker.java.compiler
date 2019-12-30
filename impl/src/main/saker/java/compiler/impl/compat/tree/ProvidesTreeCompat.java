package saker.java.compiler.impl.compat.tree;

import java.util.List;

import com.sun.source.tree.ExpressionTree;

public interface ProvidesTreeCompat extends DirectiveTreeCompat {
	public ExpressionTree getServiceName();

	public List<? extends ExpressionTree> getImplementationNames();
}
