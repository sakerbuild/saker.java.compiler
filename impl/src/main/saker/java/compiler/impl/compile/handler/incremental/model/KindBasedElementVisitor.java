package saker.java.compiler.impl.compile.handler.incremental.model;

import java.util.EnumMap;
import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;

import saker.build.thirdparty.saker.util.function.TriFunction;
import saker.java.compiler.jdk.impl.incremental.model.JavaModelUtils;

public class KindBasedElementVisitor {
	@SuppressWarnings("rawtypes")
	private static final Map<ElementKind, TriFunction<ElementVisitor, ? extends Element, Object, Object>> KIND_CALL_MAPS = getKindBasedElementVisitorMap();

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Map<ElementKind, TriFunction<ElementVisitor, ? extends Element, Object, Object>> getKindBasedElementVisitorMap() {
		EnumMap<ElementKind, TriFunction<ElementVisitor, ? extends Element, Object, Object>> result = new EnumMap<>(
				ElementKind.class);
		result.put(ElementKind.PACKAGE,
				(TriFunction<ElementVisitor, PackageElement, Object, Object>) ElementVisitor::visitPackage);

		result.put(ElementKind.ENUM,
				(TriFunction<ElementVisitor, TypeElement, Object, Object>) ElementVisitor::visitType);
		result.put(ElementKind.CLASS,
				(TriFunction<ElementVisitor, TypeElement, Object, Object>) ElementVisitor::visitType);
		result.put(ElementKind.ANNOTATION_TYPE,
				(TriFunction<ElementVisitor, TypeElement, Object, Object>) ElementVisitor::visitType);
		result.put(ElementKind.INTERFACE,
				(TriFunction<ElementVisitor, TypeElement, Object, Object>) ElementVisitor::visitType);

		result.put(ElementKind.ENUM_CONSTANT,
				(TriFunction<ElementVisitor, VariableElement, Object, Object>) ElementVisitor::visitVariable);
		result.put(ElementKind.FIELD,
				(TriFunction<ElementVisitor, VariableElement, Object, Object>) ElementVisitor::visitVariable);
		result.put(ElementKind.PARAMETER,
				(TriFunction<ElementVisitor, VariableElement, Object, Object>) ElementVisitor::visitVariable);
		result.put(ElementKind.LOCAL_VARIABLE,
				(TriFunction<ElementVisitor, VariableElement, Object, Object>) ElementVisitor::visitVariable);
		result.put(ElementKind.EXCEPTION_PARAMETER,
				(TriFunction<ElementVisitor, VariableElement, Object, Object>) ElementVisitor::visitVariable);

		result.put(ElementKind.METHOD,
				(TriFunction<ElementVisitor, ExecutableElement, Object, Object>) ElementVisitor::visitExecutable);
		result.put(ElementKind.CONSTRUCTOR,
				(TriFunction<ElementVisitor, ExecutableElement, Object, Object>) ElementVisitor::visitExecutable);
		result.put(ElementKind.STATIC_INIT,
				(TriFunction<ElementVisitor, ExecutableElement, Object, Object>) ElementVisitor::visitExecutable);
		result.put(ElementKind.INSTANCE_INIT,
				(TriFunction<ElementVisitor, ExecutableElement, Object, Object>) ElementVisitor::visitExecutable);

		result.put(ElementKind.TYPE_PARAMETER,
				(TriFunction<ElementVisitor, TypeParameterElement, Object, Object>) ElementVisitor::visitTypeParameter);

		result.put(ElementKind.OTHER,
				(TriFunction<ElementVisitor, Element, Object, Object>) ElementVisitor::visitUnknown);
		//what should we call in case of resource variable? never encountered it in real compilation scenarios
		result.put(ElementKind.RESOURCE_VARIABLE,
				(TriFunction<ElementVisitor, Element, Object, Object>) ElementVisitor::visitUnknown);

		JavaModelUtils.addJava9KindBasedElementVisitorFunctions(result);
		return result;
	}

	private KindBasedElementVisitor() {
		throw new UnsupportedOperationException();
	}

	public static <R, P> R visit(Element element, ElementVisitor<R, P> visitor, P p) {
		return visit(element.getKind(), element, visitor, p);
	}

	@SuppressWarnings("unchecked")
	public static <R, P> R visit(ElementKind kind, Element element, ElementVisitor<R, P> visitor, P p) {
		@SuppressWarnings("rawtypes")
		TriFunction got = KIND_CALL_MAPS.get(kind);
		if (got == null) {
			return visitor.visitUnknown(element, p);
		}
		return (R) got.apply(visitor, element, p);
	}

}
