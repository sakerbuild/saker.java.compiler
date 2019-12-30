package proc;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Completion;
import javax.annotation.processing.Completions;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.ModuleElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

public class ModuleDocProcessor implements Processor {
	private Elements elements;
	private boolean first = true;
	private Filer filer;

	public Set<String> getSupportedOptions() {
		return Collections.emptySet();
	}

	public Set<String> getSupportedAnnotationTypes() {
		return Collections.singleton("*");
	}

	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latest();
	}

	public void init(ProcessingEnvironment processingEnv) {
		elements = processingEnv.getElementUtils();
		filer = processingEnv.getFiler();
	}

	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (first) {
			first = false;
			ModuleElement moduleelem = elements.getModuleElement("modle.main");
			String doc = elements.getDocComment(moduleelem);
			if (doc == null) {
				return false;
			}
			try {
				FileObject outfile = filer.createResource(StandardLocation.SOURCE_OUTPUT, "", "out.txt", moduleelem);
				try (OutputStream os = outfile.openOutputStream()) {
					os.write(doc.getBytes(StandardCharsets.UTF_8));
				}
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		return false;
	}

	public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation,
			ExecutableElement member, String userText) {
		return Collections.emptySet();
	}

}