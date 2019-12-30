package saker.java.compiler.util9.impl.compat.tree;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.UsesTree;

import saker.java.compiler.impl.compat.tree.BaseTreeCompatImpl;
import saker.java.compiler.impl.compat.tree.UsesTreeCompat;

public class UsesTreeCompatImpl extends BaseTreeCompatImpl<UsesTree> implements UsesTreeCompat {

	public UsesTreeCompatImpl(UsesTree real) {
		super(real);
	}

	@Override
	public ExpressionTree getServiceName() {
		return real.getServiceName();
	}

}
