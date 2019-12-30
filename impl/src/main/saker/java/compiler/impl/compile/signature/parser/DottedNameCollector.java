package saker.java.compiler.impl.compile.signature.parser;

import java.util.Locale;

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

import saker.java.compiler.jdk.impl.compat.tree.DefaultedTreeVisitor;

public class DottedNameCollector implements DefaultedTreeVisitor<Void, StringBuilder> {

	private static final DottedNameCollector INSTANCE = new DottedNameCollector();

	private DottedNameCollector() {
	}

	public static String collectDottedName(Tree tree) {
		StringBuilder sb = new StringBuilder();
		collectDottedName(tree, sb);
		return sb.toString();
	}

	public static void collectDottedName(Tree tree, StringBuilder sb) {
		tree.accept(INSTANCE, sb);
	}

	@Override
	public Void visitAnnotatedType(AnnotatedTypeTree tree, StringBuilder sb) {
		tree.getUnderlyingType().accept(this, sb);
		return null;
	}

	@Override
	public Void visitIdentifier(IdentifierTree tree, StringBuilder sb) {
		sb.append(tree.getName().toString());
		return null;
	}

	@Override
	public Void visitImport(ImportTree tree, StringBuilder sb) {
		tree.getQualifiedIdentifier().accept(this, sb);
		return null;
	}

	@Override
	public Void visitMemberSelect(MemberSelectTree tree, StringBuilder sb) {
		tree.getExpression().accept(this, sb);
		sb.append('.');
		sb.append(tree.getIdentifier().toString());
		return null;
	}

	@Override
	public Void visitParameterizedType(ParameterizedTypeTree tree, StringBuilder sb) {
		tree.getType().accept(this, sb);
		return null;
	}

	@Override
	public Void visitPrimitiveType(PrimitiveTypeTree tree, StringBuilder sb) {
		sb.append(tree.getPrimitiveTypeKind().toString().toLowerCase(Locale.ENGLISH));
		return null;
	}

	@Override
	public Void visitAnnotation(AnnotationTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitArrayAccess(ArrayAccessTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitArrayType(ArrayTypeTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitAssert(AssertTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitAssignment(AssignmentTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitBinary(BinaryTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitBlock(BlockTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitBreak(BreakTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitCase(CaseTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitCatch(CatchTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitClass(ClassTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitCompilationUnit(CompilationUnitTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitCompoundAssignment(CompoundAssignmentTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitConditionalExpression(ConditionalExpressionTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitContinue(ContinueTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitDoWhileLoop(DoWhileLoopTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitEmptyStatement(EmptyStatementTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitEnhancedForLoop(EnhancedForLoopTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitErroneous(ErroneousTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitExpressionStatement(ExpressionStatementTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitForLoop(ForLoopTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitIf(IfTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitInstanceOf(InstanceOfTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitIntersectionType(IntersectionTypeTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitLabeledStatement(LabeledStatementTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitLambdaExpression(LambdaExpressionTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitLiteral(LiteralTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitMemberReference(MemberReferenceTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitMethod(MethodTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitMethodInvocation(MethodInvocationTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitModifiers(ModifiersTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitNewArray(NewArrayTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitNewClass(NewClassTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitOther(Tree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitParenthesized(ParenthesizedTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitReturn(ReturnTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitSwitch(SwitchTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitSynchronized(SynchronizedTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitThrow(ThrowTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitTry(TryTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitTypeCast(TypeCastTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitTypeParameter(TypeParameterTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitUnary(UnaryTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitUnionType(UnionTypeTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitVariable(VariableTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitWhileLoop(WhileLoopTree tree, StringBuilder sb) {
		return null;
	}

	@Override
	public Void visitWildcard(WildcardTree tree, StringBuilder sb) {
		return null;
	}

}
