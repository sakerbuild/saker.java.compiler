package saker.java.compiler.util9.impl.model;

import java.util.Set;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.ModuleElement.Directive;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

import saker.java.compiler.util8.impl.model.ForwardingElements8;

public abstract class ForwardingElements9 extends ForwardingElements8 {
	protected ForwardingElements9() {
		super();
	}

	@Override
	public PackageElement getPackageElement(ModuleElement module, CharSequence name) {
		return getForwardedElements().getPackageElement(module, name);
	}

	@Override
	public Set<? extends PackageElement> getAllPackageElements(CharSequence name) {
		return getForwardedElements().getAllPackageElements(name);
	}

	@Override
	public TypeElement getTypeElement(ModuleElement module, CharSequence name) {
		return getForwardedElements().getTypeElement(module, name);
	}

	@Override
	public Set<? extends TypeElement> getAllTypeElements(CharSequence name) {
		return getForwardedElements().getAllTypeElements(name);
	}

	@Override
	public ModuleElement getModuleElement(CharSequence name) {
		return getForwardedElements().getModuleElement(name);
	}

	@Override
	public Set<? extends ModuleElement> getAllModuleElements() {
		return getForwardedElements().getAllModuleElements();
	}

	@Override
	public Origin getOrigin(Element e) {
		return getForwardedElements().getOrigin(e);
	}

	@Override
	public Origin getOrigin(AnnotatedConstruct c, AnnotationMirror a) {
		return getForwardedElements().getOrigin(c, a);
	}

	@Override
	public Origin getOrigin(ModuleElement m, Directive directive) {
		return getForwardedElements().getOrigin(m, directive);
	}

	@Override
	public boolean isBridge(ExecutableElement e) {
		return getForwardedElements().isBridge(e);
	}

	@Override
	public ModuleElement getModuleOf(Element e) {
		return getForwardedElements().getModuleOf(e);
	}
}
