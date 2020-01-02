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
