package saker.java.compiler.impl.compat.tree;

import java.util.List;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.Tree;

public interface PackageTreeCompat {
	public Tree getRealObject();

	public List<? extends AnnotationTree> getAnnotations();

	public ExpressionTree getPackageName();
}
