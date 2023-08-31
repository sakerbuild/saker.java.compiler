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
import saker.java.compiler.impl.compile.signature.impl.SimpleAnnotationSignature;
import saker.java.compiler.impl.compile.signature.type.impl.CanonicalTypeSignatureImpl;
import saker.java.compiler.impl.compile.signature.type.impl.SimpleCanonicalTypeSignature;
import saker.java.compiler.impl.compile.signature.type.impl.SimpleExtendsWildcardTypeSignature;
import saker.java.compiler.impl.compile.signature.type.impl.SimpleSuperWildcardTypeSignature;
import saker.java.compiler.impl.compile.signature.type.impl.SimpleUnresolvedTypeSignature;
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
		putCanonicalCache(SimpleCanonicalTypeSignature.INSTANCE_JAVA_LANG_STRING);
		putCanonicalCache(SimpleCanonicalTypeSignature.INSTANCE_JAVA_LANG_OBJECT);
		putCanonicalCache(SimpleCanonicalTypeSignature.INSTANCE_JAVA_LANG_ANNOTATION_ANNOTATION);

		putCanonicalCache(SimpleCanonicalTypeSignature.INSTANCE_JAVA_LANG_OVERRIDE);
		putCanonicalCache(SimpleCanonicalTypeSignature.INSTANCE_JAVA_LANG_DEPRECATED);
		putCanonicalCache(SimpleCanonicalTypeSignature.INSTANCE_JAVA_LANG_FUNCTIONALINTERFACE);
		putCanonicalCache(SimpleCanonicalTypeSignature.INSTANCE_JAVA_LANG_SUPPRESSWARNINGS);

		putUnresolvedCache(SimpleUnresolvedTypeSignature.INSTANCE_OBJECT);
		putUnresolvedCache(SimpleUnresolvedTypeSignature.INSTANCE_OVERRIDE);
		putUnresolvedCache(SimpleUnresolvedTypeSignature.INSTANCE_STRING);
		putUnresolvedCache(SimpleUnresolvedTypeSignature.INSTANCE_BOOLEAN);
		putUnresolvedCache(SimpleUnresolvedTypeSignature.INSTANCE_BYTE);
		putUnresolvedCache(SimpleUnresolvedTypeSignature.INSTANCE_SHORT);
		putUnresolvedCache(SimpleUnresolvedTypeSignature.INSTANCE_INTEGER);
		putUnresolvedCache(SimpleUnresolvedTypeSignature.INSTANCE_LONG);
		putUnresolvedCache(SimpleUnresolvedTypeSignature.INSTANCE_FLOAT);
		putUnresolvedCache(SimpleUnresolvedTypeSignature.INSTANCE_DOUBLE);
		putUnresolvedCache(SimpleUnresolvedTypeSignature.INSTANCE_VOID);
		putUnresolvedCache(SimpleUnresolvedTypeSignature.INSTANCE_CHARACTER);

		putSimpleAnnotationCache(SimpleAnnotationSignature.INSTANCE_OVERRIDE);
		putSimpleAnnotationCache(SimpleAnnotationSignature.INSTANCE_JAVA_LANG_OVERRIDE);

		emptyImportScopes.put("", EmptyImportScope.EMPTY_SCOPE_INSANCE);
	}

	private void putSimpleAnnotationCache(SimpleAnnotationSignature sig) {
		simpleAnnotationSignatures.put(sig.getAnnotationType(), sig);
	}

	private void putCanonicalCache(CanonicalTypeSignature sig) {
		simpleCanonicalTypeSignatures.put(sig.getCanonicalName(), sig);
	}

	private void putUnresolvedCache(UnresolvedTypeSignature sig) {
		unresolvedSignatures.put(sig.getUnresolvedName(), sig);
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
				cn -> SimpleCanonicalTypeSignature.create(string(cn)));
	}

	public LiteralConstantResolver literalConstantResolver(Object literal) {
		if (literal == null) {
			return LiteralConstantResolver.NULL_RESOLVER;
		}
		return literalConstantResolvers.computeIfAbsent(literal, LiteralConstantResolver::create);
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
