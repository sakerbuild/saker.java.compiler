package saker.java.compiler.util9.impl.compat.tree;

import java.util.List;

import com.sun.source.tree.ExportsTree;
import com.sun.source.tree.ExpressionTree;

import saker.java.compiler.impl.compat.tree.BaseTreeCompatImpl;
import saker.java.compiler.impl.compat.tree.ExportsTreeCompat;

public class ExportsTreeCompatImpl extends BaseTreeCompatImpl<ExportsTree> implements ExportsTreeCompat {

	public ExportsTreeCompatImpl(ExportsTree real) {
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
