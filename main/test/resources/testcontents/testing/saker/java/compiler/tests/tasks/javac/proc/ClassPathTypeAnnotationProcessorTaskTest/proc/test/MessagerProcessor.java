package test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.lang.annotation.Retention;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;

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
		TreeSet<String> result = new TreeSet<>();
		result.add("test.FirstAnnot");
		result.add("test.SecondAnnot");
		return result;
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
		TypeElement fannottype = elementUtils.getTypeElement("test.FirstAnnot");
		TypeElement sannottype = elementUtils.getTypeElement("test.SecondAnnot");
		TypeElement overridetype = elementUtils.getTypeElement(Override.class.getCanonicalName());
		System.out.println("FirstAnnot: " + fannottype + " - " + fannottype.getClass());
		System.out.println("    " + fannottype.getAnnotationMirrors());
		System.out.println("SecondAnnot: " + sannottype + " - " + sannottype.getClass());
		System.out.println("    " + sannottype.getAnnotationMirrors());
		System.out.println("Override: " + overridetype + " - " + overridetype.getClass());
		System.out.println("    " + overridetype.getAnnotationMirrors());
		if (fannottype.getAnnotation(Retention.class) == null) {
			//this should work 
			throw new IllegalStateException("Failed to get @Retention of @MyAnnot.");
		}
		if (sannottype.getAnnotation(Retention.class) == null) {
			//this should work 
			throw new IllegalStateException("Failed to get @Retention of @SecondAnnot.");
		}
		if (overridetype.getAnnotation(Retention.class) == null) {
			//this should work when compiling externally too
			throw new IllegalStateException("Failed to get @Retention of @Override.");
		}
		//the processor doesn't really do anything
		System.out.println("MessagerProcessor.process()");
		return false;
	}

	@Override
	public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation,
			ExecutableElement member, String userText) {
		return Collections.emptyList();
	}

}
