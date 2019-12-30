package saker.java.compiler.util12.impl.tree;

import java.util.List;

import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.SwitchExpressionTree;

import saker.java.compiler.impl.compat.tree.BaseTreeCompatImpl;
import saker.java.compiler.impl.compat.tree.SwitchExpressionTreeCompat;

@SuppressWarnings("removal")
public class SwitchExpressionTreeCompatImpl extends BaseTreeCompatImpl<SwitchExpressionTree>
		implements SwitchExpressionTreeCompat {
	public SwitchExpressionTreeCompatImpl(SwitchExpressionTree real) {
		super(real);
	}

	@Override
	public ExpressionTree getExpression() {
		return real.getExpression();
	}

	@Override
	public List<? extends CaseTree> getCases() {
		return real.getCases();
	}
}
