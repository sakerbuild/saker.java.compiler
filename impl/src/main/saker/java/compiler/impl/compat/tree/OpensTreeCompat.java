package saker.java.compiler.impl.compat.tree;

import java.util.List;

import com.sun.source.tree.ExpressionTree;

public interface OpensTreeCompat extends DirectiveTreeCompat {
	public ExpressionTree getPackageName();

	public List<? extends ExpressionTree> getModuleNames();
}
