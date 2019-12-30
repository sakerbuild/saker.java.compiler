package test;

import java.io.IOException;
import java.io.OutputStream;
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
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

public class ExtractProcessor implements Processor {
	private static final Location LOCATION_PROC_RES = StandardLocation.locationFor("PROC_RES");
	private static final Location LOCATION_WORKING_DIRECTORY = StandardLocation.locationFor("WORKING_DIRECTORY");
	private Filer filer;

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
		filer = processingEnv.getFiler();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		switch (++roundNumber) {
			case 1: {
				String markercontents;
				try {
					String cc = filer.getResource(LOCATION_WORKING_DIRECTORY, "", "workdirres.txt").getCharContent(true).toString();
					markercontents = cc;
					if (!"wdres".equals(cc) && !"wdmodified".equals(cc)) {
						throw new AssertionError("expected contents mismatch: " + cc);
					}
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
				try {
					String cc = filer.getResource(LOCATION_WORKING_DIRECTORY, "procresdir", "procdirres.txt").getCharContent(true).toString();
					if (!"pdres".equals(cc)) {
						throw new AssertionError("expected contents mismatch: " + cc);
					}
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
				try {
					String cc = filer.getResource(LOCATION_WORKING_DIRECTORY, "", "procresdir/procdirres.txt").getCharContent(true).toString();
					if (!"pdres".equals(cc)) {
						throw new AssertionError("expected contents mismatch: " + cc);
					}
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
				try {
					String cc = filer.getResource(LOCATION_PROC_RES, "", "procdirres.txt").getCharContent(true).toString();
					if (!"pdres".equals(cc)) {
						throw new AssertionError("expected contents mismatch: " + cc);
					}
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
				//write a resource to signal that the processor was invoked
				try (OutputStream os = filer.createResource(StandardLocation.NATIVE_HEADER_OUTPUT, "", "file.txt").openOutputStream()) {
					os.write(markercontents.getBytes());
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
				try {
					filer.getResource(LOCATION_WORKING_DIRECTORY, "", "workexistence.txt");
				} catch (IOException e) {
				}

				break;
			}
			case 2: {
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
			if (roundNumber != 2) {
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
