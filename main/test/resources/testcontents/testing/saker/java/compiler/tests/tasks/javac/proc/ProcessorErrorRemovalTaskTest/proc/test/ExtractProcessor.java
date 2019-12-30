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

public class ExtractProcessor implements Processor {
	private Filer filer;
	private Elements elementUtils;

	public ExtractProcessor() {
	}

	@Override
	public Set<String> getSupportedOptions() {
		return Collections.emptySet();
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return Collections.singleton("test.ExtractInterface");
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latest();
	}

	@Override
	public void init(ProcessingEnvironment processingEnv) {
		elementUtils = processingEnv.getElementUtils();
		filer = processingEnv.getFiler();

		//assert that the processing api and javac classes are available for the loaded processors
		try {
			Class<?> procenvclass = Class.forName("saker.java.compiler.api.processing.SakerProcessingEnvironment",
					false, this.getClass().getClassLoader());
			procenvclass.cast(processingEnv);
			Class.forName("saker.java.compiler.api.processing.SakerFiler", false, this.getClass().getClassLoader())
					.cast(filer);
			Class.forName("saker.java.compiler.api.processing.SakerMessager", false, this.getClass().getClassLoader())
					.cast(processingEnv.getMessager());
			Class.forName("saker.java.compiler.api.processing.SakerElementsTypes", false,
					this.getClass().getClassLoader()).cast(elementUtils);
			Class.forName("saker.java.compiler.api.processing.SakerElementsTypes", false,
					this.getClass().getClassLoader()).cast(processingEnv.getTypeUtils());
			Class.forName("com.sun.source.tree.Tree", false, this.getClass().getClassLoader());
		} catch (Exception e) {
			throw new AssertionError(e);
		}
	}

	private static TypeElement getEnclosingType(Element element) {
		element = element.getEnclosingElement();
		while (element != null) {
			ElementKind kind = element.getKind();
			if (kind.isClass() || kind.isInterface()) {
				break;
			}
			element = element.getEnclosingElement();
		}
		return (TypeElement) element;
	}

	private static String getTypeName(TypeMirror tm) {
		TypeKind kind = tm.getKind();
		if (kind.isPrimitive()) {
			return kind.toString().toLowerCase();
		}
		if (kind == TypeKind.VOID) {
			return "void";
		}
		if (kind == TypeKind.DECLARED) {
			DeclaredType dt = (DeclaredType) tm;
			return ((TypeElement) dt.asElement()).getQualifiedName().toString();
		}
		throw new IllegalArgumentException("Unknown type: " + tm + " with kind: " + kind);
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		for (TypeElement annot : annotations) {
			Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(annot);
			//use a sorted map to collect the methods, to have a consistent generation of sources
			//the generated methods should be in the same order always.
			Map<TypeElement, SortedMap<String, ExecutableElement>> typemap = new HashMap<>();
			for (Element e : elements) {
				typemap.computeIfAbsent(getEnclosingType(e), k -> new TreeMap<>()).put(e.getSimpleName().toString(),
						(ExecutableElement) e);
			}
			for (Entry<TypeElement, SortedMap<String, ExecutableElement>> entry : typemap.entrySet()) {
				try {
					TypeElement type = entry.getKey();

					String packname = elementUtils.getPackageOf(type).getQualifiedName().toString();
					String itfname = type.getSimpleName() + "Itf";
					JavaFileObject src = filer.createSourceFile(packname + "." + itfname, type);
					try (Writer writer = src.openWriter()) {
						writer.append("package " + packname + ";\n");
						writer.append("public interface " + itfname + " {\n");
						for (ExecutableElement ee : entry.getValue().values()) {
							writer.append("public ");
							writer.append(getTypeName(ee.getReturnType()));
							writer.append(" ");
							writer.append(ee.getSimpleName());
							writer.append("(");
							Iterator<? extends VariableElement> paramit = ee.getParameters().iterator();
							while (paramit.hasNext()) {
								VariableElement p = paramit.next();
								writer.append(getTypeName(p.asType()));
								writer.append(" ");
								writer.append(p.getSimpleName());
								if (paramit.hasNext()) {
									writer.append(", ");
								}
							}
							writer.append(")");
							Iterator<? extends TypeMirror> thrownit = ee.getThrownTypes().iterator();
							if (thrownit.hasNext()) {
								writer.append(" throws ");
								do {
									writer.append(getTypeName(thrownit.next()));
									if (thrownit.hasNext()) {
										writer.append(", ");
									}
								} while (thrownit.hasNext());
							}
							writer.append(";\n");
						}
						writer.append("}\n");
					}
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
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
