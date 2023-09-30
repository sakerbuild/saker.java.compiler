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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.Parameterizable;
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
import saker.build.thirdparty.saker.rmi.annot.transfer.RMIWrap;
import saker.build.thirdparty.saker.rmi.connection.MethodTransferProperties;
import saker.build.thirdparty.saker.rmi.connection.RMITransferProperties;
import saker.build.thirdparty.saker.rmi.io.RMIObjectInput;
import saker.build.thirdparty.saker.rmi.io.RMIObjectOutput;
import saker.build.thirdparty.saker.rmi.io.wrap.RMIWrapper;
import saker.build.thirdparty.saker.rmi.io.writer.RMIObjectWriteHandler;
import saker.build.thirdparty.saker.rmi.io.writer.WrapperRMIObjectWriteHandler;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.ReflectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.function.LazySupplier;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import saker.build.thirdparty.saker.util.rmi.wrap.RMIArrayListRemoteElementWrapper;
import saker.build.thirdparty.saker.util.rmi.wrap.RMIArrayListWrapper;
import saker.build.thirdparty.saker.util.rmi.wrap.RMIIdentityHashSetRemoteElementWrapper;
import saker.build.thirdparty.saker.util.rmi.wrap.RMILinkedHashSetStringElementWrapper;
import saker.build.thirdparty.saker.util.rmi.wrap.RMITreeSetStringElementWrapper;
import saker.java.compiler.api.processing.exc.EnumerationArrayNotFoundException;
import saker.java.compiler.impl.compat.ImmutableElementTypeSet;
import saker.java.compiler.impl.compile.file.IncrementalDirectoryPaths;
import saker.java.compiler.impl.compile.handler.incremental.IncrementalCompilationHandler;
import saker.java.compiler.impl.compile.handler.info.ClassHoldingData;
import saker.java.compiler.impl.compile.handler.invoker.JavaCompilationInvoker;
import saker.java.compiler.impl.compile.handler.invoker.JavaCompilerInvocationDirector;
import saker.java.compiler.impl.compile.handler.invoker.PreviousCompilationClassInfo;
import saker.java.compiler.impl.compile.handler.invoker.SakerPathBytes;
import saker.java.compiler.impl.compile.handler.invoker.rmi.ModifierEnumSetRMIWrapper;
import saker.java.compiler.impl.compile.handler.invoker.rmi.NameRMIWrapper;
import saker.java.compiler.impl.options.OutputBytecodeManipulationOption;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.thirdparty.org.objectweb.asm.ClassReader;
import saker.java.compiler.impl.thirdparty.org.objectweb.asm.ClassVisitor;
import saker.java.compiler.impl.thirdparty.org.objectweb.asm.ClassWriter;
import saker.java.compiler.impl.thirdparty.org.objectweb.asm.ModuleVisitor;
import saker.java.compiler.impl.thirdparty.org.objectweb.asm.Opcodes;
import saker.java.compiler.impl.util.RemoteKeyValueLinkedHashMapRMIWrapper;
import saker.java.compiler.jdk.impl.JavaCompilationUtils;
import saker.java.compiler.jdk.impl.invoker.InternalIncrementalCompilationInvoker;
import saker.java.compiler.jdk.impl.model.ForwardingElements;

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

	public static String getClassSimpleNameFromBinaryName(String binaryname) {
		if (binaryname == null) {
			return null;
		}
		int dotidx = binaryname.lastIndexOf('.');
		int dollaridx = binaryname.lastIndexOf('$', dotidx);
		return binaryname.substring(Math.max(dotidx, dollaridx) + 1);
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
		if (ObjectUtils.isNullOrEmpty(annots)) {
			return "";
		}
		return StringUtils.toStringJoin(" ", annots) + " ";
	}

	private static final ImmutableElementTypeSet DEFAULT_TARGETS = ImmutableElementTypeSet.of(
			ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.LOCAL_VARIABLE,
			ElementType.METHOD, ElementType.PACKAGE, ElementType.PARAMETER, ElementType.TYPE);

	public static ImmutableElementTypeSet getAllowedAnnotationTargets(Target target) {
		if (target == null) {
			return DEFAULT_TARGETS;
		}
		try {
			ElementType[] vals = target.value();
			return ImmutableElementTypeSet.of(vals);
		} catch (EnumerationArrayNotFoundException e) {
			//some element types not available in the current jvm
			return ImmutableElementTypeSet.forCommaSeparatedNames(e.getMessage());
		}
	}

	public static ImmutableElementTypeSet getAllowedAnnotationTargets(Class<? extends Annotation> annotationtype) {
		Target target = annotationtype.getAnnotation(Target.class);
		return getAllowedAnnotationTargets(target);
	}

	private static volatile RMITransferProperties compilationRMIProperties = null;

	public synchronized static RMITransferProperties getCompilationRMIProperties() {
		RMITransferProperties result = compilationRMIProperties;
		if (result == null) {
			synchronized (IncrementalCompilationHandler.class) {
				if (result == null) {
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
							.builder(ReflectUtils.getMethodAssert(AnnotationValue.class, "getValue"))
							.returnWriter(new WrapperRMIObjectWriteHandler(AnnotationValueValueRMIWrapper.class))
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
					builder.add(MethodTransferProperties
							.builder(ReflectUtils.getMethodAssert(Elements.class, "getTypeElement", CharSequence.class))
							.parameterWriter(0, RMIObjectWriteHandler.wrapper(CharSequenceStringizeRMIWrapper.class))
							.build());

					builder.add(MethodTransferProperties
							.builder(ReflectUtils.getMethodAssert(Name.class, "contentEquals", CharSequence.class))
							.parameterWriter(0, RMIObjectWriteHandler.wrapper(CharSequenceStringizeRMIWrapper.class))
							.build());

					addCommonAnnotatedConstructRMIProperties(builder, AnnotatedConstruct.class);

					addCommonElementClassRMIProperties(builder, Element.class);
					addCommonElementClassRMIProperties(builder, Parameterizable.class);
					addCommonElementClassRMIProperties(builder, ExecutableElement.class);
					addCommonElementClassRMIProperties(builder, PackageElement.class);
					addCommonElementClassRMIProperties(builder, TypeElement.class);
					addCommonElementClassRMIProperties(builder, TypeParameterElement.class);
					addCommonElementClassRMIProperties(builder, VariableElement.class);
					addCommonElementClassRMIProperties(builder, QualifiedNameable.class);

					addCommonQualifiedNameableRMIProperties(builder, QualifiedNameable.class);
					addCommonQualifiedNameableRMIProperties(builder, PackageElement.class);
					addCommonQualifiedNameableRMIProperties(builder, TypeElement.class);

					builder.add(MethodTransferProperties
							.builder(ReflectUtils.getMethodAssert(Parameterizable.class, "getTypeParameters"))
							.returnWriter(new WrapperRMIObjectWriteHandler(RMIArrayListRemoteElementWrapper.class))
							.build());

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

					builder.add(MethodTransferProperties
							.builder(ReflectUtils.getMethodAssert(TypeParameterElement.class, "getBounds"))
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

					builder.add(MethodTransferProperties
							.builder(ReflectUtils.getMethodAssert(AnnotationMirror.class, "getElementValues"))
							.returnWriter(new WrapperRMIObjectWriteHandler(RemoteKeyValueLinkedHashMapRMIWrapper.class))
							.build());

					JavaCompilationUtils.applyRMIProperties(builder);
					result = builder.build();
					compilationRMIProperties = result;
					return result;
				}
			}
		}
		return result;
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

	/**
	 * Creates a new java compilation invoker.
	 * 
	 * @return The invoker.
	 */
	//Note: this method is invoked via RMI instead of the constructor directly to be able to customize the object transfer
	@RMIWrap(JavaCompilationInvokerRMIWrapper.class)
	public static JavaCompilationInvoker newJavaCompilationInvokerInstance() {
		return new InternalIncrementalCompilationInvoker();
	}

	public static boolean isBytecodeManipulationAffects(String name,
			OutputBytecodeManipulationOption bytecodeManipulation) {
		if (bytecodeManipulation == null) {
			return false;
		}
		if (bytecodeManipulation.isPatchEnablePreview()) {
			return true;
		}
		if ("module-info.class".equals(name)) {
			if (bytecodeManipulation.getModuleMainClassInjectValue() != null
					|| bytecodeManipulation.getModuleVersionInjectValue() != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Performs the requested bytecode manipulation on the input class bytes.
	 * <p>
	 * The function may modify the input class byte array directly.
	 * 
	 * @param name
	 *            The name of the file. E.g. <code>module-info.class</code> if this is the module class file.
	 * @param cfbytes
	 *            The class bytes.
	 * @param bytecodeManipulation
	 *            The manipulation option, may be <code>null</code>.
	 * @return The result byte array region, might be the same as the input.
	 */
	public static ByteArrayRegion performBytecodeManipulation(String name, ByteArrayRegion cfbytes,
			OutputBytecodeManipulationOption bytecodeManipulation) {

		if (bytecodeManipulation == null) {
			return cfbytes;
		}
		boolean patchenablepreview = bytecodeManipulation.isPatchEnablePreview();

		if (patchenablepreview) {
			patchEnablePreview(cfbytes);
		}

		if ("module-info.class".equals(name)) {
			String moduleMainClassInjectValue = bytecodeManipulation.getModuleMainClassInjectValue();
			String moduleVersionInjectValue = bytecodeManipulation.getModuleVersionInjectValue();
			if (moduleMainClassInjectValue != null || moduleVersionInjectValue != null) {
				ClassReader reader = new ClassReader(cfbytes.getArray(), cfbytes.getOffset(), cfbytes.getLength());
				ClassWriter writer = new ClassWriter(reader, 0);
				reader.accept(new ModuleMainClassInjectorClassVisitor(writer, moduleMainClassInjectValue,
						moduleVersionInjectValue), 0);
				cfbytes = ByteArrayRegion.wrap(writer.toByteArray());
			}
		}
		return cfbytes;
	}

	private static void patchEnablePreview(ByteArrayRegion cfbytes) {
		//should start with
		//u4             magic; 0xcafebabe
		//u2             minor_version;
		//u2             major_version;
		if (cfbytes.getLength() < 8) {
			//not enough bytes? what? shouldn't ever happen, just a sanity check
			return;
		}
		byte[] array = cfbytes.getArray();
		//change 0xFFFF minor to 0x0000
		if (array[4] == (byte) 0xFF && array[5] == (byte) 0xFF) {
			array[4] = 0;
			array[5] = 0;
		}
	}

	private static class ModuleMainClassInjectorClassVisitor extends ClassVisitor {
		private String mainClassName;
		private String moduleVersion;

		public ModuleMainClassInjectorClassVisitor(ClassVisitor classVisitor, String mainclassname,
				String moduleVersion) {
			super(Opcodes.ASM7, classVisitor);
			this.mainClassName = mainclassname;
			this.moduleVersion = moduleVersion;
		}

		@Override
		public ModuleVisitor visitModule(String name, int access, String version) {
			if (version == null) {
				version = this.moduleVersion;
			}
			ModuleVisitor sv = super.visitModule(name, access, version);
			if (mainClassName == null) {
				return sv;
			}
			return new ModuleMainClassInjectorModuleVisitor(sv);
		}

		private class ModuleMainClassInjectorModuleVisitor extends ModuleVisitor {
			private boolean alreadyHasMainClass = false;

			public ModuleMainClassInjectorModuleVisitor(ModuleVisitor moduleVisitor) {
				super(Opcodes.ASM7, moduleVisitor);
			}

			@Override
			public void visitMainClass(String mainClass) {
				alreadyHasMainClass = true;
				super.visitMainClass(mainClass);
			}

			@Override
			public void visitEnd() {
				if (!alreadyHasMainClass) {
					super.visitMainClass(mainClassName);
				}
				super.visitEnd();
			}
		}
	}

	public static final class LocaleRMIWrapper implements RMIWrapper {
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

	public static final class AnnotationValueValueRMIWrapper implements RMIWrapper {
		private Object value;

		public AnnotationValueValueRMIWrapper() {
		}

		public AnnotationValueValueRMIWrapper(Object value) {
			this.value = value;
		}

		@Override
		public void writeWrapped(RMIObjectOutput out) throws IOException {
			if (value instanceof List<?>) {
				out.writeWrappedObject(value, RMIArrayListWrapper.class);
			} else {
				out.writeObject(value);
			}
		}

		@Override
		public void readWrapped(RMIObjectInput in) throws IOException, ClassNotFoundException {
			this.value = in.readObject();
		}

		@Override
		public Object resolveWrapped() {
			return value;
		}

		@Override
		public Object getWrappedObject() {
			throw new UnsupportedOperationException();
		}
	}

	public static final class CharSequenceStringizeRMIWrapper implements RMIWrapper {
		private CharSequence cs;

		public CharSequenceStringizeRMIWrapper() {
		}

		public CharSequenceStringizeRMIWrapper(CharSequence cd) {
			this.cs = cd;
		}

		@Override
		public Object getWrappedObject() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void readWrapped(RMIObjectInput in) throws IOException, ClassNotFoundException {
			cs = (CharSequence) in.readObject();
		}

		@Override
		public Object resolveWrapped() {
			return cs;
		}

		@Override
		public void writeWrapped(RMIObjectOutput out) throws IOException {
			out.writeObject(cs.toString());
		}
	}

	public static final class JavaCompilationInvokerRMIWrapper implements RMIWrapper, JavaCompilationInvoker {
		private JavaCompilationInvoker invoker;
		private Elements elements;
		private String sourceVersionName;
		private String javaVersionProperty;
		private int compilerJVMJavaMajorVersion;

		public JavaCompilationInvokerRMIWrapper() {
		}

		public JavaCompilationInvokerRMIWrapper(JavaCompilationInvoker invoker) {
			this.invoker = invoker;
		}

		@Override
		public Object getWrappedObject() {
			return invoker;
		}

		@Override
		public Object resolveWrapped() {
			return this;
		}

		@Override
		public void writeWrapped(RMIObjectOutput out) throws IOException {
			JavaCompilationInvoker invoker = this.invoker;
			out.writeRemoteObject(invoker);
			//The actual Elements can only be created after the compilation is initialized.
			//so cache a forwarding elements on the remote side that gets the elements instance if a 
			//method is invoked on it
			out.writeRemoteObject(new InvokerForwardingElements(invoker));
			out.writeObject(invoker.getJavaVersionProperty());
			out.writeInt(invoker.getCompilerJVMJavaMajorVersion());
		}

		@Override
		public void readWrapped(RMIObjectInput in) throws IOException, ClassNotFoundException {
			this.invoker = (JavaCompilationInvoker) in.readObject();
			this.elements = (Elements) in.readObject();
			this.javaVersionProperty = (String) in.readObject();
			this.compilerJVMJavaMajorVersion = in.readInt();
		}

		@Override
		public Elements getElements() {
			return elements;
		}

		@Override
		public String getSourceVersionName() {
			if (sourceVersionName == null) {
				throw new IllegalStateException("Compilation hasn't been initialized yet.");
			}
			return sourceVersionName;
		}

		@Override
		public String getJavaVersionProperty() {
			return javaVersionProperty;
		}

		@Override
		public int getCompilerJVMJavaMajorVersion() {
			return compilerJVMJavaMajorVersion;
		}

		@Override
		public void close() throws IOException {
			invoker.close();
		}

		@Override
		public CompilationInitResultData initCompilation(JavaCompilerInvocationDirector director,
				IncrementalDirectoryPaths directorypaths, String[] options, String sourceversionoptionname,
				String targetversionoptionname) throws IOException {
			CompilationInitResultData res = invoker.initCompilation(director, directorypaths, options,
					sourceversionoptionname, targetversionoptionname);
			this.sourceVersionName = res.getSourceVersionName();
			return res;
		}

		@Override
		public void invokeCompilation(SakerPathBytes[] units) throws IOException {
			invoker.invokeCompilation(units);
		}

		@Override
		public void addSourceForCompilation(String sourcename, SakerPath file) throws IOException {
			invoker.addSourceForCompilation(sourcename, file);
		}

		@Override
		public void addClassFileForCompilation(String classname, SakerPath file) throws IOException {
			invoker.addClassFileForCompilation(classname, file);
		}

		@Override
		public Collection<? extends ClassHoldingData> parseRoundAddedSources() {
			return invoker.parseRoundAddedSources();
		}

		@Override
		public Collection<? extends ClassHoldingData> parseRoundAddedClassFiles() {
			return parseRoundAddedClassFiles();
		}

		@Override
		public Types getTypes() {
			return invoker.getTypes();
		}

		@Override
		public void addClassFilesFromPreviousCompilation(PreviousCompilationClassInfo previoussources) {
			invoker.addClassFilesFromPreviousCompilation(previoussources);
		}

		@Override
		public NavigableMap<SakerPath, ABIParseInfo> getParsedSourceABIUsages() {
			return invoker.getParsedSourceABIUsages();
		}

		@Override
		public NavigableSet<String> getCompilationModuleSet() {
			return invoker.getCompilationModuleSet();
		}
	}

	private static final class InvokerForwardingElements extends ForwardingElements {
		private final transient LazySupplier<Elements> lazyElements;

		private InvokerForwardingElements(JavaCompilationInvoker invoker) {
			lazyElements = LazySupplier.of(invoker::getElements);
		}

		@Override
		protected Elements getForwardedElements() {
			return lazyElements.get();
		}
	}
}
