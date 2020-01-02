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
package saker.java.compiler.util9.impl.compat.tree;

import com.sun.source.tree.ExportsTree;
import com.sun.source.tree.ModuleTree;
import com.sun.source.tree.OpensTree;
import com.sun.source.tree.PackageTree;
import com.sun.source.tree.ProvidesTree;
import com.sun.source.tree.RequiresTree;
import com.sun.source.tree.UsesTree;

import saker.java.compiler.impl.compat.tree.TreeVisitorCompat;

public interface DefaultedTreeVisitor9<R, P> extends TreeVisitorCompat<R, P> {
	@Override
	public default R visitPackage(PackageTree node, P p) {
		return visitPackageCompat(new PackageTreeCompatImpl(node), p);
	}

	@Override
	public default R visitModule(ModuleTree node, P p) {
		return visitModuleCompat(new ModuleTreeCompatImpl(node), p);
	}

	@Override
	public default R visitExports(ExportsTree node, P p) {
		return visitExportsCompat(new ExportsTreeCompatImpl(node), p);
	}

	@Override
	public default R visitOpens(OpensTree node, P p) {
		return visitOpensCompat(new OpensTreeCompatImpl(node), p);
	}

	@Override
	public default R visitProvides(ProvidesTree node, P p) {
		return visitProvidesCompat(new ProvidesTreeCompatImpl(node), p);
	}

	@Override
	public default R visitRequires(RequiresTree node, P p) {
		return visitRequiresCompat(new RequiresTreeCompatImpl(node), p);
	}

	@Override
	public default R visitUses(UsesTree node, P p) {
		return visitUsesCompat(new UsesTreeCompatImpl(node), p);
	}
}
