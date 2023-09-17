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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;
import javax.lang.model.type.TypeKind;
import javax.tools.JavaFileObject;

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
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.tree.DocCommentTable;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.compat.ElementKindCompatUtils;
import saker.java.compiler.impl.compile.handler.incremental.JavacPrivateAPIError;
import saker.java.compiler.impl.compile.handler.incremental.model.scope.ImportDeclaration;
import saker.java.compiler.impl.compile.handler.incremental.model.scope.ImportScope;
import saker.java.compiler.impl.compile.handler.incremental.model.scope.SimpleImportScope;
import saker.java.compiler.impl.compile.handler.info.SignaturePath;
import saker.java.compiler.impl.compile.signature.annot.val.AnnotValueImpl;
import saker.java.compiler.impl.compile.signature.annot.val.ArrayValueImpl;
import saker.java.compiler.impl.compile.signature.annot.val.LiteralValueImpl;
import saker.java.compiler.impl.compile.signature.annot.val.ReferenceValueImpl;
import saker.java.compiler.impl.compile.signature.annot.val.TypeValueImpl;
import saker.java.compiler.impl.compile.signature.annot.val.UnknownValueImpl;
import saker.java.compiler.impl.compile.signature.impl.ClassSignatureImpl;
import saker.java.compiler.impl.compile.signature.impl.FieldSignatureImpl;
import saker.java.compiler.impl.compile.signature.impl.FullMethodSignature;
import saker.java.compiler.impl.compile.signature.impl.MethodParameterSignatureImpl;
import saker.java.compiler.impl.compile.signature.impl.ModuleSignatureImpl;
import saker.java.compiler.impl.compile.signature.impl.PackageSignatureImpl;
import saker.java.compiler.impl.compile.signature.parser.CompilationUnitSignatureParserBase;
import saker.java.compiler.impl.compile.signature.parser.DottedNameCollector;
import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.impl.compile.signature.type.impl.IntersectionTypeSignatureImpl;
import saker.java.compiler.impl.compile.signature.type.impl.NoTypeSignatureImpl;
import saker.java.compiler.impl.compile.signature.type.impl.TypeParameterSignatureImpl;
import saker.java.compiler.impl.compile.signature.value.BinaryConstantOperator;
import saker.java.compiler.impl.compile.signature.value.CastConstantOperator;
import saker.java.compiler.impl.compile.signature.value.IdentifierConstantResolver;
import saker.java.compiler.impl.compile.signature.value.LiteralConstantResolver;
import saker.java.compiler.impl.compile.signature.value.NotConstantResolverImpl;
import saker.java.compiler.impl.compile.signature.value.TernaryConstantOperator;
import saker.java.compiler.impl.compile.signature.value.UnaryConstantOperator;
import saker.java.compiler.impl.compile.signature.value.VariableConstantMemberResolver;
import saker.java.compiler.impl.signature.Signature;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.element.AnnotationSignature.Value;
import saker.java.compiler.impl.signature.element.ClassMemberSignature;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.ClassSignature.PermittedSubclassesList;
import saker.java.compiler.impl.signature.element.FieldSignature;
import saker.java.compiler.impl.signature.element.MethodParameterSignature;
import saker.java.compiler.impl.signature.element.MethodSignature;
import saker.java.compiler.impl.signature.element.ModuleSignature;
import saker.java.compiler.impl.signature.element.PackageSignature;
import saker.java.compiler.impl.signature.type.IntersectionTypeSignature;
import saker.java.compiler.impl.signature.type.TypeParameterSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.signature.value.ConstantValueResolver;
import saker.java.compiler.impl.util.ImmutableModifierSet;
import saker.java.compiler.jdk.impl.compat.tree.DefaultedTreeVisitor;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

public class CompilationUnitSignatureParser8 implements CompilationUnitSignatureParserBase,
		DefaultedTreeVisitor<Signature, CompilationUnitSignatureParser8.ParseContext> {
	private static final String PACKAGE_INFO_JAVA = "package-info.java";
	private static final int PACKAGE_INFO_JAVA_LENGTH = 17;

	public static class ParseContext implements CompilationUnitSignatureParserBase.ParseContextBase {
		private final ParserCache cache;
		private CompilationUnitTree unit;
		private ImportScope importScope;
		private PackageSignature packageSignature;
		private ModuleSignatureImpl moduleSignature;
		private NestingKind nestingKind = NestingKind.TOP_LEVEL;
		private ClassSignature enclosingClassSignature;

		private NavigableMap<String, ClassSignature> classes;
		private String packageName;

		protected IdentityHashMap<Tree, Signature> treeSignatures;
		private Map<Tree, SignaturePath> treeSignaturePaths;
		private Deque<SignaturePath> signaturePathStack;

		private final StringBuilder sb = new StringBuilder();

		public ParseContext(ParserCache cache) {
			this.cache = cache;
		}

		public SignaturePath pushSignaturePath() {
			SignaturePath result;
			if (!signaturePathStack.isEmpty()) {
				result = new SignaturePath(signaturePathStack.getLast());
			} else {
				result = new SignaturePath();
			}
			signaturePathStack.addLast(result);
			return result;
		}

		public SignaturePath pushIndexedSignaturePath(Object index) {
			if (index == null) {
				return pushSignaturePath();
			}
			SignaturePath result;
			if (!signaturePathStack.isEmpty()) {
				result = SignaturePath.createIndexed(signaturePathStack.getLast(), index);
			} else {
				result = SignaturePath.createIndexed(index);
			}
			signaturePathStack.addLast(result);
			return result;
		}

		public SignaturePath pushSignaturePath(Signature signature) {
			SignaturePath result;
			if (!signaturePathStack.isEmpty()) {
				result = new SignaturePath(signaturePathStack.getLast(), signature);
			} else {
				result = new SignaturePath(signature);
			}
			signaturePathStack.addLast(result);
			return result;
		}

		public void popSignaturePath() {
			signaturePathStack.removeLast();
		}

		public void popSignaturePath(Tree tree) {
			SignaturePath last = signaturePathStack.removeLast();
			treeSignaturePaths.put(tree, last);
		}

		@Override
		public ImportScope getImportScope() {
			return importScope;
		}

		@Override
		public Map<? extends Tree, ? extends Signature> getTreeSignatures() {
			return treeSignatures;
		}

		@Override
		public Map<? extends Tree, ? extends SignaturePath> getTreeSignaturePaths() {
			return treeSignaturePaths;
		}

		public CompilationUnitTree getCompilationUnit() {
			return unit;
		}

		public void set(CompilationUnitTree unit) {
			this.unit = unit;
			this.packageSignature = null;
			this.moduleSignature = null;
			this.nestingKind = NestingKind.TOP_LEVEL;
			this.enclosingClassSignature = null;
			this.classes = new TreeMap<>();
			this.treeSignatures = new IdentityHashMap<>();
			this.treeSignaturePaths = new IdentityHashMap<>();
			this.signaturePathStack = new ArrayDeque<>();

			sb.setLength(0);
			if (CompilationUnitSignatureParser8.getPackageName(unit, sb)) {
				this.packageName = cache.string(sb);
			} else {
				this.packageName = null;
			}
			importScope = createImportScope(unit);
		}

		private ImportScope createImportScope(CompilationUnitTree unit) {
			List<? extends ImportTree> importtrees = unit.getImports();
			return SimpleImportScope.create(cache, packageName, createImportDeclarations(importtrees));
		}

		private NavigableSet<ImportDeclaration> createImportDeclarations(List<? extends ImportTree> importtrees) {
			if (importtrees.isEmpty()) {
				return Collections.emptyNavigableSet();
			}
			NavigableSet<ImportDeclaration> result = new TreeSet<>();
			for (ImportTree itree : importtrees) {
				String importname = collectDottedName(itree.getQualifiedIdentifier());
				ImportDeclaration importdecl = cache.importDeclaration(importname, itree.isStatic());
				result.add(importdecl);
			}
			return result;
		}

		public String collectDottedName(Tree tree) {
			sb.setLength(0);
			DottedNameCollector.collectDottedName(tree, sb);
			return sb.toString();
		}

		public NestingKind getNestingKind() {
			return nestingKind;
		}

		public void setNestingKind(NestingKind nestingKind) {
			this.nestingKind = nestingKind;
		}

		public void pushClassSignature(ClassSignature sig) {
			if (nestingKind == NestingKind.TOP_LEVEL) {
				classes.put(sig.getCanonicalName(), sig);
			}
			this.enclosingClassSignature = sig;
		}

		public void popClassSignature() {
			this.enclosingClassSignature = enclosingClassSignature.getEnclosingSignature();
		}

		public ClassSignature getEnclosingClassSignature() {
			return enclosingClassSignature;
		}

		public byte getDeclaringKindIndex() {
			return getEnclosingClassSignature().getKindIndex();
		}

		@Override
		public NavigableMap<String, ClassSignature> getClasses() {
			return classes;
		}

		@Override
		public PackageSignature getPackageSignature() {
			return packageSignature;
		}

		@Override
		public ModuleSignature getModuleSignature() {
			return moduleSignature;
		}

		public void setModuleSignature(Tree moduletree, ModuleSignatureImpl moduleSignature) {
			treeSignatures.put(moduletree, moduleSignature);
			this.moduleSignature = moduleSignature;
		}

		@Override
		public String toString() {
			return "ParserContext [" + (unit != null ? "unit=" + unit.getSourceFile() + ", " : "") + "]";
		}

		@Override
		public String getPackageName() {
			return packageName;
		}

	}

	private Trees trees;

	protected final TypeSignatureResolver typeResolver;

	private boolean internalQueryAPIEnabled = true;
	private boolean elementQueryAPIEnabled = true;
	protected final ParserCache cache;
	protected final String sourceVersion;

	public CompilationUnitSignatureParser8(Trees trees, String sourceVersion, ParserCache cache) {
		this.trees = trees;
		this.sourceVersion = sourceVersion;
		this.cache = cache;
		this.typeResolver = new TypeSignatureResolver(this, cache);
	}

	public Trees getTrees() {
		return trees;
	}

	public void setInternalQueryAPIEnabled(boolean internalQueryAPIEnabled) {
		this.internalQueryAPIEnabled = internalQueryAPIEnabled;
	}

	public void setElementQueryAPIEnabled(boolean elementQueryAPIEnabled) {
		this.elementQueryAPIEnabled = elementQueryAPIEnabled;
	}

	private static boolean isPackageInfo(CompilationUnitTree unit) {
		JavaFileObject src = unit.getSourceFile();
		String srcname = src.getName();
		if (!srcname.endsWith(PACKAGE_INFO_JAVA)) {
			return false;
		}
		if (srcname.length() == PACKAGE_INFO_JAVA_LENGTH) {
			return true;
		}
		char c = srcname.charAt(srcname.length() - PACKAGE_INFO_JAVA_LENGTH - 1);
		return c == '/' || c == '\\';
	}

	public static boolean getPackageName(CompilationUnitTree unit, StringBuilder sb) {
		ExpressionTree treepackagename = unit.getPackageName();
		if (treepackagename != null) {
			DottedNameCollector.collectDottedName(treepackagename, sb);
			return true;
		}
		return false;
	}

	private void parse(CompilationUnitTree unit, ParseContext context) {
		context.set(unit);
		String packname = context.getPackageName();
		if (packname != null) {
			if (isPackageInfo(unit)) {
				SignaturePath sigpath = context.pushSignaturePath();

				List<? extends AnnotationTree> annots = unit.getPackageAnnotations();
				List<AnnotationSignature> packageannots = getAnnotations(annots, context);
				PackageSignature pack = PackageSignatureImpl.create(packageannots, cache.string(packname),
						getPackageDocComment(unit));

				context.packageSignature = pack;
				Tree packtree = getPackageTreeForSignature(unit);
				context.treeSignatures.put(packtree, pack);

				sigpath.setSignature(pack);
				context.popSignaturePath(packtree);
			}
		}

		unit.accept(this, context);
	}

	protected String getPackageDocComment(CompilationUnitTree unit) {
		return getDocComment(unit, unit);
	}

	protected Tree getPackageTreeForSignature(CompilationUnitTree unit) {
		return unit.getPackageName();
	}

	public ParseContext parse(CompilationUnitTree unit) {
		ParseContext context = new ParseContext(cache);
		parse(unit, context);
		return context;
	}

	private static final Map<Tree.Kind, ElementKind> TREEKIND_TO_ELEMENTKIND_MAP = new EnumMap<>(Tree.Kind.class);

	static {
		TREEKIND_TO_ELEMENTKIND_MAP.put(Kind.ANNOTATION_TYPE, ElementKind.ANNOTATION_TYPE);
		TREEKIND_TO_ELEMENTKIND_MAP.put(Kind.CLASS, ElementKind.CLASS);
		TREEKIND_TO_ELEMENTKIND_MAP.put(Kind.ENUM, ElementKind.ENUM);
		TREEKIND_TO_ELEMENTKIND_MAP.put(Kind.INTERFACE, ElementKind.INTERFACE);
		if (ElementKindCompatUtils.ELEMENTKIND_RECORD != null) {
			TREEKIND_TO_ELEMENTKIND_MAP.put(Kind.valueOf("RECORD"), ElementKindCompatUtils.ELEMENTKIND_RECORD);
		}
	}

	private static ElementKind treeKindToElementKind(Kind kind) {
		return TREEKIND_TO_ELEMENTKIND_MAP.get(kind);
	}

	//XXX there may be an edge case in constant detection
	//   if a type with name String is defined in the same package, or is imported from elsewhere,
	//   then the following isConstantType methods may erroneously detect its constant being
	//   or if there is a type parameter with String as its name
	//   or an inner class with String name is declared
	//   or java.lang.String resolves to a different type (through a type with the name java)

	private static boolean isConstantType(Tree type) {
		if (type.getKind() == Kind.PRIMITIVE_TYPE) {
			return true;
		}
		String typestr = DottedNameCollector.collectDottedName(type);
		return "String".equals(typestr) || "java.lang.String".equals(typestr);
	}

	private static boolean isConstantType(Tree type, ParseContext context) {
		if (type.getKind() == Kind.PRIMITIVE_TYPE) {
			return true;
		}
		String typestr = context.collectDottedName(type);
		return "String".equals(typestr) || "java.lang.String".equals(typestr);
	}

	private static boolean shouldIncludeConstantValue(VariableTree tree, byte declaringkindindex,
			ParseContext context) {
		if (tree.getInitializer() == null || !isConstantType(tree.getType(), context)) {
			return false;
		}

		Set<Modifier> modifiers = tree.getModifiers().getFlags();
		// interface or annotation: no static final is required at declaration time
		// class or enum: static final is required to be ABI constant
		if (declaringkindindex == ElementKindCompatUtils.ELEMENTKIND_INDEX_INTERFACE //
				|| declaringkindindex == ElementKindCompatUtils.ELEMENTKIND_INDEX_ANNOTATION_TYPE) {
			return true;
		}
		//Don't check the presence of STATIC modifier, as the constants are present even for instance variables
		return (declaringkindindex == ElementKindCompatUtils.ELEMENTKIND_INDEX_CLASS
				|| declaringkindindex == ElementKindCompatUtils.ELEMENTKIND_INDEX_ENUM)
				&& modifiers.contains(Modifier.FINAL);
	}

	public static boolean isConstantVariable(VariableTree tree, Kind declaringkind) {
		if (tree.getInitializer() == null || !isConstantType(tree.getType())) {
			return false;
		}

		Set<Modifier> modifiers = tree.getModifiers().getFlags();
		// interface or annotation: no static final is required at declaration time
		// class or enum: static final is required to be ABI constant
		return declaringkind == Kind.INTERFACE //
				|| declaringkind == Kind.ANNOTATION_TYPE//
				|| ((declaringkind == Kind.CLASS || declaringkind == Kind.ENUM) && modifiers.contains(Modifier.FINAL)
						&& modifiers.contains(Modifier.STATIC));
	}

	private static boolean isRecordComponent(Set<Modifier> varmodifiers, byte enclosingelementkindindex) {
		if (enclosingelementkindindex != ElementKindCompatUtils.ELEMENTKIND_INDEX_RECORD) {
			return false;
		}
		if (varmodifiers.contains(Modifier.STATIC)) {
			return false;
		}
		return true;
	}

	@Override
	public Signature visitVariable(VariableTree tree, ParseContext context) {
		SignaturePath sigpath = context.pushSignaturePath();

		ModifiersTree variablemodifiers = tree.getModifiers();
		ImmutableModifierSet varmodifierflagss = ImmutableModifierSet.get(variablemodifiers.getFlags());
		switch (context.getDeclaringKindIndex()) {
			case ElementKindCompatUtils.ELEMENTKIND_INDEX_ANNOTATION_TYPE:
			case ElementKindCompatUtils.ELEMENTKIND_INDEX_INTERFACE: {
				varmodifierflagss = varmodifierflagss.added(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL);
				break;
			}
			default: {
				break;
			}
		}
		String doccomment = getDocComment(context.unit, tree);
		List<AnnotationSignature> annotations = getAnnotations(variablemodifiers, context);
		String varname = cache.string(tree.getName());

		Tree type = tree.getType();
		TypeSignature vartypesig = typeResolver.resolveWithAnnotations(type, annotations, context);

		ExpressionTree initer = tree.getInitializer();
		byte declaringkindindex = context.getDeclaringKindIndex();

		FieldSignature sig;
		//detect if the variable is an enum constant
		if (isEnumConstant(tree, varmodifierflagss, declaringkindindex, context.unit)) {
			sig = FieldSignatureImpl.createEnumSignature(vartypesig, varname, doccomment);
		} else if (isRecordComponent(varmodifierflagss, declaringkindindex)) {
			sig = FieldSignatureImpl.createRecordComponent(varmodifierflagss, vartypesig, varname, doccomment);
		} else {
			ConstantValueResolver constantvalue;
			if (shouldIncludeConstantValue(tree, declaringkindindex, context)) {
				constantvalue = getConstantValueForType(initer, context, type);
			} else {
				constantvalue = null;
			}
			sig = FieldSignatureImpl.createField(varmodifierflagss, vartypesig, varname, constantvalue, doccomment);
		}

		context.treeSignatures.put(tree, sig);

		sigpath.setSignature(sig);
		context.popSignaturePath(tree);

		return sig;
	}

	private boolean isVariableEnumConstantInEnum(VariableTree tree, ParseContext context) {
		return isEnumConstant(tree, tree.getModifiers().getFlags(), ElementKindCompatUtils.ELEMENTKIND_INDEX_ENUM,
				context.unit);
	}

	private boolean isEnumConstant(VariableTree tree, Set<Modifier> varmodifierflags, byte declaringkindindex,
			CompilationUnitTree unit) {
		if (declaringkindindex != ElementKindCompatUtils.ELEMENTKIND_INDEX_ENUM
				|| !varmodifierflags.contains(Modifier.STATIC) || !varmodifierflags.contains(Modifier.FINAL)
				|| !varmodifierflags.contains(Modifier.PUBLIC)) {
			return false;
		}

		if (internalQueryAPIEnabled) {
			try {
				return isEnumConstantInternal(tree);
			} catch (LinkageError | AssertionError e) {
				throw new JavacPrivateAPIError(e);
			}
		}
		if (elementQueryAPIEnabled) {
			return isEnumConstantPublic(tree, unit);
		}
		return false;
	}

	private boolean isEnumConstantPublic(VariableTree tree, CompilationUnitTree unit) {
		TreePath path = trees.getPath(unit, tree);
		Element element = trees.getElement(path);
		if (element != null && element.getKind() == ElementKind.ENUM_CONSTANT) {
			//no other way to detect this
			return true;
		}
		return false;
	}

	private static boolean isEnumConstantInternal(VariableTree tree) {
		JCVariableDecl vardecl = (JCVariableDecl) tree;
		if (((vardecl.mods.flags & Flags.ENUM) == Flags.ENUM)) {
			return true;
		}
		return false;
	}

	private boolean isVarargsMethod(MethodTree tree, VariableTree lastparam, CompilationUnitTree unit) {
		if (lastparam == null || lastparam.getType().getKind() != Kind.ARRAY_TYPE) {
			//had at least one parameter and it is an array type
			//can be vararg
			return false;
		}
		if (internalQueryAPIEnabled) {
			try {
				return isVarargsMethodInternal(lastparam);
			} catch (LinkageError | AssertionError e) {
				throw new JavacPrivateAPIError(e);
			}
		}
		if (elementQueryAPIEnabled) {
			return isVarargsMethodPublic(tree, unit);
		}
		return false;
	}

	private boolean isVarargsMethodPublic(MethodTree tree, CompilationUnitTree unit) {
		TreePath path = trees.getPath(unit, tree);
		Element element = trees.getElement(path);
		if (element instanceof ExecutableElement) {
			if (((ExecutableElement) element).isVarArgs()) {
				return true;
			}
		}
		return false;
	}

	private static boolean isVarargsMethodInternal(VariableTree lastparam) {
		JCVariableDecl paramdecl = (JCVariableDecl) lastparam;
		if (((paramdecl.mods.flags & Flags.VARARGS) == Flags.VARARGS)) {
			return true;
		}
		return false;
	}

	private AnnotationSignature.ArrayValue getAnnotationArray(NewArrayTree nat, ParseContext context) {
		//annotations of the NewArrayTree are irrelevant, as they are not accessible from the Model API
		List<? extends ExpressionTree> initializers = nat.getInitializers();
		List<Value> valuelist = JavaTaskUtils.cloneImmutableList(initializers, new Function<ExpressionTree, Value>() {
			private int index = 0;

			@Override
			public Value apply(ExpressionTree initer) {
				return createAnnotationValue(initer, context, index++);
			}
		});
		return ArrayValueImpl.create(valuelist);
	}

	private static ConstantValueResolver castConstantValue(ConstantValueResolver val, Tree typetree) {
		if (typetree.getKind() == Kind.PRIMITIVE_TYPE) {
			Class<?> pclass = IncrementalElementsTypes
					.primitiveTypeKindToClass(((PrimitiveTypeTree) typetree).getPrimitiveTypeKind());
			if (pclass != null) {
				return new CastConstantOperator(val, pclass);
			}
		} else {
			String typestring = DottedNameCollector.collectDottedName(typetree);
			if ("String".equals(typestring) || "java.lang.String".equals(typestring)) {
				return new CastConstantOperator(val, String.class);
			}
		}
		return null;
	}

	private ConstantValueResolver getConstantValueForType(Tree expression, ParseContext context, Tree typetree) {
		ConstantValueResolver val = getConstantValue(expression, context);
		if (val == null) {
			return null;
		}
		if (typetree.getKind() == Kind.PRIMITIVE_TYPE) {
			Class<?> pclass = IncrementalElementsTypes
					.primitiveTypeKindToClass(((PrimitiveTypeTree) typetree).getPrimitiveTypeKind());
			if (pclass != null) {
				return new CastConstantOperator(val, pclass);
			}
		}
		return val;
	}

	public ConstantValueResolver getConstantValue(Tree expression, ParseContext context) {
		Kind expkind = expression.getKind();
		//XXX visitorize
		switch (expkind) {
			case TYPE_CAST: {
				TypeCastTree tct = (TypeCastTree) expression;
				ConstantValueResolver val = getConstantValue(tct.getExpression(), context);
				ConstantValueResolver casted = castConstantValue(val, tct.getType());
				if (casted != null) {
					return casted;
				}
				break;
			}
			case PARENTHESIZED: {
				ParenthesizedTree pt = (ParenthesizedTree) expression;
				return getConstantValue(pt.getExpression(), context);
			}
			case CONDITIONAL_EXPRESSION: // x ? y : z
			{
				ConditionalExpressionTree cet = (ConditionalExpressionTree) expression;
				ConstantValueResolver cond = getConstantValue(cet.getCondition(), context);
				ConstantValueResolver ontrue = getConstantValue(cet.getTrueExpression(), context);
				ConstantValueResolver onfalse = getConstantValue(cet.getFalseExpression(), context);
				return new TernaryConstantOperator(cond, ontrue, onfalse);
			}
			case UNARY_MINUS: // +x
			case UNARY_PLUS: // -x
			case BITWISE_COMPLEMENT: //~x
			case LOGICAL_COMPLEMENT: // !x
			{
				UnaryTree ut = (UnaryTree) expression;
				ConstantValueResolver val = getConstantValue(ut.getExpression(), context);
				return new UnaryConstantOperator(val, expkind);
			}
			case PLUS: // x + y
			case MINUS: // x - y
			case OR: // x | y
			case DIVIDE: // x / y
			case XOR: // x ^ y
			case AND: // x & y
			case CONDITIONAL_AND: // x && y
			case CONDITIONAL_OR: // x || y
			case EQUAL_TO: // x == y
			case GREATER_THAN: // x > y
			case GREATER_THAN_EQUAL: // x >= y
			case LEFT_SHIFT: // x << y 
			case LESS_THAN: // x < y
			case LESS_THAN_EQUAL: // x <= y
			case MULTIPLY: // x * y
			case NOT_EQUAL_TO: // x != y
			case REMAINDER: // x % y
			case RIGHT_SHIFT: // x >> y
			case UNSIGNED_RIGHT_SHIFT: // x >>> y
			{
				BinaryTree bt = (BinaryTree) expression;
				ConstantValueResolver l = getConstantValue(bt.getLeftOperand(), context);
				ConstantValueResolver r = getConstantValue(bt.getRightOperand(), context);
				return new BinaryConstantOperator(l, r, expkind);
			}
			case CHAR_LITERAL:
			case DOUBLE_LITERAL:
			case BOOLEAN_LITERAL:
			case FLOAT_LITERAL:
			case INT_LITERAL:
			case LONG_LITERAL:
			case STRING_LITERAL: {
				LiteralTree lt = (LiteralTree) expression;
				Object lit = lt.getValue();
				if (lit instanceof String) {
					lit = cache.string((String) lit);
				}
				return cache.literalConstantResolver(lit);
			}
			case NULL_LITERAL: {
				return LiteralConstantResolver.NULL_RESOLVER;
			}
			case MEMBER_SELECT: {
				//this is a reference to a constant field of a type
				MemberSelectTree mst = (MemberSelectTree) expression;
				TypeSignature type = typeResolver.resolve(mst.getExpression(), context);
				if (type == null) {
					//this can happen if the constant is like 
					//    public static final int CONSTANT = somefunction().STATIC_FIELD_CONSTANT;
					//so the member on which we select, is a method invocation
					//we can't determine the type for that
					//anyway, its not really relevant, as javac doesn't inline the STATIC_FIELD_CONSTANT
					//even if it is a static final constant value
					//so CONSTANT won't be an actual constant field, therefore we don't need (and cant get) a proper constant resolver
					break;
				}
				return new VariableConstantMemberResolver(type, cache.string(mst.getIdentifier()));
			}
			case IDENTIFIER: {
				IdentifierTree it = (IdentifierTree) expression;
				String identifier = cache.string(it.getName());
				return new IdentifierConstantResolver(identifier);
			}
			default: {
				break;
			}
		}
		return new NotConstantResolverImpl(expression);
	}

	private AnnotationSignature.Value createAnnotationValueImpl(Tree expression, ParseContext context) {
		Kind expkind = expression.getKind();
		//XXX visitorize
		switch (expkind) {
			case ANNOTATION: {
				AnnotationTree annot = (AnnotationTree) expression;

				return new AnnotValueImpl(createAnnotationSignature(annot, context));
			}
			case NEW_ARRAY: {
				return getAnnotationArray((NewArrayTree) expression, context);
			}
			case PARENTHESIZED: {
				return createAnnotationValueImpl(((ParenthesizedTree) expression).getExpression(), context);
			}
			case CHAR_LITERAL:
			case DOUBLE_LITERAL:
			case BOOLEAN_LITERAL:
			case FLOAT_LITERAL:
			case INT_LITERAL:
			case LONG_LITERAL:
			case STRING_LITERAL:

			case TYPE_CAST:

			case CONDITIONAL_EXPRESSION: // x ? y : z

			case UNARY_MINUS: // +x
			case UNARY_PLUS: // -x
			case BITWISE_COMPLEMENT: //~x
			case LOGICAL_COMPLEMENT: // !x

			case PLUS: // x + y
			case MINUS: // x - y
			case OR: // x | y
			case XOR: // x ^ y
			case AND: // x & y

			case CONDITIONAL_AND: // x && y
			case CONDITIONAL_OR: // x || y

			case DIVIDE: // x / y
			case EQUAL_TO: // x == y
			case GREATER_THAN: // x > y
			case GREATER_THAN_EQUAL: // x >= y
			case LEFT_SHIFT: // x << y 
			case LESS_THAN: // x < y
			case LESS_THAN_EQUAL: // x <= y
			case MULTIPLY: // x * y
			case NOT_EQUAL_TO: // x != y
			case REMAINDER: // x % y
			case RIGHT_SHIFT: // x >> y
			case UNSIGNED_RIGHT_SHIFT: // x >>> y
			{
				ConstantValueResolver cexpr = getConstantValue(expression, context);
				if (cexpr != null) {
					return new LiteralValueImpl(cexpr);
				}
				return new UnknownValueImpl(expression.toString());
			}
			case MEMBER_SELECT: {
				MemberSelectTree membersel = (MemberSelectTree) expression;
				if (membersel.getIdentifier().contentEquals("class")) {
					//selecting a class
					TypeSignature typesig = typeResolver.resolve(membersel.getExpression(), context);
					return new TypeValueImpl(typesig);
				}
				//might be a value or enum constant
				String varname = cache.string(membersel.getIdentifier());
				TypeSignature type = typeResolver.resolve(membersel.getExpression(), context);
				return new ReferenceValueImpl(new VariableConstantMemberResolver(type, varname));
			}
			case IDENTIFIER: {
				//might be a value or enum constant
				IdentifierTree it = (IdentifierTree) expression;
				String identifier = cache.string(it.getName());
				return new ReferenceValueImpl(new IdentifierConstantResolver(identifier));
			}
			default: {
				return new UnknownValueImpl(expression.toString());
			}
		}
	}

	private AnnotationSignature.Value createAnnotationValue(Tree expression, ParseContext context, Object indexval) {
		SignaturePath sigpath = context.pushIndexedSignaturePath(indexval);

		Value result = createAnnotationValueImpl(expression, context);
		context.treeSignatures.put(expression, result);

		sigpath.setSignature(result);
		context.popSignaturePath(expression);
		return result;
	}

	public List<AnnotationSignature> getAnnotations(List<? extends AnnotationTree> annotations, ParseContext context) {
		return getIndexedAnnotationSignatures(annotations, context);
	}

	private List<AnnotationSignature> getAnnotations(AnnotatedTypeTree annotatedtree, ParseContext context) {
		return getIndexedAnnotationSignatures(annotatedtree.getAnnotations(), context);
	}

	private List<AnnotationSignature> getAnnotations(TypeParameterTree annotatedtree, ParseContext context) {
		return getIndexedAnnotationSignatures(annotatedtree.getAnnotations(), context);
	}

	private List<AnnotationSignature> getAnnotations(ModifiersTree modtree, ParseContext context) {
		return getIndexedAnnotationSignatures(modtree.getAnnotations(), context);
	}

	private List<AnnotationSignature> getAnnotationSignatures(List<? extends AnnotationTree> annotations,
			ParseContext context) {
		return JavaTaskUtils.cloneImmutableList(annotations, annot -> createAnnotationSignature(annot, context));
	}

	private List<AnnotationSignature> getIndexedAnnotationSignatures(List<? extends AnnotationTree> annotations,
			ParseContext context) {
		return JavaTaskUtils.cloneImmutableList(annotations, new Function<AnnotationTree, AnnotationSignature>() {
			private int index = 0;

			@Override
			public AnnotationSignature apply(AnnotationTree annot) {
				return createAnnotationSignature(annot, context, index++);
			}
		});
	}

	private AnnotationSignature createAnnotationSignature(AnnotationTree annot, ParseContext context) {
		return createAnnotationSignature(annot, context, null);
	}

	private AnnotationSignature createAnnotationSignature(AnnotationTree annot, ParseContext context, Object index) {
		SignaturePath sigpath = context.pushIndexedSignaturePath(index);

		TypeSignature typesig = typeResolver.resolve(annot.getAnnotationType(), context);
		Map<String, Value> annotvalues = getAnnotationValues(annot, context);
		AnnotationSignature result = context.cache.createAnnotationSignature(typesig, annotvalues);
		context.treeSignatures.put(annot, result);

		sigpath.setSignature(result);
		context.popSignaturePath(annot);
		return result;
	}

	private Map<String, Value> getAnnotationValues(AnnotationTree annot, ParseContext context) {
		List<? extends ExpressionTree> args = annot.getArguments();
		if (ObjectUtils.isNullOrEmpty(args)) {
			return Collections.emptyMap();
		}
		LinkedHashMap<String, Value> result = new LinkedHashMap<>();
		for (ExpressionTree arg : args) {
			ExpressionTree valuetree;
			String name;
			if (arg instanceof AssignmentTree) {
				AssignmentTree at = (AssignmentTree) arg;
				valuetree = at.getExpression();
				// there are no dots in the name, however we use the dotted name collector to convert
				// the variable name to a string rather than using .toString() on the tree
				String varname = context.collectDottedName(at.getVariable());
				name = cache.string(varname);
			} else {
				valuetree = arg;
				name = "value";
			}
			result.put(name, createAnnotationValue(valuetree, context, name));
		}
		return result;
	}

	@Override
	public Signature visitMethod(MethodTree tree, ParseContext context) {
		SignaturePath sigpath = context.pushSignaturePath();

		ModifiersTree methodmodifierstree = tree.getModifiers();
		Tree returntypetree = tree.getReturnType();
		Tree defvaltree = tree.getDefaultValue();
		List<? extends TypeParameterTree> typeparametertrees = tree.getTypeParameters();
		List<? extends VariableTree> parametertrees = tree.getParameters();
		List<? extends ExpressionTree> throwstrees = tree.getThrows();
		VariableTree receivertree = tree.getReceiverParameter();
		String doccomment = getDocComment(context.unit, tree);

		List<AnnotationSignature> methodannotations = getAnnotations(methodmodifierstree, context);

		String name = cache.string(tree.getName());
		ElementKind methodkind;
		if (name.equals(IncrementalElementsTypes.CONSTRUCTOR_METHOD_NAME)) {
			methodkind = ElementKind.CONSTRUCTOR;
		} else {
			methodkind = ElementKind.METHOD;
		}
		byte declaringkindindex = context.getDeclaringKindIndex();
		Set<Modifier> modifiers = getCorrectedMethodModifiers(methodmodifierstree, declaringkindindex);
		TypeSignature returntypesignature;
		if (returntypetree != null) {
			returntypesignature = typeResolver.resolveWithAnnotations(returntypetree, methodannotations, context);
		} else {
			returntypesignature = NoTypeSignatureImpl.create(methodannotations, TypeKind.VOID);
		}

		TypeSignature receivertypesignature;
		if (receivertree != null) {
			receivertypesignature = typeResolver.resolveWithAnnotations(receivertree.getType(),
					getAnnotations(receivertree.getModifiers(), context), context);
		} else {
			receivertypesignature = null;
		}

		List<TypeSignature> throwntypes = JavaTaskUtils.cloneImmutableList(throwstrees,
				th -> typeResolver.resolve(th, context));

		AnnotationSignature.Value defval = defvaltree == null ? null : createAnnotationValue(defvaltree, context, null);

		List<TypeParameterSignature> typeparams = JavaTaskUtils.cloneImmutableList(typeparametertrees,
				tp -> createTypeParameterTypeSignature(tp, context));

		final List<MethodParameterSignature> methodparams;
		VariableTree lastparam = null;
		if (!parametertrees.isEmpty()) {
			int idx = 0;
			MethodParameterSignature[] methodparamsigs = new MethodParameterSignature[parametertrees.size()];
			for (Iterator<? extends VariableTree> it = parametertrees.iterator(); it.hasNext();) {
				lastparam = it.next();

				SignaturePath paramsigpath = context.pushSignaturePath();

				ModifiersTree parammodifiers = lastparam.getModifiers();
				TypeSignature type = typeResolver.resolveWithAnnotations(lastparam.getType(),
						getAnnotations(parammodifiers, context), context);
				MethodParameterSignature mparam = MethodParameterSignatureImpl.create(parammodifiers.getFlags(), type,
						cache.string(lastparam.getName()));
				context.treeSignatures.put(lastparam, mparam);

				paramsigpath.setSignature(SignaturePath.getMethodParameterIndexSignature(idx));
				context.popSignaturePath(lastparam);

				methodparamsigs[idx++] = mparam;
			}
			methodparams = ImmutableUtils.asUnmodifiableArrayList(methodparamsigs);
		} else {
			methodparams = Collections.emptyList();
		}
		boolean varargs = isVarargsMethod(tree, lastparam, context.unit);

		MethodSignature sig = FullMethodSignature.create(name, modifiers, methodparams, throwntypes,
				returntypesignature, defval, methodkind, typeparams, receivertypesignature, varargs, doccomment);
		context.treeSignatures.put(tree, sig);

		sigpath.setSignature(sig);
		context.popSignaturePath(tree);

		return sig;
	}

	private TypeParameterSignature createTypeParameterTypeSignature(TypeParameterTree tree, ParseContext context) {
		SignaturePath sigpath = context.pushSignaturePath();

		List<AnnotationSignature> typeparamannotations = getAnnotations(tree, context);

		List<? extends Tree> bounds = tree.getBounds();
		//bounds is in the equals clause
		String varname = cache.string(tree.getName());
		int boundssize = bounds.size();
		TypeParameterSignature result;
		if (boundssize == 0) {
			result = TypeParameterSignatureImpl.create(typeparamannotations, varname, null, null);
		} else if (boundssize == 1) {
			//only single bounds
			TypeSignature firstbound = typeResolver.resolve(bounds.get(0), context);
			result = TypeParameterSignatureImpl.create(typeparamannotations, varname, null, firstbound);
		} else {
			List<TypeSignature> itsbounds = JavaTaskUtils.cloneImmutableList(bounds,
					btm -> typeResolver.resolve(btm, context));
			IntersectionTypeSignature its = IntersectionTypeSignatureImpl.create(itsbounds);
			result = TypeParameterSignatureImpl.create(typeparamannotations, varname, null, its);
		}

		context.treeSignatures.put(tree, result);

		sigpath.setSignature(result);
		context.popSignaturePath(tree);
		return result;
	}

	private Set<Modifier> getCorrectedMethodModifiers(ModifiersTree methodmodifiers, byte declaringkindindex) {
		switch (declaringkindindex) {
			case ElementKindCompatUtils.ELEMENTKIND_INDEX_ANNOTATION_TYPE: {
				ImmutableModifierSet methodmodifierflags = ImmutableModifierSet.get(methodmodifiers.getFlags())
						.added(Modifier.PUBLIC, Modifier.ABSTRACT);
				return methodmodifierflags;
			}
			case ElementKindCompatUtils.ELEMENTKIND_INDEX_INTERFACE: {
				return getInterfaceCorrectMethodModifiers(methodmodifiers);
			}
			default: {
				break;
			}
		}
		return methodmodifiers.getFlags();
	}

	protected Set<Modifier> getInterfaceCorrectMethodModifiers(ModifiersTree methodmodifiers) {
		ImmutableModifierSet methodmodifierflags = ImmutableModifierSet.get(methodmodifiers.getFlags())
				.added(Modifier.PUBLIC);
		if (!methodmodifierflags.contains(Modifier.STATIC)) {
			//non static method must be public abstract if not default
			if (!methodmodifierflags.contains(Modifier.DEFAULT)) {
				methodmodifierflags = methodmodifierflags.added(Modifier.ABSTRACT);
			}
		}
		return methodmodifierflags;
	}

	protected String getDocComment(CompilationUnitTree unit, Tree tree) {
		JCCompilationUnit jcunit = (JCCompilationUnit) unit;
		DocCommentTable comments = jcunit.docComments;
		if (comments != null) {
			JCTree jctree = (JCTree) tree;
			return comments.getCommentText(jctree);
		}
		return null;
	}

	@Override
	public Signature visitClass(ClassTree tree, ParseContext context) {
		SignaturePath sigpath = context.pushSignaturePath();

		ClassSignature enclosingclasssignature = context.getEnclosingClassSignature();
		NestingKind currentnestingkind = context.getNestingKind();
		String packagename = context.getPackageName();

		ModifiersTree modifierstree = tree.getModifiers();
		List<? extends Tree> membertrees = tree.getMembers();
		Tree extendstree = tree.getExtendsClause();
		List<? extends Tree> implementstrees = tree.getImplementsClause();
		List<? extends TypeParameterTree> typeparametertrees = tree.getTypeParameters();
		String doccomment = getDocComment(context.unit, tree);
		String name = cache.string(tree.getSimpleName());
		ElementKind kind = treeKindToElementKind(tree.getKind());
		Set<Modifier> modifiers = getCorrectedClassModifiers(tree, modifierstree, currentnestingkind,
				enclosingclasssignature, kind, context);

		TypeSignature supertypesignature = extendstree == null ? null : typeResolver.resolve(extendstree, context);
		List<AnnotationSignature> annotationsignatures = getAnnotations(modifierstree.getAnnotations(), context);
		final List<ClassMemberSignature> membersignatures = new ArrayList<>();

		List<TypeSignature> superinterfacesignatures = JavaTaskUtils.cloneImmutableList(implementstrees,
				impl -> typeResolver.resolve(impl, context));

		List<TypeParameterSignature> typeparamsignatures = JavaTaskUtils.cloneImmutableList(typeparametertrees,
				param -> createTypeParameterTypeSignature(param, context));

		PermittedSubclassesList permittedsubclasses = getPermittedSubclasses(tree, context);

		ClassSignature classsignature = ClassSignatureImpl.create(modifiers, packagename, name, membersignatures,
				enclosingclasssignature, supertypesignature, superinterfacesignatures, kind, currentnestingkind,
				typeparamsignatures, annotationsignatures, doccomment, permittedsubclasses);
		context.treeSignatures.put(tree, classsignature);
		context.pushClassSignature(classsignature);
		context.setNestingKind(NestingKind.MEMBER);

		if (!membertrees.isEmpty()) {
			for (Tree member : membertrees) {
				ClassMemberSignature membersig = (ClassMemberSignature) member.accept(this, context);
				if (membersig != null) {
					membersignatures.add(membersig);
				}
			}
		}
		ClassSignatureImpl.addImplicitMembers(membersignatures, classsignature);

		context.setNestingKind(currentnestingkind);
		context.popClassSignature();

		sigpath.setSignature(SignaturePath.getClassSignature(classsignature));
		context.popSignaturePath(tree);

		return classsignature;
	}

	protected PermittedSubclassesList getPermittedSubclasses(ClassTree tree, ParseContext context) {
		return null;
	}

	private boolean hasAnonymousEnumConstant(ClassTree tree, ParseContext context) {
		for (Tree mt : tree.getMembers()) {
			if (mt.getKind() != Kind.VARIABLE) {
				continue;
			}
			VariableTree vt = (VariableTree) mt;
			if (!isVariableEnumConstantInEnum(vt, context)) {
				continue;
			}
			ExpressionTree initer = vt.getInitializer();
			if (initer == null) {
				continue;
			}
			if (initer.getKind() != Kind.NEW_CLASS) {
				continue;
			}
			NewClassTree nct = (NewClassTree) initer;
			if (nct.getClassBody() != null) {
				return true;
			}
		}
		return false;
	}

	private Set<Modifier> getCorrectedClassModifiers(ClassTree tree, ModifiersTree classmodifiers,
			NestingKind currentnestingkind, ClassSignature enclosingclasssignature, ElementKind kind,
			ParseContext context) {
		//if the outer class is an interface or @interface, the inner class is automatically marked as static
		//enums cannot have any inner classes
		//if the outer class is a class, no additional modifiers are added to anything

		switch (tree.getKind()) {
			case ENUM: {
				ImmutableModifierSet classmodifierflags = ImmutableModifierSet.get(classmodifiers.getFlags());
				if (currentnestingkind == NestingKind.MEMBER) {
					classmodifierflags = classmodifierflags.added(Modifier.STATIC);
				}
				if (enclosingclasssignature != null && enclosingclasssignature.getKind().isInterface()) {
					//the inner types are also public on JDK9+
					classmodifierflags = classmodifierflags.added(Modifier.PUBLIC);
				}
				//if the enum contains no anonymous inner class members, add final to it
				if (!hasAnonymousEnumConstant(tree, context)) {
					classmodifierflags = classmodifierflags.added(Modifier.FINAL);
				}
				return classmodifierflags;
			}
			case INTERFACE:
			case ANNOTATION_TYPE: {
				ImmutableModifierSet classmodifierflags = ImmutableModifierSet.get(classmodifiers.getFlags());
				if (currentnestingkind == NestingKind.MEMBER) {
					classmodifierflags = classmodifierflags.added(Modifier.STATIC);
				}
				classmodifierflags = classmodifierflags.added(Modifier.ABSTRACT);
				if (enclosingclasssignature != null && enclosingclasssignature.getKind().isInterface()) {
					//the inner types are also public on JDK9+
					classmodifierflags = classmodifierflags.added(Modifier.PUBLIC);
				}
				return classmodifierflags;
			}
			default: {
				if (ElementKindCompatUtils.isRecordElementKind(kind)) {
					ImmutableModifierSet res = ImmutableModifierSet.get(classmodifiers.getFlags())
							.added(Modifier.FINAL);
					if (enclosingclasssignature != null) {
						res = res.added(Modifier.STATIC);
					}
					return res;
				}
				return classmodifiers.getFlags();
			}
		}

	}

	@Override
	public Signature visitCompilationUnit(CompilationUnitTree unit, ParseContext context) {
		for (Tree typedecl : unit.getTypeDecls()) {
			typedecl.accept(this, context);
		}
		return null;
	}

	@Override
	public Signature visitModifiers(ModifiersTree tree, ParseContext context) {
		for (AnnotationTree annot : tree.getAnnotations()) {
			annot.accept(this, context);
		}
		return null;
	}

	@Override
	public Signature visitAnnotatedType(AnnotatedTypeTree tree, ParseContext context) {
		tree.getUnderlyingType().accept(this, context);
		return null;
	}

	@Override
	public Signature visitAnnotation(AnnotationTree tree, ParseContext context) {
		return null;
	}

	@Override
	public Signature visitImport(ImportTree tree, ParseContext context) {
		return null;
	}

	@Override
	public Signature visitArrayAccess(ArrayAccessTree tree, ParseContext context) {
		ExpressionTree expression = tree.getExpression();
		ExpressionTree index = tree.getIndex();
		// index must be integral type, no need to add to used types

		expression.accept(this, context);
		index.accept(this, context);
		return null;
	}

	@Override
	public Signature visitArrayType(ArrayTypeTree tree, ParseContext context) {
		return null;
	}

	@Override
	public Signature visitAssert(AssertTree tree, ParseContext context) {
		tree.getCondition().accept(this, context);
		ExpressionTree detail = tree.getDetail();
		if (detail != null) {
			detail.accept(this, context);
		}
		return null;
	}

	@Override
	public Signature visitAssignment(AssignmentTree tree, ParseContext context) {
		ExpressionTree var = tree.getVariable();
		ExpressionTree exp = tree.getExpression();

		var.accept(this, context);
		exp.accept(this, context);
		return null;
	}

	@Override
	public Signature visitBinary(BinaryTree tree, ParseContext context) {
		tree.getLeftOperand().accept(this, context);
		tree.getRightOperand().accept(this, context);
		return null;
	}

	@Override
	public Signature visitBlock(BlockTree tree, ParseContext context) {
//		
//		IMPORTANT:
//			Sometimes incremental compilation crashes deep inside javac (Check:2712 NullPointerException JDK 8 in visitAnnotation, validateAnnotationTree) 
//				when we are visiting the statements of the blocks in the ABI change detection round.
//			We dont have to visit them as they don't contribute to the signature of a compilation unit.
//			Block local classes doesnt matter regarding to the Elements and Types API so they can be ignored.
//			This error surfaced when we tried to INCREMENTALLY compile a source which has local variable annotations: 
//				public Signature function() {
//					@SuppressWarnings("unused")
//					List l;
//				}
//			Compiling this without the annotation worked.
//			Still not sure why did this crash only when compiling incrementally.
//		

//		NestingKind nk = context.getNestingKind();
//		if (nk == NestingKind.MEMBER) {
//			//we probably dont need to add these blocks as they are not really relevant via the annotation processing API
//			if (tree.isStatic()) {
//				// add as methods?
//			} else {
//				// add as methods?
//			}
//		}
//		context.setNestingKind(NestingKind.LOCAL);
//		for (StatementTree stm : tree.getStatements()) {
////			stm.accept(this, context);
//		}
//		context.setNestingKind(nk);
		return null;
	}

	@Override
	public Signature visitBreak(BreakTree tree, ParseContext context) {
		return null;
	}

	@Override
	public Signature visitCase(CaseTree tree, ParseContext context) {
		List<? extends StatementTree> stms = tree.getStatements();
		if (!ObjectUtils.isNullOrEmpty(stms)) {
			for (StatementTree stm : stms) {
				stm.accept(this, context);
			}
		}
		return null;
	}

	@Override
	public Signature visitCatch(CatchTree tree, ParseContext context) {
		tree.getBlock().accept(this, context);
		return null;
	}

	@Override
	public Signature visitCompoundAssignment(CompoundAssignmentTree tree, ParseContext context) {
		ExpressionTree var = tree.getVariable();
		ExpressionTree exp = tree.getExpression();

		var.accept(this, context);
		exp.accept(this, context);

		return null;
	}

	@Override
	public Signature visitConditionalExpression(ConditionalExpressionTree tree, ParseContext context) {
		tree.getCondition().accept(this, context);
		tree.getFalseExpression().accept(this, context);
		tree.getTrueExpression().accept(this, context);
		return null;
	}

	@Override
	public Signature visitContinue(ContinueTree tree, ParseContext context) {
		return null;
	}

	@Override
	public Signature visitDoWhileLoop(DoWhileLoopTree tree, ParseContext context) {
		tree.getStatement().accept(this, context);
		tree.getCondition().accept(this, context);
		return null;
	}

	@Override
	public Signature visitEmptyStatement(EmptyStatementTree tree, ParseContext context) {
		return null;
	}

	@Override
	public Signature visitEnhancedForLoop(EnhancedForLoopTree tree, ParseContext context) {
		ExpressionTree expression = tree.getExpression();

		// stay iterable
		expression.accept(this, context);

		tree.getVariable().accept(this, context);
		tree.getStatement().accept(this, context);
		return null;
	}

	@Override
	public Signature visitErroneous(ErroneousTree tree, ParseContext context) {
		return null;
	}

	@Override
	public Signature visitExpressionStatement(ExpressionStatementTree tree, ParseContext context) {
		tree.getExpression().accept(this, context);
		return null;
	}

	@Override
	public Signature visitForLoop(ForLoopTree tree, ParseContext context) {
		for (StatementTree initertree : tree.getInitializer()) {
			initertree.accept(this, context);
		}
		ExpressionTree condition = tree.getCondition();
		if (condition != null) {
			condition.accept(this, context);
		}
		tree.getStatement().accept(this, context);
		for (ExpressionStatementTree updatetree : tree.getUpdate()) {
			updatetree.accept(this, context);
		}
		return null;
	}

	@Override
	public Signature visitIdentifier(IdentifierTree tree, ParseContext context) {
		return null;
	}

	@Override
	public Signature visitIf(IfTree tree, ParseContext context) {
		tree.getCondition().accept(this, context);
		tree.getThenStatement().accept(this, context);
		StatementTree elsestm = tree.getElseStatement();
		if (elsestm != null) {
			elsestm.accept(this, context);
		}
		return null;
	}

	@Override
	public Signature visitInstanceOf(InstanceOfTree tree, ParseContext context) {
		tree.getExpression().accept(this, context);
		return null;
	}

	@Override
	public Signature visitIntersectionType(IntersectionTypeTree tree, ParseContext context) {
		return null;
	}

	@Override
	public Signature visitLabeledStatement(LabeledStatementTree tree, ParseContext context) {
		tree.getStatement().accept(this, context);
		return null;
	}

	@Override
	public Signature visitLambdaExpression(LambdaExpressionTree tree, ParseContext context) {
		tree.getBody().accept(this, context);
		return null;
	}

	@Override
	public Signature visitLiteral(LiteralTree tree, ParseContext context) {
		return null;
	}

	@Override
	public Signature visitMemberReference(MemberReferenceTree tree, ParseContext context) {
		tree.getQualifierExpression().accept(this, context);
		return null;
	}

	@Override
	public Signature visitMemberSelect(MemberSelectTree tree, ParseContext context) {
		tree.getExpression().accept(this, context);
		return null;
	}

	@Override
	public Signature visitMethodInvocation(MethodInvocationTree tree, ParseContext context) {
		for (ExpressionTree arg : tree.getArguments()) {
			arg.accept(this, context);
		}
		tree.getMethodSelect().accept(this, context);
		return null;
	}

	@Override
	public Signature visitNewArray(NewArrayTree tree, ParseContext context) {
		for (ExpressionTree dims : tree.getDimensions()) {
			dims.accept(this, context);
		}
		List<? extends ExpressionTree> initers = tree.getInitializers();
		if (initers != null) {
			for (ExpressionTree initer : initers) {
				initer.accept(this, context);
			}
		}
		return null;
	}

	@Override
	public Signature visitNewClass(NewClassTree tree, ParseContext context) {
		ClassTree body = tree.getClassBody();
		if (body != null) {
			body.accept(this, context);
		}
		for (ExpressionTree argtree : tree.getArguments()) {
			argtree.accept(this, context);
		}
		return null;
	}

	@Override
	public Signature visitOther(Tree tree, ParseContext context) {
		return null;
	}

	@Override
	public Signature visitParameterizedType(ParameterizedTypeTree tree, ParseContext context) {
		return null;
	}

	@Override
	public Signature visitParenthesized(ParenthesizedTree tree, ParseContext context) {
		tree.getExpression().accept(this, context);
		return null;
	}

	@Override
	public Signature visitPrimitiveType(PrimitiveTypeTree tree, ParseContext context) {
		return null;
	}

	@Override
	public Signature visitReturn(ReturnTree tree, ParseContext context) {
		tree.getExpression().accept(this, context);
		return null;
	}

	@Override
	public Signature visitSwitch(SwitchTree tree, ParseContext context) {
		tree.getExpression().accept(this, context);
		for (CaseTree c : tree.getCases()) {
			c.accept(this, context);
		}
		return null;
	}

	@Override
	public Signature visitSynchronized(SynchronizedTree tree, ParseContext context) {
		tree.getExpression().accept(this, context);
		tree.getBlock().accept(this, context);
		return null;
	}

	@Override
	public Signature visitThrow(ThrowTree tree, ParseContext context) {
		tree.getExpression().accept(this, context);
		return null;
	}

	@Override
	public Signature visitTry(TryTree tree, ParseContext context) {
		tree.getBlock().accept(this, context);
		acceptOn(tree.getFinallyBlock(), context);
		for (Tree t : tree.getResources()) {
			t.accept(this, context);
		}
		for (CatchTree t : tree.getCatches()) {
			t.accept(this, context);
		}
		return null;
	}

	@Override
	public Signature visitTypeCast(TypeCastTree tree, ParseContext context) {
		acceptOn(tree.getExpression(), context);
		return null;
	}

	@Override
	public Signature visitTypeParameter(TypeParameterTree tree, ParseContext context) {
		return null;
	}

	@Override
	public Signature visitUnary(UnaryTree tree, ParseContext context) {
		acceptOn(tree.getExpression(), context);
		return null;
	}

	@Override
	public Signature visitUnionType(UnionTypeTree tree, ParseContext context) {
		return null;
	}

	@Override
	public Signature visitWhileLoop(WhileLoopTree tree, ParseContext context) {
		acceptOn(tree.getCondition(), context);
		acceptOn(tree.getStatement(), context);
		return null;
	}

	@Override
	public Signature visitWildcard(WildcardTree tree, ParseContext context) {
		return null;
	}

	private void acceptOn(Tree tree, ParseContext context) {
		if (tree != null) {
			tree.accept(this, context);
		}
	}
}
