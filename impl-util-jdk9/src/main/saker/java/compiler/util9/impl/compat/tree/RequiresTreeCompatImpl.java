package saker.java.compiler.util9.impl.compat.tree;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.RequiresTree;

import saker.java.compiler.impl.compat.tree.BaseTreeCompatImpl;
import saker.java.compiler.impl.compat.tree.RequiresTreeCompat;

public class RequiresTreeCompatImpl extends BaseTreeCompatImpl<RequiresTree> implements RequiresTreeCompat {

	public RequiresTreeCompatImpl(RequiresTree real) {
		super(real);
	}

	@Override
	public boolean isStatic() {
		return real.isStatic();
	}

	@Override
	public boolean isTransitive() {
		return real.isTransitive();
	}

	@Override
	public ExpressionTree getModuleName() {
		return real.getModuleName();
	}

}
