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
package saker.java.compiler.impl.compile.handler.full;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import com.sun.source.tree.AnnotatedTypeTree;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.AssertTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EmptyStatementTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ErroneousTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.IntersectionTypeTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.UnionTypeTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.tree.WildcardTree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;

import saker.java.compiler.impl.compile.signature.jni.NativeConstantSignature;
import saker.java.compiler.impl.compile.signature.jni.NativeMethodSignature;
import saker.java.compiler.impl.compile.signature.jni.NativeSignature;
import saker.java.compiler.jdk.impl.compat.tree.DefaultedTreeVisitor;
import saker.java.compiler.jdk.impl.parser.signature.CompilationUnitSignatureParser;

public class NativeHeaderCompilationUnitSignatureVisitor
		implements DefaultedTreeVisitor<Void, NativeHeaderCompilationUnitSignatureVisitor.ParseContext> {
	private static class NativeElementReference<T extends Element> {
		private T elem;
		private TreePath treePath;

		public NativeElementReference(T elem, TreePath treePath) {
			this.elem = elem;
			this.treePath = treePath;
		}

		public T getElement() {
			return elem;
		}

		public TreePath getTreePath() {
			return treePath;
		}

		@Override
		public String toString() {
			return "NativeElementReference[elem=" + elem + "]";
		}
	}

	private static class MemberClassStackEntry {
		final Collection<NativeElementReference<ExecutableElement>> nativeMethods = new ArrayList<>();
		final Map<String, Integer> allMethodNamesFrequencies = new TreeMap<>();
		final Collection<NativeElementReference<VariableElement>> nativeFields = new ArrayList<>();

		TreePath treePath;

		public MemberClassStackEntry(TreePath treePath) {
			this.treePath = treePath;
		}

		public boolean isOverloaded(String name) {
			Integer freq = allMethodNamesFrequencies.get(name);
			return freq > 1;
		}

	}

	static class ParseContext {
		Map<String, Collection<NativeSignature>> result;
		String packageName = "";
		String currentClassName;
		private final ArrayDeque<MemberClassStackEntry> memberStack = new ArrayDeque<>();
		private final TreePath rootTreePath;

		ParseContext(Map<String, Collection<NativeSignature>> result, TreePath treePath) {
			this.result = result;
			this.rootTreePath = treePath;
		}

		MemberClassStackEntry enterClass(ClassTree ctree) {
			String simplename = ctree.getSimpleName().toString();
			if (currentClassName == null) {
				currentClassName = simplename;
			} else {
				currentClassName = currentClassName + "$" + simplename;
			}
			TreePath ctreepath;
			if (memberStack.isEmpty()) {
				ctreepath = new TreePath(rootTreePath, ctree);
			} else {
				ctreepath = new TreePath(memberStack.getLast().treePath, ctree);
			}
			MemberClassStackEntry mentry = new MemberClassStackEntry(ctreepath);
			this.memberStack.addLast(mentry);
			return mentry;
		}

		void exitClass() {
			this.memberStack.removeLast();
		}

		MemberClassStackEntry getLastEntry() {
			return memberStack.getLast();
		}
	}

	private Trees trees;
	private Types types;
	private Elements elements;

	public NativeHeaderCompilationUnitSignatureVisitor(Trees trees, Types types, Elements elements) {
		this.trees = trees;
		this.types = types;
		this.elements = elements;
	}

	public Map<String, Collection<NativeSignature>> getNativeSignatures(CompilationUnitTree unit) {
		Map<String, Collection<NativeSignature>> result = new TreeMap<>();
		unit.accept(this, new ParseContext(result, new TreePath(unit)));
		return result;
	}

	@Override
	public Void visitCompilationUnit(CompilationUnitTree tree, ParseContext p) {
		StringBuilder packnamesb = new StringBuilder();
		if (CompilationUnitSignatureParser.getPackageName(tree, packnamesb)) {
			p.packageName = packnamesb.toString() + ".";
		}
		for (Tree t : tree.getTypeDecls()) {
			t.accept(this, p);
		}
		return null;
	}

	@Override
	public Void visitClass(ClassTree tree, ParseContext p) {
		MemberClassStackEntry mentry = p.enterClass(tree);
		try {
			for (Tree membertree : tree.getMembers()) {
				membertree.accept(this, p);
			}
			ArrayList<NativeSignature> nativesignatures = new ArrayList<>();
			String cbinaryname = p.packageName + p.currentClassName;
			for (NativeElementReference<VariableElement> elemref : mentry.nativeFields) {
				VariableElement elem = elemref.getElement();
				nativesignatures.add(new NativeConstantSignature(elem.getSimpleName().toString(),
						elem.getConstantValue(), trees.getDocComment(elemref.getTreePath()), cbinaryname));
			}
			for (NativeElementReference<ExecutableElement> elemref : mentry.nativeMethods) {
				ExecutableElement elem = elemref.getElement();
				nativesignatures.add(new NativeMethodSignature(cbinaryname, elem, types, elements,
						mentry.isOverloaded(elem.getSimpleName().toString()),
						trees.getDocComment(elemref.getTreePath())));
			}
			if (!nativesignatures.isEmpty()) {
				p.result.put(cbinaryname, nativesignatures);
			}
		} finally {
			p.exitClass();
		}
		return null;
	}

	@Override
	public Void visitMethod(MethodTree tree, ParseContext p) {
		MemberClassStackEntry centry = p.getLastEntry();
		if (tree.getModifiers().getFlags().contains(Modifier.NATIVE)) {
			TreePath treepath = new TreePath(centry.treePath, tree);
			ExecutableElement elem = (ExecutableElement) trees.getElement(treepath);
			centry.nativeMethods.add(new NativeElementReference<>(elem, treepath));
		}
		centry.allMethodNamesFrequencies.compute(tree.getName().toString(), (k, freq) -> freq == null ? 1 : (freq + 1));
		return null;
	}

	@Override
	public Void visitVariable(VariableTree tree, ParseContext p) {
		List<? extends AnnotationTree> annots = tree.getModifiers().getAnnotations();
		MemberClassStackEntry centry = p.getLastEntry();
		if (!annots.isEmpty()) {
			TreePath treepath = new TreePath(centry.treePath, tree);
			VariableElement elem = (VariableElement) trees.getElement(treepath);
			if (elem.getConstantValue() == null) {
				return null;
			}
			for (AnnotationMirror am : elem.getAnnotationMirrors()) {
				DeclaredType dt = am.getAnnotationType();
				TypeElement annotelemtype = (TypeElement) dt.asElement();
				Name qname = annotelemtype.getQualifiedName();
				if (qname.contentEquals("java.lang.annotation.Native")) {
					centry.nativeFields.add(new NativeElementReference<>(elem, treepath));
					return null;
				}
			}
		}
		return null;
	}

	@Override
	public Void visitAnnotatedType(AnnotatedTypeTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitAnnotation(AnnotationTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitArrayAccess(ArrayAccessTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitArrayType(ArrayTypeTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitAssert(AssertTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitAssignment(AssignmentTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitBinary(BinaryTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitBlock(BlockTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitBreak(BreakTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitCase(CaseTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitCatch(CatchTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitCompoundAssignment(CompoundAssignmentTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitConditionalExpression(ConditionalExpressionTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitContinue(ContinueTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitDoWhileLoop(DoWhileLoopTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitEmptyStatement(EmptyStatementTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitEnhancedForLoop(EnhancedForLoopTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitErroneous(ErroneousTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitExpressionStatement(ExpressionStatementTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitForLoop(ForLoopTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitIdentifier(IdentifierTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitIf(IfTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitImport(ImportTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitInstanceOf(InstanceOfTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitIntersectionType(IntersectionTypeTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitLabeledStatement(LabeledStatementTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitLambdaExpression(LambdaExpressionTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitLiteral(LiteralTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitMemberReference(MemberReferenceTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitMemberSelect(MemberSelectTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitMethodInvocation(MethodInvocationTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitModifiers(ModifiersTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitNewArray(NewArrayTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitNewClass(NewClassTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitOther(Tree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitParameterizedType(ParameterizedTypeTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitParenthesized(ParenthesizedTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitPrimitiveType(PrimitiveTypeTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitReturn(ReturnTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitSwitch(SwitchTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitSynchronized(SynchronizedTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitThrow(ThrowTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitTry(TryTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitTypeCast(TypeCastTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitTypeParameter(TypeParameterTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitUnary(UnaryTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitUnionType(UnionTypeTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitWhileLoop(WhileLoopTree tree, ParseContext p) {
		return null;
	}

	@Override
	public Void visitWildcard(WildcardTree tree, ParseContext p) {
		return null;
	}
}
