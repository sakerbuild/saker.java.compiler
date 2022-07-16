package saker.java.compiler.util9.impl.invoker;

import java.util.Set;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.ModuleElement.Directive;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;

import saker.java.compiler.api.processing.SakerElementsTypes;
import saker.java.compiler.util8.impl.invoker.ForwardingSakerElementsTypes8;

public class ForwardingSakerElementsTypes9 extends ForwardingSakerElementsTypes8 {

	public ForwardingSakerElementsTypes9(SakerElementsTypes elementsTypes) {
		super(elementsTypes);
	}

	@Override
	public PackageElement getPackageElement(ModuleElement module, CharSequence name) {
		return elementsTypes.getPackageElement(module, name);
	}

	@Override
	public Set<? extends PackageElement> getAllPackageElements(CharSequence name) {
		return elementsTypes.getAllPackageElements(name);
	}

	@Override
	public TypeElement getTypeElement(ModuleElement module, CharSequence name) {
		return elementsTypes.getTypeElement(module, name);
	}

	@Override
	public Set<? extends TypeElement> getAllTypeElements(CharSequence name) {
		return elementsTypes.getAllTypeElements(name);
	}

	@Override
	public ModuleElement getModuleElement(CharSequence name) {
		return elementsTypes.getModuleElement(name);
	}

	@Override
	public Set<? extends ModuleElement> getAllModuleElements() {
		return elementsTypes.getAllModuleElements();
	}

	@Override
	public Origin getOrigin(Element e) {
		return elementsTypes.getOrigin(e);
	}

	@Override
	public Origin getOrigin(AnnotatedConstruct c, AnnotationMirror a) {
		return elementsTypes.getOrigin(c, a);
	}

	@Override
	public Origin getOrigin(ModuleElement m, Directive directive) {
		return elementsTypes.getOrigin(m, directive);
	}

	@Override
	public boolean isBridge(ExecutableElement e) {
		return elementsTypes.isBridge(e);
	}

	@Override
	public ModuleElement getModuleOf(Element e) {
		return elementsTypes.getModuleOf(e);
	}
}
