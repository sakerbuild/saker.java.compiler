package saker.java.compiler.util9.impl.compat.tree;

import java.util.List;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ProvidesTree;

import saker.java.compiler.impl.compat.tree.BaseTreeCompatImpl;
import saker.java.compiler.impl.compat.tree.ProvidesTreeCompat;

public class ProvidesTreeCompatImpl extends BaseTreeCompatImpl<ProvidesTree> implements ProvidesTreeCompat {

	public ProvidesTreeCompatImpl(ProvidesTree real) {
		super(real);
	}

	@Override
	public ExpressionTree getServiceName() {
		return real.getServiceName();
	}

	@Override
	public List<? extends ExpressionTree> getImplementationNames() {
		return real.getImplementationNames();
	}
}
