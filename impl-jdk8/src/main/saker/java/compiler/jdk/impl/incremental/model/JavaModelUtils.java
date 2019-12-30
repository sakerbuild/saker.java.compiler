package saker.java.compiler.jdk.impl.incremental.model;

import java.util.Map;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.UnknownElementException;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.impl.signature.element.ModuleSignature;
import saker.build.thirdparty.saker.util.function.TriFunction;

public class JavaModelUtils {
	private JavaModelUtils() {
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void addJava9KindBasedElementVisitorFunctions(
			Map<ElementKind, TriFunction<ElementVisitor, ? extends Element, Object, Object>> result) {
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void addJava9KindBasedTypeVisitorFunctions(
			Map<TypeKind, TriFunction<TypeVisitor, ? extends TypeMirror, Object, Object>> result) {
	}

	public static ModuleSignature moduleElementToSignature(Element elem, ParserCache cache) {
		throw new UnknownElementException(elem, null);
	}
}
