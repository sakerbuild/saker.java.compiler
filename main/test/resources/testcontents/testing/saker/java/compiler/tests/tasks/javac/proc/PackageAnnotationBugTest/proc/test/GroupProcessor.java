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
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

public class GroupProcessor implements Processor {
	private Filer filer;

	private Map<Integer, SortedMap<String, TypeElement>> groups = new TreeMap<>();

	private Messager messager;

	public GroupProcessor() {
	}

	@Override
	public Set<String> getSupportedOptions() {
		return Collections.emptySet();
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return Collections.singleton("test.GroupBy");
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latest();
	}

	@Override
	public void init(ProcessingEnvironment processingEnv) {
		filer = processingEnv.getFiler();
		messager = processingEnv.getMessager();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		for (Element annotatedelem : roundEnv.getElementsAnnotatedWith(GroupBy.class)) {
			for (Element encelem : annotatedelem.getEnclosedElements()) {
				messager.printMessage(Diagnostic.Kind.WARNING, "Warning on " + encelem, encelem);
			}
			messager.printMessage(Diagnostic.Kind.WARNING, "Warning on " + annotatedelem, annotatedelem);
		}
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
