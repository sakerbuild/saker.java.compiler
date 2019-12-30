package saker.java.compiler.util9.impl.model.elem;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.java.compiler.impl.compile.handler.CompilationHandler;
import saker.java.compiler.impl.compile.handler.incremental.model.DualPackageElement;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalName;
import saker.java.compiler.impl.compile.handler.incremental.model.NonAnnotatedConstruct;
import saker.java.compiler.impl.compile.handler.incremental.model.elem.IncrementalTypeElement;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.PackageSignature;
import saker.java.compiler.util9.impl.model.IncrementalElementsTypes9;
import saker.java.compiler.util9.impl.model.mirror.SimpleModuleType;

public class IncrementalUnnamedModuleElement implements ModifiableModuleElement, NonAnnotatedConstruct {
	private IncrementalElementsTypes9 elemTypes;
	private ModuleElement javacUnnamedModule;

	private final ConcurrentHashMap<String, DualPackageElement> packageElements = new ConcurrentHashMap<>();
	//does not need to be concurrent collection as it is updated only from a single thread
	private final SortedMap<String, IncrementalTypeElement> canonicalTypeElements = new TreeMap<>();

	private TypeMirror asType;

	public IncrementalUnnamedModuleElement(IncrementalElementsTypes9 elemTypes, ModuleElement javacUnnamedModule) {
		this.elemTypes = elemTypes;
		this.javacUnnamedModule = javacUnnamedModule;
		this.asType = new SimpleModuleType(elemTypes, this);
	}

	public ModuleElement getJavacUnnamedModule() {
		return javacUnnamedModule;
	}

	@Override
	public List<? extends Element> getEnclosedElements() {
		//the enclosed elements can change depending on what the processors query
		// for example:
		//    at start the unnamed modules returns no enclosed elements
		//    the processor queries getTypeElement("path.to.Type")
		//    the enclosed elements will return the packages referenced from path.to.Type
		Set<Element> result = new LinkedHashSet<>(packageElements.values());
		result.addAll(elemTypes.forwardElements(javacUnnamedModule::getEnclosedElements));
		return ImmutableUtils.makeImmutableList(result);
	}

	@Override
	public TypeElement getTypeElement(String name) {
		TypeElement got = canonicalTypeElements.get(name);
		if (got != null) {
			return got;
		}
		//XXX cache elements by name?
		return elemTypes.forwardElementElems(el -> el.getTypeElement(javacUnnamedModule, name));
	}

	@Override
	public IncrementalTypeElement getParsedTypeElement(String name) {
		return canonicalTypeElements.get(name);
	}

	@Override
	public PackageElement getPackageElement(String name) {
		return packageElements.computeIfAbsent(name, n -> {
			PackageElement javacpackage = elemTypes.javacElements(el -> el.getPackageElement(javacUnnamedModule, name));
			if (javacpackage != null) {
				DualPackageElement result = new DualPackageElement(elemTypes, this, null, javacpackage, n);
				result.setEnclosingElement(this);
				return result;
			}
			return null;
		});
	}

	@Override
	public DualPackageElement getPresentPackageElement(String name) {
		return packageElements.get(name);
	}

	@Override
	public IncrementalTypeElement getTypeElement(ClassSignature c) {
		IncrementalTypeElement result = canonicalTypeElements.get(c.getCanonicalName());
		if (result == null) {
			throw new AssertionError("Class wasn't entered: " + c.getCanonicalName());
		}
		return result;
	}

	@Override
	public IncrementalTypeElement addParsedClass(ClassSignature c) {
		String packname = c.getPackageName();
		if (packname == null) {
			packname = "";
		}
		DualPackageElement encpackage = packageElements.computeIfAbsent(packname, p -> {
			DualPackageElement result = new DualPackageElement(elemTypes, this, p);
			result.setEnclosingElement(this);
			return result;
		});
		IncrementalTypeElement result = canonicalTypeElements.compute(c.getCanonicalName(), (k, v) -> {
			if (v == null) {
				return new IncrementalTypeElement(c, elemTypes);
			}
			v.setSignature(c);
			return v;
		});
		result.setEnclosingElement(encpackage);
		for (ClassSignature ec : c.getEnclosedClasses()) {
			IncrementalTypeElement ecres = addParsedClass(ec);
			ecres.setEnclosingElement(result);
		}
		return result;
	}

	@Override
	public PackageElement addParsedPackage(PackageSignature p) {
		return packageElements.compute(p.getName(), (k, v) -> {
			if (v == null) {
				DualPackageElement result = new DualPackageElement(elemTypes, this, p,
						elemTypes.getRealElements().getPackageElement(javacUnnamedModule, k), k);
				result.setEnclosingElement(this);
				return result;
			}
			v.setSignature(p);
			return v;
		});
	}

	@Override
	public List<? extends Element> getPackageEnclosedNonJavacElements(String packname) {
		return CompilationHandler.getPackageEnclosedElements(packname, canonicalTypeElements);
	}

	@Override
	public PackageElement forwardOverride(PackageElement javacpackage, String qualifiedname) {
		if (elemTypes.javac(javacpackage::getEnclosingElement) == javacUnnamedModule) {
			return packageElements.compute(qualifiedname, (k, v) -> {
				if (v == null) {
					DualPackageElement result = new DualPackageElement(elemTypes, this, null, javacpackage, k);
					result.setEnclosingElement(this);
					return result;
				}
				v.setJavacElement(javacpackage);
				return v;
			});
		}
		return null;
	}

	@Override
	public List<? extends Directive> getDirectives() {
		return Collections.emptyList();
	}

	@Override
	public Element getEnclosingElement() {
		return null;
	}

	@Override
	public Name getQualifiedName() {
		return IncrementalName.EMPTY_NAME;
	}

	@Override
	public Name getSimpleName() {
		return IncrementalName.EMPTY_NAME;
	}

	@Override
	public boolean isOpen() {
		return false;
	}

	@Override
	public boolean isUnnamed() {
		return true;
	}

	@Override
	public <R, P> R accept(ElementVisitor<R, P> v, P p) {
		return v.visitModule(this, p);
	}

	@Override
	public TypeMirror asType() {
		return asType;
	}

	@Override
	public ElementKind getKind() {
		return ElementKind.MODULE;
	}

	@Override
	public Set<Modifier> getModifiers() {
		return Collections.emptySet();
	}

	@Override
	public List<? extends AnnotationMirror> getAnnotationMirrors() {
		return NonAnnotatedConstruct.super.getAnnotationMirrors();
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> annotationtype) {
		return NonAnnotatedConstruct.super.getAnnotation(annotationtype);
	}

	@Override
	public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationtype) {
		return NonAnnotatedConstruct.super.getAnnotationsByType(annotationtype);
	}

	@Override
	public String toString() {
		return "unnamed module";
	}
}
