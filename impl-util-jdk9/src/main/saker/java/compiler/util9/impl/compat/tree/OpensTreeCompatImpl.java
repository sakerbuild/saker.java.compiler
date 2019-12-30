package saker.java.compiler.util9.impl.compat.tree;

import java.util.List;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.OpensTree;

import saker.java.compiler.impl.compat.tree.BaseTreeCompatImpl;
import saker.java.compiler.impl.compat.tree.OpensTreeCompat;

public class OpensTreeCompatImpl extends BaseTreeCompatImpl<OpensTree> implements OpensTreeCompat {

	public OpensTreeCompatImpl(OpensTree real) {
		super(real);
	}

	@Override
	public ExpressionTree getPackageName() {
		return real.getPackageName();
	}

	@Override
	public List<? extends ExpressionTree> getModuleNames() {
		return real.getModuleNames();
	}
}
