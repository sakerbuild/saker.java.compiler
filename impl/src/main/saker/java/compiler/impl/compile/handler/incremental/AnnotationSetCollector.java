package saker.java.compiler.impl.compile.handler.incremental;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;

import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.SakerLog;
import saker.java.compiler.impl.compat.element.ModuleElementCompat;
import saker.java.compiler.jdk.impl.compat.element.DefaultedElementVisitor;

public class AnnotationSetCollector implements DefaultedElementVisitor<Void, Void> {
	private final Elements elements;

	private Map<TypeElement, Set<Element>> elementsForAnnotationTypes = new LinkedHashMap<>();
	private Set<Element> rootElements = new LinkedHashSet<>();
	private SortedSet<SakerPath> paths = new TreeSet<>();

	public AnnotationSetCollector(Elements elements) {
		this.elements = elements;
	}

	public Map<TypeElement, Set<Element>> getElementsForAnnotationTypes() {
		return elementsForAnnotationTypes;
	}

	public Set<? extends TypeElement> getAnnotationTypes() {
		return elementsForAnnotationTypes.keySet();
	}

	public Set<? extends Element> getRootElements() {
		return rootElements;
	}

	public void removeAnnotationTypes(Collection<? extends TypeElement> annotations) {
		this.elementsForAnnotationTypes.keySet().removeAll(annotations);
	}

	private void addAnnotations(Element e) {
		List<? extends AnnotationMirror> allannots = elements.getAllAnnotationMirrors(e);
		if (allannots.isEmpty()) {
			return;
		}
		for (AnnotationMirror am : allannots) {
			DeclaredType amtype = am.getAnnotationType();
			TypeElement annottypeelem = (TypeElement) amtype.asElement();
			if (annottypeelem == null) {
				throw new IllegalStateException("Failed to resolve annotation type element: " + amtype);
			}

			getElementsForAnnotationTypeSet(annottypeelem).add(e);
		}
	}

	private void collectElements(Iterable<? extends Element> elems) {
		for (Element e : elems) {
			e.accept(this, null);
		}
	}

	@Override
	public Void visitPackage(PackageElement e, Void p) {
		addAnnotations(e);
		//dont collect package enclosed elements
		return null;
	}

	@Override
	public Void visitType(TypeElement e, Void p) {
		addAnnotations(e);
		collectElements(e.getTypeParameters());
		collectElements(e.getEnclosedElements());
		return null;
	}

	@Override
	public Void visitVariable(VariableElement e, Void p) {
		addAnnotations(e);
		collectElements(e.getEnclosedElements());
		return null;
	}

	@Override
	public Void visitExecutable(ExecutableElement e, Void p) {
		addAnnotations(e);
		collectElements(e.getTypeParameters());
		collectElements(e.getParameters());
		collectElements(e.getEnclosedElements());
		return null;
	}

	@Override
	public Void visitTypeParameter(TypeParameterElement e, Void p) {
		addAnnotations(e);
		collectElements(e.getEnclosedElements());
		return null;
	}

	@Override
	public Void visitModuleCompat(ModuleElementCompat e, Void p) {
		addAnnotations(e.getRealObject());
		//do not collect enclosed packages
		return null;
	}

	@Override
	public Void visitUnknown(Element e, Void p) {
		SakerLog.warning().println("Unknown java model element type: " + e.getKind() + ": " + e);
		addAnnotations(e);
		collectElements(e.getEnclosedElements());
		return null;
	}

	private Set<Element> getElementsForAnnotationTypeSet(TypeElement annotationtype) {
		Set<Element> s = this.elementsForAnnotationTypes.get(annotationtype);
		if (s == null) {
			s = new HashSet<>();
			this.elementsForAnnotationTypes.put(annotationtype, s);
		}
		return s;
	}

	public void clear() {
		this.rootElements.clear();
		this.elementsForAnnotationTypes.clear();
		this.paths.clear();
	}

	public void add(AnnotationSetCollector set) {
		this.rootElements.addAll(set.rootElements);
		this.paths.addAll(set.paths);
		for (Entry<TypeElement, Set<Element>> entry : set.elementsForAnnotationTypes.entrySet()) {
			getElementsForAnnotationTypeSet(entry.getKey()).addAll(entry.getValue());
		}
	}

	public void rebaseOn(AnnotationSetCollector base) {
		clear();
		this.rootElements.addAll(base.rootElements);
		this.paths.addAll(base.paths);
		for (Entry<TypeElement, Set<Element>> entry : base.elementsForAnnotationTypes.entrySet()) {
			this.elementsForAnnotationTypes.put(entry.getKey(), new HashSet<>(entry.getValue()));
		}
	}

	private void collect(Element e) {
		if (this.rootElements.add(e)) {
			e.accept(this, null);
		}
	}

	public void collect(SakerPath path, Iterable<? extends Element> rootelements) {
		for (Element e : rootelements) {
			collect(e);
		}
		this.paths.add(path);
	}

	public boolean isEmpty() {
		return rootElements.isEmpty();
	}

	public SortedSet<SakerPath> getPaths() {
		return paths;
	}

	@Override
	public String toString() {
		return rootElements.toString();
	}
}