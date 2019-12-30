package test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.processing.Completion;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

public class MessagerProcessor implements Processor {
	private Filer filer;
	private Elements elementUtils;
	private Messager messager;

	public MessagerProcessor() {
	}

	@Override
	public Set<String> getSupportedOptions() {
		return Collections.emptySet();
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return Collections.singleton(MyAnnot.class.getName());
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latest();
	}

	@Override
	public void init(ProcessingEnvironment processingEnv) {
		elementUtils = processingEnv.getElementUtils();
		filer = processingEnv.getFiler();
		messager = processingEnv.getMessager();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		TypeElement annottype = elementUtils.getTypeElement("test.MyAnnot");
		for (Element elem : roundEnv.getElementsAnnotatedWith(annottype)) {
			MyAnnot myannot = elem.getAnnotation(MyAnnot.class);
			List<? extends AnnotationMirror> ams = elem.getAnnotationMirrors();
			for (AnnotationMirror am : ams) {
				if (am.getAnnotationType().asElement().equals(annottype)) {
					Map<? extends ExecutableElement, ? extends AnnotationValue> vals = am.getElementValues();
					if (vals.isEmpty()) {
						messager.printMessage(Diagnostic.Kind.WARNING, "OUT_MESSAGE_" + elem.getSimpleName() + "=" + myannot.value(), elem, am);
					} else {
						messager.printMessage(Diagnostic.Kind.WARNING, "OUT_MESSAGE_VAL_" + elem.getSimpleName() + "=" + myannot.value(), elem, am,
								vals.values().iterator().next());
					}
				}
			}
		}
		TypeElement objelem = elementUtils.getTypeElement(Object.class.getName());
		System.out.println("MessagerProcessor.process() " + objelem.getSimpleName());
		System.out.println("MessagerProcessor.process() " + elementUtils.getBinaryName(objelem));
		for (Element ee : objelem.getEnclosedElements()) {
			System.out.println("MessagerProcessor.process()    " + ee.getSimpleName());
		}
		//try to get the annotation on a JRE class, which results in transferring the custom annotation class to the external process
		//this is in order to test a bugfix
		objelem.getAnnotation(MyAnnot.class);
		objelem.getAnnotationsByType(MyAnnot.class);
		return false;
	}

	@Override
	public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
		return Collections.emptyList();
	}

}
