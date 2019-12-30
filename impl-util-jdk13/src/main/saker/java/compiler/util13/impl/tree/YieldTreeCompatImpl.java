package saker.java.compiler.util13.impl.tree;

import com.sun.source.tree.ExpressionTree;

import com.sun.source.tree.YieldTree;
import saker.java.compiler.impl.compat.tree.BaseTreeCompatImpl;
import saker.java.compiler.impl.compat.tree.YieldTreeCompat;

@SuppressWarnings("removal")
public class YieldTreeCompatImpl extends BaseTreeCompatImpl<YieldTree> implements YieldTreeCompat {
	public YieldTreeCompatImpl(YieldTree real) {
		super(real);
	}

	@Override
	public ExpressionTree getValue() {
		return real.getValue();
	}
}
