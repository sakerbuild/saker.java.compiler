package test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.processing.Completion;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;

public class InitProcessor implements Processor {
	private static final String OPTION = "option";
	private Filer filer;

	public InitProcessor() {
	}

	@Override
	public Set<String> getSupportedOptions() {
		return Collections.singleton(OPTION);
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return Collections.singleton("java.lang.Override");
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latest();
	}

	@Override
	public void init(ProcessingEnvironment processingEnv) {
		filer = processingEnv.getFiler();
		try {
			JavaFileObject src = filer.createSourceFile("output.Generated");
			try (Writer w = src.openWriter()) {
				w.append("package output;\n\npublic class Generated {\n");
				w.append("public static final String OPTIONVALUE = "
						+ processingEnv.getElementUtils().getConstantExpression(processingEnv.getOptions().get(OPTION) + "") + ";");
				w.append("\tpublic Generated() { }\n");
				w.append("}");
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		return false;
	}

	public static void generateSource(int groupid, Set<String> typenames, Writer w) throws IOException {
		w.append("package groups;\n\npublic class Group" + groupid + " {\n");
		for (String te : typenames) {
			w.append("\tpublic Group" + groupid + "(" + te + " param) { }\n");
		}
		w.append("}");
	}

	@Override
	public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
		return Collections.emptyList();
	}

}
