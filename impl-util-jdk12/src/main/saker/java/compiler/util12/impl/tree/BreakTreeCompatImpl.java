package saker.java.compiler.util12.impl.tree;

import com.sun.source.tree.BreakTree;
import com.sun.source.tree.ExpressionTree;

import saker.java.compiler.impl.compat.tree.BaseTreeCompatImpl;
import saker.java.compiler.impl.compat.tree.BreakTreeCompat;
import saker.java.compiler.jdk.impl.compat.tree.TreeCompatUtil;

@SuppressWarnings("removal")
public class BreakTreeCompatImpl extends BaseTreeCompatImpl<BreakTree> implements BreakTreeCompat {

	public BreakTreeCompatImpl(BreakTree real) {
		super(real);
	}

	@Override
	public ExpressionTree getValue() {
		return TreeCompatUtil.getBreakTreeValue(real);
	}

}
