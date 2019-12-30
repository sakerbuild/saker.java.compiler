package saker.java.compiler.impl.compat.tree;

import com.sun.source.tree.TreeVisitor;

public interface TreeVisitorCompat<R, P> extends TreeVisitor<R, P> {
	public default R visitPackageCompat(PackageTreeCompat node, P p) {
		return visitOther(node.getRealObject(), p);
	}

	public default R visitModuleCompat(ModuleTreeCompat node, P p) {
		return visitOther(node.getRealObject(), p);
	}

	public default R visitExportsCompat(ExportsTreeCompat node, P p) {
		return visitOther(node.getRealObject(), p);
	}

	public default R visitOpensCompat(OpensTreeCompat node, P p) {
		return visitOther(node.getRealObject(), p);
	}

	public default R visitProvidesCompat(ProvidesTreeCompat node, P p) {
		return visitOther(node.getRealObject(), p);
	}

	public default R visitRequiresCompat(RequiresTreeCompat node, P p) {
		return visitOther(node.getRealObject(), p);
	}

	public default R visitUsesCompat(UsesTreeCompat node, P p) {
		return visitOther(node.getRealObject(), p);
	}

	public default R visitSwitchExpressionCompat(SwitchExpressionTreeCompat node, P p) {
		return visitOther(node.getRealObject(), p);
	}

	public default R visitCaseCompat(CaseTreeCompat node, P p) {
		return visitCase(node.getRealObject(), p);
	}

	public default R visitBreakCompat(BreakTreeCompat node, P p) {
		return visitBreak(node.getRealObject(), p);
	}

	public default R visitYieldCompat(YieldTreeCompat node, P p) {
		return visitOther(node.getRealObject(), p);
	}
}
