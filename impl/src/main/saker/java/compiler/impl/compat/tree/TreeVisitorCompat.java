/*
 * Copyright (C) 2020 Bence Sipka
 *
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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

	public default R visitBindingPatternCompat(BindingPatternTreeCompat node, P p) {
		return visitOther(node.getRealObject(), p);
	}
}
