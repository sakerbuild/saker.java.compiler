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
package saker.java.compiler.impl;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.nio.file.Path;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
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
import javax.lang.model.type.ReferenceType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.FileEntry;
import saker.build.file.provider.LocalFileProvider;
import saker.build.thirdparty.saker.rmi.connection.MethodTransferProperties;
import saker.build.thirdparty.saker.rmi.connection.RMITransferProperties;
import saker.build.thirdparty.saker.rmi.io.RMIObjectInput;
import saker.build.thirdparty.saker.rmi.io.RMIObjectOutput;
import saker.build.thirdparty.saker.rmi.io.wrap.RMIWrapper;
import saker.build.thirdparty.saker.rmi.io.writer.WrapperRMIObjectWriteHandler;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.ReflectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.rmi.wrap.RMIArrayListRemoteElementWrapper;
import saker.build.thirdparty.saker.util.rmi.wrap.RMIIdentityHashSetRemoteElementWrapper;
import saker.build.thirdparty.saker.util.rmi.wrap.RMILinkedHashSetStringElementWrapper;
import saker.build.thirdparty.saker.util.rmi.wrap.RMITreeSetStringElementWrapper;
import saker.java.compiler.impl.compile.handler.incremental.IncrementalCompilationHandler;
import saker.java.compiler.impl.compile.handler.invoker.rmi.ModifierEnumSetRMIWrapper;
import saker.java.compiler.impl.compile.handler.invoker.rmi.NameRMIWrapper;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.jdk.impl.JavaCompilationUtils;

public class JavaUtil {

	private JavaUtil() {
		throw new UnsupportedOperationException();
	}

	private static final Set<String> INVALID_PROCESSOR_INPUT_LOCATION_NAMES = new TreeSet<>();
	static {
		for (StandardLocation l : StandardLocation.values()) {
			INVALID_PROCESSOR_INPUT_LOCATION_NAMES.add(l.getName());
		}
	}

	public static boolean isValidProcessorInputLocationName(String name) {
		return !INVALID_PROCESSOR_INPUT_LOCATION_NAMES.contains(name);
	}

	public static String getDescriptorString(TypeMirror type, Elements elements) {
		TypeKind kind = type.getKind();
		switch (kind) {
			case VOID:
				return "V";
			case BOOLEAN:
				return "Z";
			case BYTE:
				return "B";
			case SHORT:
				return "S";
			case CHAR:
				return "C";
			case INT:
				return "I";
			case LONG:
				return "J";
			case FLOAT:
				return "F";
			case DOUBLE:
				return "D";
			case ARRAY:
				return "[" + getDescriptorString(((ArrayType) type).getComponentType(), elements);
			case DECLARED: {
				DeclaredType dt = (DeclaredType) type;
				TypeElement typeelem = (TypeElement) dt.asElement();
				StringBuilder sb = new StringBuilder();
				sb.append('L');
				//convert to internal name
				sb.append(elements.getBinaryName(typeelem).toString().replace('.', '/'));
				sb.append(';');
				return sb.toString();
			}
			case UNION:
			case WILDCARD:
			case INTERSECTION:
			case EXECUTABLE:
			case NONE:
			case NULL:
			case OTHER:
			case PACKAGE:
			case ERROR:
			default: {
				throw new IllegalArgumentException("Illegal type for descriptor: " + kind + " as " + type);
			}
		}
	}

	public static String toTypeString(TypeMirror type, Elements elements) {
		return toTypeString(type, elements, true);
	}

	public static String toTypeString(TypeMirror type, Elements elements, boolean withbounds) {
		TypeKind kind = type.getKind();
		switch (kind) {
			case ARRAY:
				return toTypeString(((ArrayType) type).getComponentType(), elements, false) + "[]";
			case BOOLEAN:
				return "boolean";
			case BYTE:
				return "byte";
			case CHAR:
				return "char";
			case DECLARED: {
				DeclaredType dt = (DeclaredType) type;
				TypeElement typeelem = (TypeElement) dt.asElement();
				List<? extends TypeMirror> targs = dt.getTypeArguments();
				TypeMirror dtenc = dt.getEnclosingType();
				StringBuilder sb = new StringBuilder();
				if (dtenc != null && dtenc.getKind() != TypeKind.NONE) {
					sb.append(toTypeString(dtenc, elements, false));
					sb.append('.');
				}
				sb.append(elements.getBinaryName(typeelem).toString());
				if (!targs.isEmpty()) {
					sb.append('<');
					for (Iterator<? extends TypeMirror> it = targs.iterator(); it.hasNext();) {
						TypeMirror a = it.next();
						sb.append(toTypeString(a, elements, false));
						if (it.hasNext()) {
							sb.append(", ");
						}
					}
					sb.append('>');
				}
				return sb.toString();
			}
			case DOUBLE:
				return "double";
			case FLOAT:
				return "float";
			case INT:
				return "int";
			case LONG:
				return "long";
			case SHORT:
				return "short";
			case VOID:
				return "void";
			case WILDCARD: {
				WildcardType wt = (WildcardType) type;
				TypeMirror ext = wt.getExtendsBound();
				TypeMirror sup = wt.getSuperBound();
				if (ext == null) {
					if (sup == null) {
						return "?";
					}
					return "? super " + toTypeString(sup, elements, false);
				}
				if (sup == null) {
					return "? extends " + toTypeString(ext, elements, false);
				}
				return "? extends " + toTypeString(ext, elements, false) + " super "
						+ toTypeString(sup, elements, false);
			}
			case TYPEVAR: {
				TypeVariable tv = (TypeVariable) type;
				if (!withbounds) {
					return tv.asElement().getSimpleName().toString();
				}
				TypeMirror lb = tv.getLowerBound();
				TypeMirror ub = tv.getUpperBound();
				StringBuilder sb = new StringBuilder();
				sb.append(tv.asElement().getSimpleName().toString());
				if (lb.getKind() != TypeKind.NULL) {
					sb.append(" super ");
					sb.append(toTypeString(lb, elements, false));
				}
				if (ub.getKind() == TypeKind.DECLARED && ((TypeElement) ((DeclaredType) ub).asElement())
						.getQualifiedName().contentEquals("java.lang.Object")) {
					//the upper bound is the java.lang.Object. dont include in the result
				} else {
					sb.append(" extends ");
					sb.append(toTypeString(ub, elements, false));
				}
				return sb.toString();
			}
			case INTERSECTION: {
				IntersectionType itype = (IntersectionType) type;
				StringBuilder sb = new StringBuilder();
				for (Iterator<? extends TypeMirror> it = itype.getBounds().iterator(); it.hasNext();) {
					TypeMirror b = it.next();
					sb.append(toTypeString(b, elements, false));
					if (it.hasNext()) {
						sb.append(" & ");
					}
				}
				return sb.toString();
			}
			case ERROR:
			case EXECUTABLE:
			case NONE:
			case NULL:
			case OTHER:
			case PACKAGE:
			case UNION:
			default: {
				return "<unknown " + kind + ">";
			}
		}
	}

	public static String getNativeType(TypeMirror type, Types types, Elements elements) {
		TypeKind kind = type.getKind();
		if (kind == TypeKind.VOID) {
			return "void";
		}
		if (kind.isPrimitive()) {
			return "j" + kind.toString().toLowerCase(Locale.ENGLISH);
		}
		if (kind == TypeKind.ARRAY) {
			ArrayType at = (ArrayType) type;
			TypeMirror component = at.getComponentType();
			TypeKind componentkind = component.getKind();
			if (componentkind.isPrimitive()) {
				return "j" + componentkind.toString().toLowerCase(Locale.ENGLISH) + "Array";
			}
			return "jobjectArray";
		}
		TypeMirror stringtype = elements.getTypeElement("java.lang.String").asType();
		if (types.isSubtype(type, stringtype)) {
			return "jstring";
		}
		TypeMirror classtype = types.erasure(elements.getTypeElement("java.lang.Class").asType());
		if (types.isSubtype(type, classtype)) {
			return "jclass";
		}
		TypeMirror throwabletype = elements.getTypeElement("java.lang.Throwable").asType();
		if (types.isSubtype(type, throwabletype)) {
			return "jthrowable";
		}
		return "jobject";
	}

//	public static String operatorToString(Tree.Kind op) {
//		switch (op) {
//			case UNARY_MINUS:
//				// +x
//				return "+";
//			case UNARY_PLUS:
//				// -x
//				return "-";
//			case BITWISE_COMPLEMENT:
//				//~x
//				return "~";
//			case LOGICAL_COMPLEMENT:
//				// !x
//				return "!";
//			case PLUS:
//				// x + y
//				return "+";
//			case MINUS:
//				// x - y
//				return "-";
//			case OR:
//				// x | y
//				return "|";
//			case DIVIDE:
//				// x / y
//				return "/";
//			case XOR:
//				// x ^ y
//				return "^";
//			case AND:
//				// x & y
//				return "&";
//			case CONDITIONAL_AND:
//				// x && y
//				return "&&";
//			case CONDITIONAL_OR:
//				// x || y
//				return "||";
//			case EQUAL_TO:
//				// x == y
//				return "==";
//			case GREATER_THAN:
//				// x > y
//				return ">";
//			case GREATER_THAN_EQUAL:
//				// x >= y
//				return ">=";
//			case LEFT_SHIFT:
//				// x << y 
//				return "<<";
//			case LESS_THAN:
//				// x < y
//				return "<";
//			case LESS_THAN_EQUAL:
//				// x <= y
//				return "<=";
//			case MULTIPLY:
//				// x * y
//				return "*";
//			case NOT_EQUAL_TO:
//				// x != y
//				return "!=";
//			case REMAINDER:
//				// x % y
//				return "%";
//			case RIGHT_SHIFT:
//				// x >> y
//				return ">>";
//			case UNSIGNED_RIGHT_SHIFT:
//				// x >>> y
//				return ">>>";
//			default: {
//				throw new IllegalArgumentException(op.toString());
//			}
//		}
//	}

	private static final JavaFileObject.Kind[] JAVA_FILE_OBJECT_KINDS_WITHOUT_OTHER = { JavaFileObject.Kind.SOURCE,
			JavaFileObject.Kind.CLASS, JavaFileObject.Kind.HTML };

	public static JavaFileObject.Kind getKindFromName(String name) {
		for (JavaFileObject.Kind kind : JAVA_FILE_OBJECT_KINDS_WITHOUT_OTHER) {
			if (StringUtils.endsWithIgnoreCase(name, kind.extension)) {
				return kind;
			}
		}
		return JavaFileObject.Kind.OTHER;
	}

	private static final Set<String> MODULE_UNSUPPORTED_SOURCE_VERSIONS = new TreeSet<>();
	static {
		MODULE_UNSUPPORTED_SOURCE_VERSIONS.add("RELEASE_0");
		MODULE_UNSUPPORTED_SOURCE_VERSIONS.add("RELEASE_1");
		MODULE_UNSUPPORTED_SOURCE_VERSIONS.add("RELEASE_2");
		MODULE_UNSUPPORTED_SOURCE_VERSIONS.add("RELEASE_3");
		MODULE_UNSUPPORTED_SOURCE_VERSIONS.add("RELEASE_4");
		MODULE_UNSUPPORTED_SOURCE_VERSIONS.add("RELEASE_5");
		MODULE_UNSUPPORTED_SOURCE_VERSIONS.add("RELEASE_6");
		MODULE_UNSUPPORTED_SOURCE_VERSIONS.add("RELEASE_7");
		MODULE_UNSUPPORTED_SOURCE_VERSIONS.add("RELEASE_8");
	}

	public static boolean isModuleSupportingSourceVersion(String sourceversion) {
		Objects.requireNonNull(sourceversion);
		return !MODULE_UNSUPPORTED_SOURCE_VERSIONS.contains(sourceversion);
	}

	public static String modifiersToString(Set<Modifier> modifiers) {
		if (modifiers.isEmpty()) {
			return "";
		}
		return StringUtils.toStringJoin(" ", modifiers);
	}

	public static String modifiersToStringWithSpace(Set<Modifier> modifiers) {
		if (modifiers.isEmpty()) {
			return "";
		}
		return StringUtils.toStringJoin("", " ", modifiers, " ");
	}

	public static String annotationsToStringWithSpace(List<? extends AnnotationSignature> annots) {
		return ObjectUtils.isNullOrEmpty(annots) ? "" : (StringUtils.toStringJoin(" ", annots) + " ");
	}

	private static final Set<ElementType> DEFAULT_TARGETS = EnumSet.of(ElementType.ANNOTATION_TYPE,
			ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.METHOD,
			ElementType.PACKAGE, ElementType.PARAMETER, ElementType.TYPE);

	public static Set<ElementType> getAllowedAnnotationTargets(Target target) {
		if (target == null) {
			return DEFAULT_TARGETS;
		}
		ElementType[] vals = target.value();
		if (vals.length == 0) {
			return Collections.emptySet();
		}
		Set<ElementType> result = EnumSet.of(vals[0]);
		for (int i = 1; i < vals.length; i++) {
			result.add(vals[i]);
		}
		return result;
	}

	public static Set<ElementType> getAllowedAnnotationTargets(Class<? extends Annotation> annotationtype) {
		Target target = annotationtype.getAnnotation(Target.class);
		return getAllowedAnnotationTargets(target);
	}

	private static RMITransferProperties compilationRMIProperties = null;

	public synchronized static RMITransferProperties getCompilationRMIProperties() {
		if (compilationRMIProperties == null) {
			synchronized (IncrementalCompilationHandler.class) {
				if (compilationRMIProperties == null) {
					RMITransferProperties.Builder builder = RMITransferProperties.builder();

					builder.add(MethodTransferProperties
							.builder(ReflectUtils.getMethodAssert(Processor.class, "getSupportedOptions"))
							.returnWriter(new WrapperRMIObjectWriteHandler(RMITreeSetStringElementWrapper.class))
							.build());
					builder.add(MethodTransferProperties
							.builder(ReflectUtils.getMethodAssert(Processor.class, "getSupportedAnnotationTypes"))
							.returnWriter(new WrapperRMIObjectWriteHandler(RMITreeSetStringElementWrapper.class))
							.build());
					builder.add(MethodTransferProperties
							.builder(ReflectUtils.getMethodAssert(Processor.class, "process", Set.class,
									RoundEnvironment.class))
							.parameterWriter(0,
									new WrapperRMIObjectWriteHandler(RMIIdentityHashSetRemoteElementWrapper.class))
							.build());
					builder.add(MethodTransferProperties
							.builder(ReflectUtils.getMethodAssert(Processor.class, "getCompletions", Element.class,
									AnnotationMirror.class, ExecutableElement.class, String.class))
							.forbidden(true).build());

					builder.add(MethodTransferProperties
							.builder(ReflectUtils.getMethodAssert(RoundEnvironment.class, "getRootElements"))
							.returnWriter(
									new WrapperRMIObjectWriteHandler(RMIIdentityHashSetRemoteElementWrapper.class))
							.build());
					builder.add(MethodTransferProperties
							.builder(ReflectUtils.getMethodAssert(RoundEnvironment.class, "getElementsAnnotatedWith",
									TypeElement.class))
							.returnWriter(
									new WrapperRMIObjectWriteHandler(RMIIdentityHashSetRemoteElementWrapper.class))
							.build());
					//getElementsAnnotatedWith may not work, as the class may be not transferrable
					//however, this is solved by the incremental java compiler by 
					// creating the annotation proxies on the local side
					//it still may cause bugs when full external compilation is used.
					builder.add(MethodTransferProperties
							.builder(ReflectUtils.getMethodAssert(RoundEnvironment.class, "getElementsAnnotatedWith",
									Class.class))
							.returnWriter(
									new WrapperRMIObjectWriteHandler(RMIIdentityHashSetRemoteElementWrapper.class))
							.build());

					builder.add(MethodTransferProperties
							.builder(ReflectUtils.getMethodAssert(ProcessingEnvironment.class, "getOptions"))
							.returnWriter(new WrapperRMIObjectWriteHandler(RMILinkedHashSetStringElementWrapper.class))
							.build());
					builder.add(MethodTransferProperties
							.builder(ReflectUtils.getMethodAssert(ProcessingEnvironment.class, "getLocale"))
							.returnWriter(new WrapperRMIObjectWriteHandler(LocaleRMIWrapper.class)).build());
					//XXX handle others, such as java file object and more

					builder.add(MethodTransferProperties
							.builder(ReflectUtils.getMethodAssert(Elements.class, "getBinaryName", TypeElement.class))
							.returnWriter(new WrapperRMIObjectWriteHandler(NameRMIWrapper.class)).build());
					builder.add(MethodTransferProperties
							.builder(ReflectUtils.getMethodAssert(Elements.class, "getName", CharSequence.class))
							.returnWriter(new WrapperRMIObjectWriteHandler(NameRMIWrapper.class)).build());
					builder.add(MethodTransferProperties
							.builder(ReflectUtils.getMethodAssert(Elements.class, "getAllMembers", TypeElement.class))
							.returnWriter(new WrapperRMIObjectWriteHandler(RMIArrayListRemoteElementWrapper.class))
							.build());
					builder.add(MethodTransferProperties
							.builder(ReflectUtils.getMethodAssert(Elements.class, "getAllAnnotationMirrors",
									Element.class))
							.returnWriter(new WrapperRMIObjectWriteHandler(RMIArrayListRemoteElementWrapper.class))
							.build());

					addCommonAnnotatedConstructRMIProperties(builder, AnnotatedConstruct.class);

					addCommonElementClassRMIProperties(builder, Element.class);
					addCommonElementClassRMIProperties(builder, ExecutableElement.class);
					addCommonElementClassRMIProperties(builder, PackageElement.class);
					addCommonElementClassRMIProperties(builder, TypeElement.class);
					addCommonElementClassRMIProperties(builder, TypeParameterElement.class);
					addCommonElementClassRMIProperties(builder, VariableElement.class);

					addCommonQualifiedNameableRMIProperties(builder, QualifiedNameable.class);
					addCommonQualifiedNameableRMIProperties(builder, PackageElement.class);
					addCommonQualifiedNameableRMIProperties(builder, TypeElement.class);

					builder.add(MethodTransferProperties
							.builder(ReflectUtils.getMethodAssert(ExecutableElement.class, "getTypeParameters"))
							.returnWriter(new WrapperRMIObjectWriteHandler(RMIArrayListRemoteElementWrapper.class))
							.build());
					builder.add(MethodTransferProperties
							.builder(ReflectUtils.getMethodAssert(ExecutableElement.class, "getParameters"))
							.returnWriter(new WrapperRMIObjectWriteHandler(RMIArrayListRemoteElementWrapper.class))
							.build());
					builder.add(MethodTransferProperties
							.builder(ReflectUtils.getMethodAssert(ExecutableElement.class, "getThrownTypes"))
							.returnWriter(new WrapperRMIObjectWriteHandler(RMIArrayListRemoteElementWrapper.class))
							.build());

					builder.add(MethodTransferProperties
							.builder(ReflectUtils.getMethodAssert(TypeElement.class, "getTypeParameters"))
							.returnWriter(new WrapperRMIObjectWriteHandler(RMIArrayListRemoteElementWrapper.class))
							.build());
					builder.add(MethodTransferProperties
							.builder(ReflectUtils.getMethodAssert(TypeElement.class, "getInterfaces"))
							.returnWriter(new WrapperRMIObjectWriteHandler(RMIArrayListRemoteElementWrapper.class))
							.build());

					addCommonTypeMirrorRMIProperties(builder, TypeMirror.class);
					addCommonTypeMirrorRMIProperties(builder, ExecutableType.class);
					addCommonTypeMirrorRMIProperties(builder, IntersectionType.class);
					addCommonTypeMirrorRMIProperties(builder, NoType.class);
					addCommonTypeMirrorRMIProperties(builder, PrimitiveType.class);
					addCommonTypeMirrorRMIProperties(builder, ReferenceType.class);
					addCommonTypeMirrorRMIProperties(builder, UnionType.class);
					addCommonTypeMirrorRMIProperties(builder, WildcardType.class);
					addCommonTypeMirrorRMIProperties(builder, DeclaredType.class);
					addCommonTypeMirrorRMIProperties(builder, ArrayType.class);
					addCommonTypeMirrorRMIProperties(builder, NullType.class);
					addCommonTypeMirrorRMIProperties(builder, TypeVariable.class);
					addCommonTypeMirrorRMIProperties(builder, ErrorType.class);

					builder.add(MethodTransferProperties
							.builder(ReflectUtils.getMethodAssert(ExecutableType.class, "getTypeVariables"))
							.returnWriter(new WrapperRMIObjectWriteHandler(RMIArrayListRemoteElementWrapper.class))
							.build());
					builder.add(MethodTransferProperties
							.builder(ReflectUtils.getMethodAssert(ExecutableType.class, "getParameterTypes"))
							.returnWriter(new WrapperRMIObjectWriteHandler(RMIArrayListRemoteElementWrapper.class))
							.build());
					builder.add(MethodTransferProperties
							.builder(ReflectUtils.getMethodAssert(ExecutableType.class, "getThrownTypes"))
							.returnWriter(new WrapperRMIObjectWriteHandler(RMIArrayListRemoteElementWrapper.class))
							.build());

					builder.add(MethodTransferProperties
							.builder(ReflectUtils.getMethodAssert(IntersectionType.class, "getBounds"))
							.returnWriter(new WrapperRMIObjectWriteHandler(RMIArrayListRemoteElementWrapper.class))
							.build());

					builder.add(MethodTransferProperties
							.builder(ReflectUtils.getMethodAssert(UnionType.class, "getAlternatives"))
							.returnWriter(new WrapperRMIObjectWriteHandler(RMIArrayListRemoteElementWrapper.class))
							.build());

					builder.add(MethodTransferProperties
							.builder(ReflectUtils.getMethodAssert(DeclaredType.class, "getTypeArguments"))
							.returnWriter(new WrapperRMIObjectWriteHandler(RMIArrayListRemoteElementWrapper.class))
							.build());

					JavaCompilationUtils.applyRMIProperties(builder);
					compilationRMIProperties = builder.build();
				}
			}
		}
		return compilationRMIProperties;
	}

	public static int compareSourceVersionEnumNames(String left, String right) {
		if (!left.startsWith("RELEASE_")) {
			throw new IllegalArgumentException(
					"Source version enumeration name doesn't start with RELEASE_: \"" + left + "\"");
		}
		if (!right.startsWith("RELEASE_")) {
			throw new IllegalArgumentException(
					"Source version enumeration name doesn't start with RELEASE_: \"" + right + "\"");
		}
		String lver = left.substring(8);
		int lreleasenum = Integer.parseInt(lver);
		String rver = right.substring(8);
		int rreleasenum = Integer.parseInt(rver);
		return Integer.compare(lreleasenum, rreleasenum);
	}

	public static int getSourceVersionNumber(SourceVersion sv) {
		Objects.requireNonNull(sv, "source version");
		String name = sv.name();
		return getSourceVersionNumber(name);
	}

	public static int getSourceVersionNumber(String sourceversionname) {
		Objects.requireNonNull(sourceversionname, "source version name");
		if (!sourceversionname.startsWith("RELEASE_")) {
			throw new IllegalArgumentException(
					"Source version name doesn't start with RELEASE_: \"" + sourceversionname + "\"");
		}
		String verstr = sourceversionname.substring(8);
		try {
			return Integer.parseInt(verstr);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException(
					"Failed to parse source version name: " + sourceversionname + ". Expected format: RELEASE_<num>.",
					e);
		}
	}

	public static void addCommonTypeMirrorRMIProperties(RMITransferProperties.Builder builder,
			Class<? extends TypeMirror> type) {
		addCommonAnnotatedConstructRMIProperties(builder, type);
	}

	public static void addCommonAnnotatedConstructRMIProperties(RMITransferProperties.Builder builder,
			Class<? extends AnnotatedConstruct> annocontype) {
		builder.add(MethodTransferProperties.builder(ReflectUtils.getMethodAssert(annocontype, "getAnnotationMirrors"))
				.returnWriter(new WrapperRMIObjectWriteHandler(RMIArrayListRemoteElementWrapper.class)).build());
	}

	public static void addCommonQualifiedNameableRMIProperties(RMITransferProperties.Builder builder,
			Class<? extends QualifiedNameable> qnclass) {
		builder.add(MethodTransferProperties.builder(ReflectUtils.getMethodAssert(qnclass, "getQualifiedName"))
				.returnWriter(new WrapperRMIObjectWriteHandler(NameRMIWrapper.class)).build());
	}

	public static void addCommonElementClassRMIProperties(RMITransferProperties.Builder builder,
			Class<? extends Element> elemclass) {
		addCommonAnnotatedConstructRMIProperties(builder, elemclass);
		builder.add(MethodTransferProperties.builder(ReflectUtils.getMethodAssert(elemclass, "getEnclosedElements"))
				.returnWriter(new WrapperRMIObjectWriteHandler(RMIArrayListRemoteElementWrapper.class)).build());
		builder.add(MethodTransferProperties.builder(ReflectUtils.getMethodAssert(elemclass, "getSimpleName"))
				.returnWriter(new WrapperRMIObjectWriteHandler(NameRMIWrapper.class)).build());
		builder.add(MethodTransferProperties.builder(ReflectUtils.getMethodAssert(elemclass, "getModifiers"))
				.returnWriter(new WrapperRMIObjectWriteHandler(ModifierEnumSetRMIWrapper.class)).build());
	}

	public static SakerPath getDefaultPlatformIncludeDirectory(SakerPath installdirectory) {
		return getDefaultPlatformIncludeDirectory(LocalFileProvider.toRealPath(installdirectory));
	}

	public static SakerPath getDefaultPlatformIncludeDirectory(Path installdirectory) {
		try {
			NavigableMap<String, ? extends FileEntry> entries = LocalFileProvider.getInstance()
					.getDirectoryEntries(installdirectory.resolve("include"));
			String name = null;
			for (Entry<String, ? extends FileEntry> entry : entries.entrySet()) {
				if (!entry.getValue().isDirectory()) {
					continue;
				}
				if (name != null) {
					//multiple directories found, cannot determine
					return null;
				}
				name = entry.getKey();
			}
			return SakerPath.valueOf("include/" + name);
		} catch (IOException e) {
			return null;
		}
	}

	private static class LocaleRMIWrapper implements RMIWrapper {
		private Locale locale;

		public LocaleRMIWrapper() {
		}

		public LocaleRMIWrapper(Locale locale) {
			this.locale = locale;
		}

		@Override
		public void writeWrapped(RMIObjectOutput out) throws IOException {
			out.writeObject(locale.getLanguage());
			out.writeObject(locale.getCountry());
			out.writeObject(locale.getVariant());
		}

		@Override
		public void readWrapped(RMIObjectInput in) throws IOException, ClassNotFoundException {
			String lang = (String) in.readObject();
			String country = (String) in.readObject();
			String variant = (String) in.readObject();
			locale = new Locale(lang, country, variant);
		}

		@Override
		public Object resolveWrapped() {
			return locale;
		}

		@Override
		public Object getWrappedObject() {
			throw new UnsupportedOperationException();
		}

	}
}
