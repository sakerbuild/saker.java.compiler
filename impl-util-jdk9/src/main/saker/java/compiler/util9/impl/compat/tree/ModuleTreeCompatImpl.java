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

import java.util.List;

import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.DirectiveTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.ModuleTree;
import com.sun.source.tree.ModuleTree.ModuleKind;

import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.compat.tree.BaseTreeCompatImpl;
import saker.java.compiler.impl.compat.tree.DirectiveTreeCompat;
import saker.java.compiler.impl.compat.tree.ModuleTreeCompat;

public class ModuleTreeCompatImpl extends BaseTreeCompatImpl<ModuleTree> implements ModuleTreeCompat {

	public ModuleTreeCompatImpl(ModuleTree real) {
		super(real);
	}

	@Override
	public List<? extends AnnotationTree> getAnnotations() {
		return real.getAnnotations();
	}

	@Override
	public String getModuleType() {
		ModuleKind mk = real.getModuleType();
		return mk == null ? null : mk.name();
	}

	@Override
	public ExpressionTree getName() {
		return real.getName();
	}

	@Override
	public List<? extends DirectiveTreeCompat> getDirectives() {
		List<? extends DirectiveTree> directives = real.getDirectives();
		return JavaTaskUtils.cloneImmutableList(directives,
				DirectiveTreeCompatCreatorVisitor.INSTANCE::toDirectiveTreeCompat);
	}
}
