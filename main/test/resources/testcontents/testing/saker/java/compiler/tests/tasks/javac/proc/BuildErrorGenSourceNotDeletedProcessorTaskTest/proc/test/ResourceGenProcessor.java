package test;

import java.io.IOException;
import java.io.UncheckedIOException;
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
import javax.lang.model.element.QualifiedNameable;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

public class ResourceGenProcessor implements Processor {

	private Elements elements;
	private Filer filer;

	public ResourceGenProcessor() {
	}

	@Override
	public Set<String> getSupportedOptions() {
		return Collections.singleton("LOCATION");
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
		elements = processingEnv.getElementUtils();
		filer = processingEnv.getFiler();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		TypeElement annotelem = elements.getTypeElement("test.Annot");
		for (Element e : roundEnv.getElementsAnnotatedWith(annotelem)) {
			String annotval = e.getAnnotationMirrors().get(0).getElementValues().entrySet().iterator().next().getValue()
					.getValue().toString();
			try {
				int dotidx = annotval.indexOf('.');
				String pack = annotval.substring(0, dotidx);
				String simplename = annotval.substring(dotidx + 1);
				JavaFileObject srcfile = filer.createSourceFile(annotval, e);
				try (Writer writer = srcfile.openWriter()) {
					writer.append("package ");
					writer.append(pack);
					writer.append(";");
					writer.append("public class ");
					writer.append(simplename);
					writer.append(" { }");
				}
				FileObject resfile = filer.createResource(StandardLocation.locationFor("RES_OUTPUT"), pack,
						simplename + ".txt", e);
				try (Writer writer = resfile.openWriter()) {
					writer.write(annotval);
				}
				FileObject cresfile = filer.createResource(StandardLocation.CLASS_OUTPUT, pack, simplename + ".ctxt",
						e);
				try (Writer writer = cresfile.openWriter()) {
					writer.write(annotval);
				}
			} catch (IOException exc) {
				throw new UncheckedIOException(exc);
			}
		}
		return false;
	}

	@Override
	public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation,
			ExecutableElement member, String userText) {
		return Collections.emptyList();
	}
}
