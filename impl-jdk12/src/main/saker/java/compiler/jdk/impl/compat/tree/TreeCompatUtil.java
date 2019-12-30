package saker.java.compiler.jdk.impl.compat.tree;

import com.sun.source.tree.BreakTree;
import com.sun.source.tree.ExpressionTree;

import saker.build.util.java.JavaTools;

public class TreeCompatUtil {
	private TreeCompatUtil() {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings("removal")
	public static ExpressionTree getBreakTreeValue(BreakTree tree) {
		return tree.getValue();
	}
}
