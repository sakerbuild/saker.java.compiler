package saker.java.compiler.util12.impl.tree;

import java.util.List;
import java.util.Objects;

import com.sun.source.tree.CaseTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;

import saker.java.compiler.impl.compat.tree.BaseTreeCompatImpl;
import saker.java.compiler.impl.compat.tree.CaseTreeCompat;

@SuppressWarnings("removal")
public class CaseTreeCompatImpl extends BaseTreeCompatImpl<CaseTree> implements CaseTreeCompat {

	public CaseTreeCompatImpl(CaseTree real) {
		super(real);
	}

	@Override
	public Tree getBody() {
		return real.getBody();
	}

	@Override
	public List<? extends ExpressionTree> getExpressions() {
		return real.getExpressions();
	}

	@Override
	public String getCaseKind() {
		return Objects.toString(real.getCaseKind(), null);
	}

}
