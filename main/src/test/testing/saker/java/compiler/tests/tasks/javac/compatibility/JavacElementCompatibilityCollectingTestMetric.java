/*
 * Copyright (C) 2020 Bence Sipka
 *
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package testing.saker.java.compiler.tests.tasks.javac.compatibility;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;
import javax.lang.model.type.UnionType;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import saker.build.thirdparty.saker.util.ConcurrentPrependAccumulator;
import saker.build.thirdparty.saker.util.ObjectUtils;
import testing.saker.java.compiler.CompilerCollectingTestMetric;

public class JavacElementCompatibilityCollectingTestMetric extends CompilerCollectingTestMetric {
	private static class Utils {
		protected Elements elems;
		protected Types types;
		protected Elements javacElems;
		protected Types javacTypes;

		public Utils(Elements elems, Types types, Elements javacElems, Types javacTypes) {
			this.elems = elems;
			this.types = types;
			this.javacElems = javacElems;
			this.javacTypes = javacTypes;
		}
	}

	private static class ElementDifferenceException extends RuntimeException {
		private static final long serialVersionUID = 1L;

		public ElementDifferenceException(String message, Throwable cause) {
			super(message, cause);
		}

		public ElementDifferenceException(String message) {
			super(message);
		}

		public ElementDifferenceException(AnnotatedConstruct e) {
			super(createGenericMessage(e));
		}

		public ElementDifferenceException(String message, AnnotatedConstruct e) {
			super(message + ": " + createGenericMessage(e));
		}

		public ElementDifferenceException(AnnotatedConstruct e, AnnotatedConstruct javace) {
			super(createGenericMessage(e, javace));
		}

		public ElementDifferenceException(TypeMirror t, TypeMirror javact) {
			super(createGenericMessage(t, javact));
		}

		public ElementDifferenceException(Throwable cause, TypeMirror t, TypeMirror javact) {
			super(createGenericMessage(t, javact), cause);
		}

		public ElementDifferenceException(Throwable cause, AnnotatedConstruct e, AnnotatedConstruct javace) {
			super(createGenericMessage(e, javace), cause);
		}

		public ElementDifferenceException(String message, AnnotatedConstruct e, AnnotatedConstruct javace) {
			super(message + ": " + createGenericMessage(e, javace));
		}

		public ElementDifferenceException(String message, Throwable cause, AnnotatedConstruct e,
				AnnotatedConstruct javace) {
			super(message + ": " + createGenericMessage(e, javace), cause);
		}

		private static String createGenericMessage(AnnotatedConstruct e, AnnotatedConstruct javace) {
			return createGenericMessage(e) + " - " + createGenericMessage(javace);
		}

		private static String createGenericMessage(AnnotatedConstruct e) {
			if (e == null) {
				return "null";
			}
			StringBuilder sb = new StringBuilder();
			sb.append(e.getClass());
			if (e instanceof TypeMirror) {
				sb.append("(");
				sb.append(((TypeMirror) e).getKind());
				sb.append(")");
			} else if (e instanceof Element) {
				sb.append("(");
				sb.append(((Element) e).getKind());
				sb.append(")");
			}
			sb.append(": ");
			sb.append(e);
			return sb.toString();
		}

		private static String createGenericMessage(TypeMirror t, TypeMirror javact) {
			return createGenericMessage(t) + " - " + createGenericMessage(javact);
		}

		private static String createGenericMessage(TypeMirror t) {
			if (t == null) {
				return "null";
			}
			return t.getClass() + "(" + t.getKind() + ")" + ": " + t;
		}
	}

	private ConcurrentPrependAccumulator<Runnable> javacClosings = new ConcurrentPrependAccumulator<>();

	@Override
	public void javacCompilationFinished(Elements elems, Types types, Elements javacelems, Types javactypes) {
		try {
			super.javacCompilationFinished(elems, types, javacelems, javactypes);
			Utils utils = new Utils(elems, types, javacelems, javactypes);
			PackageElement pack = elems.getPackageElement("test");
			PackageElement javacpack = javacelems.getPackageElement("test");
			compareElements(pack, javacpack, utils);
		} finally {
			javacClosings.clearAndIterator().forEachRemaining(Runnable::run);
		}
	}

	@Override
	public void javacAddCompilationFinishClosing(Runnable run) {
		this.javacClosings.add(run);
	}

	@Override
	public boolean javacCaresAboutCompilationFinish() {
		return true;
	}

	protected void compareAnnotatedConstructs(AnnotatedConstruct ac, AnnotatedConstruct javacac, Utils utils) {
		//XXX should test inheritable repeatable annotations
		testSameAnnotationsQuery(ac, javacac, MyAnyAnnotation.class);
		testSameAnnotationsQuery(ac, javacac, MyInheritableAnnot.class);
		testSameAnnotationsQuery(ac, javacac, MyRepeatableAnnot.class);
		testSameAnnotationsQuery(ac, javacac, MyRepeatableAnnotContainer.class);
	}

	private void testSameAnnotationsQuery(AnnotatedConstruct ac, AnnotatedConstruct javacac,
			Class<? extends Annotation> annottype) {
		if((ac == null) != (javacac == null)) {
			throw new ElementDifferenceException(ac + " - " + javacac, ac, javacac);
		}
		Annotation i = ac.getAnnotation(annottype);
		Annotation javaci = javacac.getAnnotation(annottype);
		if (!Objects.equals(i, javaci)) {
			throw new ElementDifferenceException(i + " - " + javaci, ac, javacac);
		}
		int ihc = Objects.hashCode(i);
		int javacihc = Objects.hashCode(javaci);
		if (ihc != javacihc) {
			throw new ElementDifferenceException(
					"Different hashcodes: " + i + " (" + ihc + ") - " + javaci + " (" + javacihc + ")", ac, javacac);
		}
		Annotation[] ia = ac.getAnnotationsByType(annottype);
		Annotation[] javacia = javacac.getAnnotationsByType(annottype);
		if (!Arrays.equals(ia, javacia)) {
			throw new ElementDifferenceException(Arrays.toString(ia) + " - " + Arrays.toString(javacia), ac, javacac);
		}
	}

	protected void compareElements(Element e, Element javace, Utils utils) {
		compareAnnotatedConstructs(e, javace, utils);
		new ElementComparingVisitor(javace).visit(e, utils);
	}

	protected void compareElements(List<? extends Element> e, List<? extends Element> javace, Utils utils) {
		if ((e == null) != (javace == null)) {
			throw new ElementDifferenceException(e + " - " + javace);
		}
		if (e.size() != javace.size()) {
			throw new ElementDifferenceException(e + " - " + javace);
		}
		try {
			for (int i = 0; i < e.size(); i++) {
				compareElements(e.get(i), javace.get(i), utils);
			}
		} catch (RuntimeException exc) {
			throw new ElementDifferenceException(e + " - " + javace, exc);
		}
	}

	protected void compareTypes(TypeMirror t, TypeMirror javact, Utils utils) {
		if ((t == null) != (javact == null)) {
			throw new ElementDifferenceException(t, javact);
		}
		if (t == null) {
			//both null
			return;
		}
		compareAnnotatedConstructs(t, javact, utils);
		new TypeComparingVisitor(javact).visit(t, utils);
	}

	protected void compareTypes(List<? extends TypeMirror> t, List<? extends TypeMirror> javact, Utils utils) {
		if ((t == null) != (javact == null)) {
			throw new ElementDifferenceException(t + " - " + javact);
		}
		if (t.size() != javact.size()) {
			throw new ElementDifferenceException(t + " - " + javact);
		}
		try {
			for (int i = 0; i < t.size(); i++) {
				compareTypes(t.get(i), javact.get(i), utils);
			}
		} catch (RuntimeException e) {
			throw new ElementDifferenceException(t + " - " + javact, e);
		}
	}

	protected void compare(Name n, Name javacn) {
		if (!n.contentEquals(javacn)) {
			throw new ElementDifferenceException("Different names: " + n + " - " + javacn);
		}
	}

	protected void compareEquals(Object o, Object javaco) {
		if (!Objects.equals(o, javaco)) {
			throw new ElementDifferenceException("Not equals: " + o + " - " + javaco);
		}
	}

	protected void compareAnnotationValues(AnnotationValue av, AnnotationValue javacav, Utils utils) {
		if ((av == null) != (javacav == null)) {
			throw new ElementDifferenceException("Annotation values differ: " + av + " - " + javacav);
		}
		if (av == null) {
			//both null
			return;
		}
		new AnnotationValueComparingVisitor(javacav).visit(av, utils);
	}

	protected void compareAnnotationValues(List<? extends AnnotationValue> av, List<? extends AnnotationValue> javacav,
			Utils utils) {
		if (av.size() != javacav.size()) {
			throw new ElementDifferenceException(av + " - " + javacav);
		}
		try {
			for (int i = 0; i < av.size(); i++) {
				compareAnnotationValues(av.get(i), javacav.get(i), utils);
			}
		} catch (RuntimeException e) {
			throw new ElementDifferenceException(av + " - " + javacav, e);
		}
	}

	protected void compareAnnotationMirrors(AnnotationMirror am, AnnotationMirror javacam, Utils utils) {
		Map<? extends ExecutableElement, ? extends AnnotationValue> ams = am.getElementValues();
		Map<? extends ExecutableElement, ? extends AnnotationValue> javacams = javacam.getElementValues();
		if (ams.size() != javacams.size()) {
			throw new ElementDifferenceException("Annotation mirrors differ: " + am + " - " + javacam);
		}
		compareTypes(am.getAnnotationType(), javacam.getAnnotationType(), utils);
		outer_loop:
		for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : ams.entrySet()) {
			Name n = entry.getKey().getSimpleName();
			for (Entry<? extends ExecutableElement, ? extends AnnotationValue> javacentry : javacams.entrySet()) {
				if (!n.contentEquals(javacentry.getKey().getSimpleName())) {
					continue;
				}
				compareElements(entry.getKey(), javacentry.getKey(), utils);
				compareAnnotationValues(entry.getValue(), javacentry.getValue(), utils);
				continue outer_loop;
			}
			throw new ElementDifferenceException("Annotation value not found for: " + entry.getKey());
		}
	}

	private void compareAnnotationMirrors(List<? extends AnnotationMirror> am, List<? extends AnnotationMirror> javacam,
			Utils utils) {
		if (am.size() != javacam.size()) {
			throw new ElementDifferenceException(am + " - " + javacam);
		}
		for (int i = 0; i < am.size(); i++) {
			compareAnnotationMirrors(am.get(i), javacam.get(i), utils);
		}
	}

	protected void compareReceiverTypes(ExecutableElement e, ExecutableElement javace, Utils p) {
		TypeMirror rec = e.getReceiverType();
		TypeMirror javacrec = javace.getReceiverType();
		compareReceiverTypes(rec, javacrec, p);
	}

	private void compareReceiverTypes(TypeMirror rec, TypeMirror javacrec, Utils p) {
		//there is a bug that javac returns null for the receiver type if it isn't present, but it should return NoType instead
		if (javacrec == null) {
			if (rec.getKind() == TypeKind.NONE) {
				return;
			}
		}
		compareTypes(rec, javacrec, p);
	}

	protected void compareReceiverTypes(ExecutableType t, ExecutableType javact, Utils p) {
		compareReceiverTypes(t.getReceiverType(), javact.getReceiverType(), p);
	}

	protected void compareIsFunctionalInterface(TypeElement e, TypeElement javace, Utils p) {
		boolean isfunc = p.elems.isFunctionalInterface(e);
		compareEquals(isfunc, p.javacElems.isFunctionalInterface(javace));
		compareEquals(isfunc, e.getAnnotation(FunctionalInterface.class) != null);
	}
	
	protected static List<Element> elementKindNameFilter(List<? extends Element> elems, String kindname) {
		ArrayList<Element> result = new ArrayList<>();
		for (Element e : elems) {
			if (e.getKind().name().equals(kindname)) {
				result.add(e);
			}
		}
		return result;
	}

	private class ElementComparingVisitor implements ElementVisitor<Void, Utils> {
		private Element javacElem;

		public ElementComparingVisitor(Element javacElem) {
			this.javacElem = javacElem;
		}

		@Override
		public Void visit(Element e, Utils p) {
			try {
				if (e.getKind() != javacElem.getKind()) {
					throw new ElementDifferenceException(e, javacElem);
				}
				compare(e.getSimpleName(), javacElem.getSimpleName());
				compareTypes(e.asType(), javacElem.asType(), p);
				//do not compare doc comments, as the are stripped after generate...
//				compareEquals(p.elems.getDocComment(e), p.javacElems.getDocComment(javacElem));
				compareEquals(p.elems.isDeprecated(e), p.javacElems.isDeprecated(javacElem));
				if (!e.getModifiers().equals(javacElem.getModifiers())) {
					throw new ElementDifferenceException(
							"Different modifiers: " + e.getModifiers() + " - " + javacElem.getModifiers(), e,
							javacElem);
				}
				return e.accept(this, p);
			} catch (RuntimeException exc) {
				throw new ElementDifferenceException(exc, e, javacElem);
			}
		}

		@Override
		public Void visit(Element e) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Void visitPackage(PackageElement pack, Utils p) {
			List<? extends Element> packelems = pack.getEnclosedElements();
			PackageElement javacpack = (PackageElement) javacElem;
			List<? extends Element> javacpackelems = javacpack.getEnclosedElements();
			for (Element e : packelems) {
				Element javace = findElementWithSameNameAndKind(e, javacpackelems);
				compareElements(e, javace, p);
			}
			return null;
		}

		@Override
		public Void visitType(TypeElement e, Utils p) {
			TypeElement javace = (TypeElement) javacElem;
			compareElements(e.getTypeParameters(), javace.getTypeParameters(), p);
			compareIsFunctionalInterface(e, javace, p);
			//ordered in source code order
			//do not include clinits
			List<? extends Element> eenc = e.getEnclosedElements();
			List<? extends Element> javacenc = javace.getEnclosedElements().stream()
					.filter(ence -> ence.getKind() != ElementKind.STATIC_INIT).collect(Collectors.toList());

			compareElements(ElementFilter.constructorsIn(eenc), ElementFilter.constructorsIn(javacenc), p);
			compareElements(ElementFilter.methodsIn(eenc), ElementFilter.methodsIn(javacenc), p);
			compareElements(ElementFilter.fieldsIn(eenc), ElementFilter.fieldsIn(javacenc), p);
			compareElements(ElementFilter.packagesIn(eenc), ElementFilter.packagesIn(javacenc), p);
			compareElements(ElementFilter.typesIn(eenc), ElementFilter.typesIn(javacenc), p);
			if ("RECORD".equals(e.getKind().name())) {
				compareElements(elementKindNameFilter(eenc, "RECORD_COMPONENT"),
						elementKindNameFilter(javacenc, "RECORD_COMPONENT"), p);	
			}

			compareTypes(e.getInterfaces(), javace.getInterfaces(), p);
			compareTypes(e.getSuperclass(), javace.getSuperclass(), p);
			compare(p.elems.getBinaryName(e), p.javacElems.getBinaryName(javace));
			return null;
		}

		@Override
		public Void visitVariable(VariableElement e, Utils p) {
			VariableElement javace = (VariableElement) javacElem;
			Object econst = e.getConstantValue();
			Object javaceconst = javace.getConstantValue();
			if (!Objects.equals(econst, javaceconst)) {
				throw new ElementDifferenceException("Constant values differ: " + econst + " - " + javaceconst + " | "
						+ ObjectUtils.classOf(econst) + " - " + ObjectUtils.classOf(javaceconst), e, javace);
			}
			return null;
		}

		@Override
		public Void visitExecutable(ExecutableElement e, Utils p) {
			ExecutableElement javace = (ExecutableElement) javacElem;
			compareElements(e.getTypeParameters(), javace.getTypeParameters(), p);
			compareTypes(e.getReturnType(), javace.getReturnType(), p);
			compareReceiverTypes(e, javace, p);
			compareElements(e.getParameters(), javace.getParameters(), p);
			compareTypes(e.getThrownTypes(), javace.getThrownTypes(), p);
			if (e.isVarArgs() != javace.isVarArgs()) {
				throw new ElementDifferenceException("Varargs differ: " + e.isVarArgs() + " - " + javace.isVarArgs(), e,
						javace);
			}
			if (e.isDefault() != javace.isDefault()) {
				throw new ElementDifferenceException("Default differ: " + e.isDefault() + " - " + javace.isDefault(), e,
						javace);
			}
			compareAnnotationValues(e.getDefaultValue(), javace.getDefaultValue(), p);
			return null;
		}

		@Override
		public Void visitTypeParameter(TypeParameterElement e, Utils p) {
			TypeParameterElement javace = (TypeParameterElement) javacElem;
			compareTypes(e.getBounds(), javace.getBounds(), p);
			//XXX compare generic element?
			return null;
		}

		@Override
		public Void visitUnknown(Element e, Utils p) {
			if ("RECORD_COMPONENT".contentEquals(e.getKind().name())) {
				compareElements(getRecordComponentElementAccessor(e), getRecordComponentElementAccessor(javacElem), p);
				compareElements(e.getEnclosedElements(), javacElem.getEnclosedElements(), p);
				return null;
			}
			throw new ElementDifferenceException(e, javacElem);
		}

		private ExecutableElement getRecordComponentElementAccessor(Element e) {
			try {
				return (ExecutableElement) e.getClass().getMethod("getAccessor").invoke(e);
			} catch (Exception e1) {
				throw new ElementDifferenceException("Failed to get record component accessor: " + e, e1);
			}
		}
	}

	private class AnnotationValueComparingVisitor implements AnnotationValueVisitor<Void, Utils> {
		private AnnotationValue javacAv;

		public AnnotationValueComparingVisitor(AnnotationValue javacAv) {
			this.javacAv = javacAv;
		}

		@Override
		public Void visit(AnnotationValue av, Utils p) {
			try {
				av.accept(this, p);
			} catch (RuntimeException exc) {
				throw new ElementDifferenceException("Annotation values differ: " + av + " - " + javacAv, exc);
			}
			return null;
		}

		@Override
		public Void visit(AnnotationValue av) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Void visitBoolean(boolean b, Utils p) {
			compareEquals(b, javacAv.getValue());
			return null;
		}

		@Override
		public Void visitByte(byte b, Utils p) {
			compareEquals(b, javacAv.getValue());
			return null;
		}

		@Override
		public Void visitChar(char c, Utils p) {
			compareEquals(c, javacAv.getValue());
			return null;
		}

		@Override
		public Void visitDouble(double d, Utils p) {
			compareEquals(d, javacAv.getValue());
			return null;
		}

		@Override
		public Void visitFloat(float f, Utils p) {
			compareEquals(f, javacAv.getValue());
			return null;
		}

		@Override
		public Void visitInt(int i, Utils p) {
			compareEquals(i, javacAv.getValue());
			return null;
		}

		@Override
		public Void visitLong(long i, Utils p) {
			compareEquals(i, javacAv.getValue());
			return null;
		}

		@Override
		public Void visitShort(short s, Utils p) {
			compareEquals(s, javacAv.getValue());
			return null;
		}

		@Override
		public Void visitString(String s, Utils p) {
			compareEquals(s, javacAv.getValue());
			return null;
		}

		@Override
		public Void visitType(TypeMirror t, Utils p) {
			compareTypes(t, (TypeMirror) javacAv.getValue(), p);
			return null;
		}

		@Override
		public Void visitEnumConstant(VariableElement c, Utils p) {
			compareElements(c, (Element) javacAv.getValue(), p);
			return null;
		}

		@Override
		public Void visitAnnotation(AnnotationMirror a, Utils p) {
			compareAnnotationMirrors(a, (AnnotationMirror) javacAv.getValue(), p);
			return null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public Void visitArray(List<? extends AnnotationValue> vals, Utils p) {
			compareAnnotationValues(vals, (List<? extends AnnotationValue>) javacAv.getValue(), p);
			return null;
		}

		@Override
		public Void visitUnknown(AnnotationValue av, Utils p) {
			throw new ElementDifferenceException(av + " - " + javacAv);
		}
	}

	private class TypeComparingVisitor implements TypeVisitor<Void, Utils> {
		private TypeMirror javacType;

		public TypeComparingVisitor(TypeMirror javacType) {
			this.javacType = javacType;
		}

		@Override
		public Void visit(TypeMirror t, Utils p) {
			if (t.getKind() != javacType.getKind()) {
				throw new ElementDifferenceException(t, javacType);
			}
			try {
				t.accept(this, p);
			} catch (RuntimeException exc) {
				throw new ElementDifferenceException(exc, t, javacType);
			}
			return null;
		}

		@Override
		public Void visit(TypeMirror t) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Void visitPrimitive(PrimitiveType t, Utils p) {
			//kind is already tested
//			PrimitiveType javact = (PrimitiveType) javacType;
			return null;
		}

		@Override
		public Void visitNull(NullType t, Utils p) {
//			NullType javact = (NullType) javacType;
			return null;
		}

		@Override
		public Void visitArray(ArrayType t, Utils p) {
			ArrayType javact = (ArrayType) javacType;
			compareTypes(t.getComponentType(), javact.getComponentType(), p);
			return null;
		}

		@Override
		public Void visitDeclared(DeclaredType t, Utils p) {
			DeclaredType javact = (DeclaredType) javacType;
			compare(t.asElement().getSimpleName(), javact.asElement().getSimpleName());
			compare(((TypeElement) t.asElement()).getQualifiedName(),
					((TypeElement) javact.asElement()).getQualifiedName());
			compareTypes(t.getTypeArguments(), javact.getTypeArguments(), p);
			compareTypes(t.getEnclosingType(), javact.getEnclosingType(), p);
			return null;
		}

		@Override
		public Void visitError(ErrorType t, Utils p) {
//			ErrorType javact = (ErrorType) javacType;
			throw new ElementDifferenceException(t, javacType);
		}

		@Override
		public Void visitTypeVariable(TypeVariable t, Utils p) {
			TypeVariable javact = (TypeVariable) javacType;
			TypeParameterElement tpe = (TypeParameterElement) t.asElement();
			TypeParameterElement javactpe = (TypeParameterElement) javact.asElement();
			Element generic = tpe.getGenericElement();
			Element javacgeneric = javactpe.getGenericElement();
			compare(tpe.getSimpleName(), javactpe.getSimpleName());
			compareEquals(generic.getKind(), javacgeneric.getKind());
			compare(generic.getSimpleName(), javacgeneric.getSimpleName());

			compareEquals(t.getLowerBound().getKind(), javact.getLowerBound().getKind());
			compareEquals(t.getUpperBound().getKind(), javact.getUpperBound().getKind());
			//TODO maybe some more refined element and bound comparison, but dont go too far so its not recursive
			return null;
		}

		@Override
		public Void visitWildcard(WildcardType t, Utils p) {
			WildcardType javact = (WildcardType) javacType;
			compareTypes(t.getExtendsBound(), javact.getExtendsBound(), p);
			compareTypes(t.getSuperBound(), javact.getSuperBound(), p);
			return null;
		}

		@Override
		public Void visitExecutable(ExecutableType t, Utils p) {
			ExecutableType javact = (ExecutableType) javacType;
			compareTypes(t.getTypeVariables(), javact.getTypeVariables(), p);
			compareTypes(t.getReturnType(), javact.getReturnType(), p);
			compareTypes(t.getParameterTypes(), javact.getParameterTypes(), p);
			compareReceiverTypes(t, javact, p);
			compareTypes(t.getThrownTypes(), javact.getThrownTypes(), p);
			return null;
		}

		@Override
		public Void visitNoType(NoType t, Utils p) {
			//kind is already tested
//			NoType javact = (NoType) javacType;
			return null;
		}

		@Override
		public Void visitUnion(UnionType t, Utils p) {
			UnionType javact = (UnionType) javacType;
			compareTypes(t.getAlternatives(), javact.getAlternatives(), p);
			return null;
		}

		@Override
		public Void visitIntersection(IntersectionType t, Utils p) {
			IntersectionType javact = (IntersectionType) javacType;
			compareTypes(t.getBounds(), javact.getBounds(), p);
			return null;
		}

		@Override
		public Void visitUnknown(TypeMirror t, Utils p) {
			throw new ElementDifferenceException(t, javacType);
		}

	}

	protected Element findElementWithSameNameAndKind(Element e, Iterable<? extends Element> elems) {
		ElementKind kind = e.getKind();
		Name name = e.getSimpleName();
		for (Element elem : elems) {
			if (elem.getKind() != kind) {
				continue;
			}
			if (!elem.getSimpleName().contentEquals(name)) {
				continue;
			}
			return elem;
		}
		throw new ElementDifferenceException("Element not found", e);
	}

}
