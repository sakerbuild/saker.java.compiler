package test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
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
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.tools.JavaFileObject;

public class TesterProcessor implements Processor {
	private boolean first = true;
	private Filer filer;
	private Elements elementUtils;

	public TesterProcessor() {
	}

	@Override
	public Set<String> getSupportedOptions() {
		return Collections.emptySet();
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return Collections.singleton("test.TestAnnotation");
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
		if (!first) {
			return false;
		}
		first = false;
		Set<? extends Element> annotatedelems = roundEnv.getElementsAnnotatedWith(TestAnnotation.class);

		for (Element elem : annotatedelems) {
			System.out.println("TesterProcessor.process() " + elem);
			TestAnnotation annot = elem.getAnnotation(TestAnnotation.class);
			System.out.println("TesterProcessor.process()     " + annot);
			Name name = elem.getSimpleName();
			if (name.contentEquals("def")) {
				eq(annot.type(), TestAnnotation.class);
				eq(annot.typeArray(), new Class<?>[] {});
				eq(annot.cEnum(), CommonEnum.FIRST);
			} else if (name.contentEquals("common")) {
				eq(annot.type(), CommonClass.class);
			} else if (name.contentEquals("compile")) {
				try {
					annot.type();
					throw new AssertionError();
				} catch (MirroredTypeException e) {
					eq(e.getTypeMirrors().size(), 1);
				}
			} else if (name.contentEquals("typenotpresent")) {
				try {
					annot.type();
					throw new AssertionError();
				} catch (MirroredTypeException e) {
					eq(e.getTypeMirrors().size(), 1);
					eq(e.getTypeMirror().getKind(), TypeKind.ERROR);
				}
			} else if (name.contentEquals("defA")) {
				eq(annot.typeArray(), new Class<?>[] { TestAnnotation.class });
			} else if (name.contentEquals("commonA")) {
				eq(annot.typeArray(), new Class<?>[] { CommonClass.class });
			} else if (name.contentEquals("compileA")) {
				try {
					annot.typeArray();
					throw new AssertionError();
				} catch (MirroredTypesException e) {
					eq(e.getTypeMirrors().size(), 1);
				}
			} else if (name.contentEquals("defAA")) {
				eq(annot.typeArray(), new Class<?>[] { TestAnnotation.class, TestAnnotation.class });
			} else if (name.contentEquals("commonAA")) {
				eq(annot.typeArray(), new Class<?>[] { CommonClass.class, CommonClass.class });
			} else if (name.contentEquals("compileAA")) {
				try {
					annot.typeArray();
					throw new AssertionError();
				} catch (MirroredTypesException e) {
					eq(e.getTypeMirrors().size(), 2);
				}
			} else if (name.contentEquals("en1")) {
				eq(annot.cEnum(), CommonEnum.FIRST);
			} else if (name.contentEquals("en2")) {
				eq(annot.cEnum(), CommonEnum.SECOND);
			} else if (name.contentEquals("enX3")) {
				try {
					annot.cEnum();
					throw new AssertionError();
				} catch (EnumConstantNotPresentException e) {
					eq(e.enumType(), CommonEnum.class);
					eq(e.constantName(), "THIRD");
				}
			} else if (name.contentEquals("enX4")) {
				try {
					annot.cEnum();
				} catch (EnumConstantNotPresentException e) {
					eq(e.enumType(), CommonEnum.class);
					eq(e.constantName(), "FOURTH");
				}
			} else {
				throw new AssertionError("Unrecognized elem: " + elem);
			}
		}
		return false;
	}

	@Override
	public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation,
			ExecutableElement member, String userText) {
		return Collections.emptyList();
	}

	private static void eq(Object o1, Object o2) {
		if (!Objects.deepEquals(o1, o2)) {
			throw new AssertionError("Not equals: " + o1 + " - " + o2);
		}
	}
}
