package saker.java.compiler.impl.compile.signature.parser;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.compile.handler.incremental.model.scope.EmptyImportScope;
import saker.java.compiler.impl.compile.handler.incremental.model.scope.ImportDeclaration;
import saker.java.compiler.impl.compile.handler.incremental.model.scope.ImportScope;
import saker.java.compiler.impl.compile.handler.incremental.model.scope.SimpleImportDeclaration;
import saker.java.compiler.impl.compile.handler.incremental.model.scope.StaticImportDeclaration;
import saker.java.compiler.impl.compile.signature.annot.val.LiteralValueImpl;
import saker.java.compiler.impl.compile.signature.impl.AnnotationSignatureImpl;
import saker.java.compiler.impl.compile.signature.type.impl.CanonicalTypeSignatureImpl;
import saker.java.compiler.impl.compile.signature.type.impl.SimpleCanonicalTypeSignature;
import saker.java.compiler.impl.compile.signature.type.impl.SimpleExtendsWildcardTypeSignature;
import saker.java.compiler.impl.compile.signature.type.impl.SimpleSuperWildcardTypeSignature;
import saker.java.compiler.impl.compile.signature.type.impl.UnresolvedTypeSignatureImpl;
import saker.java.compiler.impl.compile.signature.value.LiteralConstantResolver;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.element.AnnotationSignature.LiteralValue;
import saker.java.compiler.impl.signature.element.AnnotationSignature.Value;
import saker.java.compiler.impl.signature.type.CanonicalTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.signature.type.UnresolvedTypeSignature;
import saker.java.compiler.impl.signature.type.WildcardTypeSignature;

public class ParserCache {

	private ConcurrentSkipListMap<String, String> strings = new ConcurrentSkipListMap<>();
	private ConcurrentSkipListMap<String, ImportScope> emptyImportScopes = new ConcurrentSkipListMap<>();

	private ConcurrentSkipListMap<String, CanonicalTypeSignature> simpleCanonicalTypeSignatures = new ConcurrentSkipListMap<>();

	private ConcurrentHashMap<Object, LiteralConstantResolver> literalConstantResolvers = new ConcurrentHashMap<>();
	private ConcurrentHashMap<LiteralConstantResolver, LiteralValue> literalAnnotationValues = new ConcurrentHashMap<>();

	private ConcurrentHashMap<TypeSignature, AnnotationSignature> simpleAnnotationSignatures = new ConcurrentHashMap<>();

	private ConcurrentSkipListMap<String, ImportDeclaration> nonStaticImportDeclarations = new ConcurrentSkipListMap<>();
	private ConcurrentSkipListMap<String, ImportDeclaration> staticImportDeclarations = new ConcurrentSkipListMap<>();

	private ConcurrentHashMap<TypeSignature, WildcardTypeSignature> extendsWildcards = new ConcurrentHashMap<>();
	private ConcurrentHashMap<TypeSignature, WildcardTypeSignature> superWildcards = new ConcurrentHashMap<>();
	private ConcurrentSkipListMap<String, UnresolvedTypeSignature> unresolvedSignatures = new ConcurrentSkipListMap<>();

	{
		simpleCanonicalTypeSignatures.put(CanonicalTypeSignatureImpl.INSTANCE_JAVA_LANG_STRING.getCanonicalName(),
				CanonicalTypeSignatureImpl.INSTANCE_JAVA_LANG_STRING);
		simpleCanonicalTypeSignatures.put(CanonicalTypeSignatureImpl.INSTANCE_JAVA_LANG_OBJECT.getCanonicalName(),
				CanonicalTypeSignatureImpl.INSTANCE_JAVA_LANG_OBJECT);
		simpleCanonicalTypeSignatures.put(
				CanonicalTypeSignatureImpl.INSTANCE_JAVA_LANG_ANNOTATION_ANNOTATION.getCanonicalName(),
				CanonicalTypeSignatureImpl.INSTANCE_JAVA_LANG_ANNOTATION_ANNOTATION);

		simpleCanonicalTypeSignatures.put(CanonicalTypeSignatureImpl.INSTANCE_JAVA_LANG_OVERRIDE.getCanonicalName(),
				CanonicalTypeSignatureImpl.INSTANCE_JAVA_LANG_OVERRIDE);
		simpleCanonicalTypeSignatures.put(CanonicalTypeSignatureImpl.INSTANCE_JAVA_LANG_DEPRECATED.getCanonicalName(),
				CanonicalTypeSignatureImpl.INSTANCE_JAVA_LANG_DEPRECATED);
		simpleCanonicalTypeSignatures.put(
				CanonicalTypeSignatureImpl.INSTANCE_JAVA_LANG_FUNCTIONALINTERFACE.getCanonicalName(),
				CanonicalTypeSignatureImpl.INSTANCE_JAVA_LANG_FUNCTIONALINTERFACE);
		simpleCanonicalTypeSignatures.put(
				CanonicalTypeSignatureImpl.INSTANCE_JAVA_LANG_SUPPRESSWARNINGS.getCanonicalName(),
				CanonicalTypeSignatureImpl.INSTANCE_JAVA_LANG_SUPPRESSWARNINGS);

		emptyImportScopes.put("", EmptyImportScope.EMPTY_SCOPE_INSANCE);
	}

	public ParserCache() {
	}

	public String string(CharSequence cs) {
		if (cs == null) {
			return null;
		}
		return stringNonNull(cs.toString());
	}

	public String string(String s) {
		if (s == null) {
			return null;
		}
		return stringNonNull(s);
	}

	private String stringNonNull(String s) {
		String prev = strings.putIfAbsent(s, s);
		if (prev == null) {
			return s;
		}
		return prev;
	}

	public ImportScope emptyImportScope(String packagename) {
		if (packagename == null) {
			return EmptyImportScope.EMPTY_SCOPE_INSANCE;
		}
		return emptyImportScopes.computeIfAbsent(string(packagename), EmptyImportScope::new);
	}

	public CanonicalTypeSignature canonicalTypeSignature(String canonicalname) {
		return simpleCanonicalTypeSignatures.computeIfAbsent(canonicalname,
				cn -> new SimpleCanonicalTypeSignature(string(cn)));
	}

	public LiteralConstantResolver literalConstantResolver(Object literal) {
		if (literal == null) {
			return LiteralConstantResolver.NULL_RESOLVER;
		}
		return literalConstantResolvers.computeIfAbsent(literal, LiteralConstantResolver::new);
	}

	public LiteralValue literalAnnotationValue(Object literal) {
		return literalAnnotationValues.computeIfAbsent(literalConstantResolver(literal), LiteralValueImpl::new);
	}

	public UnresolvedTypeSignature unresolved(String qualifiedname) {
		return unresolvedSignatures.computeIfAbsent(qualifiedname, UnresolvedTypeSignatureImpl::create);
	}

	public AnnotationSignature createAnnotationSignature(TypeSignature annotationType, Map<String, Value> values) {
		if (ObjectUtils.isNullOrEmpty(values)) {
			return simpleAnnotationSignatures.computeIfAbsent(annotationType, AnnotationSignatureImpl::create);
		}
		return AnnotationSignatureImpl.create(annotationType, values);
	}

	public ImportDeclaration importDeclaration(String path, boolean isstatic) {
		if (isstatic) {
			return staticImportDeclarations.computeIfAbsent(path, p -> new StaticImportDeclaration(string(p)));
		}
		return nonStaticImportDeclarations.computeIfAbsent(path, p -> new SimpleImportDeclaration(string(p)));
	}

	public WildcardTypeSignature extendsWildcard(TypeSignature extendsignature) {
		return extendsWildcards.computeIfAbsent(extendsignature, SimpleExtendsWildcardTypeSignature::new);
	}

	public WildcardTypeSignature superWildcard(TypeSignature supersignature) {
		return superWildcards.computeIfAbsent(supersignature, SimpleSuperWildcardTypeSignature::new);
	}
}
