package saker.java.compiler.util15.impl.parser.usage;

import java.util.List;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;

import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.util14.impl.parser.usage.AbiUsageParser14;

public class AbiUsageParser15 extends AbiUsageParser14 {

	public AbiUsageParser15(Trees trees, String sourceversion, ParserCache cache) {
		super(trees, sourceversion, cache);
	}

	@Override
	protected void visitClassContext(ClassTree tree, ParseContext param) {
		List<? extends Tree> permitsclause = tree.getPermitsClause();
		for (Tree t : permitsclause) {
			TreePath path = descend(t, param);
			withExpressionType(path, param.typeElementAddUsedTypeConsumer);
		}
		super.visitClassContext(tree, param);
	}

}
