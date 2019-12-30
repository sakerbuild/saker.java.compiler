package test;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.Completion;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

public class LastRoundGeneratorProcessor implements Processor {

	private Filer filer;
	private Set<TypeElement> types = new HashSet<>();

	@Override
	public Set<String> getSupportedOptions() {
		return Collections.emptySet();
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return Collections.singleton("test.Annot");
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latest();
	}

	@Override
	public void init(ProcessingEnvironment processingEnv) {
		filer = processingEnv.getFiler();
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		for (TypeElement a : annotations) {
			types.addAll((Set<? extends TypeElement>) roundEnv.getElementsAnnotatedWith(a));
		}
		if (roundEnv.processingOver()) {
			for (TypeElement te : types) {
				try (PrintStream ps = new PrintStream(filer.createSourceFile("output." + te.getSimpleName() + "Impl").openOutputStream())) {
					ps.println("package output;");
					ps.println();
					ps.println("public class " + te.getSimpleName() + "Impl { }");
				} catch (IOException e) {
					throw new UncheckedIOException(e);
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
