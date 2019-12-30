package saker.java.compiler.util13.impl.parser.usage;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.util.Trees;

import com.sun.source.tree.YieldTree;
import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.util12.impl.parser.usage.AbiUsageParser12;

@SuppressWarnings({ "removal", "deprecation" })
public class AbiUsageParser13 extends AbiUsageParser12 {

	public AbiUsageParser13(Trees trees, String sourceversion, ParserCache cache) {
		super(trees, sourceversion, cache);
	}

	@Override
	public Void visitYield(YieldTree node, ParseContext p) {
		ExpressionTree vexpr = node.getValue();
		if (vexpr != null) {
			descend(vexpr, p);
		}
		return null;
	}

}
