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
package saker.java.compiler.util15.impl.parser.signature;

import java.util.List;

import javax.lang.model.element.Modifier;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.Trees;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.compile.signature.impl.ExplicitPermittedSubclassesList;
import saker.java.compiler.impl.compile.signature.impl.UnspecifiedPermittedSubclasses;
import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.impl.signature.element.ClassSignature.PermittedSubclassesList;
import saker.java.compiler.util9.impl.parser.signature.CompilationUnitSignatureParser9;

public class CompilationUnitSignatureParser15 extends CompilationUnitSignatureParser9 {

	public CompilationUnitSignatureParser15(Trees trees, String sourceversion, ParserCache cache) {
		super(trees, sourceversion, cache);
	}

	@Override
	protected PermittedSubclassesList getPermittedSubclasses(ClassTree tree, ParseContext context) {
		if (!tree.getModifiers().getFlags().contains(Modifier.SEALED)) {
			return null;
		}
		List<? extends Tree> permits = tree.getPermitsClause();
		if (ObjectUtils.isNullOrEmpty(permits)) {
			return UnspecifiedPermittedSubclasses.INSTANCE;
		}
		return new ExplicitPermittedSubclassesList(
				JavaTaskUtils.cloneImmutableList(permits, impl -> typeResolver.resolve(impl, context)));
	}

}
