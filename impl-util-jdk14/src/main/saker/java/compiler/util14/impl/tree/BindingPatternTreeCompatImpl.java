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
package saker.java.compiler.util14.impl.tree;

import javax.lang.model.element.Name;

import com.sun.source.tree.BindingPatternTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;

import saker.java.compiler.impl.compat.tree.BaseTreeCompatImpl;
import saker.java.compiler.impl.compat.tree.BindingPatternTreeCompat;
import saker.java.compiler.jdk.impl.compat.tree.TreeCompatUtil;

@SuppressWarnings("removal")
public class BindingPatternTreeCompatImpl extends BaseTreeCompatImpl<BindingPatternTree>
		implements BindingPatternTreeCompat {
	public BindingPatternTreeCompatImpl(BindingPatternTree real) {
		super(real);
	}

	@Override
	public Tree getType() {
		return TreeCompatUtil.getBindingPatternTreeType(real);
	}

	@Override
	public Name getBinding() {
		return TreeCompatUtil.getBindingPatternTreeBinding(real);
	}

	@Override
	public VariableTree getVariable() {
		return TreeCompatUtil.getBindingPatternTreeVariable(real);
	}

}
