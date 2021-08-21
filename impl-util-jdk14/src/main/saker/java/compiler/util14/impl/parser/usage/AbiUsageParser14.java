package saker.java.compiler.util14.impl.parser.usage;

import com.sun.source.tree.BindingPatternTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.PatternTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;

import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.jdk.impl.compat.tree.TreeCompatUtil;
import saker.java.compiler.util13.impl.parser.usage.AbiUsageParser13;

public class AbiUsageParser14 extends AbiUsageParser13 {

	public AbiUsageParser14(Trees trees, String sourceversion, ParserCache cache) {
		super(trees, sourceversion, cache);
	}

	@Override
	public Void visitInstanceOf(InstanceOfTree tree, ParseContext param) {
		super.visitInstanceOf(tree, param);
		PatternTree pattern = tree.getPattern();
		if (pattern != null) {
			descend(pattern, param);
		}
		return null;
	}

	@Override
	public Void visitBindingPattern(BindingPatternTree node, ParseContext param) {
		Tree type = TreeCompatUtil.getBindingPatternTreeType(node);
		TreePath typepath = descend(type, param);
		withExpressionType(typepath, param.typeElementAddUsedTypeConsumer);

		return null;
	}
}
