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
import com.sun.source.tree.DirectiveTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EmptyStatementTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ErroneousTree;
import com.sun.source.tree.ExportsTree;
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
import com.sun.source.tree.ModuleTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.OpensTree;
import com.sun.source.tree.PackageTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.ProvidesTree;
import com.sun.source.tree.RequiresTree;
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
import com.sun.source.tree.UsesTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.tree.WildcardTree;

import saker.java.compiler.impl.compat.tree.DirectiveTreeCompat;
import saker.java.compiler.jdk.impl.compat.tree.DefaultedTreeVisitor;

public class DirectiveTreeCompatCreatorVisitor implements DefaultedTreeVisitor<DirectiveTreeCompat, Void> {
	public static final DirectiveTreeCompatCreatorVisitor INSTANCE = new DirectiveTreeCompatCreatorVisitor();

	public DirectiveTreeCompat toDirectiveTreeCompat(DirectiveTree dt) {
		return dt.accept(this, null);
	}

	@Override
	public DirectiveTreeCompat visitExports(ExportsTree node, Void p) {
		return new ExportsTreeCompatImpl(node);
	}

	@Override
	public DirectiveTreeCompat visitOpens(OpensTree node, Void p) {
		return new OpensTreeCompatImpl(node);
	}

	@Override
	public DirectiveTreeCompat visitProvides(ProvidesTree node, Void p) {
		return new ProvidesTreeCompatImpl(node);
	}

	@Override
	public DirectiveTreeCompat visitRequires(RequiresTree node, Void p) {
		return new RequiresTreeCompatImpl(node);
	}

	@Override
	public DirectiveTreeCompat visitUses(UsesTree node, Void p) {
		return new UsesTreeCompatImpl(node);
	}

	@Override
	public DirectiveTreeCompat visitModule(ModuleTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitAnnotatedType(AnnotatedTypeTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitAnnotation(AnnotationTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitMethodInvocation(MethodInvocationTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitAssert(AssertTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitAssignment(AssignmentTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitCompoundAssignment(CompoundAssignmentTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitBinary(BinaryTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitBlock(BlockTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitBreak(BreakTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitCase(CaseTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitCatch(CatchTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitClass(ClassTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitConditionalExpression(ConditionalExpressionTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitContinue(ContinueTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitDoWhileLoop(DoWhileLoopTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitErroneous(ErroneousTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitExpressionStatement(ExpressionStatementTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitEnhancedForLoop(EnhancedForLoopTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitForLoop(ForLoopTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitIdentifier(IdentifierTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitIf(IfTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitImport(ImportTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitArrayAccess(ArrayAccessTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitLabeledStatement(LabeledStatementTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitLiteral(LiteralTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitMethod(MethodTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitModifiers(ModifiersTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitNewArray(NewArrayTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitNewClass(NewClassTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitLambdaExpression(LambdaExpressionTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitPackage(PackageTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitParenthesized(ParenthesizedTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitReturn(ReturnTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitMemberSelect(MemberSelectTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitMemberReference(MemberReferenceTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitEmptyStatement(EmptyStatementTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitSwitch(SwitchTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitSynchronized(SynchronizedTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitThrow(ThrowTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitCompilationUnit(CompilationUnitTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitTry(TryTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitParameterizedType(ParameterizedTypeTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitUnionType(UnionTypeTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitIntersectionType(IntersectionTypeTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitArrayType(ArrayTypeTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitTypeCast(TypeCastTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitPrimitiveType(PrimitiveTypeTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitTypeParameter(TypeParameterTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitInstanceOf(InstanceOfTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitUnary(UnaryTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitVariable(VariableTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitWhileLoop(WhileLoopTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitWildcard(WildcardTree node, Void p) {
		return null;
	}

	@Override
	public DirectiveTreeCompat visitOther(Tree node, Void p) {
		return null;
	}

}
