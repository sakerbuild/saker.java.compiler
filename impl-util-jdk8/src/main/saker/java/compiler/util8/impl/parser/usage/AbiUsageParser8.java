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
package saker.java.compiler.util8.impl.parser.usage;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;

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
import com.sun.source.tree.StatementTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.tree.TreeVisitor;
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

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.compile.handler.usage.AbiUsageImpl;
import saker.java.compiler.impl.compile.handler.usage.AbiUsageParserBase;
import saker.java.compiler.impl.compile.handler.usage.MemberABIUsage;
import saker.java.compiler.impl.compile.handler.usage.TopLevelABIUsageImpl;
import saker.java.compiler.impl.compile.handler.usage.TopLevelAbiUsage;
import saker.java.compiler.impl.compile.signature.parser.DottedNameCollector;
import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.impl.signature.Signature;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.FieldSignature;
import saker.java.compiler.impl.signature.element.MethodSignature;
import saker.java.compiler.impl.signature.element.PackageSignature;
import saker.java.compiler.jdk.impl.compat.element.DefaultedElementVisitor;
import saker.java.compiler.jdk.impl.compat.tree.DefaultedTreeVisitor;
import saker.java.compiler.jdk.impl.compat.type.DefaultedTypeVisitor;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;
import saker.java.compiler.jdk.impl.parser.signature.CompilationUnitSignatureParser;
import testing.saker.java.compiler.TestFlag;

public class AbiUsageParser8 implements AbiUsageParserBase, DefaultedTreeVisitor<Void, AbiUsageParser8.ParseContext> {
	protected class ParseContext {
		private CompilationUnitTree unit;
		private TreePath currentPath;

		private final TopLevelABIUsageImpl topLevelUsage;
		private AbiUsageImpl usage;

		public final Consumer<TypeElement> typeElementAddUsedTypeConsumer = te -> usage
				.addUsedType(cache.string(te.getQualifiedName()));
		public final Consumer<TypeElement> typeElementAddTypeAppearanceConsumer = te -> usage
				.addTypeNameReference(cache.string(te.getQualifiedName()));

		private final Map<? extends Tree, ? extends Signature> treeSignatures;

		private ClassSignature enclosingClassSignature;

		public ParseContext(CompilationUnitTree unit, TopLevelABIUsageImpl usage,
				Map<? extends Tree, ? extends Signature> treesignatures) {
			this.unit = unit;
			this.usage = usage;
			this.topLevelUsage = usage;
			this.treeSignatures = treesignatures;
		}

		public ParseContext(ParseContext copy, MemberABIUsage usage) {
			this.unit = copy.unit;
			this.currentPath = copy.currentPath;
			this.treeSignatures = copy.treeSignatures;
			this.topLevelUsage = copy.topLevelUsage;
			this.enclosingClassSignature = copy.enclosingClassSignature;

			this.usage = usage;
		}

		public ClassSignature getEnclosingClassSignature() {
			return enclosingClassSignature;
		}

		public void pushClassSignature(ClassSignature sig) {
			this.enclosingClassSignature = sig;
		}

		public void popClassSignature() {
			this.enclosingClassSignature = enclosingClassSignature.getEnclosingSignature();
		}

		TreePath getCurrentPath() {
			return currentPath;
		}
	}

	protected final Trees trees;
	protected final IdentifierElementVisitor identifierElementVisitor = new IdentifierElementVisitor();
	protected final ParserCache cache;
	protected final String sourceVersion;

	protected final Object javacSync = new Object();

	public AbiUsageParser8(Trees trees, String sourceVersion, ParserCache cache) {
		this.trees = trees;
		this.sourceVersion = sourceVersion;
		this.cache = cache;
	}

	public Trees getTrees() {
		return trees;
	}

	//XXX instead of creating treepath, we could use javactask.getTypeMirror or even ((JCTree) tree).type which would mean less object allocations

	protected TreePath descend(Tree tree, ParseContext param) {
		return descend(tree, param, this);
	}

	private static <R> TreePath descend(Tree tree, ParseContext param, TreeVisitor<R, ParseContext> visitor) {
		TreePath result = new TreePath(param.getCurrentPath(), tree);
		descend(tree, result, param, visitor);
		return result;
	}

	private TreePath descend(CompilationUnitTree tree, ParseContext param) {
		TreePath result = new TreePath(param.unit);
		descend(tree, result, param);
		return result;
	}

	private static <R> R descend(Tree tree, TreePath treepath, ParseContext param,
			TreeVisitor<R, ParseContext> visitor) {
		TreePath prev = param.currentPath;
		try {
			param.currentPath = treepath;
			return tree.accept(visitor, param);
		} finally {
			param.currentPath = prev;
		}
	}

	private void descend(Tree tree, TreePath treepath, ParseContext param) {
		descend(tree, treepath, param, this);
	}

	private static String getPackageName(CompilationUnitTree unit) {
		ExpressionTree treepackagename = unit.getPackageName();
		return treepackagename == null ? null : DottedNameCollector.collectDottedName(treepackagename);
	}

	public TopLevelAbiUsage parse(CompilationUnitTree unit, Map<? extends Tree, ? extends Signature> treesignatures) {
		TopLevelABIUsageImpl usage = new TopLevelABIUsageImpl(cache.string(getPackageName(unit)));
		ParseContext param = new ParseContext(unit, usage, treesignatures);
		descend(unit, param);
		return usage;
	}

	private TypeMirror getTypeOf(TreePath path) {
		//XXX could be return ((JCTree) path.getLeaf()).type;
		return trees.getTypeMirror(path);
	}

	private Element getElementOf(TreePath path) {
		return trees.getElement(path);
	}

	private NestingKind getElementNestingKind(TypeElement te) {
		synchronized (javacSync) {
			return te.getNestingKind();
		}
	}

	private Element getEnclosingElement(Element element) {
		synchronized (javacSync) {
			return element.getEnclosingElement();
		}
	}

	private List<? extends VariableElement> getParameters(ExecutableElement ee) {
		synchronized (javacSync) {
			return ee.getParameters();
		}
	}

	private static void consumeTypeElement(TypeMirror type, Consumer<TypeElement> elementconsumer) {
		if (type == null) {
			return;
		}
		type.accept(TypeElementConsumerVisitor.INSTANCE, elementconsumer);
	}

	protected void withExpressionType(TreePath path, Consumer<TypeElement> binarynameconsumer) {
		TypeMirror type = getTypeOf(path);
		consumeTypeElement(type, binarynameconsumer);
	}

	private Set<TypeElement> collectSuperTypes(TypeElement type) {
		Set<TypeElement> result = new HashSet<>();
		synchronized (javacSync) {
			//as we call getInterfaces and getSuperClass of the types
			//that can cause completion of the elements in javac
			//this results in reading files of the classpath, etc...
			//we need to synchronize on a javac sync object in order to avoid concurrent
			//filling of the elements, as that may cause AssertionErrors to be
			//thrown deep inside javac
			collectSuperTypesImpl(type, result);
		}
		return result;
	}

	private void collectSuperTypesImpl(TypeElement type, Set<TypeElement> result) {
		if (type == null) {
			return;
		}
		if (!result.add(type)) {
			return;
		}
		for (TypeMirror itf : type.getInterfaces()) {
			DeclaredType dt = (DeclaredType) itf;
			TypeElement itfelem = (TypeElement) dt.asElement();
			collectSuperTypesImpl(itfelem, result);
		}
		collectSuperTypesImpl(IncrementalElementsTypes.getSuperClassOf(type), result);
	}

	private void addMethodReferenceUsage(TypeElement enclosingtype, String methodname, ParseContext param) {
		for (TypeElement ste : collectSuperTypes(enclosingtype)) {
			String typename = cache.string(ste.getQualifiedName());
			param.usage.addUsedType(typename);
			param.usage.addMethodMemberReference(typename, methodname);
		}
	}

	private void addExecutableElementParametersUsage(ExecutableElement ee, ParseContext param) {
		if (ee == null) {
			//can be in case of error
			return;
		}
		for (VariableElement ve : getParameters(ee)) {
			consumeTypeElement(ve.asType(), param.typeElementAddUsedTypeConsumer);
		}
	}

	private void addExecutableElementUsage(ExecutableElement ee, ParseContext param) {
		if (ee.getKind() == ElementKind.CONSTRUCTOR) {
			throw new IllegalArgumentException(ee.toString() + " is constructor.");
		}
		TypeElement enclosing = (TypeElement) getEnclosingElement(ee);
		addMethodReferenceUsage(enclosing, cache.string(ee.getSimpleName()), param);
		addExecutableElementParametersUsage(ee, param);
	}

	private void addFieldReferenceUsage(TypeElement enclosingtype, String fieldname, ParseContext param) {
		for (TypeElement ste : collectSuperTypes(enclosingtype)) {
			String typename = cache.string(ste.getQualifiedName());
			param.usage.addUsedType(typename);
			param.usage.addFieldMemberReference(typename, fieldname);
		}
	}

	private void addTypeReferenceHierarchyUsage(TypeElement selectortype, String typesimplename, ParseContext param) {
		for (TypeElement ste : collectSuperTypes(selectortype)) {
			String typename = cache.string(ste.getQualifiedName());
			param.usage.addTypeNameReference(typename + "." + typesimplename);
		}
	}

	private void addTypeInheritanceHierarchy(AbiUsageImpl usage, TypeElement superelement) {
		for (TypeElement ste : collectSuperTypes(superelement)) {
			usage.addTypeInheritance(cache.string(ste.getQualifiedName()));
		}
	}

	@Override
	public Void visitAnnotatedType(AnnotatedTypeTree tree, ParseContext param) {
		TreePath typepath = descend(tree.getUnderlyingType(), param);
		withExpressionType(typepath, param.typeElementAddTypeAppearanceConsumer);
		for (AnnotationTree annottree : tree.getAnnotations()) {
			descend(annottree, param);
		}
		return null;
	}

	@Override
	public Void visitAnnotation(AnnotationTree tree, ParseContext param) {
		Tree annottype = tree.getAnnotationType();

		TreePath annottypepath = descend(annottype, param);
		withExpressionType(annottypepath, param.typeElementAddUsedTypeConsumer);

		for (ExpressionTree argtree : tree.getArguments()) {
			descend(argtree, param);
		}
		return null;
	}

	@Override
	public Void visitArrayAccess(ArrayAccessTree tree, ParseContext param) {
		ExpressionTree expression = tree.getExpression();
		ExpressionTree index = tree.getIndex();
		// index must be integral type, no need to add to used types

		descend(expression, param);
		descend(index, param);
		return null;
	}

	@Override
	public Void visitArrayType(ArrayTypeTree tree, ParseContext param) {
		Tree treetype = tree.getType();
		TreePath treetypepath = descend(treetype, param);
		withExpressionType(treetypepath, param.typeElementAddTypeAppearanceConsumer);

		return null;
	}

	@Override
	public Void visitAssert(AssertTree tree, ParseContext param) {
		descend(tree.getCondition(), param);
		ExpressionTree detail = tree.getDetail();
		if (detail != null) {
			descend(detail, param);
		}
		return null;
	}

	@Override
	public Void visitAssignment(AssignmentTree tree, ParseContext param) {
		ExpressionTree var = tree.getVariable();
		ExpressionTree exp = tree.getExpression();

		TreePath varpath = descend(var, param);
		TreePath exppath = descend(exp, param);

		withExpressionType(varpath, param.typeElementAddUsedTypeConsumer);
		withExpressionType(exppath, param.typeElementAddUsedTypeConsumer);

		return null;
	}

	@Override
	public Void visitBinary(BinaryTree tree, ParseContext param) {
		descend(tree.getLeftOperand(), param);
		descend(tree.getRightOperand(), param);
		return null;
	}

	@Override
	public Void visitBlock(BlockTree tree, ParseContext param) {
		for (StatementTree stmtree : tree.getStatements()) {
			descend(stmtree, param);
		}
		return null;
	}

	@Override
	public Void visitBreak(BreakTree tree, ParseContext param) {
		return null;
	}

	@Override
	public Void visitCase(CaseTree tree, ParseContext param) {
		ExpressionTree expression = tree.getExpression();
		if (expression != null) {
			TreePath expressionpath = descend(expression, param);
			withExpressionType(expressionpath, param.typeElementAddUsedTypeConsumer);
		}
		List<? extends StatementTree> stms = tree.getStatements();
		if (!ObjectUtils.isNullOrEmpty(stms)) {
			for (StatementTree stmtree : stms) {
				descend(stmtree, param);
			}
		}
		return null;
	}

	@Override
	public Void visitCatch(CatchTree tree, ParseContext param) {
		VariableTree paramtree = tree.getParameter();

		TreePath paramtreepath = descend(paramtree, param);
		withExpressionType(paramtreepath, param.typeElementAddUsedTypeConsumer);

		descend(tree.getBlock(), param);
		return null;
	}

	@Override
	public Void visitClass(ClassTree tree, ParseContext param) {
		ClassSignature membersig = (ClassSignature) param.treeSignatures.get(tree);
		ParseContext sigcontext;
		if (membersig == null) {
			sigcontext = param;
		} else {
			param.pushClassSignature(membersig);
			MemberABIUsage sigusage = new MemberABIUsage(param.topLevelUsage);
			param.topLevelUsage.addMember(membersig, sigusage);
			sigcontext = new ParseContext(param, sigusage);
		}

		descend(tree.getModifiers(), sigcontext);

//		TypeElement element = (TypeElement) getElementOf(param.getCurrentPath());

		for (Tree member : tree.getMembers()) {
			descend(member, param);
		}
		for (TypeParameterTree tpt : tree.getTypeParameters()) {
			descend(tpt, sigcontext);
		}
		Tree extendsclause = tree.getExtendsClause();
		if (extendsclause != null) {
			TreePath extendsclausepath = descend(extendsclause, sigcontext);
//			addTypeInheritance(extendsclause, param);
			withExpressionType(extendsclausepath, te -> {
				addTypeInheritanceHierarchy(sigcontext.usage, te);
			});
		}
		for (Tree impl : tree.getImplementsClause()) {
			TreePath implpath = descend(impl, sigcontext);
//			addTypeInheritance(impl, param);
			withExpressionType(implpath, te -> {
				addTypeInheritanceHierarchy(sigcontext.usage, te);
			});
		}
		if (membersig != null) {
			param.popClassSignature();
		}
		return null;
	}

	@Override
	public Void visitCompilationUnit(CompilationUnitTree tree, ParseContext param) {
		for (Tree typetree : tree.getTypeDecls()) {
			descend(typetree, param);
		}
		for (ImportTree importtree : tree.getImports()) {
			descend(importtree, param);
		}
		//TODO we need to use the same tree object to query the signature as in compilation unit signature parser
		PackageSignature membersig = (PackageSignature) param.treeSignatures.get(tree.getPackageName());
		ParseContext sigcontext;
		if (membersig == null) {
			sigcontext = param;
		} else {
			MemberABIUsage sigusage = new MemberABIUsage(param.topLevelUsage);
			param.topLevelUsage.setPackageUsage(sigusage);
			sigcontext = new ParseContext(param, sigusage);
		}
		for (AnnotationTree annot : tree.getPackageAnnotations()) {
			descend(annot, sigcontext);
		}
		return null;
	}

	@Override
	public Void visitCompoundAssignment(CompoundAssignmentTree tree, ParseContext param) {
		ExpressionTree var = tree.getVariable();
		ExpressionTree exp = tree.getExpression();

		TreePath varpath = descend(var, param);
		TreePath exppath = descend(exp, param);

		withExpressionType(varpath, param.typeElementAddUsedTypeConsumer);
		withExpressionType(exppath, param.typeElementAddUsedTypeConsumer);

		return null;
	}

	@Override
	public Void visitConditionalExpression(ConditionalExpressionTree tree, ParseContext param) {
		descend(tree.getCondition(), param);
		descend(tree.getTrueExpression(), param);
		descend(tree.getFalseExpression(), param);
		return null;
	}

	@Override
	public Void visitContinue(ContinueTree tree, ParseContext param) {
		return null;
	}

	@Override
	public Void visitDoWhileLoop(DoWhileLoopTree tree, ParseContext param) {
		descend(tree.getStatement(), param);
		descend(tree.getCondition(), param);
		return null;
	}

	@Override
	public Void visitEmptyStatement(EmptyStatementTree tree, ParseContext param) {
		return null;
	}

	@Override
	public Void visitEnhancedForLoop(EnhancedForLoopTree tree, ParseContext param) {
		ExpressionTree expression = tree.getExpression();

		// stay iterable
		TreePath expressionpath = descend(expression, param);
		withExpressionType(expressionpath, te -> {
			param.typeElementAddUsedTypeConsumer.accept(te);
			addMethodReferenceUsage(te, "iterator", param);
		});

		descend(tree.getVariable(), param);
		descend(tree.getStatement(), param);
		return null;
	}

	@Override
	public Void visitErroneous(ErroneousTree tree, ParseContext param) {
		return null;
	}

	@Override
	public Void visitExpressionStatement(ExpressionStatementTree tree, ParseContext param) {
		descend(tree.getExpression(), param);
		return null;
	}

	@Override
	public Void visitForLoop(ForLoopTree tree, ParseContext param) {
		for (StatementTree initertree : tree.getInitializer()) {
			descend(initertree, param);
		}
		ExpressionTree condition = tree.getCondition();
		if (condition != null) {
			descend(condition, param);
		}
		descend(tree.getStatement(), param);
		for (ExpressionStatementTree updatetree : tree.getUpdate()) {
			descend(updatetree, param);
		}
		return null;
	}

	private class IdentifierElementVisitor implements DefaultedElementVisitor<Void, ParseContext> {
		@Override
		public Void visitPackage(PackageElement elem, ParseContext param) {
			param.usage.addPresentSimpleTypeIdentifier(cache.string(elem.getSimpleName()));
			return null;
		}

		@Override
		public Void visitType(TypeElement elem, ParseContext param) {
			param.usage.addPresentSimpleTypeIdentifier(cache.string(elem.getSimpleName()));
			param.usage.addTypeNameReference(cache.string(elem.getQualifiedName()));
			return null;
		}

		@Override
		public Void visitVariable(VariableElement elem, ParseContext param) {
			// add member reference
			Element enclosing = getEnclosingElement(elem);
			ElementKind ekind = enclosing.getKind();
			String elemsimplename = cache.string(elem.getSimpleName());
			if (ekind.isInterface() || ekind.isClass()) {
				TypeElement enclosingtype = (TypeElement) enclosing;
				// was declared in a type
				param.usage.addFieldMemberReference(cache.string(enclosingtype.getQualifiedName()), elemsimplename);
			} else {
				// variable defined in method or elsewhere
//							throw new RuntimeException(tree + " --- " + enclosing.toString() + " - " + enclosing.getKind());
			}
			param.usage.addPresentSimpleVariableIdentifier(elemsimplename);
			return null;
		}

		@Override
		public Void visitExecutable(ExecutableElement elem, ParseContext param) {
			Element enclosing = getEnclosingElement(elem);
			TypeElement enclosingtype = (TypeElement) enclosing;
			// was declared in a type
			addMethodReferenceUsage(enclosingtype, cache.string(elem.getSimpleName()), param);
			return null;
		}

		@Override
		public Void visitTypeParameter(TypeParameterElement elem, ParseContext param) {
			// nothing to do here
			return null;
		}

		@Override
		public Void visitUnknown(Element elem, ParseContext param) {
			return null;
		}
	}

	@Override
	public Void visitIdentifier(IdentifierTree tree, ParseContext param) {
		Name treename = tree.getName();
		if (treename.contentEquals("this") || treename.contentEquals("super")) {
			//this, super identifier can be ignored regarding the usage
			return null;
		}
		TreePath path = param.getCurrentPath();

		Element elem = getElementOf(path);
		if (elem != null) {
			elem.accept(identifierElementVisitor, param);
		}
		return null;
	}

	@Override
	public Void visitIf(IfTree tree, ParseContext param) {
		descend(tree.getCondition(), param);
		descend(tree.getThenStatement(), param);
		StatementTree elsestm = tree.getElseStatement();
		if (elsestm != null) {
			descend(elsestm, param);
		}
		return null;
	}

	private void addTypeImportReferences(TypeElement element, ParseContext param) {
		while (true) {
			param.usage.addTypeNameReference(cache.string(element.getQualifiedName()));
			Element enclosing = getEnclosingElement(element);
			ElementKind enclosingkind = enclosing.getKind();
			if (enclosingkind.isClass() || enclosingkind.isInterface()) {
				element = (TypeElement) enclosing;
			} else {
				//no more enclosing types
				break;
			}
		}
	}

	@Override
	public Void visitImport(ImportTree tree, ParseContext param) {
		Tree quidtree = tree.getQualifiedIdentifier();
		TreePath currentpath = param.getCurrentPath();
		Kind kind = quidtree.getKind();
		// identifier cannot be as there is no simple identifier importing. From which package is it then?
		if (kind == Kind.MEMBER_SELECT) {
			MemberSelectTree ms = (MemberSelectTree) quidtree;
			if (ms.getIdentifier().contentEquals("*")) {
				//wildcard
				TreePath path = new TreePath(currentpath, ms.getExpression());
				QualifiedNameable element = (QualifiedNameable) getElementOf(path);
				if (element != null) {
					ElementKind elementkind = element.getKind();
					if (elementkind.isClass() || elementkind.isInterface()) {
						addTypeImportReferences((TypeElement) element, param);
					}
					String elementpath = cache.string(element.getQualifiedName());
					if (tree.isStatic()) {
						param.usage.addWildcardStaticImportPath(elementpath);
					}
					//add types even if the import is static as they import types as well
					param.usage.addWildcardTypeImportPath(elementpath);
				}
			} else {
				if (tree.isStatic()) {
					//a static import can be to an inner class too
					//for example import static java.util.Map.Entry; works
					TreePath path = new TreePath(currentpath, ms.getExpression());
					TypeElement element = (TypeElement) getElementOf(path);
					if (element != null) {
						String membername = cache.string(ms.getIdentifier());
						String qname = cache.string(element.getQualifiedName());
						param.usage.addFieldMemberReference(qname, membername);
						param.usage.addMethodMemberReference(qname, membername);
						param.usage.addTypeNameReference(qname + "." + membername);
						addTypeImportReferences(element, param);
					}
				} else {
					//non static -> type import
					TreePath path = new TreePath(currentpath, ms);
					TypeElement element = (TypeElement) getElementOf(path);
					if (element != null) {
						addTypeImportReferences(element, param);
					}
				}
			}
		}

		return null;
	}

	@Override
	public Void visitInstanceOf(InstanceOfTree tree, ParseContext param) {
		Tree type = tree.getType();
		ExpressionTree expression = tree.getExpression();

		descend(expression, param);
		//we dont need to add expression type as usage, as changing it doesnt affect the compilation result

		TreePath typepath = descend(type, param);
		withExpressionType(typepath, param.typeElementAddUsedTypeConsumer);

		return null;
	}

	@Override
	public Void visitIntersectionType(IntersectionTypeTree tree, ParseContext param) {
		// in a cast expression
		// e.g. (Runnable & Serializable)
		for (Tree bound : tree.getBounds()) {
			TreePath boundpath = descend(bound, param);
			withExpressionType(boundpath, param.typeElementAddUsedTypeConsumer);
		}
		return null;
	}

	@Override
	public Void visitLabeledStatement(LabeledStatementTree tree, ParseContext param) {
		descend(tree.getStatement(), param);
		return null;
	}

	@Override
	public Void visitLiteral(LiteralTree tree, ParseContext param) {
		return null;
	}

	private void addCurrentTreeLambdaTypeUsage(ParseContext param) {
		withExpressionType(param.getCurrentPath(), te -> {
			String name = cache.string(te.getQualifiedName());
			param.usage.addUsedType(name);
			// consider lambda as type inheritance
			addTypeInheritanceHierarchy(param.usage, te);
		});
	}

	@Override
	public Void visitLambdaExpression(LambdaExpressionTree tree, ParseContext param) {
		addCurrentTreeLambdaTypeUsage(param);

		descend(tree.getBody(), param);
		for (VariableTree var : tree.getParameters()) {
			descend(var, param);
		}
		return null;
	}

	@Override
	public Void visitMemberReference(MemberReferenceTree tree, ParseContext param) {
		// :: operator
		addCurrentTreeLambdaTypeUsage(param);

		ExpressionTree exp = tree.getQualifierExpression();
		TreePath exppath = descend(exp, param);
		List<? extends ExpressionTree> typeargs = tree.getTypeArguments();
		if (typeargs != null) {
			for (ExpressionTree typearg : typeargs) {
				TreePath typeargpath = descend(typearg, param);
				withExpressionType(typeargpath, param.typeElementAddUsedTypeConsumer);
			}
		}

		ExecutableElement ee = (ExecutableElement) getElementOf(param.getCurrentPath());
		switch (tree.getMode()) {
			case INVOKE: {
				//add every superclass method member reference
				//as if a method with same name is added to the hierarchy, then the unit must be recompiled
				//we have to add the super classes with the method as well, as if a more appropriate method is added to the superclass, 
				//    that will be chosen instead (with different signature)
				withExpressionType(exppath, te -> {
					String methodname = cache.string(tree.getName());
					addMethodReferenceUsage(te, methodname, param);
				});
				break;
			}
			case NEW: {
				//in case of ::new, we dont have to include the superclass method names, as it is a direct reference to a class constructor
				String typename = cache.string(((TypeElement) getEnclosingElement(ee)).getQualifiedName());
				param.usage.addUsedType(typename);
				param.usage.addMethodMemberReference(typename, IncrementalElementsTypes.CONSTRUCTOR_METHOD_NAME);
				break;
			}
			default: {
				break;
			}
		}
		addExecutableElementParametersUsage(ee, param);

		return null;
	}

	@Override
	public Void visitMemberSelect(MemberSelectTree tree, ParseContext param) {
		TreePath expressionpath = descend(tree.getExpression(), param);
		Element element = getElementOf(param.getCurrentPath());
		if (element == null) {
			return null;
		}
		//XXX visitorize?
		switch (element.getKind()) {
			case ENUM:
			case INTERFACE:
			case ANNOTATION_TYPE:
			case CLASS: {
				TypeElement te = (TypeElement) element;
				if (getElementNestingKind(te) == NestingKind.TOP_LEVEL) {
					param.usage.addTypeNameReference(cache.string(te.getQualifiedName()));
				} else {
					//not top level class was member selected
					Element selectorelement = getElementOf(expressionpath);
					if (selectorelement == getEnclosingElement(te)) {
						//the same container member was used to select the type
						param.usage.addTypeNameReference(cache.string(te.getQualifiedName()));
					} else {
						//some other container was used to select the type
						//for example a SubClass.ClassInSupeClass
						addTypeReferenceHierarchyUsage((TypeElement) selectorelement, cache.string(te.getSimpleName()),
								param);
					}
				}
				break;
			}
			case ENUM_CONSTANT:
			case FIELD: {
				VariableElement ve = (VariableElement) element;
				TypeElement enclosing = (TypeElement) getEnclosingElement(ve);
				Element selectorelement = getElementOf(expressionpath);
				String treeidentifier = cache.string(tree.getIdentifier());
				if (selectorelement == enclosing) {
					param.usage.addFieldMemberReference(cache.string(enclosing.getQualifiedName()), treeidentifier);
				} else {
					addFieldReferenceUsage(enclosing, treeidentifier, param);
				}
				break;
			}
			case PACKAGE: {
				break;
			}
			case METHOD: {
				ExecutableElement ee = (ExecutableElement) element;
				addExecutableElementUsage(ee, param);
				break;
			}
			case CONSTRUCTOR: {
				ExecutableElement ee = (ExecutableElement) element;
				String typename = cache.string(((TypeElement) getEnclosingElement(ee)).getQualifiedName());
				param.usage.addUsedType(typename);
				param.usage.addMethodMemberReference(typename, IncrementalElementsTypes.CONSTRUCTOR_METHOD_NAME);
				addExecutableElementParametersUsage(ee, param);
				break;
			}
			case LOCAL_VARIABLE:
			case EXCEPTION_PARAMETER:
			case PARAMETER:
			case RESOURCE_VARIABLE:
			case INSTANCE_INIT:
			case STATIC_INIT:
			case TYPE_PARAMETER:
			case OTHER:
			default: {
				if (TestFlag.ENABLED) {
					throw new AssertionError(
							"Unknown element kind: " + element.getKind() + " - " + tree + " - " + element);
				}
				break;
			}
		}
		return null;
	}

	@Override
	public Void visitMethod(MethodTree tree, ParseContext param) {
		MethodSignature membersig = (MethodSignature) param.treeSignatures.get(tree);
		ParseContext sigcontext;
		if (membersig == null) {
			sigcontext = param;
		} else {
			MemberABIUsage sigusage = new MemberABIUsage(param.topLevelUsage);
			param.topLevelUsage.addMember(param.getEnclosingClassSignature(), membersig, sigusage);
			sigcontext = new ParseContext(param, sigusage);
		}

		descend(tree.getModifiers(), sigcontext);

		BlockTree body = tree.getBody();
		if (body != null) {
			descend(body, param);
		}
		Tree defaultval = tree.getDefaultValue();
		if (defaultval != null) {
			// for annotations
			descend(defaultval, param);
		}
		for (VariableTree p : tree.getParameters()) {
			TreePath ppath = descend(p, sigcontext);
			Tree ptype = p.getType();
			if (ptype != null) {
				//add used type as changing the type itself can result in different overload resolution
				withExpressionType(new TreePath(ppath, ptype), sigcontext.typeElementAddUsedTypeConsumer);
			}
		}
		Tree ret = tree.getReturnType();
		if (ret != null) {
			// used type as return type, casting is important
			TreePath retpath = descend(ret, sigcontext);
			withExpressionType(retpath, sigcontext.typeElementAddUsedTypeConsumer);
		}
		for (ExpressionTree exc : tree.getThrows()) {
			TreePath excpath = descend(exc, sigcontext);
			withExpressionType(excpath, sigcontext.typeElementAddUsedTypeConsumer);
		}
		for (TypeParameterTree typetree : tree.getTypeParameters()) {
			descend(typetree, sigcontext);
		}
		VariableTree rec = tree.getReceiverParameter();
		//we should only examine its annotations
		//those must be the same as the enclosing classes arent they?
		if (rec != null) {
			descend(rec, sigcontext);
		}

		return null;
	}

	@Override
	public Void visitMethodInvocation(MethodInvocationTree tree, ParseContext param) {
		for (ExpressionTree arg : tree.getArguments()) {
			descend(arg, param);
		}
		for (Tree typearg : tree.getTypeArguments()) {
			TreePath typeargpath = descend(typearg, param);
			withExpressionType(typeargpath, param.typeElementAddUsedTypeConsumer);
		}
		TreePath selectpath = descend(tree.getMethodSelect(), param);
		Element elem = getElementOf(selectpath);
		ExecutableElement ee = (ExecutableElement) elem;
		addExecutableElementParametersUsage(ee, param);
		return null;
	}

	@Override
	public Void visitModifiers(ModifiersTree tree, ParseContext param) {
		for (AnnotationTree annot : tree.getAnnotations()) {
			descend(annot, param);
		}
		return null;
	}

	@Override
	public Void visitNewArray(NewArrayTree tree, ParseContext param) {
		Tree arraytype = tree.getType();
		if (arraytype != null) {
			// can be null in case of { ... }
			TreePath arraytypepath = descend(arraytype, param);
			withExpressionType(arraytypepath, param.typeElementAddTypeAppearanceConsumer);
		}
		for (AnnotationTree annottree : tree.getAnnotations()) {
			descend(annottree, param);
		}
		for (List<? extends AnnotationTree> dimannots : tree.getDimAnnotations()) {
			for (AnnotationTree at : dimannots) {
				descend(at, param);
			}
		}
		for (ExpressionTree dims : tree.getDimensions()) {
			descend(dims, param);
		}
		List<? extends ExpressionTree> initers = tree.getInitializers();
		if (initers != null) {
			for (ExpressionTree initer : initers) {
				TreePath initerpath = descend(initer, param);
				withExpressionType(initerpath, param.typeElementAddUsedTypeConsumer);
			}
		}
		return null;
	}

	@Override
	public Void visitNewClass(NewClassTree tree, ParseContext param) {
		ClassTree body = tree.getClassBody();
		if (body != null) {
			descend(body, param);
		}
		for (ExpressionTree argtree : tree.getArguments()) {
			descend(argtree, param);
		}
		ExpressionTree identifier = tree.getIdentifier();
		TreePath identifierpath = descend(identifier, param);

		ExecutableElement ee = (ExecutableElement) getElementOf(param.getCurrentPath());
		addExecutableElementParametersUsage(ee, param);

		if (body != null) {
			withExpressionType(identifierpath, te -> {
				addTypeInheritanceHierarchy(param.usage, te);
				if (!te.getKind().isInterface()) {
					//no need to add constructor reference for an interface
					param.usage.addMethodMemberReference(cache.string(te.getQualifiedName()),
							IncrementalElementsTypes.CONSTRUCTOR_METHOD_NAME);
				}
			});
		} else {
			withExpressionType(identifierpath, te -> {
				String qname = cache.string(te.getQualifiedName());
				param.usage.addUsedType(qname);
				if (!te.getKind().isInterface()) {
					//no need to add constructor reference for an interface
					param.usage.addMethodMemberReference(qname, IncrementalElementsTypes.CONSTRUCTOR_METHOD_NAME);
				}
			});
		}

		for (Tree typearg : tree.getTypeArguments()) {
			TreePath typeargpath = descend(typearg, param);
			withExpressionType(typeargpath, param.typeElementAddUsedTypeConsumer);
		}
		ExpressionTree enclosing = tree.getEnclosingExpression();
		if (enclosing != null) {
			descend(enclosing, param);
		}
		return null;
	}

	@Override
	public Void visitOther(Tree tree, ParseContext param) {
		//TODO make the resulting abi usage always affected
		System.err.println(
				"ABIUsageParser.visitOther() Other tree: " + tree.getKind() + " - " + tree.getClass() + " - " + tree);
		return null;
	}

	@Override
	public Void visitParameterizedType(ParameterizedTypeTree tree, ParseContext param) {
		Tree type = tree.getType();

		TreePath typepath = descend(type, param);
		withExpressionType(typepath, param.typeElementAddTypeAppearanceConsumer);

		for (Tree arg : tree.getTypeArguments()) {
			TreePath argpath = descend(arg, param);
			withExpressionType(argpath, param.typeElementAddUsedTypeConsumer);
		}
		return null;
	}

	@Override
	public Void visitParenthesized(ParenthesizedTree tree, ParseContext param) {
		descend(tree.getExpression(), param);
		return null;
	}

	@Override
	public Void visitPrimitiveType(PrimitiveTypeTree tree, ParseContext param) {
		return null;
	}

	@Override
	public Void visitReturn(ReturnTree tree, ParseContext param) {
		ExpressionTree exp = tree.getExpression();
		if (exp != null) {
			TreePath exppath = descend(exp, param);
			withExpressionType(exppath, param.typeElementAddUsedTypeConsumer);
		}
		return null;
	}

	@Override
	public Void visitSwitch(SwitchTree tree, ParseContext param) {
		ExpressionTree expression = tree.getExpression();

		TreePath expressionpath = descend(expression, param);
		withExpressionType(expressionpath, param.typeElementAddUsedTypeConsumer);
		for (CaseTree casetree : tree.getCases()) {
			descend(casetree, param);
		}
		return null;
	}

	@Override
	public Void visitSynchronized(SynchronizedTree tree, ParseContext param) {
		descend(tree.getExpression(), param);
		descend(tree.getBlock(), param);
		return null;
	}

	@Override
	public Void visitThrow(ThrowTree tree, ParseContext param) {
		ExpressionTree expression = tree.getExpression();

		TreePath expressionpath = descend(expression, param);
		withExpressionType(expressionpath, param.typeElementAddUsedTypeConsumer);
		return null;
	}

	@Override
	public Void visitTry(TryTree tree, ParseContext param) {
		for (Tree restree : tree.getResources()) {
			TreePath restreepath = descend(restree, param);
			//inheritance change is not needed to be added, as adding the method usage adds it indirectly
			withExpressionType(restreepath, te -> {
				param.typeElementAddUsedTypeConsumer.accept(te);
				addMethodReferenceUsage(te, "close", param);
			});
		}
		descend(tree.getBlock(), param);
		for (CatchTree catchtree : tree.getCatches()) {
			descend(catchtree, param);
		}
		BlockTree finallyblock = tree.getFinallyBlock();
		if (finallyblock != null) {
			descend(finallyblock, param);
		}
		return null;
	}

	@Override
	public Void visitTypeCast(TypeCastTree tree, ParseContext param) {
		Tree type = tree.getType();
		ExpressionTree expression = tree.getExpression();

		TreePath typepath = descend(type, param);
		withExpressionType(typepath, param.typeElementAddUsedTypeConsumer);

		descend(expression, param);
		return null;
	}

	@Override
	public Void visitTypeParameter(TypeParameterTree tree, ParseContext param) {
		for (Tree bound : tree.getBounds()) {
			TreePath boundpath = descend(bound, param);
			//the type should be added as used types instead of only presence
			//as if the type is changed to a class instead of interface
			//that could cause the bounds to be invalid, therefore the recompilation is necessary
			withExpressionType(boundpath, param.typeElementAddUsedTypeConsumer);
		}
		for (AnnotationTree annot : tree.getAnnotations()) {
			descend(annot, param);
		}
		return null;
	}

	@Override
	public Void visitUnary(UnaryTree tree, ParseContext param) {
		descend(tree.getExpression(), param);
		return null;
	}

	@Override
	public Void visitUnionType(UnionTypeTree tree, ParseContext param) {
		for (Tree alttree : tree.getTypeAlternatives()) {
			// this occurrs in multicatch, so types are used, as it needs Exception / Throwable / ... parents
			TreePath alttreepath = descend(alttree, param);
			withExpressionType(alttreepath, param.typeElementAddUsedTypeConsumer);
		}
		return null;
	}

	@Override
	public Void visitVariable(VariableTree tree, ParseContext param) {
		ParseContext sigcontext;
		Signature treesig = param.treeSignatures.get(tree);
		if (treesig instanceof FieldSignature) {
			FieldSignature membersig = (FieldSignature) treesig;
			MemberABIUsage sigusage = new MemberABIUsage(param.topLevelUsage);
			sigcontext = new ParseContext(param, sigusage);
			param.topLevelUsage.addMember(param.getEnclosingClassSignature(), membersig, sigusage);
		} else {
			sigcontext = param;
		}

		descend(tree.getModifiers(), sigcontext);

		Tree type = tree.getType();
		if (type != null) {
			TreePath typepath = descend(type, sigcontext);
			withExpressionType(typepath, sigcontext.typeElementAddTypeAppearanceConsumer);
		}

		ExpressionTree initer = tree.getInitializer();
		if (initer != null) {
			TreePath parentpath = param.getCurrentPath().getParentPath();
			Tree parenttree = parentpath.getLeaf();

			TreePath initerpath;
			if (CompilationUnitSignatureParser.isConstantVariable(tree, parenttree.getKind())) {
				initerpath = descend(initer, sigcontext);
			} else {
				initerpath = descend(initer, param);
			}
			withExpressionType(initerpath, param.typeElementAddUsedTypeConsumer);
		}

		return null;
	}

	@Override
	public Void visitWhileLoop(WhileLoopTree tree, ParseContext param) {
		descend(tree.getCondition(), param);
		descend(tree.getStatement(), param);
		return null;
	}

	@Override
	public Void visitWildcard(WildcardTree tree, ParseContext param) {
		Tree bound = tree.getBound();
		// bound can be null if type is ?
		if (bound != null) {
			TreePath boundpath = descend(bound, param);
			withExpressionType(boundpath, param.typeElementAddUsedTypeConsumer);
		}
		return null;
	}

	private static class TypeElementConsumerVisitor implements DefaultedTypeVisitor<Void, Consumer<TypeElement>> {
		public static final TypeElementConsumerVisitor INSTANCE = new TypeElementConsumerVisitor();

		@Override
		public Void visitArray(ArrayType t, Consumer<TypeElement> p) {
			t.getComponentType().accept(this, p);
			return null;
		}

		@Override
		public Void visitDeclared(DeclaredType t, Consumer<TypeElement> p) {
			TypeElement elem = (TypeElement) t.asElement();
			p.accept(elem);
			return null;
		}

		@Override
		public Void visitTypeVariable(TypeVariable t, Consumer<TypeElement> p) {
			TypeMirror upper = t.getUpperBound();
			TypeMirror lower = t.getLowerBound();
			upper.accept(this, p);
			lower.accept(this, p);
			return null;
		}

		@Override
		public Void visitWildcard(WildcardType t, Consumer<TypeElement> p) {
			TypeMirror upper = t.getExtendsBound();
			TypeMirror lower = t.getSuperBound();
			if (upper != null) {
				upper.accept(this, p);
			}
			if (lower != null) {
				lower.accept(this, p);
			}
			return null;
		}

		@Override
		public Void visitExecutable(ExecutableType t, Consumer<TypeElement> p) {
			t.getReturnType().accept(this, p);
			return null;
		}

		@Override
		public Void visitIntersection(IntersectionType t, Consumer<TypeElement> p) {
			for (TypeMirror b : t.getBounds()) {
				b.accept(this, p);
			}
			return null;
		}

		@Override
		public Void visitUnion(UnionType t, Consumer<TypeElement> p) {
			for (TypeMirror b : t.getAlternatives()) {
				b.accept(this, p);
			}
			return null;
		}

		@Override
		public Void visitError(ErrorType t, Consumer<TypeElement> p) {
			return null;
		}

		@Override
		public Void visitPrimitive(PrimitiveType t, Consumer<TypeElement> p) {
			return null;
		}

		@Override
		public Void visitNull(NullType t, Consumer<TypeElement> p) {
			return null;
		}

		@Override
		public Void visitNoType(NoType t, Consumer<TypeElement> p) {
			return null;
		}
	}
}
