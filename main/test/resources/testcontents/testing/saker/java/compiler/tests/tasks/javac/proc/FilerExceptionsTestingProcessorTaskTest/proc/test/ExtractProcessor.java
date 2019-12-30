package test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.processing.Completion;
import javax.annotation.processing.Filer;
import javax.annotation.processing.FilerException;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

public class ExtractProcessor implements Processor {
	private Filer filer;
	private Elements elementUtils;

	private int roundNumber = 0;

	public ExtractProcessor() {
	}

	@Override
	public Set<String> getSupportedOptions() {
		return Collections.emptySet();
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
		elementUtils = processingEnv.getElementUtils();
		filer = processingEnv.getFiler();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		switch (++roundNumber) {
			case 1: {
				try {
					JavaFileObject outfile = filer.createSourceFile("output.Source");
					try (Writer w = outfile.openWriter()) {
						w.append("package output; class Source { }");
					}
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
				try {
					filer.createSourceFile("");
					throw new AssertionError("FilerException was not thrown.");
				} catch (FilerException e) {
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
				try {
					filer.createClassFile("");
					throw new AssertionError("FilerException was not thrown.");
				} catch (FilerException e) {
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
				try {
					filer.createClassFile("some.invalid/type.Name");
					throw new AssertionError("FilerException was not thrown.");
				} catch (FilerException e) {
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
				break;
			}
			case 2: {
				try {
					filer.createSourceFile("output.Source");
					throw new AssertionError("FilerException was not thrown.");
				} catch (FilerException e) {
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
				try {
					filer.createClassFile("output.Source");
					throw new AssertionError("FilerException was not thrown.");
				} catch (FilerException e) {
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
				try {
					filer.createResource(StandardLocation.SOURCE_OUTPUT, "output", "Source.java");
					throw new AssertionError("FilerException was not thrown.");
				} catch (FilerException e) {
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
				try {
					filer.createResource(StandardLocation.CLASS_OUTPUT, "output", "Source.class");
					throw new AssertionError("FilerException was not thrown.");
				} catch (FilerException e) {
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
				break;
			}
			case 3: {

				break;
			}
			default: {
				break;
			}
		}
		if (roundEnv.processingOver()) {
			if (roundNumber != 3) {
				throw new AssertionError("Unexpected end round number: " + roundNumber);
			}
		}
		return false;
	}

	@Override
	public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation, ExecutableElement member, String userText) {
		return Collections.emptyList();
	}

}
