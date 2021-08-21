package saker.java.compiler.util16.impl.parser.usage;

import com.sun.source.tree.BindingPatternTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;

import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.util15.impl.parser.usage.AbiUsageParser15;

public class AbiUsageParser16 extends AbiUsageParser15 {
	public AbiUsageParser16(Trees trees, String sourceversion, ParserCache cache) {
		super(trees, sourceversion, cache);
	}

	@Override
	public Void visitBindingPattern(BindingPatternTree node, ParseContext param) {
		VariableTree vartree = node.getVariable();
		TreePath varpath = descend(vartree, param);
		withExpressionType(varpath, param.typeElementAddUsedTypeConsumer);

		return null;
	}
}
