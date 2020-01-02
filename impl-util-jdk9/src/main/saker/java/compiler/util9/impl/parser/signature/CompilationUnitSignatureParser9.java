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
package saker.java.compiler.util9.impl.parser.signature;

import java.util.List;
import java.util.Set;

import javax.lang.model.element.Modifier;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.DirectiveTree;
import com.sun.source.tree.ExportsTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.ModuleTree;
import com.sun.source.tree.ModuleTree.ModuleKind;
import com.sun.source.tree.OpensTree;
import com.sun.source.tree.ProvidesTree;
import com.sun.source.tree.RequiresTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.UsesTree;
import com.sun.source.util.Trees;

import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.compile.handler.info.SignaturePath;
import saker.java.compiler.impl.compile.signature.impl.ExportsDirectiveSignatureImpl;
import saker.java.compiler.impl.compile.signature.impl.ModuleSignatureImpl;
import saker.java.compiler.impl.compile.signature.impl.NameSignatureImpl;
import saker.java.compiler.impl.compile.signature.impl.OpensDirectiveSignatureImpl;
import saker.java.compiler.impl.compile.signature.impl.ProvidesDirectiveSignatureImpl;
import saker.java.compiler.impl.compile.signature.impl.RequiresDirectiveSignatureImpl;
import saker.java.compiler.impl.compile.signature.impl.UsesDirectiveSignatureImpl;
import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.impl.signature.Signature;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.element.ModuleSignature.DirectiveSignature;
import saker.java.compiler.impl.signature.element.ModuleSignature.RequiresDirectiveSignature;
import saker.java.compiler.impl.signature.element.ModuleSignature.UsesDirectiveSignature;
import saker.java.compiler.impl.signature.type.NameSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.util.ImmutableModifierSet;
import saker.java.compiler.util8.impl.parser.signature.CompilationUnitSignatureParser8;

public class CompilationUnitSignatureParser9 extends CompilationUnitSignatureParser8 {

	public CompilationUnitSignatureParser9(Trees trees, String sourceversion, ParserCache cache) {
		super(trees, sourceversion, cache);
	}

	public List<AnnotationSignature> getAnnotations(ModuleTree annotatedtree, ParseContext context) {
		return getAnnotations(annotatedtree.getAnnotations(), context);
	}

	@Override
	protected String getPackageDocComment(CompilationUnitTree unit) {
		return getDocComment(unit, unit.getPackage());
	}

	@Override
	protected Tree getPackageTreeForSignature(CompilationUnitTree unit) {
		return unit.getPackage();
	}

	@Override
	protected Set<Modifier> getInterfaceCorrectMethodModifiers(ModifiersTree methodmodifiers) {
		Set<Modifier> flags = methodmodifiers.getFlags();
		if (!flags.contains(Modifier.PRIVATE)) {
			//if the method is not private, the same rules apply as the java 8 methods
			return super.getInterfaceCorrectMethodModifiers(methodmodifiers);
		}
		//else private methods must be static or non-abstract
		//   private methods need no modification in their modifiers
		return ImmutableModifierSet.get(flags);
	}

	@Override
	public Signature visitModule(ModuleTree node, ParseContext p) {
		if (p.getModuleSignature() != null) {
			throw new RuntimeException("Multiple module declarations found in compilation unit: "
					+ p.getCompilationUnit().getSourceFile());
		}

		SignaturePath sigpath = p.pushSignaturePath();

		String doccomment = getDocComment(p.getCompilationUnit(), node);

		List<? extends AnnotationSignature> annots = getAnnotations(node, p);

		List<? extends DirectiveTree> nodedirectives = node.getDirectives();
		List<DirectiveSignature> moddirectives = JavaTaskUtils.cloneImmutableList(nodedirectives,
				dtree -> (DirectiveSignature) dtree.accept(this, p));

		ModuleSignatureImpl sig = new ModuleSignatureImpl(annots, p.collectDottedName(node.getName()),
				node.getModuleType() == ModuleKind.OPEN, moddirectives, doccomment);
		p.setModuleSignature(node, sig);

		sigpath.setSignature(sig);
		p.popSignaturePath(node);
		return sig;
	}

	@Override
	public Signature visitRequires(RequiresTree node, ParseContext p) {
		RequiresDirectiveSignature reqsig = new RequiresDirectiveSignatureImpl(node.isStatic(), node.isTransitive(),
				new NameSignatureImpl(p.collectDottedName(node.getModuleName())));
		return reqsig;
	}

	@Override
	public Signature visitExports(ExportsTree node, ParseContext p) {
		List<NameSignature> modulenames = JavaTaskUtils.cloneImmutableList(node.getModuleNames(),
				mn -> new NameSignatureImpl(p.collectDottedName(mn)));
		ExportsDirectiveSignatureImpl expsig = new ExportsDirectiveSignatureImpl(
				new NameSignatureImpl(p.collectDottedName(node.getPackageName())), modulenames);
		return expsig;
	}

	@Override
	public Signature visitOpens(OpensTree node, ParseContext p) {
		List<NameSignature> modulenames = JavaTaskUtils.cloneImmutableList(node.getModuleNames(),
				mn -> new NameSignatureImpl(p.collectDottedName(mn)));
		OpensDirectiveSignatureImpl openssig = new OpensDirectiveSignatureImpl(
				new NameSignatureImpl(p.collectDottedName(node.getPackageName())), modulenames);
		return openssig;
	}

	@Override
	public Signature visitProvides(ProvidesTree node, ParseContext p) {
		List<TypeSignature> impltypes = JavaTaskUtils.cloneImmutableList(node.getImplementationNames(),
				impl -> typeResolver.resolve(impl, p));
		ProvidesDirectiveSignatureImpl provsig = new ProvidesDirectiveSignatureImpl(
				typeResolver.resolve(node.getServiceName(), p), impltypes);
		return provsig;
	}

	@Override
	public Signature visitUses(UsesTree node, ParseContext p) {
		UsesDirectiveSignature usessig = new UsesDirectiveSignatureImpl(typeResolver.resolve(node.getServiceName(), p));
		return usessig;
	}
}
