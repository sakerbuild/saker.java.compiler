package test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.UnknownElementException;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.AbstractElementVisitor8;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import javax.tools.Diagnostic.Kind;

public class MessagerProcessor implements Processor {
	private Filer filer;
	private Elements elementUtils;
	private Messager messager;

	private boolean annotTypeMessages;

	public MessagerProcessor() {
	}

	@Override
	public Set<String> getSupportedOptions() {
		return Collections.emptySet();
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return Collections.singleton("test.MyAnnot");
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latest();
	}

	@Override
	public void init(ProcessingEnvironment processingEnv) {
		elementUtils = processingEnv.getElementUtils();
		filer = processingEnv.getFiler();
		messager = processingEnv.getMessager();
	}

	private void applyMessages(StringBuilder sb, Element elem, AnnotationMirror am) {
		int len = sb.length();
		try {
			DeclaredType at = am.getAnnotationType();
			sb.append("@");
			sb.append(((TypeElement) at.asElement()).getQualifiedName());
			messager.printMessage(Kind.WARNING, sb, elem, am);
			sb.append(":");
			for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : am.getElementValues()
					.entrySet()) {
				int slen = sb.length();
				try {
					sb.append(entry.getKey().getSimpleName());
					applyMessages(sb, elem, am, entry.getValue());
				} finally {
					sb.setLength(slen);
				}
			}
		} finally {
			sb.setLength(len);
		}
	}

	private void applyMessages(StringBuilder sb, Element elem, AnnotationMirror am, AnnotationValue av) {
		int len = sb.length();
		try {
			Object value = av.getValue();
			if (value instanceof List) {
				List<?> vallist = (List<?>) value;
				if (vallist.isEmpty()) {
					sb.append("={}");
				}
				messager.printMessage(Kind.WARNING, sb, elem, am, av);

				int idx = 0;
				for (Object o : vallist) {
					int slen = sb.length();
					try {
						sb.append("[");
						sb.append(idx++);
						sb.append("]");
						AnnotationValue subval = (AnnotationValue) o;
						applyMessages(sb, elem, am, subval);
					} finally {
						sb.setLength(slen);
					}
				}
			} else if (value instanceof AnnotationMirror) {
				messager.printMessage(Kind.WARNING, sb, elem, am, av);
				sb.append("=");
				applyMessages(sb, elem, (AnnotationMirror) value);
			} else if (value instanceof VariableElement) {
				sb.append("=");
				sb.append(((VariableElement) value).getSimpleName());
				messager.printMessage(Kind.WARNING, sb, elem, am, av);
			} else {
				sb.append("=");
				sb.append(value);
				messager.printMessage(Kind.WARNING, sb, elem, am, av);
			}
		} finally {
			sb.setLength(len);
		}
	}

	private void applyMessages(StringBuilder sb, Element elem) {
		int len = sb.length();
		try {
			elem.accept(new AbstractElementVisitor8<Void, Void>() {
				@Override
				public Void visitExecutable(ExecutableElement e, Void p) {
					sb.append(e.getSimpleName());
					messager.printMessage(Kind.WARNING, sb, e);

					for (AnnotationMirror am : elem.getAnnotationMirrors()) {
						applyMessages(sb, elem, am);
					}

					AnnotationValue defval = e.getDefaultValue();
					if (defval != null) {
						int slen = sb.length();
						try {
							sb.append(":default");
							applyMessages(sb, e, null, defval);
						} finally {
							sb.setLength(slen);
						}
					}
					TypeMirror receivertype = e.getReceiverType();
					if (receivertype != null) {
						List<? extends AnnotationMirror> receiverannots = receivertype.getAnnotationMirrors();
						if (receiverannots != null) {
							int slen = sb.length();
							try {
								sb.append(":receiver");
								for (AnnotationMirror am : receiverannots) {
									applyMessages(sb, e, am);
								}
							} finally {
								sb.setLength(slen);
							}
						}
					}

					sb.append('_');
					for (Element ee : e.getEnclosedElements()) {
						applyMessages(sb, ee);
					}
					for (Element ee : e.getTypeParameters()) {
						applyMessages(sb, ee);
					}
					for (VariableElement ee : e.getParameters()) {
						applyMessages(sb, ee);
					}
					return null;
				}

				@Override
				public Void visitPackage(PackageElement e, Void p) {
					sb.append("PACK/");
					sb.append(e.getQualifiedName());
					messager.printMessage(Kind.WARNING, sb, e);

					for (AnnotationMirror am : elem.getAnnotationMirrors()) {
						applyMessages(sb, elem, am);
					}
					return null;
				}

				@Override
				public Void visitType(TypeElement e, Void p) {
					sb.append(e.getQualifiedName());
					messager.printMessage(Kind.WARNING, sb, e);

					for (AnnotationMirror am : elem.getAnnotationMirrors()) {
						applyMessages(sb, elem, am);
					}

					sb.append('_');
					for (Element ee : e.getEnclosedElements()) {
						applyMessages(sb, ee);
					}
					for (Element ee : e.getTypeParameters()) {
						applyMessages(sb, ee);
					}
					return null;
				}

				@Override
				public Void visitTypeParameter(TypeParameterElement e, Void p) {
					sb.append(e.getSimpleName());
					messager.printMessage(Kind.WARNING, sb, e);

					for (AnnotationMirror am : elem.getAnnotationMirrors()) {
						applyMessages(sb, elem, am);
					}

					sb.append('_');
					for (Element ee : e.getEnclosedElements()) {
						applyMessages(sb, ee);
					}
					return null;
				}

				@Override
				public Void visitVariable(VariableElement e, Void p) {
					sb.append(e.getSimpleName());
					messager.printMessage(Kind.WARNING, sb, e);

					for (AnnotationMirror am : elem.getAnnotationMirrors()) {
						applyMessages(sb, elem, am);
					}

					sb.append('_');
					for (Element ee : e.getEnclosedElements()) {
						applyMessages(sb, ee);
					}
					return null;
				}
			}, null);
		} finally {
			sb.setLength(len);
		}
	}

//	private void applyMessages(StringBuilder sb, Element elem, AnnotationMirror am, AnnotationValue av) {
//		int len = sb.length();
//		try {
//			if (av != null) {
//				messager.printMessage(Kind.WARNING, sb, elem);
//				sb.append("_");
//
//			}
//			if (am != null) {
//			} else {
//				sb.append(elem.getSimpleName());
//				messager.printMessage(Kind.WARNING, sb, elem);
//				sb.append("_");
//
//				if (elem instanceof ExecutableElement) {
//					ExecutableElement exece = (ExecutableElement) elem;
//					AnnotationValue defval = exece.getDefaultValue();
//					if (defval != null) {
//						applyMessages(sb, elem, null, defval);
//					}
//				}
//
//				for (Element ee : elem.getEnclosedElements()) {
//					applyMessages(sb, ee, null, null);
//				}
//			}
//		} finally {
//			sb.setLength(len);
//		}
//	}

	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		if (!annotTypeMessages) {
			applyMessages(new StringBuilder(), elementUtils.getTypeElement("test.MyAnnot"));
			applyMessages(new StringBuilder(), elementUtils.getTypeElement("test.SecondAnnot"));
			annotTypeMessages = true;
		}
		for (Element elem : roundEnv.getElementsAnnotatedWith(elementUtils.getTypeElement("test.MyAnnot"))) {
			applyMessages(new StringBuilder(), elem);
		}
		for (Element elem : roundEnv.getElementsAnnotatedWith(elementUtils.getTypeElement("test.MyRepeatable"))) {
			applyMessages(new StringBuilder(), elem);
		}
		for (Element elem : roundEnv.getElementsAnnotatedWith(elementUtils.getTypeElement("test.MyContainer"))) {
			applyMessages(new StringBuilder(), elem);
		}
		return false;
	}

	@Override
	public Iterable<? extends Completion> getCompletions(Element element, AnnotationMirror annotation,
			ExecutableElement member, String userText) {
		return Collections.emptyList();
	}

}
