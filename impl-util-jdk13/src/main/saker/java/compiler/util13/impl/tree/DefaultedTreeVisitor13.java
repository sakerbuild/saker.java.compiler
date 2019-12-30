package saker.java.compiler.util13.impl.tree;

import com.sun.source.tree.YieldTree;
import saker.java.compiler.util12.impl.tree.DefaultedTreeVisitor12;

@SuppressWarnings("removal")
public interface DefaultedTreeVisitor13<R, P> extends DefaultedTreeVisitor12<R, P> {
	@Override
	public default R visitYield(YieldTree node, P p) {
		return visitYieldCompat(new YieldTreeCompatImpl(node), p);
	}

}
