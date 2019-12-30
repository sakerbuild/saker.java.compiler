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
		return Collections.singleton("test.MyAnnot");
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
			System.out.println("MessagerProcessor.process() " + elem + " - " + elem.getKind());
			if (elem.getKind() == ElementKind.PACKAGE) {
				messager.printMessage(Diagnostic.Kind.WARNING, "OUT_MESSAGE_" + elem.getSimpleName(), elem);
				for (AnnotationMirror am : elem.getAnnotationMirrors()) {
					messager.printMessage(Diagnostic.Kind.WARNING,
							"OUT_MESSAGE_" + elem.getSimpleName() + "@" + am.getAnnotationType().asElement().getSimpleName(), elem, am);
				}
			} else {
				for (Element ee : elem.getEnclosedElements()) {
					System.out.println("MessagerProcessor.process()     " + ee);
					if (ee.getKind() == ElementKind.CONSTRUCTOR) {
						messager.printMessage(Diagnostic.Kind.WARNING, "OUT_MESSAGE_" + elem.getSimpleName() + "." + ee.getSimpleName(), ee);
					}
				}
			}
		}
		return false;
	}

	@Override
	public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
		return Collections.emptyList();
	}

}
