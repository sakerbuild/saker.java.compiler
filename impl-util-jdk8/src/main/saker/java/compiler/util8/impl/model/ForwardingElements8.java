package saker.java.compiler.util8.impl.model;

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
import javax.lang.model.util.Elements;

public abstract class ForwardingElements8 implements Elements {

	protected ForwardingElements8() {
	}

	protected abstract Elements getForwardedElements();

	@Override
	public PackageElement getPackageElement(CharSequence name) {
		return getForwardedElements().getPackageElement(name);
	}

	@Override
	public TypeElement getTypeElement(CharSequence name) {
		return getForwardedElements().getTypeElement(name);
	}

	@Override
	public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValuesWithDefaults(
			AnnotationMirror a) {
		return getForwardedElements().getElementValuesWithDefaults(a);
	}

	@Override
	public String getDocComment(Element e) {
		return getForwardedElements().getDocComment(e);
	}

	@Override
	public boolean isDeprecated(Element e) {
		return getForwardedElements().isDeprecated(e);
	}

	@Override
	public Name getBinaryName(TypeElement type) {
		return getForwardedElements().getBinaryName(type);
	}

	@Override
	public PackageElement getPackageOf(Element e) {
		return getForwardedElements().getPackageOf(e);
	}

	@Override
	public List<? extends Element> getAllMembers(TypeElement type) {
		return getForwardedElements().getAllMembers(type);
	}

	@Override
	public List<? extends AnnotationMirror> getAllAnnotationMirrors(Element e) {
		return getForwardedElements().getAllAnnotationMirrors(e);
	}

	@Override
	public boolean hides(Element hider, Element hidden) {
		return getForwardedElements().hides(hider, hidden);
	}

	@Override
	public boolean overrides(ExecutableElement overrider, ExecutableElement overridden, TypeElement type) {
		return getForwardedElements().overrides(overrider, overridden, type);
	}

	@Override
	public String getConstantExpression(Object value) {
		return getForwardedElements().getConstantExpression(value);
	}

	@Override
	public void printElements(Writer w, Element... elements) {
		getForwardedElements().printElements(w, elements);
	}

	@Override
	public Name getName(CharSequence cs) {
		return getForwardedElements().getName(cs);
	}

	@Override
	public boolean isFunctionalInterface(TypeElement type) {
		return getForwardedElements().isFunctionalInterface(type);
	}
}
