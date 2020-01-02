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
package saker.java.compiler.impl.compile.handler.incremental.model.forwarded;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.ReflectUtils;
import saker.build.thirdparty.saker.util.function.LazySupplier;
import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;

public class ForwardingAnnotatedConstruct<T extends AnnotatedConstruct>
		extends ForwardingJavacObjectBase<IncrementalElementsTypesBase, T> implements AnnotatedConstruct {
	private static final class NoAnnotation implements Annotation {
		@Override
		public Class<? extends Annotation> annotationType() {
			return null;
		}
	}

	private final class AnnotationForwardingInvocationHandler implements InvocationHandler {
		private final Object subject;
		private Map<Method, Object> methodResults = new ConcurrentHashMap<>();

		public AnnotationForwardingInvocationHandler(Object subject) {
			if (subject == null) {
				throw new NullPointerException("subject is null.");
			}
			this.subject = subject;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			return methodResults.computeIfAbsent(method, m -> {
				try {
					Object result = method.invoke(subject, args);
					Class<?> rettype = method.getReturnType();
					if (rettype.isAnnotation()) {
						return forwardAnnotation(result, rettype);
					} else if (rettype.isArray()) {
						Class<?> componenttype = rettype.getComponentType();
						if (componenttype.isAnnotation()) {
							return forwardAnnotationArray((Object[]) result, componenttype);
						}
					}
					return result;
				} catch (InvocationTargetException e) {
					Throwable targetexc = e.getTargetException();
					if (targetexc instanceof MirroredTypesException) {
						if (targetexc instanceof MirroredTypeException) {
							MirroredTypeException mte = (MirroredTypeException) targetexc;
							throw new MirroredTypeException(elemTypes.forwardType(mte.getTypeMirror()));
						}
						MirroredTypesException mte = (MirroredTypesException) targetexc;
						throw new MirroredTypesException(elemTypes.forwardTypes(mte.getTypeMirrors()));
					}
					throw ObjectUtils.sneakyThrow(targetexc);
				} catch (Exception e) {
					throw ObjectUtils.sneakyThrow(e);
				}
			});
		}
	}

	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<ForwardingAnnotatedConstruct, List> ARFU_annotationMirrors = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingAnnotatedConstruct.class, List.class, "annotationMirrors");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<ForwardingAnnotatedConstruct, String> ARFU_toString = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingAnnotatedConstruct.class, String.class, "toString");

	private static final Annotation ANNOTATION_NOT_FOUND = new NoAnnotation();

	private volatile transient List<AnnotationMirror> annotationMirrors;
	private volatile transient String toString;

	private final transient ConcurrentHashMap<Class<? extends Annotation>, Annotation> simpleAnnotations = new ConcurrentHashMap<>();
	private final transient ConcurrentHashMap<Class<? extends Annotation>, Annotation[]> multiAnnotations = new ConcurrentHashMap<>();

	public ForwardingAnnotatedConstruct(IncrementalElementsTypesBase elemTypes, T subject) {
		super(elemTypes, subject);
	}

	@Override
	public T getForwardedSubject() {
		return subject;
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
		String typename = annotationType.getCanonicalName();
		List<? extends AnnotationMirror> amirrors = getAnnotationMirrors();
		AnnotationMirror foundam = null;
		for (AnnotationMirror am : amirrors) {
			DeclaredType amdt = am.getAnnotationType();
			TypeElement amtype = (TypeElement) amdt.asElement();
			if (amtype.getQualifiedName().contentEquals(typename)) {
				if (foundam != null) {
					//an annotation is present multiple times on the construct, return null
					//if it is repeatable, then we should return null
					return null;
				}
				foundam = am;
			}
		}
		if (foundam != null) {
			return convertToAnnotation(annotationType, foundam);
		}
		A inherited = getInheritedAnnotation(annotationType);
		if (inherited != null) {
			return inherited;
		}

		//check if the annotation is a container annotation and if we have appropriate annotations for it
		try {
			Method valmethod = annotationType.getMethod("value");
			Class<?> rettype = valmethod.getReturnType();
			if (rettype.isArray()) {
				Class<?> componenttype = rettype.getComponentType();
				if (componenttype.isAnnotation()) {
					Repeatable componentrep = componenttype.getAnnotation(Repeatable.class);
					if (componentrep != null && componentrep.value() == annotationType) {
						List<AnnotationMirror> containedannots = new ArrayList<>();
						String componenttypename = componenttype.getCanonicalName();
						for (AnnotationMirror am : amirrors) {
							DeclaredType amdt = am.getAnnotationType();
							TypeElement amtype = (TypeElement) amdt.asElement();
							if (amtype.getQualifiedName().contentEquals(componenttypename)) {
								containedannots.add(am);
							}
						}
						//if only a single repeatable annotation is present, do not create a container
						if (containedannots.size() > 1) {
							@SuppressWarnings("unchecked")
							A result = (A) Proxy.newProxyInstance(annotationType.getClassLoader(),
									new Class<?>[] { annotationType }, new RepeatableContainerInvocationHandler<>(
											annotationType, containedannots, valmethod));
							return result;
						}
					}
				}
			}
		} catch (NoSuchMethodException | SecurityException e) {
		}
		return null;

//		Annotation computed = simpleAnnotations.computeIfAbsent(annotationType, a -> {
//			A gotannot = elemTypes.javac(() -> subject.getAnnotation(annotationType));
//			if (gotannot == null) {
//				return ANNOTATION_NOT_FOUND;
//			}
//			return forwardAnnotation(gotannot, annotationType);
//		});
//		if (computed == ANNOTATION_NOT_FOUND) {
//			return null;
//		}
//		@SuppressWarnings("unchecked")
//		A result = (A) computed;
//		return result;
	}

	@Override
	public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
		Repeatable repeatable = annotationType.getAnnotation(Repeatable.class);
		if (repeatable == null) {
			A resannot = getAnnotation(annotationType);
			if (resannot == null) {
				@SuppressWarnings("unchecked")
				A[] result = (A[]) ReflectUtils.createEmptyArray(annotationType);
				return result;
			}
			@SuppressWarnings("unchecked")
			A[] result = (A[]) ReflectUtils.createArray(annotationType, 1);
			result[0] = resannot;
			return result;
		}
		//annotationType is a repeatable annotation
		Class<? extends Annotation> containerclass = repeatable.value();
		String containertypename = containerclass.getCanonicalName();
		String annottypename = annotationType.getCanonicalName();

		//XXX if container is present, do we have to add only one direct annotation?
		List<Annotation> annotlist = new ArrayList<>();

		for (AnnotationMirror am : getAnnotationMirrors()) {
			Name annotcanonicalname = ((TypeElement) am.getAnnotationType().asElement()).getQualifiedName();
			if (annotcanonicalname.contentEquals(containertypename)) {
				getPackedRepeatableAnnotations(am, annotationType, annotlist);
			} else if (annotcanonicalname.contentEquals(annottypename)) {
				annotlist.add(convertToAnnotation(annotationType, am));
			}
		}
		if (annotlist.isEmpty()) {
			if (annotationType.isAnnotationPresent(Inherited.class)) {
				A[] inheriteds = getInheritedAnnotations(annotationType);
				if (inheriteds != null) {
					return inheriteds;
				}
			}
			//no annotation like this at all
			@SuppressWarnings("unchecked")
			A[] result = (A[]) ReflectUtils.createEmptyArray(annotationType);
			return result;
		}

		@SuppressWarnings("unchecked")
		A[] result = (annotlist.toArray((A[]) ReflectUtils.createArray(annotationType, annotlist.size())));
		return result;

//		Annotation[] computed = multiAnnotations.computeIfAbsent(annotationType, a -> {
//			return forwardAnnotationArray(elemTypes.javac(() -> subject.getAnnotationsByType(annotationType)), annotationType);
//		});
//		@SuppressWarnings("unchecked")
//		A[] result = (A[]) computed;
//		if (result.length > 0) {
//			//clone for defensive copy
//			return result.clone();
//		}
//		return result;
	}

	@Override
	public List<? extends AnnotationMirror> getAnnotationMirrors() {
		List<AnnotationMirror> thisannotationmirrors = this.annotationMirrors;
		if (thisannotationmirrors != null) {
			return thisannotationmirrors;
		}
		List<? extends AnnotationMirror> mirrors = elemTypes.javac(subject::getAnnotationMirrors);
		thisannotationmirrors = JavaTaskUtils.cloneImmutableList(mirrors, elemTypes::forward);
		if (ARFU_annotationMirrors.compareAndSet(this, null, thisannotationmirrors)) {
			return thisannotationmirrors;
		}
		return this.annotationMirrors;
	}

	protected <A extends Annotation> A getInheritedAnnotation(Class<A> annoType) {
		return null;
	}

	protected <A extends Annotation> A[] getInheritedAnnotations(Class<A> annoType) {
		return null;
	}

	@Override
	public String toString() {
		String thistostring = this.toString;
		if (thistostring != null) {
			return thistostring;
		}
		thistostring = elemTypes.javac(subject::toString);
		if (ARFU_toString.compareAndSet(this, null, thistostring)) {
			return thistostring;
		}
		return this.toString;
	}

	private void getPackedRepeatableAnnotations(AnnotationMirror am, Class<? extends Annotation> componenttype,
			List<Annotation> resultlist) {
		for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : am.getElementValues().entrySet()) {
			if (entry.getKey().getSimpleName().contentEquals("value")) {
				AnnotationValue val = entry.getValue();
				Object vval = val.getValue();
				if (vval instanceof AnnotationMirror) {
					resultlist.add(convertToAnnotation(componenttype, (AnnotationMirror) vval));
				} else if (vval instanceof List) {
					@SuppressWarnings("unchecked")
					List<? extends AnnotationValue> l = ((List<? extends AnnotationValue>) vval);
					for (AnnotationValue av : l) {
						Object avval = av.getValue();
						if (!(avval instanceof AnnotationMirror)) {
							throw new IllegalArgumentException("Repeatable annotation value is not annotation: " + avval
									+ " in " + componenttype.getCanonicalName());
						}
						resultlist.add(convertToAnnotation(componenttype, (AnnotationMirror) avval));
					}
				}
				return;
			}
		}
	}

	private <A> A forwardAnnotation(Object annotation, Class<A> annottype) {
		//wrap the annotation calls in case of Class<?> and Class<?>[] return type
		//if an annotation throws a MirroredTypesException, then those will need to be forwarded
		Class<?>[] classarray = { annottype };
		@SuppressWarnings("unchecked")
		A result = (A) Proxy.newProxyInstance(annottype.getClassLoader(), classarray,
				new AnnotationForwardingInvocationHandler(annotation));
		return result;
	}

	private <A> A[] forwardAnnotationArray(Object[] array, Class<A> annottype) {
		@SuppressWarnings("unchecked")
		A[] result = (A[]) ReflectUtils.createArray(annottype, array.length);
		for (int i = 0; i < result.length; i++) {
			result[i] = forwardAnnotation(array[i], annottype);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private static <A extends Annotation> A convertToAnnotation(Class<A> type, AnnotationMirror am) {
		return (A) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type },
				new ForwardingAnnotationMirrorInvocationHandler<>(type, am));
	}

	private static class RepeatableContainerInvocationHandler<A extends Annotation> implements InvocationHandler {
		private final Class<A> type;
		private final List<AnnotationMirror> signatures;
		private final Method valueMethod;
		private final Supplier<Annotation[]> valueMethodResultSupplier = LazySupplier
				.of(this::calculateValueMethodResult);

		public RepeatableContainerInvocationHandler(Class<A> type, List<AnnotationMirror> signatures,
				Method valueMethod) {
			this.type = type;
			this.signatures = signatures;
			this.valueMethod = valueMethod;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			String member = method.getName();
			Class<?>[] paramTypes = method.getParameterTypes();
			if (member.equals("equals") && paramTypes.length == 1 && paramTypes[0] == Object.class) {
				if (proxy == args[0]) {
					return true;
				}
				return equalsImpl(args[0]);
			}
			if (paramTypes.length != 0) {
				throw new AssertionError("Too many parameters for an annotation method");
			}
			switch (member) {
				case "toString": {
					return toStringImpl();
				}
				case "hashCode": {
					return hashCodeImpl();
				}
				case "annotationType": {
					return type;
				}
				case "value": {
					return valueMethodResultSupplier.get();
				}
			}
			return method.getDefaultValue();
		}

		private Annotation[] calculateValueMethodResult() {
			@SuppressWarnings("unchecked")
			Class<? extends Annotation> annottype = (Class<? extends Annotation>) valueMethod.getReturnType()
					.getComponentType();
			Annotation[] result = (Annotation[]) Array.newInstance(annottype, signatures.size());
			for (int i = 0; i < result.length; i++) {
				result[i] = convertToAnnotation(annottype, signatures.get(i));
			}
			return result;
		}

		private boolean equalsImpl(Object o) {
			if (!type.isInstance(o)) {
				return false;
			}
			for (Method m : type.getDeclaredMethods()) {
				Object ourValue = valueMethod.equals(m) ? valueMethodResultSupplier.get() : m.getDefaultValue();
				Object hisValue = null;
				try {
					hisValue = m.invoke(o);
				} catch (InvocationTargetException e) {
					//this shouldn't happen, but print it anyways
					e.printStackTrace();
					return false;
				} catch (IllegalAccessException e) {
					throw new AssertionError(e);
				}
				if (!ReflectUtils.annotationValuesEqual(ourValue, hisValue)) {
					return false;
				}
			}
			return true;
		}

		private String toStringImpl() {
			StringBuilder result = new StringBuilder(128);
			result.append('@');
			result.append(type.getName());
			result.append('(');
			result.append('{');
			Annotation[] vals = valueMethodResultSupplier.get();
			for (int i = 0; i < vals.length; i++) {
				Annotation annot = vals[i];
				result.append(annot);
				if (i + 1 < vals.length) {
					result.append(", ");
				}
			}
			result.append('}');
			result.append(')');
			return result.toString();
		}

		private int hashCodeImpl() {
			int result = 0;
			for (Method m : type.getDeclaredMethods()) {
				String methodname = m.getName();
				Object val = m.equals(valueMethod) ? valueMethodResultSupplier.get() : m.getDefaultValue();
				if (val != null) {
					result += (127 * methodname.hashCode()) ^ ReflectUtils.annotationValueHashCode(val);
					continue;
				}
				result += (127 * methodname.hashCode()) ^ ReflectUtils.annotationValueHashCode(m.getDefaultValue());
			}
			return result;
		}
	}

	private static class ForwardingAnnotationMirrorInvocationHandler<A extends Annotation>
			implements InvocationHandler, AnnotationValueVisitor<Object, Class<?>> {

		private final Class<A> type;
		private final Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues;

		public ForwardingAnnotationMirrorInvocationHandler(Class<A> type,
				Map<? extends ExecutableElement, ? extends AnnotationValue> elementValues) {
			this.type = type;
			this.elementValues = elementValues;
		}

		public ForwardingAnnotationMirrorInvocationHandler(Class<A> type, AnnotationMirror am) {
			this(type, am.getElementValues());
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			String member = method.getName();
			Class<?>[] paramTypes = method.getParameterTypes();
			if (member.equals("equals") && paramTypes.length == 1 && paramTypes[0] == Object.class) {
				if (proxy == args[0]) {
					return true;
				}
				return equalsImpl(args[0]);
			}
			if (paramTypes.length != 0) {
				throw new AssertionError("Too many parameters for an annotation method");
			}
			switch (member) {
				case "toString": {
					return toStringImpl();
				}
				case "hashCode": {
					return hashCodeImpl();
				}
				case "annotationType": {
					return type;
				}
			}
			AnnotationValue av = getValueForName(member);
			if (av != null) {
				return av.accept(this, method.getReturnType());
			}
			return method.getDefaultValue();
		}

		private AnnotationValue getValueForName(String member) {
			for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : elementValues.entrySet()) {
				if (entry.getKey().getSimpleName().contentEquals(member)) {
					//found the value
					return entry.getValue();
				}
			}
			return null;
		}

		private String toStringImpl() {
			StringBuilder result = new StringBuilder(128);
			result.append('@');
			result.append(type.getName());
			result.append('(');
			for (Iterator<? extends Entry<? extends ExecutableElement, ? extends AnnotationValue>> it = elementValues
					.entrySet().iterator(); it.hasNext();) {
				Entry<? extends ExecutableElement, ? extends AnnotationValue> entry = it.next();
				result.append(entry.getKey().getSimpleName());
				result.append(" = ");
				//XXX convert the value to a more appropriate string instead of just passing it
				result.append(entry.getValue());
				if (it.hasNext()) {
					result.append(", ");
				}
			}
			result.append(')');
			return result.toString();
		}

		private int hashCodeImpl() {
			int result = 0;
			for (Method m : type.getDeclaredMethods()) {
				String methodname = m.getName();
				AnnotationValue av = getValueForName(methodname);
				if (av != null) {
					Object val = av.accept(this, m.getReturnType());
					result += (127 * methodname.hashCode()) ^ ReflectUtils.annotationValueHashCode(val);
					continue;
				}
				result += (127 * methodname.hashCode()) ^ ReflectUtils.annotationValueHashCode(m.getDefaultValue());
			}
			return result;
		}

		private boolean equalsImpl(Object o) {
			if (!type.isInstance(o)) {
				return false;
			}
			for (Method m : type.getDeclaredMethods()) {
				String member = m.getName();
				AnnotationValue av = getValueForName(member);
				Object ourValue = av == null ? m.getDefaultValue() : av.accept(this, m.getReturnType());
				Object hisValue = null;
				try {
					hisValue = m.invoke(o);
				} catch (InvocationTargetException e) {
					//this shouldn't happen, but print it anyways
					e.printStackTrace();
					return false;
				} catch (IllegalAccessException e) {
					throw new AssertionError(e);
				}
				if (!ReflectUtils.annotationValuesEqual(ourValue, hisValue)) {
					return false;
				}
			}
			return true;
		}

		@Override
		public Object visit(AnnotationValue av, Class<?> p) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Object visit(AnnotationValue av) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Object visitBoolean(boolean b, Class<?> p) {
			return b;
		}

		@Override
		public Object visitByte(byte b, Class<?> p) {
			return b;
		}

		@Override
		public Object visitChar(char c, Class<?> p) {
			return c;
		}

		@Override
		public Object visitDouble(double d, Class<?> p) {
			return d;
		}

		@Override
		public Object visitFloat(float f, Class<?> p) {
			return f;
		}

		@Override
		public Object visitInt(int i, Class<?> p) {
			return i;
		}

		@Override
		public Object visitLong(long i, Class<?> p) {
			return i;
		}

		@Override
		public Object visitShort(short s, Class<?> p) {
			return s;
		}

		@Override
		public Object visitString(String s, Class<?> p) {
			return s;
		}

		@Override
		public Object visitType(TypeMirror t, Class<?> p) {
			throw new MirroredTypeException(t);
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public Object visitEnumConstant(VariableElement c, Class<?> p) {
			return Enum.valueOf((Class) p, c.getSimpleName().toString());
		}

		@Override
		public Object visitAnnotation(AnnotationMirror a, Class<?> p) {
			return convertToAnnotation(type, a);
		}

		@Override
		public Object visitArray(List<? extends AnnotationValue> vals, Class<?> p) {
			int len = vals.size();
			Class<?> componenttype = p.getComponentType();
			Object res = Array.newInstance(componenttype, len);
			for (int i = 0; i < len; i++) {
				Array.set(res, i, vals.get(i).accept(this, componenttype));
			}
			return res;
		}

		@Override
		public Object visitUnknown(AnnotationValue av, Class<?> p) {
			throw new UnsupportedOperationException("Unknown annotation value type: " + av);
		}
	}

}
