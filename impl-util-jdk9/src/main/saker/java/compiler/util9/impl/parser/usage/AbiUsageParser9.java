package saker.java.compiler.util9.impl.parser.usage;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.DirectiveTree;
import com.sun.source.tree.ExportsTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ModuleTree;
import com.sun.source.tree.OpensTree;
import com.sun.source.tree.ProvidesTree;
import com.sun.source.tree.RequiresTree;
import com.sun.source.tree.UsesTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;

import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.util8.impl.parser.usage.AbiUsageParser8;

public class AbiUsageParser9 extends AbiUsageParser8 {

	public AbiUsageParser9(Trees trees, String sourceversion, ParserCache cache) {
		super(trees, sourceversion, cache);
	}

	@Override
	public Void visitModule(ModuleTree node, ParseContext p) {
		for (AnnotationTree annot : node.getAnnotations()) {
			descend(annot, p);
		}
		for (DirectiveTree dt : node.getDirectives()) {
			descend(dt, p);
		}
		return null;
	}

	@Override
	public Void visitRequires(RequiresTree node, ParseContext p) {
		return null;
	}

	@Override
	public Void visitExports(ExportsTree node, ParseContext p) {
		return null;
	}

	@Override
	public Void visitOpens(OpensTree node, ParseContext p) {
		return null;
	}

	@Override
	public Void visitProvides(ProvidesTree node, ParseContext p) {
		ExpressionTree service = node.getServiceName();
		TreePath servicepath = descend(service, p);
		withExpressionType(servicepath, p.typeElementAddUsedTypeConsumer);
		for (ExpressionTree impl : node.getImplementationNames()) {
			TreePath implpath = descend(impl, p);
			withExpressionType(implpath, p.typeElementAddUsedTypeConsumer);
		}
		return null;
	}

	@Override
	public Void visitUses(UsesTree node, ParseContext p) {
		ExpressionTree service = node.getServiceName();
		TreePath servicepath = descend(service, p);
		withExpressionType(servicepath, p.typeElementAddUsedTypeConsumer);
		return null;
	}

}
