package saker.java.compiler.util8.impl.invoker;

import java.io.Writer;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;

import saker.java.compiler.api.processing.SakerElementsTypes;

public class ForwardingSakerElementsTypes8 implements SakerElementsTypes {
	protected final SakerElementsTypes elementsTypes;

	public ForwardingSakerElementsTypes8(SakerElementsTypes elementsTypes) {
		this.elementsTypes = elementsTypes;
	}

	@Override
	public PackageElement getPackageElement(CharSequence name) {
		return elementsTypes.getPackageElement(name);
	}

	@Override
	public TypeElement getTypeElement(CharSequence name) {
		return elementsTypes.getTypeElement(name);
	}

	@Override
	public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValuesWithDefaults(
			AnnotationMirror a) {
		return elementsTypes.getElementValuesWithDefaults(a);
	}

	@Override
	public String getDocComment(Element e) {
		return elementsTypes.getDocComment(e);
	}

	@Override
	public boolean isDeprecated(Element e) {
		return elementsTypes.isDeprecated(e);
	}

	@Override
	public Name getBinaryName(TypeElement type) {
		return elementsTypes.getBinaryName(type);
	}

	@Override
	public PackageElement getPackageOf(Element e) {
		return elementsTypes.getPackageOf(e);
	}

	@Override
	public List<? extends Element> getAllMembers(TypeElement type) {
		return elementsTypes.getAllMembers(type);
	}

	@Override
	public List<? extends AnnotationMirror> getAllAnnotationMirrors(Element e) {
		return elementsTypes.getAllAnnotationMirrors(e);
	}

	@Override
	public boolean hides(Element hider, Element hidden) {
		return elementsTypes.hides(hider, hidden);
	}

	@Override
	public boolean overrides(ExecutableElement overrider, ExecutableElement overridden, TypeElement type) {
		return elementsTypes.overrides(overrider, overridden, type);
	}

	@Override
	public String getConstantExpression(Object value) {
		return elementsTypes.getConstantExpression(value);
	}

	@Override
	public void printElements(Writer w, Element... elements) {
		elementsTypes.printElements(w, elements);
	}

	@Override
	public Name getName(CharSequence cs) {
		return elementsTypes.getName(cs);
	}

	@Override
	public boolean isFunctionalInterface(TypeElement type) {
		return elementsTypes.isFunctionalInterface(type);
	}

	@Override
	public Element asElement(TypeMirror t) {
		return elementsTypes.asElement(t);
	}

	@Override
	public boolean isSameType(TypeMirror t1, TypeMirror t2) {
		return elementsTypes.isSameType(t1, t2);
	}

	@Override
	public boolean isSubtype(TypeMirror t1, TypeMirror t2) {
		return elementsTypes.isSubtype(t1, t2);
	}

	@Override
	public boolean isAssignable(TypeMirror t1, TypeMirror t2) {
		return elementsTypes.isAssignable(t1, t2);
	}

	@Override
	public boolean contains(TypeMirror t1, TypeMirror t2) {
		return elementsTypes.contains(t1, t2);
	}

	@Override
	public boolean isSubsignature(ExecutableType m1, ExecutableType m2) {
		return elementsTypes.isSubsignature(m1, m2);
	}

	@Override
	public List<? extends TypeMirror> directSupertypes(TypeMirror t) {
		return elementsTypes.directSupertypes(t);
	}

	@Override
	public TypeMirror erasure(TypeMirror t) {
		return elementsTypes.erasure(t);
	}

	@Override
	public TypeElement boxedClass(PrimitiveType p) {
		return elementsTypes.boxedClass(p);
	}

	@Override
	public PrimitiveType unboxedType(TypeMirror t) {
		return elementsTypes.unboxedType(t);
	}

	@Override
	public TypeMirror capture(TypeMirror t) {
		return elementsTypes.capture(t);
	}

	@Override
	public PrimitiveType getPrimitiveType(TypeKind kind) {
		return elementsTypes.getPrimitiveType(kind);
	}

	@Override
	public NullType getNullType() {
		return elementsTypes.getNullType();
	}

	@Override
	public NoType getNoType(TypeKind kind) {
		return elementsTypes.getNoType(kind);
	}

	@Override
	public ArrayType getArrayType(TypeMirror componentType) {
		return elementsTypes.getArrayType(componentType);
	}

	@Override
	public WildcardType getWildcardType(TypeMirror extendsBound, TypeMirror superBound) {
		return elementsTypes.getWildcardType(extendsBound, superBound);
	}

	@Override
	public DeclaredType getDeclaredType(TypeElement typeElem, TypeMirror... typeArgs) {
		return elementsTypes.getDeclaredType(typeElem, typeArgs);
	}

	@Override
	public DeclaredType getDeclaredType(DeclaredType containing, TypeElement typeElem, TypeMirror... typeArgs) {
		return elementsTypes.getDeclaredType(containing, typeElem, typeArgs);
	}

	@Override
	public TypeMirror asMemberOf(DeclaredType containing, Element element) {
		return elementsTypes.asMemberOf(containing, element);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(getClass().getSimpleName());
		builder.append("[");
		builder.append(elementsTypes);
		builder.append("]");
		return builder.toString();
	}
}
