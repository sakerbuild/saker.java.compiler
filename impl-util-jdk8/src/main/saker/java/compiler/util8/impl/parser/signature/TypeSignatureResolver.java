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
package saker.java.compiler.util8.impl.parser.signature;

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.type.TypeKind;

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
import com.sun.source.tree.ExpressionTree;
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

import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.impl.compile.signature.type.impl.ArrayTypeSignatureImpl;
import saker.java.compiler.impl.compile.signature.type.impl.IntersectionTypeSignatureImpl;
import saker.java.compiler.impl.compile.signature.type.impl.NoTypeSignatureImpl;
import saker.java.compiler.impl.compile.signature.type.impl.PrimitiveTypeSignatureImpl;
import saker.java.compiler.impl.compile.signature.type.impl.UnionTypeSignatureImpl;
import saker.java.compiler.impl.compile.signature.type.impl.UnknownTypeSignatureImpl;
import saker.java.compiler.impl.compile.signature.type.impl.UnresolvedTypeSignatureImpl;
import saker.java.compiler.impl.compile.signature.type.impl.WildcardTypeSignatureImpl;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.type.ParameterizedTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.jdk.impl.compat.tree.DefaultedTreeVisitor;
import saker.java.compiler.jdk.impl.parser.signature.CompilationUnitSignatureParser;
import saker.java.compiler.util8.impl.parser.signature.CompilationUnitSignatureParser8.ParseContext;
import testing.saker.java.compiler.TestFlag;

public class TypeSignatureResolver
		implements DefaultedTreeVisitor<TypeSignature, TypeSignatureResolver.ResolverContext> {
	static final class ResolverContext {
		protected List<TypeSignature> typeParameters;
		protected List<AnnotationSignature> annotations;
		protected ParseContext parseContext;

		public ResolverContext(ParseContext parsecontext) {
			this.parseContext = parsecontext;
		}

		public ResolverContext(List<TypeSignature> typeParameters, List<AnnotationSignature> annotations,
				ParseContext parsecontext) {
			this.typeParameters = typeParameters;
			this.annotations = annotations;
			this.parseContext = parsecontext;
		}

	}

	protected final CompilationUnitSignatureParser8 parser;
	protected final ParserCache cache;

	public TypeSignatureResolver(CompilationUnitSignatureParser8 parser, ParserCache cache) {
		this.parser = parser;
		this.cache = cache;
	}

	public TypeSignature resolve(Tree tree, CompilationUnitSignatureParser.ParseContext parsecontext) {
		return tree.accept(this, new ResolverContext(parsecontext));
	}

	public TypeSignature resolveWithAnnotations(Tree tree, List<AnnotationSignature> annotations,
			ParseContext parsecontext) {
		ResolverContext context = new ResolverContext(null, annotations, parsecontext);
		TypeSignature result = tree.accept(this, context);
		if (TestFlag.ENABLED) {
			if (context.annotations != null) {
				throw new AssertionError(
						"Annotations wasn't consumed on: " + tree + " by " + result + " with: " + annotations);
			}
		}
		return result;
	}

	private List<TypeSignature> getTypeParametersSignatures(ParameterizedTypeTree tree, ResolverContext context) {
		List<? extends Tree> treeargtypes = tree.getTypeArguments();
		List<TypeSignature> typeparams = JavaTaskUtils.cloneImmutableList(treeargtypes,
				arg -> arg.accept(this, context));
		return typeparams;
	}

	@Override
	public TypeSignature visitArrayType(ArrayTypeTree tree, ResolverContext context) {
		List<AnnotationSignature> prevannots = context.annotations;
		context.annotations = null;
		TypeSignature component = tree.getType().accept(this, context);
		return ArrayTypeSignatureImpl.create(prevannots, component);
	}

	@Override
	public TypeSignature visitIdentifier(IdentifierTree tree, ResolverContext context) {
		List<AnnotationSignature> prevannots = context.annotations;
		List<TypeSignature> prevtypeparams = context.typeParameters;
		context.annotations = null;
		context.typeParameters = null;
		return UnresolvedTypeSignatureImpl.create(cache, prevannots, null, cache.string(tree.getName()),
				prevtypeparams);
	}

	private String isJustIdentifierSelects(MemberSelectTree tree) {
		ExpressionTree exp = tree.getExpression();
		switch (exp.getKind()) {
			case IDENTIFIER: {
				return cache.string(
						cache.string(((IdentifierTree) exp).getName()) + "." + cache.string(tree.getIdentifier()));
			}
			case MEMBER_SELECT: {
				String sub = isJustIdentifierSelects((MemberSelectTree) exp);
				if (sub != null) {
					return cache.string(sub + "." + cache.string(tree.getIdentifier()));
				}
				return null;
			}
			default: {
				break;
			}
		}
		return null;
	}

	@Override
	public TypeSignature visitMemberSelect(MemberSelectTree tree, ResolverContext context) {
		List<AnnotationSignature> prevannots = context.annotations;
		List<TypeSignature> prevtypeparams = context.typeParameters;
		context.annotations = null;
		context.typeParameters = null;

		String justidpath = isJustIdentifierSelects(tree);
		if (justidpath != null) {
			return UnresolvedTypeSignatureImpl.create(cache, prevannots, null, justidpath, prevtypeparams);
		}
		ExpressionTree expression = tree.getExpression();
		TypeSignature enclosing = expression.accept(this, context);
		String identifier = cache.string(tree.getIdentifier());
		return UnresolvedTypeSignatureImpl.create(cache, prevannots, (ParameterizedTypeSignature) enclosing, identifier,
				prevtypeparams);
	}

	@Override
	public TypeSignature visitParameterizedType(ParameterizedTypeTree tree, ResolverContext context) {
		List<AnnotationSignature> prevannots = context.annotations;
		context.annotations = null;
		context.typeParameters = getTypeParametersSignatures(tree, context);
		context.annotations = prevannots;
		TypeSignature typeres = tree.getType().accept(this, context);
		return typeres;
	}

	@Override
	public TypeSignature visitWildcard(WildcardTree tree, ResolverContext context) {
		List<AnnotationSignature> prevannots = context.annotations;
		context.annotations = null;
		switch (tree.getKind()) {
			case UNBOUNDED_WILDCARD:
				return WildcardTypeSignatureImpl.create(cache, prevannots, null, null);
			case SUPER_WILDCARD:
				return WildcardTypeSignatureImpl.create(cache, prevannots, tree.getBound().accept(this, context), null);
			case EXTENDS_WILDCARD:
				return WildcardTypeSignatureImpl.create(cache, prevannots, null, tree.getBound().accept(this, context));
			default:
				throw new RuntimeException(tree.getKind().toString());
		}
	}

	@Override
	public TypeSignature visitPrimitiveType(PrimitiveTypeTree tree, ResolverContext context) {
		TypeKind kind = tree.getPrimitiveTypeKind();
		List<AnnotationSignature> prevannots = context.annotations;
		context.annotations = null;
		if (kind.isPrimitive()) {
			return PrimitiveTypeSignatureImpl.create(prevannots, kind);
		}
		if (kind == TypeKind.VOID || kind == TypeKind.NONE) {
			return NoTypeSignatureImpl.create(prevannots, kind);
		}
		throw new IllegalArgumentException("Unknown primitive type: " + kind);
	}

	@Override
	public TypeSignature visitAnnotatedType(AnnotatedTypeTree tree, ResolverContext context) {
		List<AnnotationSignature> treeannots = parser.getAnnotations(tree.getAnnotations(), context.parseContext);
		if (context.annotations == null) {
			context.annotations = treeannots;
		} else {
			context.annotations = new ArrayList<>(context.annotations);
			context.annotations.addAll(treeannots);
		}
		TypeSignature result = tree.getUnderlyingType().accept(this, context);
		return result;
	}

	@Override
	public TypeSignature visitIntersectionType(IntersectionTypeTree tree, ResolverContext context) {
		List<? extends Tree> bounds = tree.getBounds();
		List<TypeSignature> resultbounds = JavaTaskUtils.cloneImmutableList(bounds, b -> b.accept(this, context));
		return IntersectionTypeSignatureImpl.create(resultbounds);
	}

	@Override
	public TypeSignature visitUnionType(UnionTypeTree tree, ResolverContext context) {
		List<? extends Tree> alternatives = tree.getTypeAlternatives();
		List<TypeSignature> resultalts = JavaTaskUtils.cloneImmutableList(alternatives, b -> b.accept(this, context));
		return UnionTypeSignatureImpl.create(resultalts);
	}

	@Override
	public TypeSignature visitTypeParameter(TypeParameterTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitErroneous(ErroneousTree tree, ResolverContext context) {
		return UnknownTypeSignatureImpl.create(tree.toString());
	}

	@Override
	public TypeSignature visitAnnotation(AnnotationTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitArrayAccess(ArrayAccessTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitAssert(AssertTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitAssignment(AssignmentTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitBinary(BinaryTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitBlock(BlockTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitBreak(BreakTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitCase(CaseTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitCatch(CatchTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitClass(ClassTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitCompilationUnit(CompilationUnitTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitCompoundAssignment(CompoundAssignmentTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitConditionalExpression(ConditionalExpressionTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitContinue(ContinueTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitDoWhileLoop(DoWhileLoopTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitEmptyStatement(EmptyStatementTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitEnhancedForLoop(EnhancedForLoopTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitExpressionStatement(ExpressionStatementTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitForLoop(ForLoopTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitIf(IfTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitImport(ImportTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitInstanceOf(InstanceOfTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitLabeledStatement(LabeledStatementTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitLambdaExpression(LambdaExpressionTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitLiteral(LiteralTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitMemberReference(MemberReferenceTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitMethod(MethodTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitMethodInvocation(MethodInvocationTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitModifiers(ModifiersTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitNewArray(NewArrayTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitNewClass(NewClassTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitOther(Tree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitParenthesized(ParenthesizedTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitReturn(ReturnTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitSwitch(SwitchTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitSynchronized(SynchronizedTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitThrow(ThrowTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitTry(TryTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitTypeCast(TypeCastTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitUnary(UnaryTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitVariable(VariableTree tree, ResolverContext context) {
		return null;
	}

	@Override
	public TypeSignature visitWhileLoop(WhileLoopTree tree, ResolverContext context) {
		return null;
	}

}
