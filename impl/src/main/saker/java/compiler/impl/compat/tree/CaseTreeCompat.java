package saker.java.compiler.impl.compat.tree;

import java.util.List;

import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;

public interface CaseTreeCompat {
	public CaseTree getRealObject();

	// From JDK 12:

	public Tree getBody();

	public List<? extends ExpressionTree> getExpressions();

	public String getCaseKind();
}
