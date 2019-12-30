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
import javax.tools.StandardLocation;

public class ResourceGenProcessor implements Processor {

	private Location genLocation;
	private Elements elements;
	private Filer filer;

	public ResourceGenProcessor() {
	}

	public ResourceGenProcessor(Location genLocation) {
		this.genLocation = genLocation;
	}

	@Override
	public Set<String> getSupportedOptions() {
		return Collections.singleton("LOCATION");
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return Collections.singleton("*");
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latest();
	}

	@Override
	public void init(ProcessingEnvironment processingEnv) {
		elements = processingEnv.getElementUtils();
		filer = processingEnv.getFiler();

		genLocation = StandardLocation.locationFor(processingEnv.getOptions().get("LOCATION"));
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		for (Element e : roundEnv.getRootElements()) {
			if (!(e instanceof QualifiedNameable)) {
				continue;
			}
			QualifiedNameable qnamed = (QualifiedNameable) e;
			try {

				String genname = qnamed.getSimpleName().toString() + ".txt";
				FileObject outres = filer.createResource(genLocation,
						elements.getPackageOf(e).getQualifiedName().toString(), genname, e);

				try (Writer w = outres.openWriter()) {
					w.append(((QualifiedNameable) e).getQualifiedName());
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
