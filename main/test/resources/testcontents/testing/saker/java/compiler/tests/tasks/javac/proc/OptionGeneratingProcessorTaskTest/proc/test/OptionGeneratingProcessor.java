package test;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
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
import javax.tools.JavaFileObject;

public class OptionGeneratingProcessor implements Processor {
	public static final String OPTION_VALUE = "test.processor.optiongenerating.value";

	private Filer filer;

	@Override
	public Set<String> getSupportedOptions() {
		return Collections.singleton(OPTION_VALUE);
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return Collections.emptySet();
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latest();
	}

	@Override
	public void init(ProcessingEnvironment processingEnv) {
		this.filer = processingEnv.getFiler();
		String value = processingEnv.getOptions().get(OPTION_VALUE);
		if (value == null) {
			throw new RuntimeException("Value option is missing: " + OPTION_VALUE);
		}

		try {
			JavaFileObject src = filer.createSourceFile("option.Value");
			try (Writer w = src.openWriter()) {
				w.append("package option;");

				w.append("public class Value { public static final String VALUE = " + processingEnv.getElementUtils().getConstantExpression(value) + "; }");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		return false;
	}

	@Override
	public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
		return Collections.emptyList();
	}

}
