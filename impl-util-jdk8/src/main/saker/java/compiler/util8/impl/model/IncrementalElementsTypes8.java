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
package saker.java.compiler.util8.impl.model;

import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.Parameterizable;
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
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.AbstractAnnotationValueVisitor8;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleTypeVisitor8;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.function.Functionals;
import saker.build.thirdparty.saker.util.function.LazySupplier;
import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.compat.ImmutableElementTypeSet;
import saker.java.compiler.impl.compat.ElementKindCompatUtils;
import saker.java.compiler.impl.compat.element.ModuleElementCompat;
import saker.java.compiler.impl.compat.element.RecordComponentElementCompat;
import saker.java.compiler.impl.compile.handler.incremental.model.CommonDeclaredType;
import saker.java.compiler.impl.compile.handler.incremental.model.CommonElement;
import saker.java.compiler.impl.compile.handler.incremental.model.CommonPackageType;
import saker.java.compiler.impl.compile.handler.incremental.model.CommonTypeElement;
import saker.java.compiler.impl.compile.handler.incremental.model.CommonTypeMirror;
import saker.java.compiler.impl.compile.handler.incremental.model.CommonWildcardType;
import saker.java.compiler.impl.compile.handler.incremental.model.ContainsTypeVisitor;
import saker.java.compiler.impl.compile.handler.incremental.model.DualPackageElement;
import saker.java.compiler.impl.compile.handler.incremental.model.ErasureVisitor;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalName;
import saker.java.compiler.impl.compile.handler.incremental.model.IsAssignableTypeVisitor;
import saker.java.compiler.impl.compile.handler.incremental.model.IsSubTypeVisitor;
import saker.java.compiler.impl.compile.handler.incremental.model.KindBasedElementVisitor;
import saker.java.compiler.impl.compile.handler.incremental.model.NoModulePackagesTypesContainer;
import saker.java.compiler.impl.compile.handler.incremental.model.PackagesTypesContainer;
import saker.java.compiler.impl.compile.handler.incremental.model.elem.CaptureEnclosingElement;
import saker.java.compiler.impl.compile.handler.incremental.model.elem.CapturePackageElement;
import saker.java.compiler.impl.compile.handler.incremental.model.elem.DocumentedElement;
import saker.java.compiler.impl.compile.handler.incremental.model.elem.IncrementalAnnotationMirror;
import saker.java.compiler.impl.compile.handler.incremental.model.elem.IncrementalAnnotationValue;
import saker.java.compiler.impl.compile.handler.incremental.model.elem.IncrementalElement;
import saker.java.compiler.impl.compile.handler.incremental.model.elem.IncrementalTypeElement;
import saker.java.compiler.impl.compile.handler.incremental.model.elem.SignaturedElement;
import saker.java.compiler.impl.compile.handler.incremental.model.forwarded.ForwardingAnnotationMirror;
import saker.java.compiler.impl.compile.handler.incremental.model.forwarded.ForwardingAnnotationValue;
import saker.java.compiler.impl.compile.handler.incremental.model.forwarded.elem.ForwardingElement;
import saker.java.compiler.impl.compile.handler.incremental.model.forwarded.elem.ForwardingElementBase;
import saker.java.compiler.impl.compile.handler.incremental.model.forwarded.elem.ForwardingExecutableElement;
import saker.java.compiler.impl.compile.handler.incremental.model.forwarded.elem.ForwardingPackageElement;
import saker.java.compiler.impl.compile.handler.incremental.model.forwarded.elem.ForwardingTypeElement;
import saker.java.compiler.impl.compile.handler.incremental.model.forwarded.elem.ForwardingTypeParameterElement;
import saker.java.compiler.impl.compile.handler.incremental.model.forwarded.elem.ForwardingUnknownElement;
import saker.java.compiler.impl.compile.handler.incremental.model.forwarded.elem.ForwardingVariableElement;
import saker.java.compiler.impl.compile.handler.incremental.model.forwarded.mirror.ForwardingArrayType;
import saker.java.compiler.impl.compile.handler.incremental.model.forwarded.mirror.ForwardingDeclaredType;
import saker.java.compiler.impl.compile.handler.incremental.model.forwarded.mirror.ForwardingErrorType;
import saker.java.compiler.impl.compile.handler.incremental.model.forwarded.mirror.ForwardingExecutableType;
import saker.java.compiler.impl.compile.handler.incremental.model.forwarded.mirror.ForwardingIntersectionType;
import saker.java.compiler.impl.compile.handler.incremental.model.forwarded.mirror.ForwardingNullType;
import saker.java.compiler.impl.compile.handler.incremental.model.forwarded.mirror.ForwardingPackageType;
import saker.java.compiler.impl.compile.handler.incremental.model.forwarded.mirror.ForwardingPrimitiveType;
import saker.java.compiler.impl.compile.handler.incremental.model.forwarded.mirror.ForwardingTypeMirrorBase;
import saker.java.compiler.impl.compile.handler.incremental.model.forwarded.mirror.ForwardingTypeVariable;
import saker.java.compiler.impl.compile.handler.incremental.model.forwarded.mirror.ForwardingUnionType;
import saker.java.compiler.impl.compile.handler.incremental.model.forwarded.mirror.ForwardingUnknownType;
import saker.java.compiler.impl.compile.handler.incremental.model.forwarded.mirror.ForwardingWildcardType;
import saker.java.compiler.impl.compile.handler.incremental.model.mirror.CapturedDeclaredType;
import saker.java.compiler.impl.compile.handler.incremental.model.mirror.CapturedTypeVariable;
import saker.java.compiler.impl.compile.handler.incremental.model.mirror.IncrementalArrayType;
import saker.java.compiler.impl.compile.handler.incremental.model.mirror.IncrementalDeclaredType;
import saker.java.compiler.impl.compile.handler.incremental.model.mirror.IncrementalErrorType;
import saker.java.compiler.impl.compile.handler.incremental.model.mirror.IncrementalIntersectionType;
import saker.java.compiler.impl.compile.handler.incremental.model.mirror.IncrementalNoType;
import saker.java.compiler.impl.compile.handler.incremental.model.mirror.IncrementalNullType;
import saker.java.compiler.impl.compile.handler.incremental.model.mirror.IncrementalPrimitiveType;
import saker.java.compiler.impl.compile.handler.incremental.model.mirror.IncrementalTypeMirror;
import saker.java.compiler.impl.compile.handler.incremental.model.mirror.IncrementalTypeVariable;
import saker.java.compiler.impl.compile.handler.incremental.model.mirror.IncrementalUnionType;
import saker.java.compiler.impl.compile.handler.incremental.model.mirror.IncrementalWildcardType;
import saker.java.compiler.impl.compile.handler.incremental.model.mirror.SimpleArrayType;
import saker.java.compiler.impl.compile.handler.incremental.model.mirror.SimpleDeclaredType;
import saker.java.compiler.impl.compile.handler.incremental.model.mirror.SimpleExecutableType;
import saker.java.compiler.impl.compile.handler.incremental.model.mirror.SimpleIntersectionType;
import saker.java.compiler.impl.compile.handler.incremental.model.mirror.SimplePrimitiveType;
import saker.java.compiler.impl.compile.handler.incremental.model.mirror.SimpleUnionType;
import saker.java.compiler.impl.compile.handler.incremental.model.mirror.SimpleWildcardType;
import saker.java.compiler.impl.compile.handler.incremental.model.scope.ClassBodyResolutionScope;
import saker.java.compiler.impl.compile.handler.incremental.model.scope.ClassHeaderResolutionScope;
import saker.java.compiler.impl.compile.handler.incremental.model.scope.CompilationUnitResolutionScope;
import saker.java.compiler.impl.compile.handler.incremental.model.scope.EnclosingTypeResolutionScope;
import saker.java.compiler.impl.compile.handler.incremental.model.scope.MethodResolutionScope;
import saker.java.compiler.impl.compile.handler.info.ClassHoldingData;
import saker.java.compiler.impl.compile.handler.info.ClassHoldingFileData;
import saker.java.compiler.impl.compile.handler.info.SignaturePath;
import saker.java.compiler.impl.compile.handler.invoker.CompilationContextInformation;
import saker.java.compiler.impl.compile.signature.annot.val.AnnotValueImpl;
import saker.java.compiler.impl.compile.signature.annot.val.ArrayValueImpl;
import saker.java.compiler.impl.compile.signature.annot.val.TypeValueImpl;
import saker.java.compiler.impl.compile.signature.annot.val.UnknownValueImpl;
import saker.java.compiler.impl.compile.signature.annot.val.VariableValueImpl;
import saker.java.compiler.impl.compile.signature.impl.ClassSignatureImpl;
import saker.java.compiler.impl.compile.signature.impl.ExplicitPermittedSubclassesList;
import saker.java.compiler.impl.compile.signature.impl.FieldSignatureImpl;
import saker.java.compiler.impl.compile.signature.impl.FullMethodSignature;
import saker.java.compiler.impl.compile.signature.impl.MethodParameterSignatureImpl;
import saker.java.compiler.impl.compile.signature.impl.PackageSignatureImpl;
import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.impl.compile.signature.type.impl.ArrayTypeSignatureImpl;
import saker.java.compiler.impl.compile.signature.type.impl.CanonicalTypeSignatureImpl;
import saker.java.compiler.impl.compile.signature.type.impl.IntersectionTypeSignatureImpl;
import saker.java.compiler.impl.compile.signature.type.impl.NoTypeSignatureImpl;
import saker.java.compiler.impl.compile.signature.type.impl.NullTypeSignatureImpl;
import saker.java.compiler.impl.compile.signature.type.impl.PrimitiveTypeSignatureImpl;
import saker.java.compiler.impl.compile.signature.type.impl.SimpleCanonicalTypeSignature;
import saker.java.compiler.impl.compile.signature.type.impl.TypeParameterSignatureImpl;
import saker.java.compiler.impl.compile.signature.type.impl.TypeReferenceSignatureImpl;
import saker.java.compiler.impl.compile.signature.type.impl.TypeVariableTypeSignatureImpl;
import saker.java.compiler.impl.compile.signature.type.impl.UnionTypeSignatureImpl;
import saker.java.compiler.impl.compile.signature.type.impl.UnknownTypeSignatureImpl;
import saker.java.compiler.impl.compile.signature.type.impl.UnresolvedTypeSignatureImpl;
import saker.java.compiler.impl.compile.signature.type.impl.WildcardTypeSignatureImpl;
import saker.java.compiler.impl.signature.Signature;
import saker.java.compiler.impl.signature.element.AnnotatedSignature;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.element.AnnotationSignature.AnnotValue;
import saker.java.compiler.impl.signature.element.AnnotationSignature.ArrayValue;
import saker.java.compiler.impl.signature.element.AnnotationSignature.LiteralValue;
import saker.java.compiler.impl.signature.element.AnnotationSignature.TypeValue;
import saker.java.compiler.impl.signature.element.AnnotationSignature.Value;
import saker.java.compiler.impl.signature.element.AnnotationSignature.VariableValue;
import saker.java.compiler.impl.signature.element.ClassMemberSignature;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.ClassSignature.PermittedSubclassesList;
import saker.java.compiler.impl.signature.element.FieldSignature;
import saker.java.compiler.impl.signature.element.MethodParameterSignature;
import saker.java.compiler.impl.signature.element.MethodSignature;
import saker.java.compiler.impl.signature.element.PackageSignature;
import saker.java.compiler.impl.signature.type.ArrayTypeSignature;
import saker.java.compiler.impl.signature.type.IntersectionTypeSignature;
import saker.java.compiler.impl.signature.type.NoTypeSignature;
import saker.java.compiler.impl.signature.type.NullTypeSignature;
import saker.java.compiler.impl.signature.type.ParameterizedTypeSignature;
import saker.java.compiler.impl.signature.type.PrimitiveTypeSignature;
import saker.java.compiler.impl.signature.type.ResolutionScope;
import saker.java.compiler.impl.signature.type.TypeParameterSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignatureVisitor;
import saker.java.compiler.impl.signature.type.TypeVariableTypeSignature;
import saker.java.compiler.impl.signature.type.UnionTypeSignature;
import saker.java.compiler.impl.signature.type.UnknownTypeSignature;
import saker.java.compiler.impl.signature.type.UnresolvedTypeSignature;
import saker.java.compiler.impl.signature.type.WildcardTypeSignature;
import saker.java.compiler.impl.util.ImmutableModifierSet;
import saker.java.compiler.impl.util.operators.CastOperators;
import saker.java.compiler.jdk.impl.JavaCompilationUtils;
import saker.java.compiler.jdk.impl.compat.element.DefaultedElementVisitor;
import saker.java.compiler.jdk.impl.compat.type.DefaultedTypeVisitor;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;
import saker.java.compiler.jdk.impl.incremental.model.JavaModelUtils;
import testing.saker.java.compiler.TestFlag;

public class IncrementalElementsTypes8 implements IncrementalElementsTypesBase {
	private static final String JAVA_LANG_OBJECT = "java.lang.Object";
	private static final String JAVA_LANG_ENUM = "java.lang.Enum";
	private static final String JAVA_IO_SERIALIZABLE = "java.io.Serializable";
	private static final String JAVA_LANG_CLONEABLE = "java.lang.Cloneable";
	private static final String JAVA_LANG_RECORD = "java.lang.Record";

	public static final String CONSTRUCTOR_METHOD_NAME = "<init>";

	public static final Set<TypeKind> REFERENCE_TYPEKINDS = EnumSet.of(TypeKind.ARRAY, TypeKind.DECLARED, TypeKind.NULL,
			TypeKind.TYPEVAR, TypeKind.INTERSECTION, TypeKind.WILDCARD);
	public static final Map<TypeKind, Set<TypeKind>> PRIMITIVE_SUBTYPE_MAP = new EnumMap<>(TypeKind.class);
	public static final Map<TypeKind, Set<TypeKind>> PRIMITIVE_ASSIGNABLE_MAP = PRIMITIVE_SUBTYPE_MAP;

	//XXX these ElementType, ElementKind and Modifier sets should be immutable
	public static final ImmutableElementTypeSet ELEMENT_TYPE_TYPE_USE = ImmutableElementTypeSet
			.of(ElementType.TYPE_USE);
	public static final ImmutableElementTypeSet ELEMENT_TYPE_METHOD = ImmutableElementTypeSet.of(ElementType.METHOD);
	public static final ImmutableElementTypeSet ELEMENT_TYPE_CONSTRUCTOR = ImmutableElementTypeSet
			.of(ElementType.CONSTRUCTOR);
	public static final ImmutableElementTypeSet ELEMENT_TYPE_PARAMETER = ImmutableElementTypeSet
			.of(ElementType.PARAMETER);
	public static final ImmutableElementTypeSet ELEMENT_TYPE_PACKAGE = ImmutableElementTypeSet.of(ElementType.PACKAGE);
	public static final ImmutableElementTypeSet ELEMENT_TYPE_FIELD = ImmutableElementTypeSet.of(ElementType.FIELD);
	public static final ImmutableElementTypeSet ELEMENT_TYPE_LOCAL_VARIABLE = ImmutableElementTypeSet
			.of(ElementType.LOCAL_VARIABLE);
	public static final ImmutableElementTypeSet ELEMENT_TYPE_CONSTRUCTOR_TYPE_USE = ImmutableElementTypeSet
			.of(ElementType.CONSTRUCTOR, ElementType.TYPE_USE);
	public static final ImmutableElementTypeSet ELEMENT_TYPE_PARAMETER_TYPE_USE = ImmutableElementTypeSet
			.of(ElementType.PARAMETER, ElementType.TYPE_USE);
	public static final ImmutableElementTypeSet ELEMENT_TYPE_FIELD_TYPE_USE = ImmutableElementTypeSet
			.of(ElementType.FIELD, ElementType.TYPE_USE);
	public static final ImmutableElementTypeSet ELEMENT_TYPE_LOCAL_VARIABLE_TYPE_USE = ImmutableElementTypeSet
			.of(ElementType.LOCAL_VARIABLE, ElementType.TYPE_USE);
	public static final ImmutableElementTypeSet ELEMENT_TYPE_ANNOTATION_TYPE_TYPE_TYPE_USE = ImmutableElementTypeSet
			.of(ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.TYPE_USE);
	public static final ImmutableElementTypeSet ELEMENT_TYPE_TYPE_TYPE_USE = ImmutableElementTypeSet
			.of(ElementType.TYPE, ElementType.TYPE_USE);
	public static final ImmutableElementTypeSet ELEMENT_TYPE_TYPE_PARAMETER_TYPE_USE = ImmutableElementTypeSet
			.of(ElementType.TYPE_PARAMETER, ElementType.TYPE_USE);

	public static final Set<Modifier> MODIFIERS_PUBLIC_STATIC = ImmutableModifierSet.of(Modifier.PUBLIC,
			Modifier.STATIC);
	public static final Set<Modifier> MODIFIERS_PUBLIC_STATIC_FINAL = ImmutableModifierSet.of(Modifier.PUBLIC,
			Modifier.STATIC, Modifier.FINAL);
	public static final Set<Modifier> MODIFIERS_FINAL = ImmutableModifierSet.of(Modifier.FINAL);
	public static final Set<Modifier> MODIFIERS_PUBLIC_PROTECTED = ImmutableModifierSet.of(Modifier.PUBLIC,
			Modifier.PROTECTED);

	public static final Set<Modifier> MODIFIERS_PUBLIC = ImmutableModifierSet.of(Modifier.PUBLIC);
	public static final Set<Modifier> MODIFIERS_PUBLIC_FINAL = ImmutableModifierSet.of(Modifier.PUBLIC, Modifier.FINAL);
	public static final Set<Modifier> MODIFIERS_PUBLIC_ABSTRACT = ImmutableModifierSet.of(Modifier.PUBLIC,
			Modifier.ABSTRACT);
	public static final Set<Modifier> MODIFIERS_PROTECTED = ImmutableModifierSet.of(Modifier.PROTECTED);
	public static final Set<Modifier> MODIFIERS_PRIVATE = ImmutableModifierSet.of(Modifier.PRIVATE);

	public static final Set<ElementKind> ELEMENT_KIND_PACKAGE_MODULE;
	public static final Set<ElementKind> ELEMENT_KIND_INTERFACES = EnumSet.of(ElementKind.INTERFACE,
			ElementKind.ANNOTATION_TYPE);
	public static final Set<ElementKind> ELEMENT_KIND_TYPES = EnumSet.of(ElementKind.INTERFACE,
			ElementKind.ANNOTATION_TYPE, ElementKind.CLASS, ElementKind.ENUM);
	public static final Set<ElementKind> ELEMENT_KIND_FIELDS = EnumSet.of(ElementKind.FIELD, ElementKind.ENUM_CONSTANT);

	private static final Map<TypeKind, Class<?>> PRIMITIVE_BOXING_CLASS_MAP = new EnumMap<>(TypeKind.class);

	private static final Name EMPTY_NAME = new IncrementalName("");
	private static final Name CAPTURED_WILDCARD_NAME = new IncrementalName("<captured wildcard>");

	static {
		ELEMENT_KIND_PACKAGE_MODULE = EnumSet.of(ElementKind.PACKAGE);
		ElementKind moduleelemkind = ElementKindCompatUtils.ELEMENTKIND_MODULE;
		if (moduleelemkind != null) {
			ELEMENT_KIND_PACKAGE_MODULE.add(moduleelemkind);
		}
	}

	static {
		PRIMITIVE_BOXING_CLASS_MAP.put(TypeKind.BOOLEAN, Boolean.class);
		PRIMITIVE_BOXING_CLASS_MAP.put(TypeKind.BYTE, Byte.class);
		PRIMITIVE_BOXING_CLASS_MAP.put(TypeKind.SHORT, Short.class);
		PRIMITIVE_BOXING_CLASS_MAP.put(TypeKind.INT, Integer.class);
		PRIMITIVE_BOXING_CLASS_MAP.put(TypeKind.LONG, Long.class);
		PRIMITIVE_BOXING_CLASS_MAP.put(TypeKind.FLOAT, Float.class);
		PRIMITIVE_BOXING_CLASS_MAP.put(TypeKind.DOUBLE, Double.class);
		PRIMITIVE_BOXING_CLASS_MAP.put(TypeKind.CHAR, Character.class);
	}
	static {
		PRIMITIVE_SUBTYPE_MAP.put(TypeKind.BOOLEAN, EnumSet.of(TypeKind.BOOLEAN));
		PRIMITIVE_SUBTYPE_MAP.put(TypeKind.CHAR,
				EnumSet.of(TypeKind.CHAR, TypeKind.FLOAT, TypeKind.INT, TypeKind.LONG, TypeKind.DOUBLE));
		PRIMITIVE_SUBTYPE_MAP.put(TypeKind.BYTE, EnumSet.of(TypeKind.BYTE, TypeKind.FLOAT, TypeKind.INT, TypeKind.SHORT,
				TypeKind.LONG, TypeKind.DOUBLE));
		PRIMITIVE_SUBTYPE_MAP.put(TypeKind.SHORT,
				EnumSet.of(TypeKind.SHORT, TypeKind.LONG, TypeKind.INT, TypeKind.FLOAT, TypeKind.DOUBLE));
		PRIMITIVE_SUBTYPE_MAP.put(TypeKind.INT,
				EnumSet.of(TypeKind.INT, TypeKind.LONG, TypeKind.FLOAT, TypeKind.DOUBLE));
		PRIMITIVE_SUBTYPE_MAP.put(TypeKind.LONG, EnumSet.of(TypeKind.LONG, TypeKind.FLOAT, TypeKind.DOUBLE));
		PRIMITIVE_SUBTYPE_MAP.put(TypeKind.FLOAT, EnumSet.of(TypeKind.FLOAT, TypeKind.DOUBLE));
		PRIMITIVE_SUBTYPE_MAP.put(TypeKind.DOUBLE, EnumSet.of(TypeKind.DOUBLE));
	}

	private static final Map<TypeKind, PrimitiveType> PRIMITIVE_TYPES = new EnumMap<>(TypeKind.class);
	private static final Map<TypeKind, String> PRIMITIVE_TO_BOXED_TYPE_NAMES = new EnumMap<>(TypeKind.class);
	private static final Map<String, PrimitiveType> UNBOXED_TYPES = new TreeMap<>();
	static {
		SimplePrimitiveType bytetype = new SimplePrimitiveType(TypeKind.BYTE);
		SimplePrimitiveType shorttype = new SimplePrimitiveType(TypeKind.SHORT);
		SimplePrimitiveType inttype = new SimplePrimitiveType(TypeKind.INT);
		SimplePrimitiveType longtype = new SimplePrimitiveType(TypeKind.LONG);
		SimplePrimitiveType chartype = new SimplePrimitiveType(TypeKind.CHAR);
		SimplePrimitiveType booleantype = new SimplePrimitiveType(TypeKind.BOOLEAN);
		SimplePrimitiveType floattype = new SimplePrimitiveType(TypeKind.FLOAT);
		SimplePrimitiveType doubletype = new SimplePrimitiveType(TypeKind.DOUBLE);

		PRIMITIVE_TYPES.put(TypeKind.BYTE, bytetype);
		PRIMITIVE_TYPES.put(TypeKind.SHORT, shorttype);
		PRIMITIVE_TYPES.put(TypeKind.INT, inttype);
		PRIMITIVE_TYPES.put(TypeKind.LONG, longtype);
		PRIMITIVE_TYPES.put(TypeKind.FLOAT, floattype);
		PRIMITIVE_TYPES.put(TypeKind.DOUBLE, doubletype);
		PRIMITIVE_TYPES.put(TypeKind.CHAR, chartype);
		PRIMITIVE_TYPES.put(TypeKind.BOOLEAN, booleantype);

		UNBOXED_TYPES.put("java.lang.Byte", bytetype);
		UNBOXED_TYPES.put("java.lang.Short", shorttype);
		UNBOXED_TYPES.put("java.lang.Integer", inttype);
		UNBOXED_TYPES.put("java.lang.Long", longtype);
		UNBOXED_TYPES.put("java.lang.Character", chartype);
		UNBOXED_TYPES.put("java.lang.Boolean", booleantype);
		UNBOXED_TYPES.put("java.lang.Float", floattype);
		UNBOXED_TYPES.put("java.lang.Double", doubletype);

		PRIMITIVE_TO_BOXED_TYPE_NAMES.put(TypeKind.BYTE, "java.lang.Byte");
		PRIMITIVE_TO_BOXED_TYPE_NAMES.put(TypeKind.SHORT, "java.lang.Short");
		PRIMITIVE_TO_BOXED_TYPE_NAMES.put(TypeKind.INT, "java.lang.Integer");
		PRIMITIVE_TO_BOXED_TYPE_NAMES.put(TypeKind.LONG, "java.lang.Long");
		PRIMITIVE_TO_BOXED_TYPE_NAMES.put(TypeKind.CHAR, "java.lang.Character");
		PRIMITIVE_TO_BOXED_TYPE_NAMES.put(TypeKind.BOOLEAN, "java.lang.Boolean");
		PRIMITIVE_TO_BOXED_TYPE_NAMES.put(TypeKind.FLOAT, "java.lang.Float");
		PRIMITIVE_TO_BOXED_TYPE_NAMES.put(TypeKind.DOUBLE, "java.lang.Double");
	}

	private static final PackageElement CAPTURE_VARIABLES_ENCLOSING_PACKAGE_ELEMENT = new CapturePackageElement(
			EMPTY_NAME);
	private static final Element CAPTURES_ENCLOSING_ELEMENT = new CaptureEnclosingElement(
			CAPTURE_VARIABLES_ENCLOSING_PACKAGE_ELEMENT, IncrementalNoType.INSTANCE_NONE, EMPTY_NAME);

	private static final ForwardingTypeElement FORWARDED_ELEMENT_NOT_FOUND_TAG = new ForwardingTypeElement(null, null);

	protected static final int DEFAULT_FORWARDING_HASHMAP_SIZE = 1024 * 8;

	protected final Elements realElements;
	protected final Object javacSync;

	private final ConcurrentHashMap<String, ForwardingTypeElement> forwardedTypeElements = new ConcurrentHashMap<>(
			DEFAULT_FORWARDING_HASHMAP_SIZE);
	/**
	 * Guarded by {@link #javacSync}.
	 */
	protected final Map<Element, Element> forwardedElements = new IdentityHashMap<>(DEFAULT_FORWARDING_HASHMAP_SIZE);

	private final ErasureVisitor erasureVisitor = new ErasureVisitor(this);
	private final TypeSignatureToMirrorVisitor typeSignatureToMirrorVisitor = new TypeSignatureToMirrorVisitor();
	private final TypeSignatureCanonicalNameVisitor typeSignatureCanonicalNameVisitor = new TypeSignatureCanonicalNameVisitor();
	private final TypeArgumentTypeSignatureToMirrorVisitor typeArgumentTypeSignatureToMirrorVisitor = new TypeArgumentTypeSignatureToMirrorVisitor();
	private final IsSubTypeVisitor subTypeVisitor = new IsSubTypeVisitor(this);
	private final IsAssignableTypeVisitor assignableTypeVisitor = new IsAssignableTypeVisitor(this);
	private final ContainsTypeVisitor containsTypeVisitor = new ContainsTypeVisitor(this);
	private final ElementHiderVisitor elementHiderVisitor = new ElementHiderVisitor();
	private final TypeVariableMirrorReplacerVisitor typeVariableMirrorReplacerVisitor = new TypeVariableMirrorReplacerVisitor();
	private final AsMemberOfVisitor asMemberOfVisitor = new AsMemberOfVisitor();
	private ForwardingElementVisitor forwardingElementVisitor = new ForwardingElementVisitor();

	private final TypeElement javaLangObjectElement;
	private final TypeElement javaLangEnumElement;
	private final TypeElement javaIoSerializableElement;
	private final TypeElement javaLangCloneableElement;
	private final Supplier<TypeElement> javaLangRecordElementSupplier;

	private volatile transient List<TypeMirror> primitiveArrayDirectSuperTypes;

	protected final Map<Element, ClassHoldingFileData> elementsToFilesMap = new IdentityHashMap<>();
	protected final Map<SakerPath, Set<Element>> filesToElementsMap = new TreeMap<>();
	protected PackagesTypesContainer packageTypesContainer;

	protected final ParserCache cache;
	private CompilationContextInformation compilationContext;

	public IncrementalElementsTypes8(Elements realelements, Object javacsync, ParserCache cache,
			CompilationContextInformation context) {
		this.realElements = realelements;
		this.javacSync = javacsync;
		this.cache = cache;
		this.compilationContext = context;

		this.javaLangObjectElement = getTypeElementFromRealElements(JAVA_LANG_OBJECT);
		this.javaLangEnumElement = getTypeElementFromRealElements(JAVA_LANG_ENUM);
		this.javaIoSerializableElement = getTypeElementFromRealElements(JAVA_IO_SERIALIZABLE);
		this.javaLangCloneableElement = getTypeElementFromRealElements(JAVA_LANG_CLONEABLE);
		this.javaLangRecordElementSupplier = LazySupplier.of(() -> getTypeElementFromRealElements(JAVA_LANG_RECORD));
	}

	protected void setForwardingElementVisitor(ForwardingElementVisitor forwardingElementVisitor) {
		this.forwardingElementVisitor = forwardingElementVisitor;
	}

	@Override
	public int getCompilerJVMJavaMajorVersion() {
		return compilationContext.getCompilerJVMJavaMajorVersion();
	}

	@Override
	public Elements getRealElements() {
		return realElements;
	}

	public NavigableMap<SakerPath, ClassHoldingFileData> mapElementsToFiles(Iterable<? extends Element> elements) {
		NavigableMap<SakerPath, ClassHoldingFileData> result = new TreeMap<>();
		for (Element e : elements) {
			ClassHoldingFileData fd = getFileDataForElement(e);
			if (fd != null) {
				result.put(fd.getPath(), fd);
			}
		}
		return result;
	}

	public final Set<? extends Element> addRootClassFile(ClassHoldingFileData fd) {
		Set<Element> result = new HashSet<>();
		addRootClassFileElements(fd, result);
		filesToElementsMap.put(fd.getPath(), result);
		return result;
	}

	public final Set<? extends Element> getRootClassFileElements(SakerPath path) {
		return filesToElementsMap.get(path);
	}

	/**
	 * Initializes the module information for this compilation.
	 * 
	 * @param fd
	 *            The compilation unit holding the module signature.
	 */
	public void initCompilationModule(ClassHoldingFileData fd) {
		throw new UnsupportedOperationException("Modules are not supported.");
	}

	public void initCompilationModuleNotSpecified() {
		//we dont care about that < jdk 9
		this.packageTypesContainer = new NoModulePackagesTypesContainer(this);
	}

	protected void addRootClassFileElements(ClassHoldingFileData fd, Set<Element> result) throws AssertionError {
		addRootTypeElements(fd, result);
		addRootPackageElement(fd, result);
	}

	protected void addRootPackageElement(ClassHoldingFileData fd, Set<Element> result) {
		PackageSignature pack = fd.getPackageSignature();
		if (pack == null) {
			return;
		}
		PackageElement packelem = packageTypesContainer.addParsedPackage(pack);
		elementsToFilesMap.put(packelem, fd);
		result.add(packelem);
	}

	protected void addRootTypeElements(ClassHoldingFileData fd, Set<Element> result) throws AssertionError {
		for (ClassSignature c : fd.getClassSignatures()) {
			TypeElement typeelem = packageTypesContainer.addParsedClass(c);
			elementsToFilesMap.put(typeelem, fd);
			result.add(typeelem);
		}
	}

	/**
	 * Returns no module in case if <= JDK 8, unnamed or the compilation module in case of JDK >= 9
	 * 
	 * @return
	 */
	@Override
	public PackagesTypesContainer getLocalPackagesTypesContainer() {
		return packageTypesContainer;
	}

	public String getCurrentModuleName() {
		return null;
	}

	/**
	 * @return May be <code>null</code>.
	 */
	public Element getCurrentModuleElement() {
		return null;
	}

//	public IncrementalTypeElement getTypeElement(ClassSignature c) {
//		IncrementalTypeElement got = canonicalTypeElements.get(c.getCanonicalName());
//		if (got != null) {
//			return got;
//		}
//		IncrementalTypeElement typeelem = new IncrementalTypeElement(c, this);
//		canonicalTypeElements.put(c.getCanonicalName(), typeelem);
//		return typeelem;
//	}

	public TypeElement getClassTypeElement(Class<?> clazz) {
		if (clazz.isPrimitive() || clazz.isArray()) {
			return null;
		}
		String cname = clazz.getCanonicalName();
		if (cname == null) {
			return null;
		}
		return getTypeElement(cname);
	}

	@Override
	public TypeElement getTypeElement(TypeSignature sig, Element enclosingelement) {
		String cname = getCanonicalName(sig, enclosingelement);
		if (cname == null) {
			return null;
		}
		return getTypeElement(cname);
	}

	public ClassHoldingFileData getFileDataForElement(Element e) {
		ClassHoldingFileData cfd = elementsToFilesMap.get(e);
		if (cfd != null) {
			return cfd;
		}
		if (e instanceof IncrementalElement) {
			for (Element enc = e; (enc = enc.getEnclosingElement()) != null;) {
				if (ELEMENT_KIND_PACKAGE_MODULE.contains(enc.getKind())) {
					//do not go to the enclosing package or module
					break;
				}

				cfd = elementsToFilesMap.get(enc);
				if (cfd != null) {
					return cfd;
				}
			}
		}
		return null;
	}

	public SakerPath getFilePathForElement(Element e) {
		ClassHoldingData cfd = elementsToFilesMap.get(e);
		if (cfd != null) {
			return cfd.getPath();
		}

		if (e instanceof IncrementalElement) {
			for (Element enc = e; (enc = enc.getEnclosingElement()) != null;) {
				if (ELEMENT_KIND_PACKAGE_MODULE.contains(enc.getKind())) {
					//do not go to the enclosing package or module
					break;
				}

				cfd = elementsToFilesMap.get(enc);
				if (cfd != null) {
					return cfd.getPath();
				}
			}
			if (TestFlag.ENABLED) {
				throw new AssertionError("ELEM path not found " + e + " - " + System.identityHashCode(e));
			}
			//none of the enclosing elements are in the elements map
			//this should never happen
			return null;
		} else if (e instanceof ForwardingElement) {
			//element is a javac element
			//it was probably forwarded from the classpath
			//we cannot determine the real class file
			return null;
		} else if (e instanceof DualPackageElement) {
			//if the package element has not been added to the element file map then it must be forwarded or on demand created
			//in that case we cannot determine the file path because no corresponding package-info is present
			return null;
		} else {
			throw new IllegalArgumentException("Unknown element class: " + ObjectUtils.classOf(e));
		}
	}

	@Override
	public List<TypeMirror> resolveUnspecifiedPermitSubclasses(IncrementalTypeElement type) {
		Objects.requireNonNull(type, "type");
		ClassHoldingFileData file = elementsToFilesMap.get(type);
		if (file == null) {
			throw new IllegalArgumentException("Containing file not found for type: " + type);
		}
		boolean searchextend = type.getKind().isClass();
		List<TypeMirror> result = new ArrayList<>();
		for (String typename : file.getAllClassCanonicalNames()) {
			TypeElement te = getTypeElement(typename);
			if (te == type) {
				continue;
			}
			if (searchextend) {
				TypeMirror sc = te.getSuperclass();
				if (sc.getKind() == TypeKind.DECLARED) {
					if (((DeclaredType) sc).asElement() == type) {
						result.add(te.asType());
					}
				}
			} else {
				for (TypeMirror sc : te.getInterfaces()) {
					if (sc.getKind() == TypeKind.DECLARED) {
						if (((DeclaredType) sc).asElement() == type) {
							result.add(te.asType());
							break;
						}
					}
				}
			}

		}
		return result;
	}

	@Override
	public boolean isJavacElementDeprecated(Element elem) {
		//lock on javac, as checking deprecation may induce completion
		return javac(() -> realElements.isDeprecated(elem));
	}

	@Override
	public Name getJavacTypeBinaryName(TypeElement type) {
		//XXX might need lock, but might not really 
		return realElements.getBinaryName(type);
	}

	@Override
	public String getCanonicalName(TypeSignature signature, Element enclosingelement) {
		return signature.accept(typeSignatureCanonicalNameVisitor, enclosingelement);
	}

	@Override
	public DeclaredType getAnnotationDeclaredType(AnnotationSignature signature, Element enclosingresolutionelement) {
		TypeSignature annottype = signature.getAnnotationType();
		TypeElement foundtype = getTypeElement(annottype, enclosingresolutionelement);
		if (foundtype != null) {
			return (DeclaredType) foundtype.asType();
		}
		//failed to get the annotation type
		return new IncrementalErrorType(this, signature);
	}

	protected VariableElement getEnumConstant(TypeSignature signature, String name,
			Element enclosingresolutionelement) {
		TypeElement elem = getTypeElement(signature, enclosingresolutionelement);
		if (elem != null) {
			for (Element e : elem.getEnclosedElements()) {
				if (e.getKind() != ElementKind.ENUM_CONSTANT) {
					continue;
				}
				VariableElement ve = (VariableElement) e;
				if (ve.getSimpleName().contentEquals(name)) {
					return ve;
				}
			}
		}
		return null;
	}

	@Override
	public TypeMirror getTypeMirror(TypeSignature signature, Element enclosingelement) {
		return signature.accept(typeSignatureToMirrorVisitor, enclosingelement);
	}

	protected TypeMirror getTypeMirror(WildcardTypeSignature sig, Element enclosingelement) {
		return new IncrementalWildcardType(this, sig, enclosingelement, null);
	}

	protected TypeMirror getTypeMirror(TypeVariableTypeSignature sig, Element enclosingelement) {
		TypeParameterElement tpe = findTypeParameterElement(sig.getVariableName(), enclosingelement);
		if (tpe != null) {
			return tpe.asType();
		}
		return new IncrementalTypeVariable(this, sig, null);
	}

	@Override
	public List<? extends TypeMirror> getDeclaredTypeArguments(IncrementalDeclaredType type) {
		List<? extends TypeSignature> signatures = type.getSignature().getTypeParameters();
		if (signatures.isEmpty()) {
			return Collections.emptyList();
		}
		TypeElement element = type.asElement();
		List<? extends TypeParameterElement> tpeparams = element == null ? Collections.emptyList()
				: element.getTypeParameters();
		Iterator<? extends TypeParameterElement> tpeit = tpeparams.iterator();

		TypeMirror[] typemirrors = new TypeMirror[signatures.size()];
		int i = 0;
		for (TypeSignature sig : signatures) {
			TypeParameterElement ntpe = tpeit.hasNext() ? tpeit.next() : null;
			TypeMirror tm = typeArgumentTypeSignatureToMirrorVisitor.visit(sig, type.getEnclosingElement(), ntpe);
			typemirrors[i++] = tm;
		}

		return ImmutableUtils.unmodifiableArrayList(typemirrors);
	}

	public static TypeParameterElement findTypeParameterElement(String name, Element enclosing) {
		while (enclosing != null) {
			if (enclosing instanceof Parameterizable) {
				Parameterizable prm = (Parameterizable) enclosing;
				for (TypeParameterElement tpe : prm.getTypeParameters()) {
					if (tpe.getSimpleName().contentEquals(name)) {
						return tpe;
					}
				}
			}
			enclosing = enclosing.getEnclosingElement();
		}
		return null;
	}

	public static TypeElement findOuterMostEnclosingType(TypeElement elem) {
		while (elem.getNestingKind() != NestingKind.TOP_LEVEL) {
			elem = (TypeElement) elem.getEnclosingElement();
		}
		return elem;
	}

	public static Element getOuterMostEnclosingElement(Element elem) {
		if (elem.getKind() == ElementKind.PACKAGE || ElementKindCompatUtils.isModuleElementKind(elem.getKind())) {
			return elem;
		}
		while (!(elem.getKind().isClass() || elem.getKind().isInterface())) {
			elem = elem.getEnclosingElement();
		}
		return findOuterMostEnclosingType((TypeElement) elem);
	}

	protected TypeMirror getTypeMirror(UnknownTypeSignature sig, @SuppressWarnings("unused") Element enclosingelement) {
		return new IncrementalErrorType(this, sig);
	}

	protected IncrementalTypeMirror<ArrayTypeSignature> getTypeMirror(ArrayTypeSignature sig,
			Element enclosingelement) {
		return new IncrementalArrayType(this, sig, enclosingelement);
	}

	protected TypeMirror getTypeMirror(PrimitiveTypeSignature sig, Element enclosingelement) {
		if (sig.getAnnotations().isEmpty()) {
			return getPrimitiveType(sig.getTypeKind());
		}
		return new IncrementalPrimitiveType(this, sig, enclosingelement);
	}

	protected TypeMirror getTypeMirror(NoTypeSignature sig, @SuppressWarnings("unused") Element enclosingelement) {
		//no annotations possible on these
		return getNoType(sig.getKind());
	}

	protected TypeMirror getTypeMirror(IntersectionTypeSignature sig, Element enclosingelement) {
		return new IncrementalIntersectionType(this, sig, enclosingelement);
	}

	protected TypeMirror getTypeMirror(UnionTypeSignature sig, Element enclosingelement) {
		return new IncrementalUnionType(this, sig, enclosingelement);
	}

	@Override
	public DeclaredType getJavaLangObjectTypeMirror() {
		return (DeclaredType) javaLangObjectElement.asType();
	}

	@Override
	public DeclaredType getJavaLangRecordTypeMirror() {
		return (DeclaredType) javaLangRecordElementSupplier.get().asType();
	}

	@Override
	public DeclaredType getJavaLangCloneableTypeMirror() {
		return (DeclaredType) javaLangCloneableElement.asType();
	}

	@Override
	public DeclaredType getJavaIoSerializableTypeMirror() {
		return (DeclaredType) javaIoSerializableElement.asType();
	}

	@Override
	public TypeElement getJavaLangObjectTypeElement() {
		return javaLangObjectElement;
	}

	@Override
	public TypeElement getJavaLangEnumTypeElement() {
		return javaLangEnumElement;
	}

	@Override
	public TypeElement getJavaIoSerializableTypeElement() {
		return javaIoSerializableElement;
	}

	@Override
	public TypeElement getJavaLangCloneableTypeElement() {
		return javaLangCloneableElement;
	}

	public TypeMirror getCanonicalNameTypeMirror(CharSequence name) {
		TypeElement te = getTypeElement(name);
		if (te == null) {
			return new IncrementalErrorType(this, name);
		}
		return te.asType();
	}

	private static ParameterizedTypeSignature typeElementToTypeReference(TypeElement te, ParserCache cache) {
		if (isClassUnrelatedToEnclosing(te)) {
			return cache.canonicalTypeSignature(te.getQualifiedName().toString());
		}
		return TypeReferenceSignatureImpl.create(
				typeElementToTypeReference((TypeElement) te.getEnclosingElement(), cache),
				te.getSimpleName().toString());
	}

	private static AnnotationSignature annotationMirrorToSignature(AnnotationMirror a, ParserCache cache) {
		LinkedHashMap<String, AnnotationSignature.Value> vals = new LinkedHashMap<>();
		for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : a.getElementValues().entrySet()) {
			ExecutableElement method = entry.getKey();
			AnnotationValue value = entry.getValue();
			vals.put(method.getSimpleName().toString(),
					AnnotationValueToSignatureConverterVisitor.convert(value, cache));
		}
		DeclaredType annottype = a.getAnnotationType();
		//XXX should we create a dynamically unresolved declared type class?
		TypeElement typeelem = (TypeElement) annottype.asElement();
		TypeSignature annotationtype;
		if (typeelem == null) {
			annotationtype = UnknownTypeSignatureImpl.create(annottype.toString());
		} else {
			//canonical name signature is okay, as annotation type is not dependent on its enclosing type
			annotationtype = cache.canonicalTypeSignature(typeelem.getQualifiedName().toString());
		}
		return cache.createAnnotationSignature(annotationtype, vals);
	}

//	private ModifiablePackageSignature createPackageSignature(PackageElement javacpackage) {
//		PackageSignatureImpl packagesignature = new PackageSignatureImpl(javacpackage.getQualifiedName().toString());
//		List<? extends AnnotationMirror> javacannots = javacpackage.getAnnotationMirrors();
//		if (!javacannots.isEmpty()) {
//			Collection<ModifiableAnnotationSignature> modannots = packagesignature.getModifiableAnnotations();
//			for (AnnotationMirror am : javacannots) {
//				am = forward(am);
//				modannots.add(annotationMirrorToSignature(am));
//			}
//		}
//		return packagesignature;
//	}

//	private IncrementalPackageElement createForwardedPackageElement(PackageElement javacpackage) {
//		ModifiablePackageSignature packagesignature = createPackageSignature(javacpackage);
//		return new IncrementalPackageElement(packagesignature, this);
//	}

//	private IncrementalPackageElement createPackageElement(String name) {
//		//we have no package like this in the sources
//		PackageElement javacpackage = realElements.getPackageElement(name);
//		PackageSignature packagesignature;
//		if (javacpackage != null) {
//			packagesignature = createPackageSignature(javacpackage);
//		} else {
//			//no javac package, create empty package signature
//			packagesignature = new PackageSignatureImpl(name);
//		}
//		return new IncrementalPackageElement(packagesignature, this);
//	}

//	
//	Elements functions start
//	

	@Override
	public PackageElement getPackageElement(CharSequence name) {
		return packageTypesContainer.getPackageElement(name.toString());
	}

	@Override
	public TypeElement getTypeElementFromRealElements(String name) {
		ForwardingTypeElement forwarded = forwardedTypeElements.get(name);
		if (forwarded == FORWARDED_ELEMENT_NOT_FOUND_TAG) {
			return null;
		}
		if (forwarded != null) {
			return forwarded;
		}
		synchronized (javacSync) {
			TypeElement realtype = realElements.getTypeElement(name);
			if (realtype != null) {
				return forwardElementLockedImpl(realtype);
			}
		}
		forwardedTypeElements.put(name, FORWARDED_ELEMENT_NOT_FOUND_TAG);
		return null;
	}

	@Override
	public TypeElement getTypeElement(CharSequence name) {
		return packageTypesContainer.getTypeElement(name.toString());
	}

	@Override
	public Map<? extends ExecutableElement, ? extends AnnotationValue> getAnnotationValues(
			IncrementalAnnotationMirror a, boolean includedefaults, Element enclosingresolutionelement) {
		AnnotationSignature asignature = a.getSignature();
		TypeSignature annottype = asignature.getAnnotationType();

		TypeElement type = getTypeElement(annottype, enclosingresolutionelement);
		if (type == null) {
			return Collections.emptyMap();
		}
		Map<ExecutableElement, AnnotationValue> result = new LinkedHashMap<>();

		Map<? extends String, ? extends Value> values = asignature.getValues();

		List<? extends Element> encloseds = type.getEnclosedElements();
		Map<String, ExecutableElement> methods = new LinkedHashMap<>(encloseds.size() * 3 / 2);

		for (Element elem : encloseds) {
			if (elem.getKind() != ElementKind.METHOD) {
				continue;
			}
			ExecutableElement ee = (ExecutableElement) elem;
			methods.put(ee.getSimpleName().toString(), ee);
		}
		//add the values
		for (Entry<? extends String, ? extends Value> entry : values.entrySet()) {
			ExecutableElement ee = methods.remove(entry.getKey());
			if (ee != null) {
				//ee can be null, if we have an erroneous annotation.
				//e.g. an annotation method just got removed and the referencing annotation still has it referenced.
				String fieldname = ee.getSimpleName().toString();
				AnnotationSignature.Value annotvalue = entry.getValue();

				SignaturePath valpath = SignaturePath.createIndexed(a.getAnnotationSignaturePath(), annotvalue,
						fieldname);
				result.put(ee, new IncrementalAnnotationValue(this, annotvalue, ee.getReturnType(),
						enclosingresolutionelement, valpath));
			}
		}
		if (includedefaults) {
			//add the defaults
			for (ExecutableElement ee : methods.values()) {
				result.put(ee, ee.getDefaultValue());
			}
		}

		return result;
	}

	@Override
	public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValuesWithDefaults(
			AnnotationMirror a) {
		IncrementalAnnotationMirror iam = (IncrementalAnnotationMirror) a;
		return getAnnotationValues(iam, true, iam.getResolutionEnclosingElement());
	}

	@Override
	public String getDocComment(Element e) {
		if (e instanceof DocumentedElement) {
			DocumentedElement<?> docelem = (DocumentedElement<?>) e;
			return docelem.getDocComment();
		}
		return null;
	}

	@Override
	public boolean isDeprecated(Element e) {
		return ((CommonElement) e).isDeprecated();
	}

	@Override
	public Name getBinaryName(TypeElement type) {
		return ((CommonTypeElement) type).getBinaryName();
	}

	@Override
	public PackageElement getPackageOf(Element type) {
		return getPackageOfImpl(type);
	}

	private static PackageElement getPackageOfImpl(Element elem) {
		while (elem.getKind() != ElementKind.PACKAGE) {
			Element enclosing = elem.getEnclosingElement();
			if (enclosing == null) {
				throw new IllegalStateException("Enclosing is null for: " + elem);
			}
			elem = enclosing;
		}
		return (PackageElement) elem;
	}

	private static final int ALLMEMBERS_SCOPE_TYPE = 0;
	private static final int ALLMEMBERS_SCOPE_SUPER = 1;
	private static final int ALLMEMBERS_SCOPE_INTERFACE = 2;

	private void getAllMembers(TypeElement type, PackageElement typepackage, List<Element> elementresults,
			List<ExecutableElement> methods, Set<TypeElement> includedtypes, int allmembersscope,
			PackageElement visibilitypackage, TypeElement originaltype,
			Map<TypeParameterElement, TypeMirror> parammirrors) {
		if (!includedtypes.add(type)) {
			//already added this type to element results
			return;
		}
		for (TypeMirror itf : type.getInterfaces()) {
			TypeElement itfelem = (TypeElement) asElement(itf);
			if (itfelem != null) {
				getAllMembers(itfelem, getPackageOf(itfelem), elementresults, methods, includedtypes,
						ALLMEMBERS_SCOPE_SUPER, visibilitypackage, originaltype, parammirrors);
			}
		}
		TypeElement superclasselem = getSuperClassOf(type);
		if (superclasselem != null) {
			getAllMembers(superclasselem, getPackageOf(superclasselem), elementresults, methods, includedtypes,
					ALLMEMBERS_SCOPE_SUPER, visibilitypackage, originaltype, parammirrors);
		}
		for (Element element : type.getEnclosedElements()) {
			switch (allmembersscope) {
				case ALLMEMBERS_SCOPE_TYPE: {
					break;
				}
				case ALLMEMBERS_SCOPE_SUPER: {
					if (element.getKind() == ElementKind.CONSTRUCTOR) {
						continue;
					}
					Set<Modifier> modifiers = element.getModifiers();
					if (modifiers.contains(Modifier.PRIVATE)) {
						continue;
					}
					if (modifiers.contains(Modifier.STATIC) && element.getKind() == ElementKind.METHOD
							&& type.getKind().isInterface()) {
						continue;
					}
					if (!modifiers.contains(Modifier.PUBLIC) && !modifiers.contains(Modifier.PROTECTED)) {
						//default visibility
						if (typepackage != visibilitypackage) {
							//package visibility, but different package
							continue;
						}
					}
					break;
				}
				case ALLMEMBERS_SCOPE_INTERFACE: {
					if (element.getKind() == ElementKind.CONSTRUCTOR) {
						continue;
					}
					Set<Modifier> modifiers = element.getModifiers();
					if (!modifiers.contains(Modifier.PUBLIC)) {
						continue;
					}
					if (modifiers.contains(Modifier.STATIC) && element.getKind() == ElementKind.METHOD) {
						continue;
					}
					break;
				}
				default: {
					break;
				}
			}
			switch (element.getKind()) {
				case ENUM_CONSTANT:
				case ENUM:
				case CLASS:
				case ANNOTATION_TYPE:
				case INTERFACE:
				case CONSTRUCTOR:
				case FIELD: {
					elementresults.add(element);
					break;
				}
				case METHOD: {
					//we are trying to add a method
					//if this overrides any methods, remove it
					ExecutableElement ee = (ExecutableElement) element;
					for (Iterator<ExecutableElement> it = methods.iterator(); it.hasNext();) {
						ExecutableElement e = it.next();
						if (overrides(ee, e, originaltype, parammirrors)) {
							//we need to check if the methods are related in their types
							//    if they are contained in two totally different classes & interfaces
							//    then keep it in the result collection
							//see the overrides method documentation for example
							//    without checking the relation, the interface method would be filtered out
							if (hasSuperType((TypeElement) ee.getEnclosingElement(),
									(TypeElement) e.getEnclosingElement())) {
								it.remove();
							}
						}
					}
					methods.add(ee);
					break;
				}
				default: {
					break;
				}
			}
		}
	}

	private List<? extends Element> getAllNonObjectMembers(TypeElement type) {
		PackageElement typepackage = getPackageOf(type);

		Map<TypeParameterElement, TypeMirror> parammirrors = getTypeParameterMapResolve(type);

		List<Element> elementresults = new ArrayList<>();
		List<ExecutableElement> methods = new ArrayList<>();
		Set<TypeElement> includedtypes = ObjectUtils.newSetFromMap(new IdentityHashMap<>());

		getAllMembers(type, typepackage, elementresults, methods, includedtypes, ALLMEMBERS_SCOPE_TYPE, typepackage,
				type, parammirrors);

		return ObjectUtils.newArrayList(elementresults, methods);
	}

	@Override
	public List<? extends Element> getAllMembers(TypeElement type) {
		PackageElement typepackage = getPackageOf(type);

		Map<TypeParameterElement, TypeMirror> parammirrors = getTypeParameterMapResolve(type);

		List<Element> elementresults = new ArrayList<>();
		List<ExecutableElement> methods = new ArrayList<>();
		Set<TypeElement> includedtypes = ObjectUtils.newSetFromMap(new IdentityHashMap<>());

		//add the object first so interface overrides work
		TypeElement objectelem = javaLangObjectElement;
		getAllMembers(objectelem, getPackageOf(objectelem), elementresults, methods, includedtypes,
				type.getKind().isInterface() ? ALLMEMBERS_SCOPE_INTERFACE : ALLMEMBERS_SCOPE_SUPER, typepackage, type,
				parammirrors);

		getAllMembers(type, typepackage, elementresults, methods, includedtypes, ALLMEMBERS_SCOPE_TYPE, typepackage,
				type, parammirrors);

		List<Element> result = new ArrayList<>(elementresults.size() + methods.size());
		result.addAll(elementresults);
		result.addAll(methods);
		return result;
	}

	private boolean hasAnnotationWithType(List<? extends AnnotationMirror> annotations, TypeElement type) {
		for (AnnotationMirror a : annotations) {
			DeclaredType dt = a.getAnnotationType();
			if (dt != null) {
				if (dt.asElement() == type) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean isInheritedAnnotationType(TypeElement annotationtype) {
		return annotationtype.getAnnotation(Inherited.class) != null;
	}

	private void getSuperClassAllAnnotationMirrors(TypeElement type, List<AnnotationMirror> result) {
		while (type != null) {
			for (AnnotationMirror a : type.getAnnotationMirrors()) {
				DeclaredType dt = a.getAnnotationType();
				if (dt == null) {
					continue;
				}
				TypeElement atype = (TypeElement) dt.asElement();
				if (atype == null) {
					continue;
				}
				if (isInheritedAnnotationType(atype) && !hasAnnotationWithType(result, atype)) {
					//the annotation is an inherited type and not yet present
					result.add(a);
				}
			}
			type = getSuperClassOf(type);
		}
	}

	@Override
	public List<? extends AnnotationMirror> getAllAnnotationMirrors(Element e) {
		List<? extends AnnotationMirror> elemannotations = e.getAnnotationMirrors();
		if (e.getKind() == ElementKind.CLASS) {
			List<AnnotationMirror> result = new ArrayList<>(elemannotations);
			getSuperClassAllAnnotationMirrors(getSuperClassOf((TypeElement) e), result);
			return result;
		}
		return elemannotations;
	}

	private static class ElementKindSimplifierVisitor implements DefaultedElementVisitor<ElementKind, Void> {
		public static final ElementKindSimplifierVisitor INSTANCE = new ElementKindSimplifierVisitor();

		@Override
		public ElementKind visitModuleCompat(ModuleElementCompat moduleElement, Void p) {
			return moduleElement.getRealObject().getKind();
		}

		@Override
		public ElementKind visitPackage(PackageElement e, Void p) {
			return ElementKind.PACKAGE;
		}

		@Override
		public ElementKind visitType(TypeElement e, Void p) {
			return ElementKind.CLASS;
		}

		@Override
		public ElementKind visitVariable(VariableElement e, Void p) {
			return ElementKind.FIELD;
		}

		@Override
		public ElementKind visitExecutable(ExecutableElement e, Void p) {
			return ElementKind.METHOD;
		}

		@Override
		public ElementKind visitTypeParameter(TypeParameterElement e, Void p) {
			return ElementKind.TYPE_PARAMETER;
		}

		@Override
		public ElementKind visitUnknown(Element e, Void p) {
			return null;
		}
	}

	private class ElementHiderVisitor implements DefaultedElementVisitor<Boolean, Element> {
		@Override
		public Boolean visitPackage(PackageElement hider, Element hidden) {
			return false;
		}

		@Override
		public Boolean visitModuleCompat(ModuleElementCompat moduleElement, Element p) {
			return false;
		}

		@Override
		public Boolean visitType(TypeElement hider, Element hidden) {
			if (hider.getNestingKind() != NestingKind.MEMBER) {
				return false;
			}
			switch (hidden.getKind()) {
				case ANNOTATION_TYPE:
				case CLASS:
				case ENUM:
				case INTERFACE: {
					//type hiding type
					TypeElement te = (TypeElement) hidden;
					if (te.getNestingKind() == NestingKind.MEMBER) {
						if (hasSuperType((TypeElement) hider.getEnclosingElement(),
								(TypeElement) hidden.getEnclosingElement())) {
							return true;
						}
					}
					return false;
				}
				case TYPE_PARAMETER: {
					//hides type parameters only in the directly enclosing scope
					//although it hides in the broader enclosing scope, javac still returns false
					//for example:
					//class C<T> { class I { class T{ } } }
					if (hider.getEnclosingElement() == hidden.getEnclosingElement()) {
						return true;
					}
					return false;
				}
				default:
					return false;
			}
		}

		@Override
		public Boolean visitVariable(VariableElement hider, Element hidden) {
			if (hidden.accept(ElementKindSimplifierVisitor.INSTANCE, null) != ElementKind.FIELD) {
				return false;
			}
			if (hidden.getKind() == ElementKind.PARAMETER) {
				//parameter cannot be hidden
				return false;
			}
			TypeElement hidertype = getEnclosingTypeElement(hider);
			TypeElement hiddentype = getEnclosingTypeElement(hidden);
			if (hidertype == hiddentype) {
				//can only be in the same type, if the hider is a parameter
				if (hider.getKind() != ElementKind.PARAMETER) {
					return false;
				}
				//can be hidden
			} else if (!hasSuperType(hidertype, hiddentype)) {
				return false;
			}
			return true;
		}

		@Override
		public Boolean visitExecutable(ExecutableElement hider, Element hidden) {
			if (hider.getKind() != ElementKind.METHOD || hidden.getKind() != ElementKind.METHOD) {
				//only methods can hide each other, constructors an initializers dont
				return false;
			}
			if (!hider.getModifiers().contains(Modifier.STATIC)) {
				//in case of methods, only static methods can hide each other
				//if a static method hides an instance method, it is a compile error
				return false;
			}
			if (!isSubsignature((ExecutableType) hider.asType(), (ExecutableType) hidden.asType())) {
				return false;
			}

			TypeElement hidertype = (TypeElement) hider.getEnclosingElement();
			TypeElement hiddentype = (TypeElement) hidden.getEnclosingElement();
			if (hidertype == hiddentype || !hasSuperType(hidertype, hiddentype)) {
				return false;
			}

			return true;
		}

		@Override
		public Boolean visitTypeParameter(TypeParameterElement hider, Element hidden) {
			switch (hidden.getKind()) {
				case TYPE_PARAMETER: {
					//only hides, if the hider is a function type parameter, and hidden is a class type parameter
					Element hiderenc = hider.getEnclosingElement();
					ElementKind hiderkind = hiderenc.getKind();
					if (hiderkind != ElementKind.METHOD && hiderkind != ElementKind.CONSTRUCTOR) {
						//hider kind must be a function
						return false;
					}
					//the enclosing of the function must be the same as the enclosing of the hidden
					Element hiddenenc = hidden.getEnclosingElement();
					if (hiddenenc != hiderenc.getEnclosingElement()) {
						return false;
					}
					return true;
				}
				case ANNOTATION_TYPE:
				case CLASS:
				case ENUM:
				case INTERFACE: {
					//note:
					//this doesnt return true
					//class SelfHide<SelfHide> { }

					//hides the types in the exact same scope
					//or types in super classes

					TypeElement hiddentype = (TypeElement) hidden;
					Element hiddenenclosing = hidden.getEnclosingElement();
					TypeElement hiderenclosingtype = getEnclosingTypeElement(hider);
					if (hiddenenclosing == hiderenclosingtype) {
						return true;
					}
					if (hiddentype.getNestingKind() == NestingKind.MEMBER
							&& hasSuperType(hiderenclosingtype, hiddenenclosing)) {
						return true;
					}
					return false;
				}
				default: {
					return false;
				}
			}
		}

		@Override
		public Boolean visitUnknown(Element e, Element p) {
			return false;
		}

	}

	public static TypeElement getEnclosingTypeElement(Element element) {
		element = element.getEnclosingElement();
		while (element != null) {
			ElementKind kind = element.getKind();
			if (kind.isClass() || kind.isInterface()) {
				break;
			}
			element = element.getEnclosingElement();
		}
		return (TypeElement) element;
	}

	public static Modifier getVisibilityOfModifiers(Collection<Modifier> modifiers) {
		if (modifiers.contains(Modifier.PUBLIC)) {
			return Modifier.PUBLIC;
		}
		if (modifiers.contains(Modifier.PRIVATE)) {
			return Modifier.PRIVATE;
		}
		if (modifiers.contains(Modifier.PROTECTED)) {
			return Modifier.PROTECTED;
		}
		return Modifier.DEFAULT;
	}

	public static Modifier getVisibilityOfElement(Element elem) {
		return getVisibilityOfModifiers(elem.getModifiers());
	}

	@Override
	public boolean hides(Element hider, Element hidden) {
		if (hider == hidden || !hider.getSimpleName().contentEquals(hidden.getSimpleName())) {
			//same element dont hide itself
			//different names cant hide each other
			return false;
		}
		switch (getVisibilityOfElement(hidden)) {
			case PROTECTED:
			case PUBLIC: {
				break;
			}
			case PRIVATE: {
				//a private element cannot be hidden as it is not visible
				//even if it would be visible e.g. from an inner extending class
				return false;
			}
			case DEFAULT: {
				//make sure the hider and the hidden are in the same package
				if (getPackageOf(hider) != getPackageOf(hidden)) {
					return false;
				}
				break;
			}
			default: {
				break;
			}
		}

		return hider.accept(elementHiderVisitor, hidden);
	}

	boolean equalsOrHasSuperType(TypeElement subject, TypeElement base) {
		if (subject == base) {
			return true;
		}
		return hasSuperType(subject, base);
	}

	@Override
	public boolean hasSuperType(TypeElement subject, Element base) {
		switch (base.getKind()) {
			case ANNOTATION_TYPE:
			case INTERFACE: {
				for (TypeMirror itf : subject.getInterfaces()) {
					DeclaredType dtitf = (DeclaredType) itf;
					TypeElement itfelem = (TypeElement) dtitf.asElement();
					if (itfelem != null && (itfelem == base || hasSuperType(itfelem, base))) {
						return true;
					}
				}
				TypeElement superclass = getSuperClassOf(subject);
				if (superclass != null && hasSuperType(superclass, base)) {
					return true;
				}
				break;
			}
			case CLASS: {
				if (subject.getKind().isInterface()) {
					//no need to examine interfaces and superclasses of java.lang.Object as it has none of them
					return javaLangObjectElement == base;
				}
				TypeElement superclass = getSuperClassOf(subject);
				if (superclass != null && (superclass == base || hasSuperType(superclass, base))) {
					return true;
				}
				break;
			}
			case ENUM: {
				//we are looking for an enum super type
				//an enum cannot be extended, so there is no way to have an enum super type
				return false;
			}
			default:
				break;
		}
		return false;
	}

	private TypeElement getEnclosingElementInHierarchy(TypeElement type, Element element) {
		Element expectedenclosing = element.getEnclosingElement();
		return type == expectedenclosing || hasSuperType(type, expectedenclosing) ? (TypeElement) expectedenclosing
				: null;
	}

	private boolean hasElementInHierarchy(TypeElement type, Element element) {
		Element enclosing = element.getEnclosingElement();
		return type == enclosing || hasSuperType(type, enclosing);
	}

	private class IsOverrideCompatibleArgumentsVisitor implements DefaultedTypeVisitor<Boolean, TypeMirror> {
		private Map<TypeParameterElement, TypeMirror> paramMirrors;

		public IsOverrideCompatibleArgumentsVisitor(Map<TypeParameterElement, TypeMirror> parammirrors) {
			this.paramMirrors = parammirrors;
		}

		private TypeMirror deTypeVariablize(TypeMirror p) {
			if (p.getKind() == TypeKind.TYPEVAR) {
				TypeMirror got = paramMirrors.get(((TypeVariable) p).asElement());
				if (got != null) {
					return got;
				}
			}
			return p;
		}

		@Override
		public Boolean visitPrimitive(PrimitiveType t, TypeMirror p) {
			return t.getKind() == p.getKind();
		}

		@Override
		public Boolean visitNull(NullType t, TypeMirror p) {
			return false;
		}

		@Override
		public Boolean visitArray(ArrayType t, TypeMirror p) {
			p = deTypeVariablize(p);
			if (p.getKind() != TypeKind.ARRAY) {
				return false;
			}
			return t.getComponentType().accept(this, ((ArrayType) p).getComponentType());
		}

		@Override
		public Boolean visitDeclared(DeclaredType t, TypeMirror p) {
			p = deTypeVariablize(p);
			return isSameType(erasure(t), erasure(p));
		}

		@Override
		public Boolean visitError(ErrorType t, TypeMirror p) {
			return false;
		}

		@Override
		public Boolean visitTypeVariable(TypeVariable t, TypeMirror p) {
			TypeMirror substitute = paramMirrors.get(t.asElement());
			if (substitute != null && substitute != t) {
				return substitute.accept(this, p);
			}
			return erasure(t).accept(this, p);
		}

		@Override
		public Boolean visitWildcard(WildcardType t, TypeMirror p) {
			//wildcards cannot be parameters
			return false;
		}

		@Override
		public Boolean visitExecutable(ExecutableType t, TypeMirror p) {
			return false;
		}

		@Override
		public Boolean visitNoType(NoType t, TypeMirror p) {
			return false;
		}

		@Override
		public Boolean visitIntersection(IntersectionType t, TypeMirror p) {
			return erasure(t).accept(this, p);
		}

		@Override
		public Boolean visitUnion(UnionType t, TypeMirror p) {
			//union as parameter type?
			return false;
		}

	}

	private boolean isOverrideParamsSubSignature(ExecutableElement m1, ExecutableElement m2,
			Map<TypeParameterElement, TypeMirror> parammirrors) {
		IsOverrideCompatibleArgumentsVisitor visitor = new IsOverrideCompatibleArgumentsVisitor(parammirrors);
		if (!ObjectUtils.collectionOrderedEquals(m1.getParameters(), m2.getParameters(),
				(l, r) -> l.asType().accept(visitor, r.asType()))) {
			return false;
		}
		return true;
	}

	private boolean overrides(ExecutableElement overrider, ExecutableElement overridden, TypeElement type,
			Map<TypeParameterElement, TypeMirror> parammirrors) {
		if (overrider == overridden) {
			return false;
		}
		ElementKind kind = overrider.getKind();
		if (kind != overridden.getKind() || kind != ElementKind.METHOD) {
			return false;
		}
		Name methodname = overrider.getSimpleName();
		if (!methodname.contentEquals(overridden.getSimpleName())) {
			return false;
		}
		Set<Modifier> overriddenmodifiers = overridden.getModifiers();
		if (overriddenmodifiers.contains(Modifier.STATIC) || overriddenmodifiers.contains(Modifier.PRIVATE)) {
			return false;
		}
		Set<Modifier> overridermodifiers = overrider.getModifiers();
		if (overridermodifiers.contains(Modifier.STATIC) || overridermodifiers.contains(Modifier.PRIVATE)) {
			return false;
		}
		if (!hasElementInHierarchy(type, overridden)) {
			return false;
		}
		TypeElement overriderenclosing = (TypeElement) overrider.getEnclosingElement();
		TypeElement overriddenenclosing = (TypeElement) overridden.getEnclosingElement();
		if (overriddenenclosing == overriderenclosing) {
			return false;
		}

		if (hasSuperType(overriderenclosing, overriddenenclosing)) {
			//if overrider method declaring class is already a subclass of the overridden method declaring class

			//continue method
			if (parammirrors == null) {
				parammirrors = getTypeParameterMapResolve(overriderenclosing);
			}
		} else if (hasSuperType(type, overriderenclosing) && hasSuperType(type, overriddenenclosing)) {
			//if the overrider method and overridden method declaring classes have no relation
			//they must intersect in the type parameter

			if (overridermodifiers.contains(Modifier.ABSTRACT)) {
				return false;
			}

			if (hasSuperType(overriddenenclosing, overriderenclosing)) {
				//if the overriden has a supertype of overrider
				return false;
			}

			if (parammirrors == null) {
				parammirrors = getTypeParameterMapResolve(type);
			}

			for (Element enclosed : type.getEnclosedElements()) {
				if (enclosed.getKind() != ElementKind.METHOD) {
					continue;
				}
				if (!enclosed.getSimpleName().contentEquals(methodname)) {
					continue;
				}
				Set<Modifier> enclosedmodifiers = enclosed.getModifiers();
				if (enclosedmodifiers.contains(Modifier.STATIC)) {
					continue;
				}
				if (isOverrideParamsSubSignature((ExecutableElement) enclosed, overridden, parammirrors)) {
					//found a method with the same signature in type

					//overrider doesnt override overridden, as there is a method that overrides it
					return false;
				}
			}

			//continue method
		} else {
			return false;
		}
		if (!isOverrideParamsSubSignature(overrider, overridden, parammirrors)) {
			return false;
		}
		return true;
	}

	@Override
	public boolean overrides(ExecutableElement overrider, ExecutableElement overridden, TypeElement type) {
		//pass null as type parameter map, it is lazily populated if the preconditions are true
		return overrides(overrider, overridden, type, null);
	}

	@Override
	public String getConstantExpression(Object value) {
		//we can use the real elements here
		return getConstantExpressionStatic(value);
	}

	public static String getConstantExpressionStatic(Object value) {
		if (value instanceof Integer || value instanceof Boolean) {
			return value.toString();
		}
		if (value instanceof Byte) {
			byte b = (byte) value;
			return getConstantExpression(b);
		}
		if (value instanceof Short) {
			short s = (short) value;
			return getConstantExpression(s);
		}
		if (value instanceof Long) {
			long l = (long) value;
			return getConstantExpression(l);
		}
		if (value instanceof Float) {
			float f = (Float) value;
			return getConstantExpression(f);
		}
		if (value instanceof Double) {
			double d = (Double) value;
			return getConstantExpression(d);
		}
		if (value instanceof Character) {
			return getConstantExpression((char) value);
		}
		if (value instanceof String) {
			return getConstantExpression((String) value);
		}
		throw new IllegalArgumentException(
				"Invalid constant expression object class: " + (value == null ? "null" : value.getClass()));
	}

	public static String getConstantExpression(boolean b) {
		return Boolean.toString(b);
	}

	public static String getConstantExpression(int i) {
		return Integer.toString(i);
	}

	public static String getConstantExpression(byte b) {
		return "(byte) 0x" + Integer.toHexString(b & 0xFF);
	}

	public static String getConstantExpression(short s) {
		return "(short) " + s;
	}

	public static String getConstantExpression(long l) {
		return l + "L";
	}

	public static String getConstantExpression(float f) {
		if (Float.isNaN(f)) {
			return "0.0f / 0.0f";
		}
		if (Float.isInfinite(f)) {
			return (f < 0) ? "-1.0f / 0.0f" : "1.0f / 0.0f";
		}
		return f + "f";
	}

	public static String getConstantExpression(double d) {
		if (Double.isNaN(d)) {
			return "0.0 / 0.0";
		}
		if (Double.isInfinite(d)) {
			return (d < 0) ? "-1.0 / 0.0" : "1.0 / 0.0";
		}
		return Double.toString(d);
	}

	public static String getConstantExpression(char c) {
		return "\'" + toEscapedCharacter(c) + "\'";
	}

	public static String getConstantExpression(String s) {
		int slen = s.length();

		StringBuilder buf = new StringBuilder(slen * 2);
		buf.append('\"');
		for (int i = 0; i < slen; i++) {
			buf.append(toEscapedCharacter(s.charAt(i)));
		}
		buf.append('\"');
		return buf.toString();
	}

	private static String toEscapedCharacter(char ch) {
		if (ch >= 32 && ch <= 126) {
			switch (ch) {
				case '\'': {
					return "\\'";
				}
				case '\"': {
					return "\\\"";
				}
				case '\\': {
					return "\\\\";
				}
				default: {
					return String.valueOf(ch);
				}
			}
		}
		switch (ch) {
			case '\b': {
				return "\\b";
			}
			case '\f': {
				return "\\f";
			}
			case '\n': {
				return "\\n";
			}
			case '\r': {
				return "\\r";
			}
			case '\t': {
				return "\\t";
			}
			default: {
				return String.format("\\u%04x", (int) ch);
			}
		}
	}

	@Override
	public void printElements(Writer w, Element... elements) {
		String lineseparator = System.lineSeparator();
		try {
			for (Element element : elements) {
				w.write(Objects.toString(element));
				w.write(lineseparator);
			}
		} catch (IOException e) {
		}
	}

	@Override
	public Name getName(CharSequence cs) {
		return new IncrementalName(cs.toString());
	}

	private static boolean isJavaLangObjectFunction(ExecutableElement elem) {
		Name name = elem.getSimpleName();
		if (name.contentEquals("hashCode")) {
			return elem.getParameters().isEmpty();
		}
		if (name.contentEquals("toString")) {
			return elem.getParameters().isEmpty();
		}
		if (name.contentEquals("equals")) {
			List<? extends VariableElement> params = elem.getParameters();
			if (params.size() != 1) {
				return false;
			}
			TypeMirror paramtype = params.get(0).asType();
			if (paramtype.getKind() != TypeKind.DECLARED) {
				return false;
			}
			DeclaredType dtp = (DeclaredType) paramtype;
			TypeElement paramelem = (TypeElement) dtp.asElement();
			if (paramelem.getQualifiedName().contentEquals(JAVA_LANG_OBJECT)) {
				return true;
			}
			return false;
		}
		return false;
	}

//	private static boolean hasNoOrOneAbstractMethodForFunctionalInterface(TypeElement type) {
//		ExecutableElement singleelem = null;
//		for (Element e : type.getEnclosedElements()) {
//			if (e.getKind() != ElementKind.METHOD) {
//				continue;
//			}
//			Set<Modifier> mods = e.getModifiers();
//			if (mods.contains(Modifier.STATIC) || !mods.contains(Modifier.PUBLIC) || !mods.contains(Modifier.ABSTRACT)) {
//				continue;
//			}
//			//e is an abstract public non static method
//			ExecutableElement ee = (ExecutableElement) e;
//			if (ee.isDefault() || isJavaLangObjectFunction(ee)) {
//				continue;
//			}
//			if (singleelem != null) {
//				//more than one abstract methods
//				return false;
//			}
//			singleelem = ee;
//		}
//		return true;
//	}

	private void collectLeafInstanceMethodsInInterfaceForFunctionalInterfaceImpl(TypeElement itf,
			Map<TypeParameterElement, TypeMirror> parammirrors, Map<String, List<ExecutableElement>> result,
			Set<TypeElement> addedinterfaces) {
		if (!addedinterfaces.add(itf)) {
			return;
		}
		for (TypeMirror itftm : itf.getInterfaces()) {
			DeclaredType dtitf = (DeclaredType) itftm;
			TypeElement superitfelem = (TypeElement) dtitf.asElement();
			if (superitfelem != null) {
				collectLeafInstanceMethodsInInterfaceForFunctionalInterfaceImpl(superitfelem, parammirrors, result,
						addedinterfaces);
			}
		}
		for (Element e : itf.getEnclosedElements()) {
			if (e.getKind() != ElementKind.METHOD) {
				continue;
			}
			Set<Modifier> mods = e.getModifiers();
			if (mods.contains(Modifier.STATIC) || !mods.contains(Modifier.PUBLIC)) {
				continue;
			}
			//e is an abstract public non static method
			ExecutableElement ee = (ExecutableElement) e;
			if (isJavaLangObjectFunction(ee)) {
				continue;
			}

			List<ExecutableElement> execcoll = result.computeIfAbsent(ee.getSimpleName().toString(),
					Functionals.arrayListComputer());
			for (Iterator<ExecutableElement> it = execcoll.iterator(); it.hasNext();) {
				ExecutableElement presentee = it.next();
				if (isOverrideParamsSubSignature(ee, presentee, parammirrors)) {
					//an override compatible method is already present
					it.remove();
				}
			}
			execcoll.add(ee);
		}
	}

	private Map<String, List<ExecutableElement>> collectLeafInstanceMethodsInInterfaceForFunctionalInterface(
			TypeElement itf, Map<TypeParameterElement, TypeMirror> parammirrors) {
		Map<String, List<ExecutableElement>> result = new TreeMap<>();
		collectLeafInstanceMethodsInInterfaceForFunctionalInterfaceImpl(itf, parammirrors, result, new HashSet<>());
		return result;
	}

	@Override
	public boolean isFunctionalInterface(TypeElement type) {
		if (type == null || type.getKind() != ElementKind.INTERFACE) {
			return false;
		}
		Map<TypeParameterElement, TypeMirror> typeparameters = getTypeParameterMapResolve(type);
		Map<String, List<ExecutableElement>> leafmethods = collectLeafInstanceMethodsInInterfaceForFunctionalInterface(
				type, typeparameters);
		if (leafmethods.isEmpty()) {
			return false;
		}
		int abstractcount = 0;
		for (List<ExecutableElement> methods : leafmethods.values()) {
			for (ExecutableElement m : methods) {
				if (!m.isDefault()) {
					abstractcount++;
					if (abstractcount > 1) {
						return false;
					}
				}
			}
		}
		return abstractcount == 1;
	}

//	
//	Elements functions end
//	

//	
//	Types functions start
//	

	@Override
	public Element asElement(TypeMirror t) {
		return ((CommonTypeMirror) t).asElement();
	}

	@SuppressWarnings("unchecked")
	private boolean isAnnotationValueSame(AnnotationValue v1, AnnotationValue v2) {
		if (v1 == v2) {
			return true;
		}
		Object eval1 = v1.getValue();
		Object eval2 = v2.getValue();
		if (eval1 == null || eval2 == null || eval1.getClass() != eval2.getClass()) {
			//return true if both null
			//can any of them be null? in case of errorneous code?
			return eval1 == eval2;
		}
		if (eval1 instanceof TypeMirror) {
			if (!isSameType((TypeMirror) eval1, (TypeMirror) eval2)) {
				return false;
			}
		} else if (eval1 instanceof VariableElement) {
			VariableElement ve1 = (VariableElement) eval1;
			VariableElement ve2 = (VariableElement) eval2;
			if (!ve1.getSimpleName().contentEquals(ve2.getSimpleName())) {
				return false;
			}
			//do not need to check for enum type, as they must be the same as the method return type
		} else if (eval1 instanceof AnnotationMirror) {
			if (!isAnnotationMirrorSame((AnnotationMirror) eval1, (AnnotationMirror) eval2)) {
				return false;
			}
		} else if (eval1 instanceof List) {
			List<? extends AnnotationValue> l1 = (List<? extends AnnotationValue>) eval1;
			List<? extends AnnotationValue> l2 = (List<? extends AnnotationValue>) eval2;
			if (!ObjectUtils.collectionOrderedEquals(l1, l2, this::isAnnotationValueSame)) {
				return false;
			}
		} else if (!eval1.equals(eval2)) {
			//wrapper Number class for primitive or String
			return false;
		}
		return true;
	}

	private boolean isAnnotationMirrorSame(AnnotationMirror n1, AnnotationMirror n2) {
		if (n1 == n2) {
			return true;
		}
		if (!isSameType(n1.getAnnotationType(), n2.getAnnotationType())) {
			return false;
		}
		Map<? extends ExecutableElement, ? extends AnnotationValue> val1 = n1.getElementValues();
		Map<? extends ExecutableElement, ? extends AnnotationValue> val2 = n2.getElementValues();
		if (val1.size() != val2.size()) {
			return false;
		}

		outer:
		for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : val1.entrySet()) {
			Name methodname = entry.getKey().getSimpleName();
			for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry2 : val2.entrySet()) {
				if (entry2.getKey().getSimpleName().contentEquals(methodname)) {
					if (!isAnnotationValueSame(entry.getValue(), entry2.getValue())) {
						return false;
					}
					continue outer;
				}
			}
			//no value found in other annotation with same executable name
			return false;
		}
		return true;
	}

	private boolean isSameCorrespondingTypes(List<? extends TypeMirror> list1, List<? extends TypeMirror> list2,
			boolean ignorewildcarddifference) {
		return ObjectUtils.collectionOrderedEquals(list1, list2, (l, r) -> isSameType(l, r, ignorewildcarddifference));
	}

	private boolean isSameTypeVariableBounds(TypeVariable tv1, TypeVariable tv2, boolean ignorewildcarddifference) {
		if (!isSameType(tv1.getUpperBound(), tv2.getUpperBound(), ignorewildcarddifference)) {
			return false;
		}
		if (!isSameType(tv1.getLowerBound(), tv2.getLowerBound(), ignorewildcarddifference)) {
			return false;
		}
		return true;
	}

	private boolean isWildcardSuperObjectSameTypeImpl(WildcardType wc, TypeMirror t2) {
		CommonWildcardType cwt = (CommonWildcardType) wc;
		List<? extends TypeMirror> bounds = cwt.getCorrespondingTypeParameter().getBounds();
		if (bounds.size() != 1) {
			return false;
		}
		TypeMirror objtm = bounds.get(0);
		return isSameType(wc.getSuperBound(), objtm) && isSameType(t2, objtm);
	}

	private boolean isWildcardSuperObjectSameType(TypeMirror t1, TypeMirror t2) {
		if (t1.getKind() == TypeKind.WILDCARD) {
			return isWildcardSuperObjectSameTypeImpl((WildcardType) t1, t2);
		}
		if (t2.getKind() == TypeKind.WILDCARD) {
			return isWildcardSuperObjectSameTypeImpl((WildcardType) t2, t1);
		}
		return false;
	}

	private boolean isSameType(TypeMirror t1, TypeMirror t2, boolean ignorewildcarddifference,
			Map<TypeParameterElement, TypeParameterElement> typeparametersubstitutions) {
		if (t1 == null || t2 == null) {
			//return true if both are null
			return t1 == t2;
		}
		TypeKind kind = t1.getKind();
		TypeKind t2kind = t2.getKind();

		if (!ignorewildcarddifference) {
			if (kind == TypeKind.WILDCARD) {
				//wildcards are never the same type
				return false;
			}
		} else {
			//ignore wildcard differences
			//X and "? super X" is considered the same if the wildcarded type parameter bounds is T extends X
			if (isWildcardSuperObjectSameType(t1, t2)) {
				return true;
			}
		}
		if (kind != t2kind) {
			//not same type kinds
			return false;
		}
		//annotations doesnt need to be checked per documentation
		//XXX visitorize ?
		switch (kind) {
			case BYTE:
			case SHORT:
			case INT:
			case LONG:
			case FLOAT:
			case DOUBLE:
			case BOOLEAN:
			case CHAR:
			case NONE:
			case NULL:
			case VOID: {
				break;
			}
			case ARRAY: {
				ArrayType at1 = (ArrayType) t1;
				ArrayType at2 = (ArrayType) t2;
				if (!isSameType(at1.getComponentType(), at2.getComponentType(), true)) {
					return false;
				}
				break;
			}
			case DECLARED: {
				DeclaredType dt1 = (DeclaredType) t1;
				DeclaredType dt2 = (DeclaredType) t2;

				if (!isSameCorrespondingTypes(dt1.getTypeArguments(), dt2.getTypeArguments(), true)) {
					return false;
				}
				if (!isSameType(dt1.getEnclosingType(), dt2.getEnclosingType(), true)) {
					return false;
				}
				TypeElement te1 = (TypeElement) dt1.asElement();
				TypeElement te2 = (TypeElement) dt2.asElement();
				if (te1 != te2) {
					return false;
				}
				break;
			}
			case PACKAGE: {
				//javac uses == to determine same package
				CommonPackageType nt1 = (CommonPackageType) t1;
				CommonPackageType nt2 = (CommonPackageType) t2;
				if (nt1.getPackageElement() != nt2.getPackageElement()) {
					return false;
				}
				break;
			}
			case EXECUTABLE: {
				ExecutableType et1 = (ExecutableType) t1;
				ExecutableType et2 = (ExecutableType) t2;

				//the type variable names doesnt matter for executables 
				if (!ObjectUtils.collectionOrderedEquals(et1.getTypeVariables(), et2.getTypeVariables(),
						(l, r) -> isSameTypeVariableBounds(l, r, true))) {
					return false;
				}
				if (!isSameType(et1.getReturnType(), et2.getReturnType(), true)) {
					return false;
				}
				if (!isSameCorrespondingTypes(et1.getParameterTypes(), et2.getParameterTypes(), true)) {
					return false;
				}
				//receiver types doesnt matter
				//javac doesnt take thrown types into account
				//name doesnt need to be checked, javac doesnt care either
				break;
			}
			case TYPEVAR: {
				//make sure the two variables have the same bounds

				TypeVariable tv1 = (TypeVariable) t1;
				TypeVariable tv2 = (TypeVariable) t2;

				TypeParameterElement tpe1 = (TypeParameterElement) tv1.asElement();
				TypeParameterElement tpe2 = (TypeParameterElement) tv2.asElement();
				Element elem1 = typeparametersubstitutions.getOrDefault(tpe1, tpe1);
				Element elem2 = typeparametersubstitutions.getOrDefault(tpe2, tpe2);
				if (elem1 != elem2) {
					return false;
				}
//				if (!isSameTypeVariableBounds(tv1, tv2, true)) {
//					return false;
//				}
				break;
			}
			case INTERSECTION: {
				IntersectionType it1 = (IntersectionType) t1;
				IntersectionType it2 = (IntersectionType) t2;
				if (!isSameCorrespondingTypes(it1.getBounds(), it2.getBounds(), true)) {
					return false;
				}
				break;
			}
			case UNION: {
				UnionType ut1 = (UnionType) t1;
				UnionType ut2 = (UnionType) t2;
				if (!isSameCorrespondingTypes(ut1.getAlternatives(), ut2.getAlternatives(), true)) {
					return false;
				}
				break;
			}
			case WILDCARD: {
				WildcardType wc1 = (WildcardType) t1;
				WildcardType wc2 = (WildcardType) t2;
				TypeMirror sup1 = wc1.getSuperBound();
				TypeMirror sup2 = wc2.getSuperBound();
				if (!isSameType(sup1, sup2, true)) {
					return false;
				}
				TypeMirror ext1 = wc1.getExtendsBound();
				TypeMirror ext2 = wc2.getExtendsBound();
				if (!isSameType(ext1, ext2, true)) {
					//"?" and "? extends Object" is the same
					TypeMirror testagainst = ext1 == null ? ext2 : (ext2 == null ? ext1 : null);
					if (!isSameType(getJavaLangObjectTypeMirror(), testagainst, true)) {
						return false;
					}
				}
				break;
			}
			case ERROR:
			case OTHER:
				return false;
			default: {
				return isSameTypeUnknownKind(kind, t1, t2);
			}
		}
		return true;
	}

	/**
	 * Compares the parameter TypeMirrors in the same manner as {@link #isSameType(TypeMirror, TypeMirror)}. This method
	 * is for forward compatibility.
	 * 
	 * @param kind
	 *            The kind of both of the types.
	 * @param t1
	 *            The first type.
	 * @param t2
	 *            The second type.
	 * @return
	 */
	protected boolean isSameTypeUnknownKind(TypeKind kind, TypeMirror t1, TypeMirror t2) {
		return false;
	}

	public boolean isSameType(TypeMirror t1, TypeMirror t2, boolean ignorewildcarddifference) {
		return isSameType(t1, t2, ignorewildcarddifference, Collections.emptyMap());
	}

	@Override
	public boolean isSameType(TypeMirror t1, TypeMirror t2) {
		return isSameType(t1, t2, false);
	}

	@Override
	public boolean isSubtype(TypeMirror t1, TypeMirror t2) {
		if (t1 == t2) {
			return true;
		}
		return t1.accept(subTypeVisitor, t2);
	}

	@Override
	public boolean isAssignable(TypeMirror t1, TypeMirror t2) {
		if (t1 == t2) {
			return true;
		}
		return t1.accept(assignableTypeVisitor, t2);
	}

	@Override
	public boolean contains(TypeMirror t1, TypeMirror t2) {
		if (isSameType(t1, t2, true)) {
			return true;
		}
		if (t1.getKind() != TypeKind.WILDCARD) {
			return false;
		}
		return containsTypeVisitor.contains((WildcardType) t1, t2);
	}

	private boolean isSameSignature(ExecutableType m1, ExecutableType m2,
			Map<TypeParameterElement, TypeParameterElement> parammmap) {
		//Two methods or constructors, M and N, have the same signature if they have the same name,

		//the same type parameters (if any) (§8.4.4), and,  
		if (!ObjectUtils.collectionOrderedEquals(m1.getTypeVariables(), m2.getTypeVariables(), (n1, n2) -> {
			//we dont want to check if the variables correspont to the same elements
			return isSameTypeVariableBounds(n1, n2, true);
		})) {
			return false;
		}
		//after adapting the formal parameter types of N to the the type parameters of M, the same formal parameter types.
		if (!ObjectUtils.collectionOrderedEquals(m1.getParameterTypes(), m2.getParameterTypes(), (n1, n2) -> {
			return isSameType(n1, n2, true, parammmap);
		})) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isSubsignature(ExecutableType m1, ExecutableType m2) {
		//Don't need to check the name. Javac doesnt check it
		if (m1 == m2) {
			return true;
		}

		Map<TypeParameterElement, TypeParameterElement> parammap = new IdentityHashMap<>();

		List<? extends TypeVariable> vars1 = m1.getTypeVariables();
		List<? extends TypeVariable> vars2 = m2.getTypeVariables();
		Iterator<? extends TypeVariable> it2 = vars2.iterator();
		for (TypeVariable tv1 : vars1) {
			if (!it2.hasNext()) {
				break;
			}
			parammap.put((TypeParameterElement) tv1.asElement(), (TypeParameterElement) it2.next().asElement());
		}

		//The signature of a method m1 is a subsignature of the signature of a method m2 if either:

		//m2 has the same signature as m1, or
		if (isSameSignature(m1, m2, parammap)) {
			return true;
		}

		//the signature of m1 is the same as the erasure (§4.6) of the signature of m2.
		if (isSameSignature(m1, erasure(m2), parammap)) {
			return true;
		}

		return false;
	}

	private TypeMirror getComponentTypeForTypeVariableUpperBound(TypeMirror upperbound) {
		if (upperbound.getKind() == TypeKind.INTERSECTION) {
			//if all of the bounds are interfaces, then object should be the component type
			//if there is a class type for a bound, then that class should be the component type
			//the class type must be the first parameter in an intersection

			IntersectionType itt = (IntersectionType) upperbound;
			TypeMirror firstbound = itt.getBounds().get(0);
			if (firstbound.getKind() == TypeKind.DECLARED) {
				return getComponentTypeForTypeVariableUpperBound(firstbound);
			}
		} else if (upperbound.getKind() == TypeKind.DECLARED) {
			//if a simple declared type is the bound of the type variable
			//if it is an interface then object should be the component type
			DeclaredType fbdt = (DeclaredType) upperbound;
			TypeElement fbelem = (TypeElement) fbdt.asElement();
			if (fbelem.getKind().isClass()) {
				return upperbound;
			}
			return getJavaLangObjectTypeMirror();
		}
		return upperbound;
	}

	@Deprecated
	private Map<TypeParameterElement, TypeMirror> getTypeParameterMapForDeclaredType(DeclaredType type,
			TypeElement elem) {
		if (isRaw(type, elem)) {
			//raw type
			return Collections.emptyMap();
		}
		IdentityHashMap<TypeParameterElement, TypeMirror> result = new IdentityHashMap<>();
		foreachSuperClassMirror(type, elem, (dt, t) -> {
			putTypeArgumentMirrorsResolveOptionalWithEnclosing(t, dt, result);
		});
		return result;
	}

	public static boolean hasAnyTypeArguments(DeclaredType type) {
		TypeMirror enclosing = type.getEnclosingType();
		if (enclosing.getKind() == TypeKind.DECLARED && hasAnyTypeArguments((DeclaredType) enclosing)) {
			return true;
		}
		return !type.getTypeArguments().isEmpty();
	}

	private static boolean hasAnyWildcardTypeArguments(DeclaredType type) {
		TypeMirror enclosing = type.getEnclosingType();
		if (enclosing.getKind() == TypeKind.DECLARED && hasAnyWildcardTypeArguments((DeclaredType) enclosing)) {
			return true;
		}
		List<? extends TypeMirror> args = type.getTypeArguments();
		if (args.isEmpty()) {
			return false;
		}
		for (TypeMirror a : args) {
			if (a.getKind() == TypeKind.WILDCARD) {
				return true;
			}
		}
		return false;
	}

	private static boolean isRaw(DeclaredType type) {
		return isRaw(type, (TypeElement) type.asElement());
	}

	private static boolean isRaw(DeclaredType type, TypeElement elem) {
		List<? extends TypeParameterElement> tparams = elem.getTypeParameters();
		List<? extends TypeMirror> dtparams = type.getTypeArguments();
		if (tparams.size() != dtparams.size()) {
			return true;
		}
		TypeMirror enclosing = type.getEnclosingType();
		if (enclosing.getKind() == TypeKind.DECLARED) {
			DeclaredType edt = (DeclaredType) enclosing;
			if (isRaw(edt)) {
				return true;
			}
		}
		return false;
	}

	private DeclaredType getCorrectParameterizedType(DeclaredType type,
			Map<TypeParameterElement, TypeMirror> typeparameters) {
		TypeElement elem = (TypeElement) type.asElement();
		List<? extends TypeParameterElement> tparams = elem.getTypeParameters();
		List<? extends TypeMirror> dtparams = type.getTypeArguments();
		if (dtparams.isEmpty() && !tparams.isEmpty()) {
			//type is raw
			return type;
		}
		TypeMirror[] args = new TypeMirror[tparams.size()];
		for (int i = 0; i < args.length; i++) {
			TypeMirror corresponding = typeparameters.get(tparams.get(i));
			if (corresponding == null) {
				//type parameter value not found
				//return raw type
				return (DeclaredType) erasure(type);
			}
			args[i] = corresponding;
		}
		TypeMirror enclosing = type.getEnclosingType();
		if (enclosing.getKind() == TypeKind.DECLARED) {
			return new SimpleDeclaredType(this, getCorrectParameterizedType((DeclaredType) enclosing, typeparameters),
					elem, ImmutableUtils.asUnmodifiableArrayList(args));
		}
		return new SimpleDeclaredType(this, IncrementalNoType.INSTANCE_NONE, elem,
				ImmutableUtils.asUnmodifiableArrayList(args));
	}

	private static boolean isInterfaceElementKind(DeclaredType dt) {
		Element elem = dt.asElement();
		if (elem == null) {
			return false;
		}
		return elem.getKind().isInterface();
	}

	@Override
	public List<? extends TypeMirror> directSupertypes(TypeMirror t) {
		switch (t.getKind()) {
			case BOOLEAN:
			case BYTE:
			case CHAR:
			case DOUBLE:
			case FLOAT:
			case INT:
			case LONG:
			case SHORT:
			case VOID:
			case NONE:
			case NULL:
			case ERROR:
			case OTHER:
				return Collections.emptyList();

			case ARRAY: {
				ArrayType at = (ArrayType) t;
				TypeMirror component = at.getComponentType();
				TypeKind ckind = component.getKind();
				if (ckind.isPrimitive()) {
					if (primitiveArrayDirectSuperTypes == null) {
						primitiveArrayDirectSuperTypes = Collections
								.singletonList(createIntersectionType(ImmutableUtils.asUnmodifiableArrayList(
										getJavaIoSerializableTypeMirror(), getJavaLangCloneableTypeMirror())));
					}
					return primitiveArrayDirectSuperTypes;
				}
				switch (ckind) {
					case DECLARED: {
						DeclaredType cdt = (DeclaredType) component;
						TypeElement compelem = (TypeElement) cdt.asElement();
						switch (compelem.getKind()) {
							case CLASS: {
								TypeMirror superclass = compelem.getSuperclass();
								if (superclass.getKind() == TypeKind.DECLARED) {
									Map<TypeParameterElement, TypeMirror> parammap = getTypeParameterMapForDeclaredType(
											cdt, compelem);
									DeclaredType corrected = getCorrectParameterizedType((DeclaredType) superclass,
											parammap);
									return ImmutableUtils.singletonList(getArrayType(corrected));
								}
								//can happen in case of Object[]
								return ImmutableUtils.singletonList(SimpleArrayType
										.erasured(IncrementalElementsTypes8.this, IncrementalNoType.INSTANCE_NONE));
							}
							case ENUM: {
								return ImmutableUtils.singletonList(
										getArrayType(new SimpleDeclaredType(this, IncrementalNoType.INSTANCE_NONE,
												javaLangEnumElement, ImmutableUtils.singletonList(component))));
							}
							case ANNOTATION_TYPE:
							case INTERFACE: {
								return ImmutableUtils.singletonList(SimpleArrayType
										.erasured(IncrementalElementsTypes8.this, getJavaLangObjectTypeMirror()));
							}
							default: {
								throw new IllegalArgumentException(
										"Unknown declared type element kind: " + compelem.getKind());
							}
						}
					}
					case TYPEVAR: {
						TypeVariable ctv = (TypeVariable) component;
						TypeMirror upperbound = ctv.getUpperBound();
						return Collections
								.singletonList(getArrayType(getComponentTypeForTypeVariableUpperBound(upperbound)));
					}
					default: {
						break;
					}
				}
				throw new IllegalArgumentException("Unknown component kind: " + ckind);
			}
			case DECLARED: {
				DeclaredType dt = (DeclaredType) t;
				TypeElement elem = (TypeElement) dt.asElement();
				if (elem == javaLangObjectElement) {
					return Collections.emptyList();
				}
				Map<TypeParameterElement, TypeMirror> parammap = getTypeParameterMapForDeclaredType(dt, elem);
				switch (elem.getKind()) {
					case CLASS: {
						List<? extends TypeMirror> itfs = elem.getInterfaces();
						//safe cast as only java.lang.Object has a NONE superclass
						//everything else has at least Object as superclass
						DeclaredType supertm = getCorrectParameterizedType((DeclaredType) elem.getSuperclass(),
								parammap);
						if (itfs.isEmpty()) {
							return ImmutableUtils.singletonList(supertm);
						}
						List<TypeMirror> result = new ArrayList<>(1 + itfs.size());
						result.add(supertm);
						for (TypeMirror i : itfs) {
							result.add(getCorrectParameterizedType((DeclaredType) i, parammap));
						}
						return result;
					}
					case ENUM: {
						List<? extends TypeMirror> itfs = elem.getInterfaces();
						DeclaredType enumtm = new SimpleDeclaredType(this, IncrementalNoType.INSTANCE_NONE,
								getJavaLangEnumTypeElement(), ImmutableUtils.singletonList(dt));
						if (itfs.isEmpty()) {
							return ImmutableUtils.singletonList(enumtm);
						}
						List<TypeMirror> result = new ArrayList<>(1 + itfs.size());
						result.add(enumtm);
						for (TypeMirror i : itfs) {
							result.add(getCorrectParameterizedType((DeclaredType) i, parammap));
						}
						return result;
					}
					case ANNOTATION_TYPE:
					case INTERFACE: {
						List<? extends TypeMirror> itfs = elem.getInterfaces();
						DeclaredType objtm = getJavaLangObjectTypeMirror();
						if (itfs.isEmpty()) {
							return ImmutableUtils.singletonList(objtm);
						}
						List<TypeMirror> result = new ArrayList<>(1 + itfs.size());
						result.add(objtm);
						for (TypeMirror i : itfs) {
							result.add(getCorrectParameterizedType((DeclaredType) i, parammap));
						}
						return result;
					}
					default: {
						throw new IllegalArgumentException("Unknown declared type element kind: " + elem.getKind());
					}
				}
			}
			case INTERSECTION: {
				IntersectionType it = (IntersectionType) t;
				return Collections.unmodifiableList(it.getBounds());
			}
			case TYPEVAR: {
				TypeVariable tv = (TypeVariable) t;
				TypeMirror upper = tv.getUpperBound();
				switch (upper.getKind()) {
					case DECLARED: {
						DeclaredType dt = (DeclaredType) upper;
						if (isInterfaceElementKind(dt)) {
							//if upper bound is just an interface, prepend with object
							return ImmutableUtils.asUnmodifiableArrayList(getJavaLangObjectTypeMirror(), upper);
						}
						break;
					}
					case INTERSECTION: {
						//flatten out the intersection type
						IntersectionType it = (IntersectionType) upper;
						List<? extends TypeMirror> bounds = it.getBounds();
						TypeMirror first = bounds.get(0);
						if (first.getKind() == TypeKind.DECLARED) {
							if (isInterfaceElementKind((DeclaredType) first)) {
								//if the intersection type is only interfaces (meaning no class prepends the declarations)
								//then prepend with object
								List<TypeMirror> result = new ArrayList<>(bounds.size() + 1);
								result.add(getJavaLangObjectTypeMirror());
								result.addAll(bounds);
								return result;
							}
						}
						return Collections.unmodifiableList(it.getBounds());
					}
					default: {
						break;
					}
				}
				return ImmutableUtils.singletonList(upper);
			}
			case UNION: {
				//XXX direct super types of UNION?
				break;
			}
			case WILDCARD: {
				break;
			}
			case PACKAGE:
			case EXECUTABLE:
				//case MODULE: //left out for compatibility
			default:
				throw new IllegalArgumentException("Invalid type mirror for directSupertypes");
		}
		return Collections.emptyList();
	}

	@Override
	public TypeMirror erasure(TypeMirror t) {
		return ((CommonTypeMirror) t).getErasedType();
	}

	@Override
	public List<TypeMirror> erasure(List<? extends TypeMirror> types) {
		List<TypeMirror> result = JavaTaskUtils.cloneImmutableList(types, this::erasure);
		return result;
	}

	public ExecutableType erasure(ExecutableType t) {
		return (ExecutableType) erasure((TypeMirror) t);
	}

	public static boolean needsErasure(ExecutableType et) {
		if (!et.getTypeVariables().isEmpty()) {
			return true;
		}
		if (hasAnyErasureType(et.getReturnType())) {
			return true;
		}
		if (hasAnyErasureType(et.getParameterTypes())) {
			return true;
		}
		if (hasAnyErasureType(et.getThrownTypes())) {
			return true;
		}
		return false;
	}

	public static boolean hasAnyErasureType(TypeMirror type) {
		TypeKind tkind = type.getKind();
		switch (tkind) {
			case UNION:
			case INTERSECTION:
			case TYPEVAR: {
				return true;
			}
			case DECLARED: {
				return hasAnyTypeArguments((DeclaredType) type);
			}
			case ARRAY: {
				//primitive array doesnt trigger erasure
				ArrayType at = (ArrayType) type;
				return hasAnyErasureType(at.getComponentType());
			}
			default: {
				break;
			}
		}
		return false;
	}

	public static boolean hasAnyErasureType(Collection<? extends TypeMirror> types) {
		if (types.isEmpty()) {
			return false;
		}
		for (TypeMirror t : types) {
			if (hasAnyErasureType(t)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public TypeMirror erasureImpl(TypeMirror t) {
		return erasureVisitor.erasure(t);
	}

	@Override
	public TypeElement boxedClass(PrimitiveType p) {
		String got = PRIMITIVE_TO_BOXED_TYPE_NAMES.get(p.getKind());
		if (got != null) {
			return getTypeElement(got);
		}
		throw new IllegalArgumentException(p.getKind() + " - " + p.toString());
	}

	@Override
	public PrimitiveType unboxedType(TypeMirror t) {
		if (t.getKind() != TypeKind.DECLARED) {
			throw new IllegalArgumentException("Invalid kind: " + t.getKind() + ": " + t);
		}
		DeclaredType dt = (DeclaredType) t;
		TypeElement elem = (TypeElement) dt.asElement();
		String qname = elem.getQualifiedName().toString();
		PrimitiveType unboxed = UNBOXED_TYPES.get(qname);
		if (unboxed != null) {
			return unboxed;
		}
		throw new IllegalArgumentException("Invalid type: " + t);
	}

	@Override
	public TypeMirror capture(TypeMirror type) {
		// JLS §5.1.10 states: "Capture conversion on any type other than a parameterized type (§4.5) acts as an
		// identity conversion (§5.1.1)."
		TypeKind kind = type.getKind();
		if (kind != TypeKind.DECLARED) {
			return type;
		}
		CommonDeclaredType declaredType = (CommonDeclaredType) type;
		return declaredType.getCapturedType();
	}

	@Override
	public DeclaredType captureImpl(DeclaredType declaredType) {
		//capture conversion converts ? wildcards to <captured wildcard> type variables
		//if there are no wildcard type arguments in the parameter, just return the type itself
		if (!hasAnyWildcardTypeArguments(declaredType)) {
			return declaredType;
		}
		return new CapturedDeclaredType(this, declaredType);
	}

	@Override
	public PrimitiveType getPrimitiveType(TypeKind kind) {
		PrimitiveType result = PRIMITIVE_TYPES.get(kind);
		if (result == null) {
			throw new IllegalArgumentException(Objects.toString(kind));
		}
		return result;
	}

	@Override
	public NullType getNullType() {
		return IncrementalNullType.INSTANCE;
	}

	@Override
	public NoType getNoType(TypeKind kind) {
		switch (kind) {
			case VOID:
				return IncrementalNoType.INSTANCE_VOID;
			case NONE:
				return IncrementalNoType.INSTANCE_NONE;
			default: {
				throw new IllegalArgumentException(kind.toString());
			}
		}
	}

	public static NoType getNoneTypeKind() {
		return IncrementalNoType.INSTANCE_NONE;
	}

	public static NoType getVoidTypeKind() {
		return IncrementalNoType.INSTANCE_VOID;
	}

	@Override
	public ArrayType getArrayType(TypeMirror componentType) {
		return new SimpleArrayType(this, componentType);
	}

	@Override
	public WildcardType getWildcardType(TypeMirror extendsBound, TypeMirror superBound) {
		return new SimpleWildcardType(this, extendsBound, superBound, null);
	}

	@Override
	public DeclaredType getDeclaredType(TypeElement typeElem, TypeMirror... typeArgs) {
		return getDeclaredType(null, typeElem, typeArgs);
	}

	@Override
	public DeclaredType getDeclaredType(DeclaredType containing, TypeElement typeElem, TypeMirror... typeArgs) {
		TypeMirror enclosing;
		if (containing == null) {
			enclosing = IncrementalNoType.INSTANCE_NONE;
		} else {
			enclosing = containing;
		}
		Iterator<? extends TypeParameterElement> teit = typeElem.getTypeParameters().iterator();
		if (teit.hasNext()) {
			if (typeArgs.length > 0) {
				//if there is at least one type argument
				int argi = 0;
				for (; teit.hasNext(); ++argi) {
					if (argi >= typeArgs.length) {
						throw new IllegalArgumentException("Insufficient type arguments. Required: "
								+ typeElem.getTypeParameters().size() + " got: " + typeArgs.length);
					}
					TypeParameterElement tpe = teit.next();
					TypeMirror arg = typeArgs[argi];
					if (arg.getKind() == TypeKind.WILDCARD) {
						WildcardType argwc = (WildcardType) arg;
						//replace the wildcard type with one that has a parameter element linked
						typeArgs[argi] = new SimpleWildcardType(this, argwc.getExtendsBound(), argwc.getSuperBound(),
								tpe);
					}
				}
			}
		} else {
			if (typeArgs.length > 0) {
				throw new IllegalArgumentException(
						"Type " + typeElem.getQualifiedName() + " does not have type arguments.");
			}
		}
		return new SimpleDeclaredType(this, enclosing, typeElem, ImmutableUtils.asUnmodifiableArrayList(typeArgs));
	}

	private class AsMemberOfVisitor implements DefaultedElementVisitor<TypeMirror, DeclaredType> {

		@Override
		public TypeMirror visitPackage(PackageElement e, DeclaredType containing) {
			throw new IllegalArgumentException(e.getKind() + " is not a valid element for asMemberOf: " + e);
		}

		@Override
		public TypeMirror visitModuleCompat(ModuleElementCompat e, DeclaredType p) {
			throw new IllegalArgumentException(
					e.getRealObject().getKind() + " is not a valid element for asMemberOf: " + e);
		}

		@Override
		public TypeMirror visitType(TypeElement e, DeclaredType containing) {
			TypeElement containingelem = (TypeElement) containing.asElement();
			TypeElement enclosingelem = getEnclosingElementInHierarchy(containingelem, e);
			if (enclosingelem == null) {
				throwIllegal(containing, e);
			}
			boolean parentunrelated = isClassUnrelatedToEnclosing(e);
			if (containingelem != enclosingelem) {
				if (parentunrelated) {
					//parent astype can be safely used, as the parameters will not matter in the result
					return visitType(e, (DeclaredType) enclosingelem.asType());
				}
				if (!isRaw(containing, containingelem)) {
					return asMemberOf(getSuperCorrectParameterizedTypeMirror(containing, enclosingelem), e);
				}
				//containing type is a raw type1
				//continue method execution, but dont include any type parameters anywhere
				containing = (DeclaredType) erasure(enclosingelem.asType());
				containingelem = enclosingelem;
			}
			Map<TypeParameterElement, TypeMirror> parammap;
			if (parentunrelated || !isRaw(containing, containingelem)) {
				parammap = new IdentityHashMap<>();
				//put the type parameters of the element in 
				for (TypeParameterElement etpe : e.getTypeParameters()) {
					parammap.put(etpe, etpe.asType());
				}
				collectTypeParameterMapDirect(containing, containingelem, parammap);
			} else
				parammap = Collections.emptyMap();
			return createDeclaredType(e, parammap);
		}

		@Override
		public TypeMirror visitVariable(VariableElement e, DeclaredType containing) {
			TypeElement containingelem = (TypeElement) containing.asElement();
			TypeElement enclosingelem = getEnclosingElementInHierarchy(containingelem, e);
			if (enclosingelem == null) {
				throwIllegal(containing, e);
			}
			if (e.getModifiers().contains(Modifier.STATIC)) {
				return e.asType();
			}
			if (isRaw(containing)) {
				//replicate javac behaviour
				return erasure(e.asType());
			}
			Map<TypeParameterElement, TypeMirror> parammap = getTypeParameterMapResolve(containing);
			return e.asType().accept(typeVariableMirrorReplacerVisitor, parammap);
		}

		@Override
		public TypeMirror visitExecutable(ExecutableElement e, DeclaredType containing) {
			if (!hasElementInHierarchy((TypeElement) containing.asElement(), e)) {
				throwIllegal(containing, e);
			}
			if (e.getModifiers().contains(Modifier.STATIC)) {
				return e.asType();
			}
			if (isRaw(containing)) {
				return erasure(e.asType());
			}
			Map<TypeParameterElement, TypeMirror> parammap = getTypeParameterMapResolve(containing);
			for (TypeParameterElement tpe : e.getTypeParameters()) {
				parammap.put(tpe, tpe.asType());
			}
			return e.asType().accept(typeVariableMirrorReplacerVisitor, parammap);
		}

		@Override
		public TypeMirror visitTypeParameter(TypeParameterElement e, DeclaredType containing) {
			if (!hasElementInHierarchy((TypeElement) containing.asElement(), e)) {
				throwIllegal(containing, e);
			}
			if (isRaw(containing)) {
				return erasure(e.asType());
			}
			Map<TypeParameterElement, TypeMirror> parammap = getTypeParameterMapResolve(containing);
			TypeMirror got = parammap.get(e);
			if (got != null) {
				return got;
			}
			return erasure(e.asType());
		}

		@Override
		public TypeMirror visitUnknown(Element e, DeclaredType containing) {
			throw new IllegalArgumentException("Unknown element kind: " + e.getKind() + " in " + containing);
		}

		private void throwIllegal(DeclaredType containing, Element element) throws IllegalArgumentException {
			throw new IllegalArgumentException("Element " + element + " not found in " + containing + " hierarchy.");
		}
	}

	private class TypeVariableMirrorReplacerVisitor
			implements DefaultedTypeVisitor<TypeMirror, Map<TypeParameterElement, TypeMirror>> {

		@Override
		public TypeMirror visitPrimitive(PrimitiveType t, Map<TypeParameterElement, TypeMirror> p) {
			return t;
		}

		@Override
		public TypeMirror visitNull(NullType t, Map<TypeParameterElement, TypeMirror> p) {
			return t;
		}

		@Override
		public TypeMirror visitArray(ArrayType t, Map<TypeParameterElement, TypeMirror> p) {
			TypeMirror componenttype = t.getComponentType();
			TypeMirror compreplaced = componenttype.accept(this, p);
			if (compreplaced == componenttype) {
				return t;
			}
			return new SimpleArrayType(IncrementalElementsTypes8.this, compreplaced);
		}

		@Override
		public TypeMirror visitDeclared(DeclaredType t, Map<TypeParameterElement, TypeMirror> p) {
			TypeMirror enclosing = t.getEnclosingType();
			TypeMirror enclosingreplaced = enclosing.accept(this, p);
			List<? extends TypeMirror> arguments = t.getTypeArguments();
			if (arguments.isEmpty()) {
				if (enclosingreplaced == enclosing) {
					return t;
				}
				return new SimpleDeclaredType(IncrementalElementsTypes8.this, enclosingreplaced,
						(TypeElement) t.asElement(), arguments);
			}
			//non empty arguments, replace them
			return new SimpleDeclaredType(IncrementalElementsTypes8.this, enclosingreplaced,
					(TypeElement) t.asElement(), replaceList(arguments, p));
		}

		private List<? extends TypeMirror> replaceList(List<? extends TypeMirror> list,
				Map<TypeParameterElement, TypeMirror> p) {
			if (list.isEmpty()) {
				return Collections.emptyList();
			}
			boolean edited = false;
			List<TypeMirror> result = new ArrayList<>(list);
			for (ListIterator<TypeMirror> it = result.listIterator(); it.hasNext();) {
				TypeMirror next = it.next();
				TypeMirror toset = next.accept(this, p);
				if (toset != next) {
					it.set(toset);
					edited = true;
				}
			}
			if (!edited) {
				return list;
			}
			return result;
		}

		@Override
		public TypeMirror visitError(ErrorType t, Map<TypeParameterElement, TypeMirror> p) {
			return t;
		}

		@Override
		public TypeMirror visitTypeVariable(TypeVariable t, Map<TypeParameterElement, TypeMirror> p) {
			TypeMirror got = p.get(t.asElement());
			if (got != null) {
				return got;
			}
			return erasure(t);
		}

		@Override
		public TypeMirror visitWildcard(WildcardType t, Map<TypeParameterElement, TypeMirror> p) {
			CommonWildcardType commonwc = (CommonWildcardType) t;
			TypeMirror ext = t.getExtendsBound();
			TypeMirror sup = t.getSuperBound();
			if (ext != null) {
				TypeMirror extrep = ext.accept(this, p);
				if (extrep == ext) {
					return t;
				}
				if (extrep.getKind() == TypeKind.WILDCARD) {
					WildcardType extrepwc = (WildcardType) extrep;
					TypeMirror extrepwcext = extrepwc.getExtendsBound();
					if (extrepwcext != null) {
						//if would replace to (? extends (? extends X))
						//change to (? extends X)
						return new SimpleWildcardType(IncrementalElementsTypes8.this, extrepwcext, null,
								commonwc.getCorrespondingTypeParameter());
					}
				}
				return new SimpleWildcardType(IncrementalElementsTypes8.this, extrep, null,
						commonwc.getCorrespondingTypeParameter());
			}
			if (sup != null) {
				TypeMirror suprep = sup.accept(this, p);
				if (suprep == sup) {
					return t;
				}
				if (suprep.getKind() == TypeKind.WILDCARD) {
					WildcardType suprepwc = (WildcardType) suprep;
					TypeMirror suprepwcsup = suprepwc.getSuperBound();
					if (suprepwcsup != null) {
						//if would replace to (? super (? super X))
						return new SimpleWildcardType(IncrementalElementsTypes8.this, null, suprepwcsup,
								commonwc.getCorrespondingTypeParameter());
					}
				}
				return new SimpleWildcardType(IncrementalElementsTypes8.this, null, suprep,
						commonwc.getCorrespondingTypeParameter());
			}
			return t;
		}

		@Override
		public TypeMirror visitExecutable(ExecutableType t, Map<TypeParameterElement, TypeMirror> p) {
			TypeMirror receiver = t.getReceiverType().accept(this, p);
			List<? extends TypeVariable> typevars = t.getTypeVariables();
			TypeMirror returntype = t.getReturnType().accept(this, p);
			List<? extends TypeMirror> parameters = replaceList(t.getParameterTypes(), p);
			List<? extends TypeMirror> throwntypes = replaceList(t.getThrownTypes(), p);
			return new SimpleExecutableType(IncrementalElementsTypes8.this, typevars, returntype, receiver, parameters,
					throwntypes);
		}

		@Override
		public TypeMirror visitNoType(NoType t, Map<TypeParameterElement, TypeMirror> p) {
			return t;
		}

		@Override
		public TypeMirror visitIntersection(IntersectionType t, Map<TypeParameterElement, TypeMirror> p) {
			List<? extends TypeMirror> bounds = t.getBounds();
			List<? extends TypeMirror> replaced = replaceList(bounds, p);
			if (replaced == bounds) {
				return t;
			}
			return createIntersectionType(replaced);
		}

		@Override
		public TypeMirror visitUnion(UnionType t, Map<TypeParameterElement, TypeMirror> p) {
			List<? extends TypeMirror> alts = t.getAlternatives();
			List<? extends TypeMirror> replaced = replaceList(alts, p);
			if (replaced == alts) {
				return t;
			}
			return new SimpleUnionType(IncrementalElementsTypes8.this, replaced);
		}

	}

	private DeclaredType createDeclaredType(TypeElement element, Map<TypeParameterElement, TypeMirror> parameters) {
		List<? extends TypeParameterElement> elemparams = element.getTypeParameters();

		TypeMirror enclosing;
		if (isClassUnrelatedToEnclosing(element)) {
			enclosing = IncrementalNoType.INSTANCE_NONE;
		} else {
			TypeElement enclosingtype = (TypeElement) element.getEnclosingElement();
			DeclaredType enclosingdt = createDeclaredType(enclosingtype, parameters);
			enclosing = enclosingdt;
			if (isRaw(enclosingdt, enclosingtype)) {
				elemparams = Collections.emptyList();
			}
		}

		List<TypeMirror> typeArguments;
		if (elemparams.isEmpty()) {
			typeArguments = Collections.emptyList();
		} else {
			ArrayList<TypeMirror> ntypeargs = new ArrayList<>(elemparams.size());
			typeArguments = ImmutableUtils.unmodifiableList(ntypeargs);
			for (TypeParameterElement tpe : elemparams) {
				TypeMirror subs = parameters.get(tpe);
				if (subs != null) {
					ntypeargs.add(subs);
				} else {
					typeArguments = Collections.emptyList();
					break;
				}
			}
		}
		return new SimpleDeclaredType(this, enclosing, element, typeArguments);
	}

	@Override
	public TypeMirror asMemberOf(DeclaredType containing, Element element) {
		return element.accept(asMemberOfVisitor, containing);
	}

//	
//	Types functions end
//

	public static Name getEmptyName() {
		return EMPTY_NAME;
	}

	public static Name getCapturedWildcardName() {
		return CAPTURED_WILDCARD_NAME;
	}

	public PackageElement getCaptureVariablesEnclosingPackageElement() {
		return CAPTURE_VARIABLES_ENCLOSING_PACKAGE_ELEMENT;
	}

	@Override
	public Element getCapturesEnclosingElement() {
		return CAPTURES_ENCLOSING_ELEMENT;
	}

	@Override
	public final <T> T javac(Supplier<T> function) {
		synchronized (javacSync) {
			return function.get();
		}
	}

	@Override
	public final <T> T javacElements(Function<? super Elements, ? extends T> function) {
		synchronized (javacSync) {
			return function.apply(realElements);
		}
	}

	private <T extends TypeMirror> List<? extends T> forwardTypeListLockedImpl(List<? extends T> list, int size) {
		TypeMirror[] elems = new TypeMirror[size];
		int i = 0;
		for (T t : list) {
			elems[i++] = forwardTypeLockedImpl(t, null);
		}
		@SuppressWarnings("unchecked")
		List<? extends T> result = ImmutableUtils.unmodifiableArrayList((T[]) elems);
		return result;
	}

	private List<? extends TypeMirror> forwardTypeArgumentListLockedImpl(List<? extends TypeMirror> list,
			ForwardingDeclaredType enclosingtype, int size) {
		TypeElement elem = (TypeElement) enclosingtype.asElement();
		List<? extends TypeParameterElement> typeparams = elem.getTypeParameters();
		Iterator<? extends TypeParameterElement> tpeit = typeparams.iterator();

		TypeMirror[] elems = new TypeMirror[size];
		int i = 0;
		for (TypeMirror t : list) {
			elems[i++] = forwardTypeLockedImpl(t, tpeit.next());
		}
		return ImmutableUtils.unmodifiableArrayList(elems);
	}

	private List<? extends AnnotationValue> forwardAnnotationValueListLockedImpl(List<? extends AnnotationValue> list,
			int size) {
		AnnotationValue[] elems = new AnnotationValue[size];
		int i = 0;
		for (AnnotationValue t : list) {
			elems[i++] = forward(t);
		}
		List<? extends AnnotationValue> result = ImmutableUtils.unmodifiableArrayList(elems);
		return result;
	}

	private <E extends Element> List<? extends E> forwardElementListLockedImpl(List<? extends E> list, int size) {
		Element[] elems = new Element[size];
		int i = 0;
		for (E t : list) {
			elems[i++] = forwardElementLockedImpl(t);
		}
		@SuppressWarnings("unchecked")
		List<? extends E> result = ImmutableUtils.unmodifiableArrayList((E[]) elems);
		return result;
	}

	@Override
	public List<? extends AnnotationValue> forwardAnnotationValues(List<? extends AnnotationValue> list) {
		int size = list.size();
		if (size == 0) {
			return Collections.emptyList();
		}
		synchronized (javacSync) {
			return forwardAnnotationValueListLockedImpl(list, size);
		}
	}

	@Override
	public <E extends Element> List<? extends E> forwardElements(List<? extends E> list) {
		int size = list.size();
		if (size == 0) {
			return Collections.emptyList();
		}
		synchronized (javacSync) {
			return forwardElementListLockedImpl(list, size);
		}
	}

	@Override
	public <E extends Element> List<? extends E> forwardElements(Supplier<List<? extends E>> javaclistsupplier) {
		synchronized (javacSync) {
			List<? extends E> list = javaclistsupplier.get();
			int size = list.size();
			if (size == 0) {
				return Collections.emptyList();
			}
			return forwardElementListLockedImpl(list, size);
		}
	}

	@Override
	public <E extends Element> E forwardElementElems(Function<Elements, E> javacelementsupplier) {
		synchronized (javacSync) {
			E got = javacelementsupplier.apply(realElements);
			if (got == null) {
				return null;
			}
			return forwardElementLockedImpl(got);
		}
	}

	@Override
	public <E extends Element> E forwardElement(Supplier<E> javacelementsupplier) {
		synchronized (javacSync) {
			E got = javacelementsupplier.get();
			if (got == null) {
				return null;
			}
			return forwardElementLockedImpl(got);
		}
	}

	@Override
	public VariableElement forwardElement(VariableElement element) {
		synchronized (javacSync) {
			return forwardElementLockedImpl(element);
		}
	}

	@Override
	public ExecutableElement forwardElement(ExecutableElement element) {
		synchronized (javacSync) {
			return forwardElementLockedImpl(element);
		}
	}

	@Override
	public Element forwardElement(Element element) {
		synchronized (javacSync) {
			return forwardElementLockedImpl(element);
		}
	}

	private <E extends Element> E forwardElementLockedImpl(E element) {
		ElementKind elemkind = element.getKind();
		return forwardElementLockedImpl(element, elemkind);
	}

	@SuppressWarnings("unchecked")
	protected <E extends Element> E forwardElementLockedImpl(E element, ElementKind elemkind) {
		if (elemkind == ElementKind.PACKAGE) {
			return (E) forwardPackageLockedImpl((PackageElement) element);
		}
		return (E) forwardedElements.computeIfAbsent(element, e -> {
			ForwardingElementBase<?> result = KindBasedElementVisitor.visit(elemkind, e, forwardingElementVisitor,
					null);
			result.setElementKind(elemkind);
			return result;
		});
	}

	protected PackageElement forwardPackageLockedImpl(PackageElement pe) {
		String pname = pe.getQualifiedName().toString();

		PackageElement over = packageTypesContainer.forwardOverrideJavacLocked(pe, pname);
		if (over != null) {
			return over;
		}

		return (PackageElement) forwardedElements.computeIfAbsent(pe,
				e -> new ForwardingPackageElement(this, pe, new IncrementalName(pname)));
	}

	protected class ForwardingElementVisitor implements DefaultedElementVisitor<ForwardingElementBase<?>, Void> {
		@Override
		public ForwardingElementBase<?> visitPackage(PackageElement e, Void p) {
			throw new IllegalArgumentException("Package element cannot be forwarded.");
		}

		@Override
		public ForwardingElementBase<?> visitType(TypeElement e, Void p) {
			ForwardingTypeElement res = new ForwardingTypeElement(IncrementalElementsTypes8.this, e);
			String qname = res.getQualifiedName().toString();
			ForwardingTypeElement prev = forwardedTypeElements.put(qname, res);
			if (prev != null) {
				throw new IllegalStateException("Element forwarded multiple times: " + qname);
			}
			return res;
		}

		@Override
		public ForwardingElementBase<?> visitVariable(VariableElement e, Void p) {
			return new ForwardingVariableElement(IncrementalElementsTypes8.this, e);
		}

		@Override
		public ForwardingElementBase<?> visitExecutable(ExecutableElement e, Void p) {
			return new ForwardingExecutableElement(IncrementalElementsTypes8.this, e);
		}

		@Override
		public ForwardingElementBase<?> visitTypeParameter(TypeParameterElement e, Void p) {
			return new ForwardingTypeParameterElement(IncrementalElementsTypes8.this, e);
		}

		@Override
		public ForwardingElementBase<?> visitUnknown(Element e, Void p) {
			return new ForwardingUnknownElement(IncrementalElementsTypes8.this, e);
		}
	}

	@Override
	public <T extends TypeMirror> List<? extends T> forwardTypes(List<? extends T> list) {
		int size = list.size();
		if (size == 0) {
			return Collections.emptyList();
		}
		synchronized (javacSync) {
			return forwardTypeListLockedImpl(list, size);
		}
	}

	@Override
	public <T extends TypeMirror> List<? extends T> forwardTypes(Supplier<List<? extends T>> javaclistsupplier) {
		synchronized (javacSync) {
			List<? extends T> list = javaclistsupplier.get();
			int size = list.size();
			if (size == 0) {
				return Collections.emptyList();
			}
			return forwardTypeListLockedImpl(list, size);
		}
	}

	@Override
	public List<? extends TypeMirror> forwardTypeArguments(Supplier<List<? extends TypeMirror>> javaclistsupplier,
			ForwardingDeclaredType enclosingtype) {
		synchronized (javacSync) {
			List<? extends TypeMirror> list = javaclistsupplier.get();
			int size = list.size();
			if (size == 0) {
				return Collections.emptyList();
			}
			return forwardTypeArgumentListLockedImpl(list, enclosingtype, size);
		}
	}

	@Override
	public List<? extends TypeMirror> forwardTypeArguments(List<? extends TypeMirror> list,
			ForwardingDeclaredType enclosingtype) {
		int size = list.size();
		if (size == 0) {
			return Collections.emptyList();
		}
		synchronized (javacSync) {
			return forwardTypeArgumentListLockedImpl(list, enclosingtype, size);
		}
	}

	private <T extends TypeMirror> T forwardTypeLockedImpl(T mirror, TypeParameterElement correspondingtypeparameter) {
		return forwardTypeKindImpl(mirror, correspondingtypeparameter, mirror.getKind());
	}

	@SuppressWarnings("unchecked")
	private <T extends TypeMirror> T forwardTypeKindImpl(T mirror, TypeParameterElement correspondingtypeparameter,
			TypeKind kind) {
		//XXX visitorize?
		ForwardingTypeMirrorBase<?> result;
		switch (kind) {
			case PACKAGE:
				result = new ForwardingPackageType(this, (CommonPackageType) mirror);
				break;
			case ARRAY:
				result = new ForwardingArrayType(this, (ArrayType) mirror);
				break;
			case BOOLEAN:
			case CHAR:
			case FLOAT:
			case DOUBLE:
			case BYTE:
			case SHORT:
			case INT:
			case LONG:
				result = new ForwardingPrimitiveType(this, (PrimitiveType) mirror);
				break;
			case VOID:
			case NONE:
				//there is no way to place annotations on a VOID or NONE so its fine to use our types
				return (T) getNoType(kind);
			case NULL:
				result = new ForwardingNullType(this, (NullType) mirror);
				break;
			case DECLARED:
				result = new ForwardingDeclaredType(this, (DeclaredType) mirror);
				break;
			case EXECUTABLE:
				result = new ForwardingExecutableType(this, (ExecutableType) mirror);
				break;
			case ERROR:
				result = new ForwardingErrorType(this, (ErrorType) mirror);
				break;
			case INTERSECTION:
				result = new ForwardingIntersectionType(this, (IntersectionType) mirror);
				break;
			case UNION:
				result = new ForwardingUnionType(this, (UnionType) mirror);
				break;
			case TYPEVAR:
				result = new ForwardingTypeVariable(this, (TypeVariable) mirror);
				break;
			case WILDCARD:
				result = new ForwardingWildcardType(this, (WildcardType) mirror, correspondingtypeparameter);
				break;
			case OTHER:
				result = new ForwardingUnknownType(this, mirror);
				break;
			default: {
				result = forwardUnknownType(kind, mirror);
				break;
			}
		}
		result.setTypeKind(kind);
		return (T) result;
	}

	protected ForwardingTypeMirrorBase<?> forwardUnknownType(TypeKind kind, TypeMirror mirror) {
		return new ForwardingUnknownType(this, mirror);
	}

	@Override
	public <T extends TypeMirror> T forwardType(T mirror, TypeParameterElement correspondingtypeparameter) {
		synchronized (javacSync) {
			return forwardTypeLockedImpl(mirror, correspondingtypeparameter);
		}
	}

//	public DeclaredType forwardType(DeclaredType mirror) {
//		return new ForwardingDeclaredType(this, mirror);
//	}

	@Override
	public <T extends TypeMirror> T forwardType(Supplier<T> javacmirrorsupplier,
			TypeParameterElement correspondingtypeparameter) {
		synchronized (javacSync) {
			return forwardTypeLockedImpl(javacmirrorsupplier.get(), correspondingtypeparameter);
		}
	}

	@Override
	public <T extends TypeMirror> T forwardType(Supplier<T> javacmirrorsupplier) {
		return forwardType(javacmirrorsupplier, null);
	}

	@Override
	public TypeMirror forwardType(TypeMirror mirror) {
		return forwardType(mirror, null);
	}

	@Override
	public TypeMirror forwardTypeOrNone(Supplier<? extends TypeMirror> javacmirrorsupplier) {
		synchronized (javacSync) {
			TypeMirror type = javacmirrorsupplier.get();
			if (type == null) {
				return IncrementalNoType.INSTANCE_NONE;
			}
			return forwardTypeLockedImpl(type, null);
		}
	}

	@Override
	public AnnotationMirror forward(AnnotationMirror a) {
		return new ForwardingAnnotationMirror(this, a);
	}

	@Override
	public AnnotationValue forward(AnnotationValue av) {
		if (av == null) {
			return null;
		}
		return new ForwardingAnnotationValue(this, av);
	}

	public static Class<?> primitiveTypeKindToClass(TypeKind kind) {
		return PRIMITIVE_BOXING_CLASS_MAP.get(kind);
	}

	@Override
	public Object getAnnotationValue(SignaturePath annotationSignaturePath, AnnotationSignature.Value value,
			TypeMirror targettype, Element enclosingresolutionelement) {
		return targettype
				.accept(new AnnotationValueResolverVisitor(annotationSignaturePath, enclosingresolutionelement), value);
	}

	public static TypeElement getSuperClassOf(TypeElement element) {
		TypeMirror sc = element.getSuperclass();
		if (sc.getKind() == TypeKind.NONE) {
			return null;
		}
		return (TypeElement) ((DeclaredType) sc).asElement();
	}

	public static boolean isClassUnrelatedToEnclosing(ClassSignature sig) {
		return sig.getKindIndex() != ElementKindCompatUtils.ELEMENTKIND_INDEX_CLASS
				|| sig.getNestingKind() == NestingKind.TOP_LEVEL || sig.getModifiers().contains(Modifier.STATIC);
	}

	public static boolean isClassUnrelatedToEnclosing(TypeElement elem) {
		return elem.getKind() != ElementKind.CLASS || elem.getNestingKind() == NestingKind.TOP_LEVEL
				|| elem.getModifiers().contains(Modifier.STATIC);
	}

	private final class AnnotationValueResolverVisitor extends SimpleTypeVisitor8<Object, Value>
			implements DefaultedTypeVisitor<Object, Value> {
		private SignaturePath annotationSignaturePath;
		private Element resolutionElement;

		public AnnotationValueResolverVisitor(SignaturePath annotationSignaturePath, Element resolutionElement) {
			this.annotationSignaturePath = annotationSignaturePath;
			this.resolutionElement = resolutionElement;
		}

		@Override
		protected Object defaultAction(TypeMirror e, Value value) {
			return "<error>";
		}

		@Override
		public Object visitUnknown(TypeMirror t, Value p) {
			return "<error-unknown>";
		}

		@Override
		public Object visitPrimitive(PrimitiveType t, Value value) {
			if (value instanceof LiteralValue) {
				Object resolved = ((LiteralValue) value).getValue().resolve(IncrementalElementsTypes8.this,
						resolutionElement);
				Function<Object, ?> castop = CastOperators.getOperatorFunction(resolved,
						IncrementalElementsTypes.primitiveTypeKindToClass(t.getKind()));
				return castop.apply(resolved);
			}
			return defaultAction(t, value);
		}

		@Override
		public List<? extends AnnotationValue> visitArray(ArrayType t, Value value) {
			TypeMirror componenttype = t.getComponentType();
			if (value instanceof ArrayValue) {
				ArrayValue av = (ArrayValue) value;
				List<? extends Value> avvalues = av.getValues();
				List<AnnotationValue> list = JavaTaskUtils.cloneImmutableList(avvalues,
						new Function<Value, AnnotationValue>() {
							private int index = 0;

							@Override
							public AnnotationValue apply(Value v) {
								SignaturePath valpath = SignaturePath.createIndexed(annotationSignaturePath, v,
										index++);
								return new IncrementalAnnotationValue(IncrementalElementsTypes8.this, v, componenttype,
										resolutionElement, valpath);
							}
						});
				return list;
			}
			//value is not an arrayvalue
			//convert single value to array
			return ImmutableUtils.singletonList(new IncrementalAnnotationValue(IncrementalElementsTypes8.this, value,
					componenttype, resolutionElement, annotationSignaturePath));
		}

		@Override
		public Object visitDeclared(DeclaredType t, Value value) {
			TypeElement type = (TypeElement) t.asElement();
			switch (type.getKind()) {
				case ANNOTATION_TYPE: {
					if (value instanceof AnnotValue) {
						AnnotationSignature annotsig = ((AnnotValue) value).getAnnotation();
						return new IncrementalAnnotationMirror(IncrementalElementsTypes8.this, annotsig,
								resolutionElement, new SignaturePath(annotationSignaturePath, annotsig));
					}
					return "<error-not-annotation>";
				}
				case ENUM: {
					if (value instanceof VariableValue) {
						VariableValue vv = (VariableValue) value;
						VariableElement ve = IncrementalElementsTypes8.this.getEnumConstant(
								vv.getEnclosingType(IncrementalElementsTypes8.this, resolutionElement), vv.getName(),
								resolutionElement);
						if (ve != null) {
							return ve;
						}
						return "<error-enum-not-found-" + vv.getName() + ">";
					}
					return "<error-not-enum>";
				}
				case CLASS: {
					Name qname = type.getQualifiedName();
					if (qname.contentEquals("java.lang.String")) {
						if (value instanceof LiteralValue) {
							return ((LiteralValue) value).getValue().resolve(IncrementalElementsTypes8.this,
									resolutionElement);
						}
						return "<error>";
					}
					if (qname.contentEquals("java.lang.Class")) {
						if (value instanceof TypeValue) {
							return getTypeMirror(((TypeValue) value).getType(), resolutionElement);
						}
						return "<error>";
					}
					return "<error-invalid-type>";
				}
				default: {
					return "<error>";
				}
			}
		}
	}

	private static class AnnotationValueToSignatureConverterVisitor
			extends AbstractAnnotationValueVisitor8<AnnotationSignature.Value, Void> {

		protected final ParserCache cache;

		public AnnotationValueToSignatureConverterVisitor(ParserCache cache) {
			this.cache = cache;
		}

		public static AnnotationSignature.Value convert(AnnotationValue value, ParserCache cache) {
			return value.accept(new AnnotationValueToSignatureConverterVisitor(cache), null);
		}

		private Value literal(Object o) {
			return cache.literalAnnotationValue(o);
		}

		@Override
		public Value visitBoolean(boolean b, Void p) {
			return literal(b);
		}

		@Override
		public Value visitByte(byte b, Void p) {
			return literal(b);
		}

		@Override
		public Value visitChar(char c, Void p) {
			return literal(c);
		}

		@Override
		public Value visitDouble(double d, Void p) {
			return literal(d);
		}

		@Override
		public Value visitFloat(float f, Void p) {
			return literal(f);
		}

		@Override
		public Value visitInt(int i, Void p) {
			return literal(i);
		}

		@Override
		public Value visitLong(long i, Void p) {
			return literal(i);
		}

		@Override
		public Value visitShort(short s, Void p) {
			return literal(s);
		}

		@Override
		public Value visitString(String s, Void p) {
			return literal(s);
		}

		@Override
		public Value visitType(TypeMirror t, Void p) {
			TypeKind tkind = t.getKind();
			if (tkind.isPrimitive()) {
				return new TypeValueImpl(PrimitiveTypeSignatureImpl.create(tkind));
			}
			if (tkind != TypeKind.DECLARED) {
				return new UnknownValueImpl(t.toString());
			}
			DeclaredType dt = (DeclaredType) t;
			TypeElement typeelem = (TypeElement) dt.asElement();
			if (typeelem == null) {
				return new UnknownValueImpl(t.toString());
			}
			return new TypeValueImpl(typeElementToTypeReference(typeelem, cache));
		}

		@Override
		public Value visitEnumConstant(VariableElement c, Void p) {
			TypeElement typeelem = (TypeElement) c.getEnclosingElement();
			return new VariableValueImpl(c.getSimpleName().toString(), typeElementToTypeReference(typeelem, cache));
		}

		@Override
		public Value visitAnnotation(AnnotationMirror a, Void p) {
			return new AnnotValueImpl(annotationMirrorToSignature(a, cache));
		}

		@Override
		public Value visitArray(List<? extends AnnotationValue> vals, Void p) {
			List<Value> values = JavaTaskUtils.cloneImmutableList(vals, av -> convert(av, cache));
			return ArrayValueImpl.create(values);
		}

		@Override
		public Value visitUnknown(AnnotationValue av, Void p) {
			return new UnknownValueImpl(av);
		}
	}

	private abstract class AbstractTypeSignatureToMirrorVisitor<Param>
			implements TypeSignatureVisitor<TypeMirror, Param> {
		protected abstract Element getEnclosingElement(Param p);

		@Override
		public TypeMirror visitArray(ArrayTypeSignature array, Param p) {
			return getTypeMirror(array, getEnclosingElement(p));
		}

		@Override
		public TypeMirror visitWildcard(WildcardTypeSignature type, Param p) {
			return getTypeMirror(type, getEnclosingElement(p));
		}

		@Override
		public TypeMirror visitTypeVariable(TypeVariableTypeSignature type, Param p) {
			return getTypeMirror(type, getEnclosingElement(p));
		}

		@Override
		public TypeMirror visitIntersection(IntersectionTypeSignature intersection, Param p) {
			return getTypeMirror(intersection, getEnclosingElement(p));
		}

		@Override
		public TypeMirror visitNoType(NoTypeSignature notype, Param p) {
			return getTypeMirror(notype, getEnclosingElement(p));
		}

		@Override
		public TypeMirror visitPrimitive(PrimitiveTypeSignature primitive, Param p) {
			return getTypeMirror(primitive, getEnclosingElement(p));
		}

		@Override
		public TypeMirror visitUnion(UnionTypeSignature union, Param p) {
			return getTypeMirror(union, getEnclosingElement(p));
		}

		@Override
		public TypeMirror visitUnknown(UnknownTypeSignature unknown, Param p) {
			return getTypeMirror(unknown, getEnclosingElement(p));
		}

		@Override
		public TypeMirror visitUnresolved(UnresolvedTypeSignature unresolved, Param p) {
			Element resolutionelement = getEnclosingElement(p);
			ResolutionScope scope;
			ParameterizedTypeSignature enclosingsig = unresolved.getEnclosingSignature();
			if (enclosingsig != null) {
				scope = new EnclosingTypeResolutionScope(enclosingsig, resolutionelement);
			} else {
				scope = createResolutionScope(resolutionelement);
			}
			if (scope == null) {
				return new IncrementalErrorType(IncrementalElementsTypes8.this, unresolved.toString());
			}
			TypeSignature resolved = scope.resolveTypeSignature(IncrementalElementsTypes8.this,
					unresolved.getUnresolvedName(), unresolved.getTypeParameters());
			if (resolved == null) {
				return new IncrementalErrorType(IncrementalElementsTypes8.this, unresolved.toString());
			}
			List<? extends AnnotationSignature> annots = unresolved.getAnnotations();
			if (!ObjectUtils.isNullOrEmpty(annots)) {
				resolved = resolved.accept(TypeSignatureAnnotationSetterVisitor.INSTANCE, annots);
			}
			return resolved.accept(this, p);
		}

		@Override
		public IncrementalDeclaredType visitParameterized(ParameterizedTypeSignature parameterized, Param p) {
			Element enclosingelem = getEnclosingElement(p);
			return new IncrementalDeclaredType(IncrementalElementsTypes8.this, parameterized, null, enclosingelem);
		}

		@Override
		public IncrementalDeclaredType visitEncloser(ParameterizedTypeSignature encloser, Param p) {
			Element enclosingelem = getEnclosingElement(p);
			return new IncrementalDeclaredType(IncrementalElementsTypes8.this, encloser, null, enclosingelem);
		}

		@Override
		public TypeMirror visitNull(NullTypeSignature type, Param p) {
			return IncrementalNullType.INSTANCE;
		}
	}

	private static final class TypeSignatureAnnotationSetterVisitor
			implements TypeSignatureVisitor<TypeSignature, List<? extends AnnotationSignature>> {
		public static final TypeSignatureAnnotationSetterVisitor INSTANCE = new TypeSignatureAnnotationSetterVisitor();

		@Override
		public TypeSignature visitArray(ArrayTypeSignature type, List<? extends AnnotationSignature> p) {
			return ArrayTypeSignatureImpl.create(p, type.getComponentType());
		}

		@Override
		public TypeSignature visitWildcard(WildcardTypeSignature type, List<? extends AnnotationSignature> p) {
			return WildcardTypeSignatureImpl.create(null, p, type.getLowerBounds(), type.getUpperBounds());
		}

		@Override
		public TypeSignature visitTypeVariable(TypeVariableTypeSignature type, List<? extends AnnotationSignature> p) {
			return TypeVariableTypeSignatureImpl.create(p, type.getVariableName());
		}

		@Override
		public TypeSignature visitIntersection(IntersectionTypeSignature type, List<? extends AnnotationSignature> p) {
			return IntersectionTypeSignatureImpl.create(p, type.getBounds());
		}

		@Override
		public TypeSignature visitNoType(NoTypeSignature type, List<? extends AnnotationSignature> p) {
			return NoTypeSignatureImpl.create(p, type.getKind());
		}

		@Override
		public TypeSignature visitPrimitive(PrimitiveTypeSignature type, List<? extends AnnotationSignature> p) {
			return PrimitiveTypeSignatureImpl.create(p, type.getTypeKind());
		}

		@Override
		public TypeSignature visitUnion(UnionTypeSignature type, List<? extends AnnotationSignature> p) {
			return UnionTypeSignatureImpl.create(p, type.getAlternatives());
		}

		@Override
		public TypeSignature visitUnknown(UnknownTypeSignature type, List<? extends AnnotationSignature> p) {
			return UnknownTypeSignatureImpl.create(p, type.getTypeDescription());
		}

		@Override
		public TypeSignature visitUnresolved(UnresolvedTypeSignature type, List<? extends AnnotationSignature> p) {
			return UnresolvedTypeSignatureImpl.create(p, type.getEnclosingSignature(), type.getUnresolvedName(),
					type.getTypeParameters());
		}

		@Override
		public TypeSignature visitParameterized(ParameterizedTypeSignature type,
				List<? extends AnnotationSignature> p) {
			return TypeReferenceSignatureImpl.create(p, type.getEnclosingSignature(), type.getSimpleName(),
					type.getTypeParameters());
		}

		@Override
		public TypeSignature visitEncloser(ParameterizedTypeSignature type, List<? extends AnnotationSignature> p) {
			return TypeReferenceSignatureImpl.create(p, type.getEnclosingSignature(), type.getSimpleName(),
					type.getTypeParameters());
		}

		@Override
		public TypeSignature visitNull(NullTypeSignature type, List<? extends AnnotationSignature> p) {
			return NullTypeSignatureImpl.create(p);
		}

	}

	private class TypeSignatureToMirrorVisitor extends AbstractTypeSignatureToMirrorVisitor<Element> {
		@Override
		protected Element getEnclosingElement(Element p) {
			return p;
		}
	}

	private class TypeArgumentTypeSignatureToMirrorVisitor
			extends AbstractTypeSignatureToMirrorVisitor<TypeArgumentTypeSignatureToMirrorVisitor.Param> {
		class Param {
			private Element enclosingElement;
			private TypeParameterElement parameter;

			public Param(Element enclosingElement, TypeParameterElement parameter) {
				this.enclosingElement = enclosingElement;
				this.parameter = parameter;
			}
		}

		@Override
		protected Element getEnclosingElement(Param p) {
			return p.enclosingElement;
		}

		@Override
		public TypeMirror visitWildcard(WildcardTypeSignature type, Param p) {
			return new IncrementalWildcardType(IncrementalElementsTypes8.this, type, p.enclosingElement, p.parameter);
		}

		public TypeMirror visit(TypeSignature sig, Element enclosingelement, TypeParameterElement parameter) {
			if (parameter == null) {
				return sig.accept(typeSignatureToMirrorVisitor, enclosingelement);
			}
			return sig.accept(this, new Param(enclosingelement, parameter));
		}
	}

	private ResolutionScope createTypeBodyResolutionScope(TypeElement type) {
		NestingKind nk = type.getNestingKind();
		switch (nk) {
			case MEMBER: {
				TypeElement encelem = (TypeElement) type.getEnclosingElement();
				ResolutionScope enclosingscope = createTypeBodyResolutionScope(encelem);
				if (enclosingscope == null) {
					return null;
				}
				return new ClassBodyResolutionScope(enclosingscope, type);
			}
			case TOP_LEVEL: {
				ClassHoldingFileData file = elementsToFilesMap.get(type);
				if (file == null) {
					//we need to have the file as it is a top level element
					return null;
				}
				return createTypeBodyResolutionScopeWithFile(type, file);
			}

			case LOCAL:
			case ANONYMOUS:
			default: {
				//can't handle
				return null;
			}
		}
	}

	private ResolutionScope createTypeHeaderResolutionScope(TypeElement type) {
		NestingKind nk = type.getNestingKind();
		switch (nk) {
			case MEMBER: {
				if (!(type instanceof SignaturedElement<?>)) {
					return null;
				}
				TypeElement encelem = (TypeElement) type.getEnclosingElement();
				ResolutionScope enclosingscope = createTypeBodyResolutionScope(encelem);
				if (enclosingscope == null) {
					return null;
				}
				return new ClassHeaderResolutionScope(enclosingscope, type);
			}
			case TOP_LEVEL: {
				ClassHoldingFileData file = elementsToFilesMap.get(type);
				if (file == null) {
					//we need to have the file as it is a top level element
					return null;
				}
				return createTypeHeaderResolutionScopeWithFile(type, file);
			}

			case LOCAL:
			case ANONYMOUS:
			default: {
				//can't handle
				return null;
			}
		}
	}

	@Override
	public IncrementalElement<?> createRecordComponentElement(IncrementalTypeElement recordtype, FieldSignature m) {
		//TODO support in a way that delays this exception? can be useful when cross compiling
		throw new UnsupportedOperationException("cannot create record component element");
	}

	@Override
	public ResolutionScope createResolutionScope(Element resolutionelement) {
		if (resolutionelement == null) {
			return null;
		}
		ElementKind kind = resolutionelement.getKind();
		ClassHoldingFileData file = elementsToFilesMap.get(resolutionelement);
		if (file != null) {
			//either a module, package, or top level type
			if (kind == ElementKind.PACKAGE || ElementKindCompatUtils.isModuleElementKind(kind)) {
				return createUnitResolutionScope(file);
			}
			if (ELEMENT_KIND_TYPES.contains(kind)) {
				//top level type
				return createTypeHeaderResolutionScopeWithFile((TypeElement) resolutionelement, file);
			}
		}
		if (kind == ElementKind.PACKAGE || ElementKindCompatUtils.isModuleElementKind(kind)) {
			//no file found for top level kind.
			return null;
		}
		switch (kind) {
			case ANNOTATION_TYPE:
			case CLASS:
			case ENUM:
			case INTERFACE: {
				return createTypeHeaderResolutionScope((TypeElement) resolutionelement);
			}

			case CONSTRUCTOR:
			case METHOD: {
				//enclosing element must be a type
				TypeElement enclosing = (TypeElement) resolutionelement.getEnclosingElement();
				ResolutionScope enclosingscope = createTypeBodyResolutionScope(enclosing);
				if (enclosingscope == null) {
					return null;
				}

				return new MethodResolutionScope(enclosingscope, (ExecutableElement) resolutionelement);
			}

			case ENUM_CONSTANT:
			case FIELD: {
				//enclosing element must be a type
				//get the resolution scope for it
				TypeElement enclosing = (TypeElement) resolutionelement.getEnclosingElement();
				return createTypeBodyResolutionScope(enclosing);
			}

			case TYPE_PARAMETER: {
				Parameterizable paramedelem = (Parameterizable) ((TypeParameterElement) resolutionelement)
						.getGenericElement();
				if (ELEMENT_KIND_TYPES.contains(paramedelem.getKind())) {
					return createTypeHeaderResolutionScope((TypeElement) paramedelem);
				}
				ResolutionScope enclosingscope = createTypeBodyResolutionScope(
						(TypeElement) paramedelem.getEnclosingElement());
				if (enclosingscope == null) {
					return null;
				}
				return new MethodResolutionScope(enclosingscope, (ExecutableElement) paramedelem);
			}

			case PARAMETER: {
				ExecutableElement method = (ExecutableElement) resolutionelement.getEnclosingElement();
				TypeElement enclosing = (TypeElement) method.getEnclosingElement();
				ResolutionScope enclosingscope = createTypeBodyResolutionScope(enclosing);
				if (enclosingscope == null) {
					return null;
				}
				return new MethodResolutionScope(enclosingscope, method);
			}

			default: {
				if (ElementKindCompatUtils.isRecordComponentElementKind(kind)) {
					return createTypeHeaderResolutionScope((TypeElement) resolutionelement.getEnclosingElement());
				}
				if (ElementKindCompatUtils.isRecordElementKind(kind)) {
					return createTypeHeaderResolutionScope((TypeElement) resolutionelement);
				}
				//unrecognized element type for resolution
				return null;
			}
		}
		//unreachable
	}

	private ResolutionScope createTypeHeaderResolutionScopeWithFile(TypeElement resolutionelement,
			ClassHoldingFileData file) {
		CompilationUnitResolutionScope unitscope = createUnitResolutionScope(file);
		return new ClassHeaderResolutionScope(unitscope, resolutionelement);
	}

	private ResolutionScope createTypeBodyResolutionScopeWithFile(TypeElement resolutionelement,
			ClassHoldingFileData file) {
		CompilationUnitResolutionScope unitscope = createUnitResolutionScope(file);
		return new ClassBodyResolutionScope(unitscope, resolutionelement);
	}

	private CompilationUnitResolutionScope createUnitResolutionScope(ClassHoldingFileData file) {
		CompilationUnitResolutionScope unitscope = new CompilationUnitResolutionScope(file.getImportScope());
		NavigableMap<String, ? extends ClassSignature> fileclasses = file.getClasses();
		if (!ObjectUtils.isNullOrEmpty(fileclasses)) {
			for (ClassSignature cs : fileclasses.values()) {
				String canonicalname = cs.getCanonicalName();
				IncrementalTypeElement type = packageTypesContainer.getParsedTypeElement(canonicalname);
				if (type == null) {
					throw new AssertionError("Internal error, type not found in file: " + file.getPath()
							+ " with canonical name: " + canonicalname);
				}
				unitscope.addDeclaredType(cs.getSimpleName(), new ClassBodyResolutionScope(unitscope, type));
			}
		}
		return unitscope;
	}

	private class TypeSignatureCanonicalNameVisitor implements TypeSignatureVisitor<String, Element> {
		@Override
		public String visitUnresolved(UnresolvedTypeSignature unresolved, Element resolutionelement) {
			ResolutionScope scope;
			ParameterizedTypeSignature enclosingsig = unresolved.getEnclosingSignature();
			if (enclosingsig != null) {
				scope = new EnclosingTypeResolutionScope(enclosingsig, resolutionelement);
			} else {
				scope = createResolutionScope(resolutionelement);
			}
			if (scope == null) {
				return null;
			}
			TypeSignature resolved = scope.resolveTypeSignature(IncrementalElementsTypes8.this,
					unresolved.getUnresolvedName(), Collections.emptyList());
			if (resolved == null) {
				return null;
			}
			return resolved.accept(this, resolutionelement);
		}

		@Override
		public String visitParameterized(ParameterizedTypeSignature parameterized, Element resolutionelement) {
			return parameterized.getCanonicalName();
		}

		@Override
		public String visitEncloser(ParameterizedTypeSignature encloser, Element resolutionelement) {
			return encloser.getEnclosingSignature().accept(this, resolutionelement) + "." + encloser.getSimpleName();
		}

		@Override
		public String visitArray(ArrayTypeSignature array, Element resolutionelement) {
			return null;
		}

		@Override
		public String visitWildcard(WildcardTypeSignature type, Element resolutionelement) {
			return null;
		}

		@Override
		public String visitTypeVariable(TypeVariableTypeSignature type, Element resolutionelement) {
			return null;
		}

		@Override
		public String visitIntersection(IntersectionTypeSignature intersection, Element resolutionelement) {
			return null;
		}

		@Override
		public String visitNoType(NoTypeSignature notype, Element resolutionelement) {
			return null;
		}

		@Override
		public String visitPrimitive(PrimitiveTypeSignature primitive, Element resolutionelement) {
			return null;
		}

		@Override
		public String visitUnion(UnionTypeSignature union, Element resolutionelement) {
			return null;
		}

		@Override
		public String visitUnknown(UnknownTypeSignature unknown, Element resolutionelement) {
			return null;
		}

		@Override
		public String visitNull(NullTypeSignature type, Element resolutionelement) {
			return null;
		}
	}

	private static class TypeMirrorToSignatureVisitor implements DefaultedTypeVisitor<TypeSignature, Void> {
		private final Map<TypeVariable, TypeSignature> convertedVariables = new IdentityHashMap<>();
		protected final ParserCache cache;
		private List<AnnotationSignature> additionalAnnotations;

		public TypeMirrorToSignatureVisitor(ParserCache cache) {
			this.cache = cache;
		}

		public TypeMirrorToSignatureVisitor(ParserCache cache, List<AnnotationSignature> additionalannotations) {
			this.cache = cache;
			this.additionalAnnotations = additionalannotations;
		}

		public static TypeSignature convert(TypeMirror tm, ParserCache cache) {
			return tm.accept(new TypeMirrorToSignatureVisitor(cache), null);
		}

		public static TypeSignature convert(TypeMirror tm, ParserCache cache,
				List<AnnotationSignature> additionalannotations) {
			return tm.accept(new TypeMirrorToSignatureVisitor(cache, additionalannotations), null);
		}

		private List<AnnotationSignature> getAnnotationsForTypeMirror(TypeMirror t) {
			List<AnnotationSignature> annotations = getAnnotationSignaturesForAnnotatedConstruct(t, cache);
			if (additionalAnnotations != null) {
				if (ObjectUtils.isNullOrEmpty(annotations)) {
					annotations = additionalAnnotations;
				} else {
					AnnotationSignature[] narray = annotations
							.toArray(new AnnotationSignature[annotations.size() + additionalAnnotations.size()]);
					int i = annotations.size();
					for (AnnotationSignature as : additionalAnnotations) {
						narray[i++] = as;
					}
					annotations = ImmutableUtils.unmodifiableArrayList(narray);
				}
				additionalAnnotations = null;
			}
			return annotations;
		}

		@Override
		public TypeSignature visitPrimitive(PrimitiveType t, Void p) {
			return PrimitiveTypeSignatureImpl.create(getAnnotationsForTypeMirror(t), t.getKind());
		}

		@Override
		public TypeSignature visitNull(NullType t, Void p) {
			return NullTypeSignatureImpl.create(getAnnotationsForTypeMirror(t));
		}

		@Override
		public TypeSignature visitArray(ArrayType t, Void p) {
			return ArrayTypeSignatureImpl.create(getAnnotationsForTypeMirror(t), t.getComponentType().accept(this, p));
		}

		@Override
		public TypeSignature visitDeclared(DeclaredType t, Void p) {
			TypeElement aselem = (TypeElement) t.asElement();
			TypeMirror enclosing = t.getEnclosingType();
			List<AnnotationSignature> annotations = getAnnotationsForTypeMirror(t);
			if (aselem != null) {
				List<TypeSignature> typeparams = getTypeParameterSignatures(t);

				ParameterizedTypeSignature paramed;
				if (enclosing.getKind() == TypeKind.DECLARED) {
					paramed = TypeReferenceSignatureImpl.create(annotations,
							(ParameterizedTypeSignature) enclosing.accept(this, p),
							cache.string(aselem.getSimpleName()), typeparams);
				} else {
					paramed = CanonicalTypeSignatureImpl.create(cache, annotations,
							cache.string(aselem.getQualifiedName().toString()), typeparams);
				}
				return paramed;
			}
			return UnknownTypeSignatureImpl.create(annotations, t.toString());
		}

		protected List<TypeSignature> getTypeParameterSignatures(DeclaredType t) {
			List<? extends TypeMirror> typeparammirrors = t.getTypeArguments();
			if (ObjectUtils.isNullOrEmpty(typeparammirrors)) {
				return Collections.emptyList();
			}
			List<TypeSignature> typeparams = JavaTaskUtils.cloneImmutableList(typeparammirrors,
					targ -> targ.accept(this, null));
			return typeparams;
		}

		@Override
		public TypeSignature visitError(ErrorType t, Void p) {
			return UnknownTypeSignatureImpl.create(t.toString());
		}

		@Override
		public TypeSignature visitTypeVariable(TypeVariable t, Void p) {
			String varname = cache.string(t.asElement().getSimpleName());
			return TypeVariableTypeSignatureImpl.create(getAnnotationsForTypeMirror(t), varname);

//			BoundedTypeSignatureImpl result = new BoundedTypeSignatureImpl(varname);
////			convertedVariables.put(t, result);
//
//			TypeMirror lower = t.getLowerBound();
//			TypeMirror upper = t.getUpperBound();
//			if (lower != null && lower.getKind() != TypeKind.NULL) {
//				result.setLowerBounds(lower.accept(this, p));
//			}
//			result.setUpperBounds(upper.accept(this, p));
//			addAnnotations(result, t);
//			return result;
		}

		@Override
		public TypeSignature visitWildcard(WildcardType t, Void p) {
			TypeMirror ext = t.getExtendsBound();
			TypeMirror sup = t.getSuperBound();
			TypeSignature upperbounds = ext == null ? null : convert(ext, cache);
			TypeSignature lowerbounds = sup == null ? null : convert(sup, cache);
			return WildcardTypeSignatureImpl.create(cache, getAnnotationsForTypeMirror(t), lowerbounds, upperbounds);
		}

		@Override
		public TypeSignature visitExecutable(ExecutableType t, Void p) {
			return UnknownTypeSignatureImpl.create(getAnnotationsForTypeMirror(t), t.toString());
		}

		@Override
		public TypeSignature visitNoType(NoType t, Void p) {
			return NoTypeSignatureImpl.create(getAnnotationsForTypeMirror(t), t.getKind());
		}

		@Override
		public TypeSignature visitIntersection(IntersectionType t, Void p) {
			List<? extends TypeMirror> bounds = t.getBounds();
			List<TypeSignature> resultbounds = JavaTaskUtils.cloneImmutableList(bounds, b -> b.accept(this, p));
			return IntersectionTypeSignatureImpl.create(getAnnotationsForTypeMirror(t), resultbounds);
		}

		@Override
		public TypeSignature visitUnion(UnionType t, Void p) {
			List<? extends TypeMirror> alternatives = t.getAlternatives();
			List<TypeSignature> resultalts = JavaTaskUtils.cloneImmutableList(alternatives, alt -> alt.accept(this, p));
			return UnionTypeSignatureImpl.create(getAnnotationsForTypeMirror(t), resultalts);
		}

	}

	public static TypeSignature createTypeSignature(TypeMirror paramtm, ParserCache cache) {
		return TypeMirrorToSignatureVisitor.convert(paramtm, cache);
	}

	private static TypeSignature createTypeSignature(TypeMirror paramtm, ParserCache cache,
			List<AnnotationSignature> additionalannotations) {
		return TypeMirrorToSignatureVisitor.convert(paramtm, cache, additionalannotations);
	}

	public static ParameterizedTypeSignature createRawTypeElementSignature(TypeElement typeelement,
			List<? extends TypeSignature> typeparameters) {
		ParameterizedTypeSignature result;
		if (isClassUnrelatedToEnclosing(typeelement)) {
			result = CanonicalTypeSignatureImpl.create(typeelement.getQualifiedName().toString(), typeparameters);
		} else {
			TypeElement enclosingelement = (TypeElement) typeelement.getEnclosingElement();
			ParameterizedTypeSignature enclosingsignature = createRawTypeElementSignature(enclosingelement);
			result = TypeReferenceSignatureImpl.create(enclosingsignature, typeelement.getSimpleName().toString(),
					typeparameters);
		}
		return result;
	}

	public static ParameterizedTypeSignature createRawTypeElementSignature(TypeElement typeelement) {
		return createRawTypeElementSignature(typeelement, Collections.emptyList());
	}

	public static TypeElement findTypeInHierarchy(TypeElement type, String name) {
		return findInHierarchy(type, t -> findDirectlyEnclosedType(t, name));
	}

	private static <R> R foreachFindInSuperClassHierarchyMirror(TypeElement type, DeclaredType supermirror,
			BiFunction<? super TypeElement, ? super DeclaredType, R> function) {
		while (type != null) {
			R res = function.apply(type, supermirror);
			if (res != null) {
				return res;
			}
			res = findInSuperInterfacesMirror(type, function);
			if (res != null) {
				return res;
			}
			TypeMirror supmirror = type.getSuperclass();
			if (supmirror.getKind() != TypeKind.DECLARED) {
				return null;
			}
			supermirror = (DeclaredType) supmirror;
			type = (TypeElement) supermirror.asElement();
		}
		return null;
	}

	public static <R> R findInHierarchy(TypeElement type, Function<? super TypeElement, R> function) {
		while (type != null) {
			R res = function.apply(type);
			if (res != null) {
				return res;
			}
			res = findInSuperInterfaces(type, function);
			if (res != null) {
				return res;
			}
			type = getSuperClassOf(type);
		}
		return null;
	}

	public static <R> R findInSuperInterfacesMirror(TypeElement type,
			BiFunction<? super TypeElement, ? super DeclaredType, R> function) {
		for (TypeMirror itf : type.getInterfaces()) {
			DeclaredType dt = (DeclaredType) itf;
			TypeElement itfelem = (TypeElement) dt.asElement();
			if (itfelem != null) {
				R res = function.apply(itfelem, dt);
				if (res != null) {
					return res;
				}
				res = findInSuperInterfacesMirror(itfelem, function);
				if (res != null) {
					return res;
				}
			}
		}
		return null;
	}

	public static <R> R findInSuperInterfaces(TypeElement type, Function<? super TypeElement, R> function) {
		for (TypeMirror itf : type.getInterfaces()) {
			DeclaredType dt = (DeclaredType) itf;
			TypeElement itfelem = (TypeElement) dt.asElement();
			if (itfelem != null) {
				R res = function.apply(itfelem);
				if (res != null) {
					return res;
				}
				res = findInSuperInterfaces(itfelem, function);
				if (res != null) {
					return res;
				}
			}
		}
		return null;
	}

	public static TypeElement findDirectlyEnclosedType(TypeElement type, String typesimplename) {
		if (type instanceof IncrementalTypeElement) {
			//handle this specially as getting the enclosing elements may cause stackoverflows with records
			IncrementalTypeElement ite = (IncrementalTypeElement) type;
			for (ClassSignature c : ite.getSignature().getEnclosedTypes()) {
				if (!c.getSimpleName().equals(typesimplename)) {
					continue;
				}
				//found a type that matches the name
				return ite.getIncrementalElementsTypes().getLocalPackagesTypesContainer().getTypeElement(c);
			}
			return null;
		}
		for (Element elem : type.getEnclosedElements()) {
			ElementKind kind = elem.getKind();
			if (kind == null || !(kind.isClass() || kind.isInterface())) {
				continue;
			}
			if (elem.getSimpleName().contentEquals(typesimplename)) {
				return (TypeElement) elem;
			}
		}
		return null;
	}

	public static VariableElement findDirectlyEnclosedVariable(TypeElement type, String variablename) {
		for (Element elem : type.getEnclosedElements()) {
			ElementKind kind = elem.getKind();
			if (kind == null || !kind.isField()) {
				continue;
			}
			if (elem.getSimpleName().contentEquals(variablename)) {
				return (VariableElement) elem;
			}
		}
		return null;
	}

	private DeclaredType getSuperCorrectParameterizedTypeMirrorImpl(TypeElement supertype,
			Map<TypeParameterElement, TypeMirror> parametermirrors) {
		final TypeMirror enclosing;
		if (isClassUnrelatedToEnclosing(supertype)) {
			enclosing = IncrementalNoType.INSTANCE_NONE;
		} else {
			enclosing = getSuperCorrectParameterizedTypeMirrorImpl((TypeElement) supertype.getEnclosingElement(),
					parametermirrors);
		}
		List<? extends TypeParameterElement> tparams = supertype.getTypeParameters();
		int tparamsize = tparams.size();
		if (tparamsize == 0) {
			return new SimpleDeclaredType(this, enclosing, supertype, Collections.emptyList());
		}
		TypeMirror[] typeArgs = new TypeMirror[tparamsize];
		List<TypeMirror> arglist = ImmutableUtils.asUnmodifiableArrayList(typeArgs);
		int i = 0;
		for (TypeParameterElement tpe : tparams) {
			TypeMirror tm = parametermirrors.get(tpe);
			if (tm == null) {
				arglist = Collections.emptyList();
				break;
			}
			typeArgs[i++] = tm;
		}
		return new SimpleDeclaredType(this, enclosing, supertype, arglist);
	}

	@Override
	public DeclaredType getSuperCorrectParameterizedTypeMirror(DeclaredType type, TypeElement supertype) {
		return getSuperCorrectParameterizedTypeMirrorImpl(supertype, getTypeParameterMapResolve(type));
	}

	public ParameterizedTypeSignature resolveSignatureInScope(TypeElement element, Iterator<String> names,
			List<? extends TypeSignature> typeparameters) {
		return resolveSignatureInScope(element, (DeclaredType) element.asType(), names, typeparameters);
	}

	public ParameterizedTypeSignature resolveSignatureInScope(TypeElement element, DeclaredType elementmirror,
			Iterator<String> names, List<? extends TypeSignature> typeparameters) {
		if (!names.hasNext()) {
			throw new IllegalArgumentException("Name iterator is empty.");
		}
		Map<TypeParameterElement, TypeMirror> parametermirrors = new IdentityHashMap<>();
		putDeclaredTypeTypeArgumentMirrorsDirectWithEnclosing(element, elementmirror, parametermirrors);
		while (names.hasNext()) {
			String nextname = names.next();
			TypeElement found = foreachFindInSuperClassHierarchyMirror(element, elementmirror, (t, m) -> {
				TypeElement directenclosed = findDirectlyEnclosedType(t, nextname);
				if (directenclosed == null) {
					return null;
				}
				putTypeArgumentMirrorsResolve(t, m, parametermirrors);
				return directenclosed;
			});
			if (found == null) {
				return null;
			}
			element = found;
			elementmirror = (DeclaredType) found.asType();
		}
		//create signature based on the element
		return createTypeSignature(element, typeparameters, parametermirrors);
	}

	public static ParameterizedTypeSignature createTypeElementSignature(TypeElement typeelement,
			List<? extends TypeSignature> typeparameters) {
		List<TypeSignature> clonedtypeparameters = ImmutableUtils.makeImmutableList(typeparameters);
		if (isClassUnrelatedToEnclosing(typeelement)) {
			return CanonicalTypeSignatureImpl.create(typeelement.getQualifiedName().toString(), clonedtypeparameters);
		}
		ParameterizedTypeSignature enclosingsignature;
		TypeElement enclosingelement = (TypeElement) typeelement.getEnclosingElement();
		if (typeparameters.size() == typeelement.getTypeParameters().size()) {
			List<? extends TypeParameterElement> enclosingtypeparams = enclosingelement.getTypeParameters();
			List<TypeSignature> enclosingtparamssignatures = JavaTaskUtils.cloneImmutableList(enclosingtypeparams,
					tpe -> TypeVariableTypeSignatureImpl.create(tpe.getSimpleName().toString()));
			enclosingsignature = createTypeElementSignature(enclosingelement, enclosingtparamssignatures);
		} else {
			enclosingsignature = createRawTypeElementSignature(enclosingelement);
		}
		return TypeReferenceSignatureImpl.create(enclosingsignature, typeelement.getSimpleName().toString(),
				clonedtypeparameters);
	}

	private ParameterizedTypeSignature createTypeSignature(TypeElement target,
			List<? extends TypeSignature> typeparameters, Map<TypeParameterElement, TypeMirror> typeparametermirrors) {
		List<TypeSignature> clonedtypeparameters = ImmutableUtils.makeImmutableList(typeparameters);
		if (isClassUnrelatedToEnclosing(target)) {
			return CanonicalTypeSignatureImpl.create(cache, cache.string(target.getQualifiedName()),
					clonedtypeparameters);
		}
		if (typeparameters.size() != target.getTypeParameters().size()) {
			//type is raw
			return IncrementalElementsTypes.createRawTypeElementSignature(target);
		}
		TypeElement enclosingelement = (TypeElement) target.getEnclosingElement();
		List<? extends TypeParameterElement> enclosingtypeparameters = enclosingelement.getTypeParameters();
		ArrayList<TypeSignature> nenclosingtypeparamsigs = new ArrayList<>(enclosingtypeparameters.size());
		List<TypeSignature> enclosingtypeparametersignatures = ImmutableUtils.unmodifiableList(nenclosingtypeparamsigs);

		for (TypeParameterElement etpe : enclosingtypeparameters) {
			TypeMirror paramtm = typeparametermirrors.get(etpe);
			if (paramtm != null) {
				nenclosingtypeparamsigs.add(createTypeSignature(paramtm, cache));
			} else {
				enclosingtypeparametersignatures = Collections.emptyList();
				break;
			}
		}

		ParameterizedTypeSignature enclosingsignature = createTypeSignature(enclosingelement,
				enclosingtypeparametersignatures, typeparametermirrors);
		return TypeReferenceSignatureImpl.create(enclosingsignature, cache.string(target.getSimpleName()),
				clonedtypeparameters);
	}

	private void putDeclaredTypeTypeArgumentMirrorsDirectWithEnclosing(TypeElement elem, DeclaredType type,
			Map<TypeParameterElement, TypeMirror> typeparameters) {
		foreachEnclosingDeclaredType(elem, type, (e, t) -> putTypeArgumentMirrorsDirect(e, t, typeparameters));
	}

	private static void putTypeArgumentMirrorsResolveOptionalWithEnclosing(TypeElement elem, DeclaredType type,
			Map<TypeParameterElement, TypeMirror> typeparameters) {
		foreachEnclosingDeclaredType(elem, type, (e, t) -> putTypeArgumentMirrorsResolveOptional(e, t, typeparameters));
	}

	private static void foreachEnclosingDeclaredType(TypeElement elem, DeclaredType type,
			BiConsumer<? super TypeElement, ? super DeclaredType> consumer) {
		//iterate via recursion, enclosing first order
		TypeMirror enclosing = type.getEnclosingType();
		if (enclosing.getKind() == TypeKind.DECLARED) {
			DeclaredType edt = (DeclaredType) enclosing;
			TypeElement eelem = (TypeElement) edt.asElement();
			if (eelem != null) {
				foreachEnclosingDeclaredType(eelem, edt, consumer);
			}
		}
		consumer.accept(elem, type);
	}

	private Map<TypeParameterElement, TypeMirror> getTypeParameterMapDirect(DeclaredType type) {
		Map<TypeParameterElement, TypeMirror> result = new IdentityHashMap<>();
		collectTypeParameterMapDirect(type, result);
		return result;
	}

	private Map<TypeParameterElement, TypeMirror> getTypeParameterMapResolve(TypeElement elem) {
		return getTypeParameterMapResolve((DeclaredType) elem.asType());
	}

	private Map<TypeParameterElement, TypeMirror> getTypeParameterMapResolve(DeclaredType type) {
		Map<TypeParameterElement, TypeMirror> result = new IdentityHashMap<>();
		collectTypeParameterMapResolve(type, result);
		return result;
	}

	private void collectTypeParameterMapDirect(DeclaredType type,
			Map<TypeParameterElement, TypeMirror> typeparameters) {
		TypeElement elem = (TypeElement) type.asElement();
		if (elem == null) {
			return;
		}
		collectTypeParameterMapDirect(type, elem, typeparameters);
	}

	private void collectTypeParameterMapDirect(TypeElement elem, Map<TypeParameterElement, TypeMirror> typeparameters) {
		collectTypeParameterMapDirect((DeclaredType) elem.asType(), elem, typeparameters);
	}

	private void collectTypeParameterMapDirect(DeclaredType type, TypeElement elem,
			Map<TypeParameterElement, TypeMirror> typeparameters) {
		foreachEnclosingDeclaredType(elem, type, (e, t) -> {
			List<? extends TypeParameterElement> tparams = e.getTypeParameters();
			Iterator<? extends TypeMirror> argit = t.getTypeArguments().iterator();
			for (TypeParameterElement tpe : tparams) {
				if (!argit.hasNext()) {
					break;
				}
				TypeMirror arg = argit.next();
				typeparameters.put(tpe, arg);
			}
		});
	}

	private void collectTypeParameterMapResolve(DeclaredType type,
			Map<TypeParameterElement, TypeMirror> typeparameters) {
		TypeElement elem = (TypeElement) type.asElement();
		if (elem == null) {
			return;
		}
		collectTypeParameterMapResolve(type, elem, typeparameters);
	}

	private void collectTypeParameterMapResolve(TypeElement elem,
			Map<TypeParameterElement, TypeMirror> typeparameters) {
		collectTypeParameterMapResolve((DeclaredType) elem.asType(), elem, typeparameters);
	}

	private void collectTypeParameterMapResolve(DeclaredType type, TypeElement elem,
			Map<TypeParameterElement, TypeMirror> typeparameters) {
		foreachSuperClassMirror(type, elem, (sdt, se) -> {
			if (sdt == type) {
				//put the first arguments directly
				collectTypeParameterMapDirect(sdt, se, typeparameters);
			} else {
				foreachEnclosingDeclaredType(se, sdt, (e, t) -> {
					List<? extends TypeParameterElement> tparams = e.getTypeParameters();
					Iterator<? extends TypeMirror> argit = t.getTypeArguments().iterator();
					for (TypeParameterElement tpe : tparams) {
						if (!argit.hasNext()) {
							break;
						}
						TypeMirror arg = argit.next();
						if (arg.getKind() == TypeKind.TYPEVAR) {
							Element tvelem = ((TypeVariable) arg).asElement();
							TypeMirror substitute = typeparameters.get(tvelem);
							if (substitute != null) {
								arg = substitute;
							} else {
								//type variable not found with the corresponding element
								//probably because type is raw
								//can happen in the following scenario:
								//type is simply Sub
								//   class Sub<T> extends Super<T> { }
								//   class Super<ST> extends SuperSuper<ST> { }
								//here when we examine SuperSuper<ST>, we will not find a mirror for ST, because Sub was raw 
								continue;
							}
						}
						TypeMirror prev = typeparameters.put(tpe, arg);
						if (prev != null && prev != arg) {
							//can happen in case of declaring an inheritance multiple times
							//for example:
							//    class A extends Supplier<Thread> {}
							//    class B extends A implements Supplier<Thread> {}
							//this puts Thread twice to Supplier.T type argument
							throw new IllegalStateException(
									"Type parameter set multiple times: " + tpe.getEnclosingElement().getSimpleName()
											+ "." + tpe + ": " + arg + " previously: " + prev + " in " + type);
						}
					}
				});
			}
		});
	}

	private void putTypeArgumentMirrorsDirect(Parameterizable paramable, TypeMirror type,
			Map<TypeParameterElement, TypeMirror> typeparameters) {
		if (type.getKind() != TypeKind.DECLARED) {
			return;
		}
		DeclaredType declared = (DeclaredType) type;
		Iterator<? extends TypeMirror> argit = declared.getTypeArguments().iterator();
		for (TypeParameterElement tpe : paramable.getTypeParameters()) {
			if (!argit.hasNext()) {
				break;
			}
			TypeMirror arg = argit.next();
			if (arg == null) {
				continue;
			}
			if (arg.getKind() == TypeKind.TYPEVAR) {
				TypeParameterElement typeparamelem = (TypeParameterElement) ((TypeVariable) arg).asElement();
				arg = new IncrementalTypeVariable(this,
						TypeVariableTypeSignatureImpl.create(cache.string(typeparamelem.getSimpleName())),
						typeparamelem);
			}
			typeparameters.put(tpe, arg);
		}
	}

	private static void putTypeArgumentMirrorsResolve(Parameterizable paramable, DeclaredType declared,
			Map<TypeParameterElement, TypeMirror> typeparameters) {
		Iterator<? extends TypeMirror> argit = declared.getTypeArguments().iterator();
		for (TypeParameterElement tpe : paramable.getTypeParameters()) {
			if (!argit.hasNext()) {
				break;
			}
			TypeMirror arg = argit.next();
			if (arg == null) {
				continue;
			}
			if (arg.getKind() == TypeKind.TYPEVAR) {
				TypeVariable tv = (TypeVariable) arg;
				TypeParameterElement tvelem = (TypeParameterElement) tv.asElement();
				TypeMirror tvparam = typeparameters.get(tvelem);
				if (tvparam != null) {
					arg = tvparam;
				} else {
					//failed to resolve type variable to element
					continue;
				}
			}
			TypeMirror prev = typeparameters.put(tpe, arg);
			if (prev != null && prev != arg) {
				//XXX log and warn somehow?
//				System.out.println(
//						"IncrementalElementsTypes8.putTypeArgumentMirrorsResolve() WARNING type parameterized multiple times "
//								+ paramable + " with " + arg + " and " + prev + " ==: " + (prev == arg) + " - "
//								+ prev.getKind() + " - " + arg.getKind() + " for " + tpe.getSimpleName());
			}
		}
	}

	private static void putTypeArgumentMirrorsResolveOptional(Parameterizable paramable, DeclaredType declared,
			Map<TypeParameterElement, TypeMirror> typeparameters) {
		Iterator<? extends TypeMirror> argit = declared.getTypeArguments().iterator();
		for (TypeParameterElement tpe : paramable.getTypeParameters()) {
			if (!argit.hasNext()) {
				break;
			}
			TypeMirror arg = argit.next();
			if (arg == null) {
				continue;
			}
			if (arg.getKind() == TypeKind.TYPEVAR) {
				TypeVariable tv = (TypeVariable) arg;
				TypeParameterElement tvelem = (TypeParameterElement) tv.asElement();
				TypeMirror tvparam = typeparameters.get(tvelem);
				if (tvparam != null) {
					arg = tvparam;
				}
			}
			TypeMirror prev = typeparameters.put(tpe, arg);
			if (prev != null && prev != arg) {
				//XXX log and warn somehow?
//				System.out.println(
//						"IncrementalElementsTypes8.putTypeArgumentMirrorsResolveOptional() WARNING type parameterized multiple times "
//								+ paramable + " with " + arg + " and " + prev + " ==: " + (prev == arg) + " - "
//								+ prev.getKind() + " - " + arg.getKind() + " for " + tpe.getSimpleName());
			}
		}
	}

	private static void foreachSuperClassMirror(DeclaredType elemtype, TypeElement element,
			BiConsumer<? super DeclaredType, ? super TypeElement> elemconsumer) {
		while (element != null) {
			elemconsumer.accept(elemtype, element);
			for (TypeMirror itf : element.getInterfaces()) {
				DeclaredType dt = (DeclaredType) itf;
				TypeElement itfelem = (TypeElement) dt.asElement();
				foreachSuperClassMirror(dt, itfelem, elemconsumer);
			}
			TypeMirror superclass = element.getSuperclass();
			if (superclass.getKind() != TypeKind.DECLARED) {
				break;
			}
			elemtype = (DeclaredType) superclass;
			element = (TypeElement) elemtype.asElement();
		}
	}

	public static void foreachSuperClass(TypeElement element, Consumer<? super TypeElement> elemconsumer) {
		while (element != null) {
			elemconsumer.accept(element);
			for (TypeMirror itf : element.getInterfaces()) {
				DeclaredType dt = (DeclaredType) itf;
				TypeElement itfelem = (TypeElement) dt.asElement();
				foreachSuperClass(itfelem, elemconsumer);
			}
			element = getSuperClassOf(element);
		}
	}

	public TypeMirror createIntersectionType(List<? extends TypeMirror> bounds) {
		if (bounds.isEmpty()) {
			throw new IllegalArgumentException("Bounds is empty");
		}
		if (bounds.size() == 1) {
			return bounds.get(0);
		}
		return new SimpleIntersectionType(this, bounds);
	}

	private void addIntersectionTypeRecursive(TypeMirror type, List<TypeMirror> result) {
		if (type.getKind() == TypeKind.INTERSECTION) {
			addIntersectionTypeRecursive(((IntersectionType) type).getBounds(), result);
		} else {
			for (TypeMirror tm : result) {
				if (isSubtype(tm, type)) {
					//we dont need to add anything as type is already contained in tm
					return;
				}
			}
			//we are going to add type to the list
			//check if we need to remove anything in the result list
			//this might be the case when e.g.:
			//    type == java.lang.Thread
			//    result == [java.lang.Runnable, java.util.function.Supplier]
			//in this case when we add thread, we can remove the runnable type as thread already a subtype of it
			for (Iterator<TypeMirror> it = result.iterator(); it.hasNext();) {
				TypeMirror tm = it.next();
				if (isSubtype(type, tm)) {
					it.remove();
				}
			}
			result.add(type);
		}
	}

	private void addIntersectionTypeRecursive(List<? extends TypeMirror> bounds, List<TypeMirror> result) {
		for (TypeMirror b : bounds) {
			addIntersectionTypeRecursive(b, result);
		}
	}

	@Override
	public List<TypeMirror> createCaptureWildcardElementBounds(TypeMirror type) {
		if (type.getKind() != TypeKind.INTERSECTION) {
			return ImmutableUtils.singletonList(type);
		}
		List<TypeMirror> result = new ArrayList<>();
		IntersectionType it = (IntersectionType) type;
		flattenOutIntersection(it, result);
		return result;
	}

	private static void flattenOutIntersection(IntersectionType it, List<TypeMirror> result) {
		for (TypeMirror tm : it.getBounds()) {
			if (tm.getKind() == TypeKind.INTERSECTION) {
				flattenOutIntersection((IntersectionType) tm, result);
			} else {
				result.add(tm);
			}
		}
	}

	@Override
	public TypeMirror captureTypeParameter(TypeMirror type) {
		if (type.getKind() != TypeKind.WILDCARD) {
			return type;
		}
		return createCapturedWildCard((CommonWildcardType) type);
	}

	private TypeMirror createCapturedWildCard(CommonWildcardType wc) {
		TypeParameterElement correspondingtypeparameter = wc.getCorrespondingTypeParameter();
		TypeMirror wcext = wc.getExtendsBound();
		TypeMirror wcsup = wc.getSuperBound();

		final TypeMirror upperbound;
		final TypeMirror lowerbound;

		TypeVariable elemastype = (TypeVariable) correspondingtypeparameter.asType();
		TypeMirror originalupperbound = elemastype.getUpperBound();

		if (wcext != null) {
			//wildcard is "? extends " format
			List<TypeMirror> upperboundlist = new ArrayList<>();
			addIntersectionTypeRecursive(wcext, upperboundlist);
			addIntersectionTypeRecursive(originalupperbound, upperboundlist);
			upperbound = createIntersectionType(upperboundlist);
			lowerbound = IncrementalNullType.INSTANCE;
		} else if (wcsup != null) {
			//wildcard is "? super " format
			if (isSameType(wcsup, originalupperbound)) {
				//if the wildcard is something like ? super X
				//and the corresponding parameter type is ? extends X
				//then the wildcard must correspond to the type X
				return wcsup;
			}
			lowerbound = wcsup;
			upperbound = originalupperbound;
		} else {
			//wildcard is just a single "?"
			lowerbound = IncrementalNullType.INSTANCE;
			upperbound = originalupperbound;
		}

		CapturedTypeVariable result = new CapturedTypeVariable(this, wc, upperbound, lowerbound);
		return result;
	}

	@Override
	public boolean isWildcardConstrainedToSingleType(CommonWildcardType wc) {
		TypeMirror ext = wc.getExtendsBound();
		TypeMirror sup = wc.getSuperBound();
		if (ext != null) {
			return isSameType(ext, sup);
		}
		TypeParameterElement tpe = wc.getCorrespondingTypeParameter();
		if (tpe == null) {
			return isSameType(ext, sup);
		}
		List<? extends TypeMirror> bounds = tpe.getBounds();
		//only sup and bounds
		return bounds.size() == 1 && isSameType(bounds.get(0), sup);
	}

	public static List<AnnotationSignature> getAnnotationSignaturesForAnnotatedConstruct(AnnotatedConstruct e,
			ParserCache cache) {
		return getAnnotationSignaturesForMirrors(e.getAnnotationMirrors(), cache);
	}

	protected static List<AnnotationSignature> getAnnotationSignaturesForMirrors(
			List<? extends AnnotationMirror> annotations, ParserCache cache) {
		if (ObjectUtils.isNullOrEmpty(annotations)) {
			return Collections.emptyList();
		}
		List<AnnotationSignature> result = JavaTaskUtils.cloneImmutableList(annotations,
				am -> annotationMirrorToSignature(am, cache));
		return result;
	}

	private static class ElementToSignatureVisitor implements DefaultedElementVisitor<Signature, Void> {
		private final ParserCache cache;
		private final Signature enclosingSignature;

		public ElementToSignatureVisitor(ParserCache cache, Signature enclosingSignature) {
			this.cache = cache;
			this.enclosingSignature = enclosingSignature;
		}

		public static Signature convert(Element elem, ParserCache cache) {
			return convert(elem, cache, null);
		}

		public static Signature convert(Element elem, ParserCache cache, Signature enclosingsignature) {
			return elem.accept(new ElementToSignatureVisitor(cache, enclosingsignature), null);
		}

		protected List<TypeParameterSignature> getTypeParametersSignatures(Parameterizable e) {
			return getTypeParametersSignatures(e.getTypeParameters());
		}

		protected List<TypeParameterSignature> getTypeParametersSignatures(
				List<? extends TypeParameterElement> tparamelems) {
			if (ObjectUtils.isNullOrEmpty(tparamelems)) {
				return Collections.emptyList();
			}

			//can we directly call the visit method instead of calling convert?
			List<TypeParameterSignature> typeparams = JavaTaskUtils.cloneImmutableList(tparamelems,
					tpe -> (TypeParameterSignature) convert(tpe, cache));
			return typeparams;
		}

//		private String getSimpleNameOf(TypeElement e) {
//			NestingKind nesting = e.getNestingKind();
//			switch (nesting) {
//				case ANONYMOUS:
//				case LOCAL: {
//					TypeElement enc = getEnclosingTypeElement(e);
//					String encbin = realElements.getBinaryName(enc).toString();
//					String bin = realElements.getBinaryName(e).toString();
//					if (!bin.startsWith(encbin)) {
//						throw new IllegalStateException("Binary name doesnt start with enclosing binary name: " + encbin + " - " + bin);
//					}
//					return bin.substring(encbin.length(), bin.length());
//				}
//				case MEMBER:
//				case TOP_LEVEL:
//				default: {
//					return e.getSimpleName().toString();
//				}
//			}
//		}
		@Override
		public Signature visitModuleCompat(ModuleElementCompat moduleElement, Void p) {
			return JavaModelUtils.moduleElementToSignature(moduleElement.getRealObject(), cache);
		}

		@Override
		public Signature visitRecordComponentElementCompat(RecordComponentElementCompat e, Void p) {
			Element realelem = e.getRealObject();
			List<AnnotationSignature> annotationsignatures = getAnnotationSignaturesForAnnotatedConstruct(realelem,
					cache);
			TypeSignature variabletypesignature = createTypeSignature(realelem.asType(), cache, annotationsignatures);
			return FieldSignatureImpl.createRecordComponent(realelem.getModifiers(), variabletypesignature,
					realelem.getSimpleName().toString(), null);
		}

		@Override
		public PackageSignature visitPackage(PackageElement e, Void p) {
			PackageSignature result = PackageSignatureImpl.create(
					getAnnotationSignaturesForAnnotatedConstruct(e, cache), cache.string(e.getQualifiedName()), null);
			return result;
		}

		@Override
		public ClassSignature visitType(TypeElement e, Void p) {
			List<? extends Element> enclosedelems = e.getEnclosedElements();
			TypeMirror sc = e.getSuperclass();
			PackageElement pack = getPackageOfImpl(e);
			ElementKind kind = e.getKind();
			NestingKind nestingkind = e.getNestingKind();

			String name = cache.string(e.getSimpleName());
			Set<Modifier> modifiers = e.getModifiers();
			List<TypeParameterSignature> typeparamsignatures = getTypeParametersSignatures(e);
			List<AnnotationSignature> annotationsignatures = getAnnotationSignaturesForAnnotatedConstruct(e, cache);
			String packname = pack.isUnnamed() ? null : cache.string(pack.getQualifiedName());

			List<ClassMemberSignature> members = new ArrayList<>();

			TypeSignature superclasssignature;
			if (sc.getKind() != TypeKind.NONE) {
				superclasssignature = createTypeSignature(sc, cache);
			} else {
				superclasssignature = null;
			}
			List<TypeSignature> superinterfacesignatures = JavaTaskUtils.cloneImmutableList(e.getInterfaces(),
					tm -> createTypeSignature(tm, cache));

			PermittedSubclassesList permittedsubclasses;
			if (modifiers.contains(ImmutableModifierSet.MODIFIER_SEALED)) {
				List<? extends TypeMirror> psc = JavaCompilationUtils.getPermittedSubclasses(e);
				permittedsubclasses = new ExplicitPermittedSubclassesList(
						JavaTaskUtils.cloneImmutableList(psc, tm -> createTypeSignature(tm, cache)));
			} else {
				permittedsubclasses = null;
			}
			ClassSignature result = ClassSignatureImpl.create(modifiers, packname, name, members,
					(ClassSignature) enclosingSignature, superclasssignature, superinterfacesignatures, kind,
					nestingkind, typeparamsignatures, annotationsignatures, null, permittedsubclasses);

			if (!enclosedelems.isEmpty()) {
				for (Element enclosed : enclosedelems) {
					members.add((ClassMemberSignature) convert(enclosed, cache, result));
				}
			}

			return result;
		}

		@Override
		public AnnotatedSignature visitVariable(VariableElement e, Void p) {
			ElementKind ekind = e.getKind();
			List<AnnotationSignature> annotationsignatures = getAnnotationSignaturesForAnnotatedConstruct(e, cache);
			TypeSignature variabletypesignature = createTypeSignature(e.asType(), cache, annotationsignatures);
			String variablename = cache.string(e.getSimpleName());
			switch (ekind) {
				case FIELD: {
					Object cval = e.getConstantValue();
					FieldSignature field = FieldSignatureImpl.createField(e.getModifiers(), variabletypesignature,
							variablename, cval == null ? null : cache.literalConstantResolver(cval), null);
					return field;
				}
				case ENUM_CONSTANT: {
					FieldSignature field = FieldSignatureImpl.createEnumSignature(variabletypesignature, variablename,
							null);
					return field;
				}
				case PARAMETER: {
					MethodParameterSignature paramsig = MethodParameterSignatureImpl.create(e.getModifiers(),
							variabletypesignature, variablename);
					return paramsig;
				}
				default: {
					throw new IllegalArgumentException("Unknown kind: " + ekind);
				}
			}
		}

		@Override
		public MethodSignature visitExecutable(ExecutableElement e, Void p) {
			List<AnnotationSignature> annotationsignatures = getAnnotationSignaturesForAnnotatedConstruct(e, cache);

			TypeMirror rec = e.getReceiverType();
			List<? extends VariableElement> params = e.getParameters();
			List<? extends TypeMirror> throwntypes = e.getThrownTypes();
			boolean varag = e.isVarArgs();
			Set<Modifier> modifiers = e.getModifiers();
			String name = cache.string(e.getSimpleName());
			TypeSignature returntypesignature = createTypeSignature(e.getReturnType(), cache, annotationsignatures);
			ElementKind methodkind = e.getKind();
			TypeSignature receiversignature = getReceiverSignature(rec);
			List<TypeSignature> throwntypesignatures = getThrownTypeSignatures(throwntypes);
			Value defaultvaluesignature = getAnnotationValueSignature(e.getDefaultValue());
			List<TypeParameterSignature> typeparametersignatures = getTypeParametersSignatures(e);

			final List<MethodParameterSignature> methodparamsignatures = params.isEmpty() ? Collections.emptyList()
					: new ArrayList<>(params.size());

			MethodSignature result = FullMethodSignature.create(name, modifiers, methodparamsignatures,
					throwntypesignatures, returntypesignature, defaultvaluesignature, methodkind,
					typeparametersignatures, receiversignature, varag, null);

			if (!params.isEmpty()) {
				for (VariableElement param : params) {
					methodparamsignatures.add((MethodParameterSignature) convert(param, cache, result));
				}
			}

			return result;
		}

		protected Value getAnnotationValueSignature(AnnotationValue defval) {
			if (defval == null) {
				return null;
			}
			return AnnotationValueToSignatureConverterVisitor.convert(defval, cache);
		}

		protected List<TypeSignature> getThrownTypeSignatures(List<? extends TypeMirror> throwntypes) {
			if (ObjectUtils.isNullOrEmpty(throwntypes)) {
				return Collections.emptyList();
			}
			List<TypeSignature> throwntypesignatures = JavaTaskUtils.cloneImmutableList(throwntypes,
					tt -> createTypeSignature(tt, cache));
			return throwntypesignatures;
		}

		protected TypeSignature getReceiverSignature(TypeMirror rec) {
			TypeSignature receiversignature;
			if (rec != null) {
				receiversignature = createTypeSignature(rec, cache);
			} else {
				receiversignature = null;
			}
			return receiversignature;
		}

		@Override
		public TypeParameterSignature visitTypeParameter(TypeParameterElement e, Void p) {
			List<AnnotationSignature> annotations = getAnnotationSignaturesForAnnotatedConstruct(e, cache);

			TypeSignature upperbound;

			List<? extends TypeMirror> bounds = e.getBounds();
			int boundssize = bounds.size();
			if (boundssize == 0) {
				upperbound = SimpleCanonicalTypeSignature.INSTANCE_JAVA_LANG_OBJECT;
			} else {
				if (boundssize == 1) {
					upperbound = createTypeSignature(bounds.get(0), cache);
				} else {
					List<TypeSignature> resultbounds = JavaTaskUtils.cloneImmutableList(bounds,
							btm -> createTypeSignature(btm, cache));
					upperbound = IntersectionTypeSignatureImpl.create(resultbounds);
				}
			}

			return TypeParameterSignatureImpl.create(annotations, cache.string(e.getSimpleName()), null, upperbound);
		}

	}

	public static Signature createSignatureFromJavacElement(Element e, ParserCache cache) {
		return ElementToSignatureVisitor.convert(e, cache);
	}

	public TypeElement getTopLevelEnclosingType(Element e) {
		TypeElement type;
		while (true) {
			type = getEnclosingTypeElement(e);
			if (type == null) {
				break;
			}
			if (type.getNestingKind() == NestingKind.TOP_LEVEL) {
				return type;
			}
			e = type;
		}
		return null;
	}

	@Override
	public boolean isJavacElementBridge(ExecutableElement ee) {
		return false;
	}
}
