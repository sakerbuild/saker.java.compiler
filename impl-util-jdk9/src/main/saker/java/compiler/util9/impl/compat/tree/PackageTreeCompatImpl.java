package saker.java.compiler.util9.impl.compat.tree;

import java.util.List;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.PackageTree;

import saker.java.compiler.impl.compat.tree.BaseTreeCompatImpl;
import saker.java.compiler.impl.compat.tree.PackageTreeCompat;

public class PackageTreeCompatImpl extends BaseTreeCompatImpl<PackageTree> implements PackageTreeCompat {
	public PackageTreeCompatImpl(PackageTree real) {
		super(real);
	}

	@Override
	public List<? extends AnnotationTree> getAnnotations() {
		return real.getAnnotations();
	}

	@Override
	public ExpressionTree getPackageName() {
		return real.getPackageName();
	}

}
