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
package saker.java.compiler.impl.compile.handler.incremental.model;

import java.lang.annotation.Annotation;
import java.lang.annotation.AnnotationTypeMismatchException;
import java.lang.annotation.ElementType;
import java.lang.annotation.IncompleteAnnotationException;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;

import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ReflectUtils;
import saker.build.thirdparty.saker.util.function.LazySupplier;
import saker.java.compiler.impl.JavaUtil;
import saker.java.compiler.impl.compile.handler.incremental.model.elem.IncrementalAnnotationMirror;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.element.AnnotationSignature.AnnotValue;
import saker.java.compiler.impl.signature.element.AnnotationSignature.ArrayValue;
import saker.java.compiler.impl.signature.element.AnnotationSignature.LiteralValue;
import saker.java.compiler.impl.signature.element.AnnotationSignature.TypeValue;
import saker.java.compiler.impl.signature.element.AnnotationSignature.Value;
import saker.java.compiler.impl.signature.element.AnnotationSignature.VariableValue;
import saker.java.compiler.impl.signature.type.TypeSignature;

public abstract class IncrementalAnnotatedConstruct implements AnnotatedConstruct, IncrementallyModelled {
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<IncrementalAnnotatedConstruct, List> ARFU_annotationMirrors = AtomicReferenceFieldUpdater
			.newUpdater(IncrementalAnnotatedConstruct.class, List.class, "annotationMirrors");

	protected IncrementalElementsTypesBase elemTypes;
	protected Set<ElementType> elementTypes = Collections.emptySet();

	private volatile transient List<AnnotationMirror> annotationMirrors;

	public IncrementalAnnotatedConstruct(IncrementalElementsTypesBase elemTypes) {
		this.elemTypes = elemTypes;
	}

	public void invalidate() {
		this.annotationMirrors = null;
	}

	public void setElementTypes(Set<ElementType> elementTypes) {
		this.elementTypes = elementTypes;
	}

	public boolean hasDeprecatedAnnotation() {
		//XXX can we do this better?
		for (AnnotationSignature as : getSignatureAnnotations()) {
			if ("java.lang.Deprecated"
					.equals(elemTypes.getCanonicalName(as.getAnnotationType(), getEnclosingResolutionElement()))) {
				return true;
			}
		}
		return false;
	}

	protected abstract Collection<? extends AnnotationSignature> getSignatureAnnotations();

	protected abstract Element getEnclosingResolutionElement();

	@Override
	public final List<? extends AnnotationMirror> getAnnotationMirrors() {
		List<AnnotationMirror> thisannotationmirrors = this.annotationMirrors;
		if (thisannotationmirrors != null) {
			return thisannotationmirrors;
		}
		Collection<? extends AnnotationSignature> annots = getSignatureAnnotations();
		if (annots.isEmpty()) {
			thisannotationmirrors = Collections.emptyList();
		} else {
			ArrayList<AnnotationMirror> nannotationmirrors = new ArrayList<>(annots.size());
			for (AnnotationSignature a : annots) {
				if (!shouldIncludeAnnotation(a)) {
					continue;
				}
				nannotationmirrors.add(new IncrementalAnnotationMirror(elemTypes, a, getEnclosingResolutionElement()));
			}
			thisannotationmirrors = ImmutableUtils.makeImmutableList(nannotationmirrors);
		}
		if (ARFU_annotationMirrors.compareAndSet(this, null, thisannotationmirrors)) {
			return thisannotationmirrors;
		}
		return this.annotationMirrors;
	}

	@Override
	public final <A extends Annotation> A getAnnotation(Class<A> annotationType) {
		if (!shouldIncludeAnnotation(annotationType)) {
			return null;
		}
		String typename = annotationType.getCanonicalName();
		AnnotationSignature founda = null;
		for (AnnotationSignature a : getSignatureAnnotations()) {
			TypeSignature atype = a.getAnnotationType();
			if (typename.equals(elemTypes.getCanonicalName(atype, getEnclosingResolutionElement()))) {
				if (founda != null) {
					//an annotation is present multiple times on the construct, return null
					//if it is repeatable, then we should return null
					return null;
				}
				founda = a;
			}
		}
		if (founda != null) {
			//a single annotation of the type is present
			return convertToAnnotation(annotationType, founda);
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
						List<AnnotationSignature> containedannots = new ArrayList<>();
						String componenttypename = componenttype.getCanonicalName();
						for (AnnotationSignature a : getSignatureAnnotations()) {
							TypeSignature atype = a.getAnnotationType();
							if (componenttypename
									.equals(elemTypes.getCanonicalName(atype, getEnclosingResolutionElement()))) {
								containedannots.add(a);
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
	}

	@Override
	public final <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
		if (!shouldIncludeAnnotation(annotationType)) {
			@SuppressWarnings("unchecked")
			A[] result = (A[]) ReflectUtils.createEmptyArray(annotationType);
			return result;
		}
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

		for (AnnotationSignature a : getSignatureAnnotations()) {
			TypeSignature atype = a.getAnnotationType();
			String annotcanonicalname = elemTypes.getCanonicalName(atype, getEnclosingResolutionElement());
			if (containertypename.equals(annotcanonicalname)) {
				getPackedRepeatableAnnotations(a, annotationType, annotlist);
			} else if (annottypename.equals(annotcanonicalname)) {
				annotlist.add(convertToAnnotation(annotationType, a));
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
	}

	protected <A extends Annotation> A getInheritedAnnotation(Class<A> annoType) {
		return null;
	}

	protected <A extends Annotation> A[] getInheritedAnnotations(Class<A> annoType) {
		return null;
	}

	private boolean shouldIncludeAnnotation(AnnotationSignature signature) {
		TypeSignature type = signature.getAnnotationType();
		TypeElement annotelem = elemTypes.getTypeElement(type, getEnclosingResolutionElement());
		if (annotelem != null) {
			Target target = annotelem.getAnnotation(Target.class);
			return shouldIncludeAnnotation(JavaUtil.getAllowedAnnotationTargets(target));
		}
		//include nonetheless
		return false;
	}

	private boolean shouldIncludeAnnotation(Class<? extends Annotation> annotationtype) {
		Set<ElementType> targets = JavaUtil.getAllowedAnnotationTargets(annotationtype);
		return shouldIncludeAnnotation(targets);
	}

	private boolean shouldIncludeAnnotation(Set<ElementType> targets) {
		Set<ElementType> types = elementTypes;
		for (ElementType t : types) {
			if (targets.contains(t)) {
				return true;
			}
		}
		return false;
	}

	private void getPackedRepeatableAnnotations(AnnotationSignature sig, Class<? extends Annotation> componenttype,
			List<Annotation> resultlist) {
		Value val = sig.getValues().get("value");
		if (val instanceof ArrayValue) {
			ArrayValue av = (ArrayValue) val;
			List<? extends Value> annotvalues = av.getValues();
			for (Value ival : annotvalues) {
				if (!(ival instanceof AnnotValue)) {
					throw new IllegalArgumentException("Repeatable annotation signature value is not annotation: "
							+ ival + " in " + componenttype.getCanonicalName());
				}
				AnnotValue annotval = (AnnotValue) ival;
				resultlist.add(convertToAnnotation(componenttype, annotval.getAnnotation()));
			}
		} else if (val instanceof AnnotValue) {
			AnnotValue av = (AnnotValue) val;
			resultlist.add(convertToAnnotation(componenttype, av.getAnnotation()));
		}
	}

	@SuppressWarnings("unchecked")
	private <A extends Annotation> A convertToAnnotation(Class<A> type, AnnotationSignature signature) {
		return (A) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type },
				new AnnotationSignatureInvocationHandler<>(type, signature));
	}

	private Object getValueAsType(AnnotationSignature.Value value, Class<?> type, Method method, ClassLoader cl) {
		if (type.isAnnotation()) {
			if (value instanceof AnnotValue) {
				AnnotValue av = (AnnotValue) value;
				AnnotationSignature asig = av.getAnnotation();
				@SuppressWarnings("unchecked")
				Annotation result = convertToAnnotation((Class<Annotation>) type, asig);
				return result;
			}
			throw new AnnotationTypeMismatchException(method, value.toString());
		} else if (type.isArray()) {
			if (value instanceof ArrayValue) {
				ArrayValue av = (ArrayValue) value;
				List<? extends Value> avals = av.getValues();
				if (Class[].class == type) {
					Class<?>[] result = new Class<?>[avals.size()];
					boolean fail = false;
					int i = 0;
					for (Value v : avals) {
						if (!(v instanceof TypeValue)) {
							throw new AnnotationTypeMismatchException(method, value.toString());
						}
						TypeValue tv = (TypeValue) v;
						TypeSignature typesig = tv.getType();
						TypeElement typeelem = elemTypes.getTypeElement(typesig, getEnclosingResolutionElement());
						if (typeelem == null) {
							fail = true;
							break;
						}
						try {
							result[i++] = Class.forName(elemTypes.getBinaryName(typeelem).toString(), false, cl);
						} catch (ClassNotFoundException e) {
							fail = true;
							break;
						}
					}
					if (!fail) {
						return result;
					}

					List<TypeMirror> types = new ArrayList<>();
					for (Value v : avals) {
						TypeValue tv = (TypeValue) v;
						TypeSignature typesig = tv.getType();
						TypeMirror tm = elemTypes.getTypeMirror(typesig, null);
						types.add(tm);
					}
					throw new MirroredTypesException(types);
				}
				Class<?> componenttype = type.getComponentType();
				int arraylength = avals.size();
				Object array = ReflectUtils.createArray(componenttype, arraylength);
				for (int i = 0; i < arraylength; i++) {
					Array.set(array, i, getValueAsType(avals.get(i), componenttype, method, cl));
				}
				return array;
			}
			Class<?> componenttype = type.getComponentType();
			Object wrapped = getValueAsType(value, componenttype, method, cl);
			if (wrapped != null) {
				return ReflectUtils.wrapIntoSingletonArray(wrapped, componenttype);
			}
			throw new AnnotationTypeMismatchException(method, value.toString());
		} else if (type.isPrimitive()) {
			if (value instanceof LiteralValue) {
				LiteralValue lv = (LiteralValue) value;
				Object val = lv.getValue().resolve(elemTypes, getEnclosingResolutionElement());
				return val;
			}
			throw new AnnotationTypeMismatchException(method, value.toString());
		} else if (type == String.class) {
			if (value instanceof LiteralValue) {
				LiteralValue lv = (LiteralValue) value;
				Object val = lv.getValue().resolve(elemTypes, getEnclosingResolutionElement());
				return Objects.toString(val, null);
			}
			throw new AnnotationTypeMismatchException(method, value.toString());
		} else if (type.isEnum()) {
			if (value instanceof VariableValue) {
				VariableValue vv = (VariableValue) value;
				TypeElement enumtype = elemTypes.getTypeElement(
						vv.getEnclosingType(elemTypes, getEnclosingResolutionElement()),
						getEnclosingResolutionElement());
				//validate that the enum type and value is found for compilation
				if (enumtype != null) {
					Name binaryname = elemTypes.getBinaryName(enumtype);
					if (binaryname.contentEquals(type.getName())) {
						@SuppressWarnings({ "unchecked", "rawtypes" })
						Class<? extends Enum> enumclass = (Class<? extends Enum>) type;
						try {
							@SuppressWarnings("unchecked")
							Enum<?> result = Enum.valueOf(enumclass, vv.getName());
							return result;
						} catch (IllegalArgumentException e) {
							throw new EnumConstantNotPresentException(enumclass, vv.getName());
						}
					}
				}
				@SuppressWarnings({ "unchecked", "rawtypes" })
				Class<? extends Enum> entype = (Class<? extends Enum>) type;
				throw new EnumConstantNotPresentException(entype, vv.getName());
			}
			throw new AnnotationTypeMismatchException(method, value.toString());
		} else if (type == Class.class) {
			if (value instanceof TypeValue) {
				TypeSignature typesig = ((TypeValue) value).getType();
				TypeElement typeelem = elemTypes.getTypeElement(typesig, getEnclosingResolutionElement());
				if (typeelem != null) {
					try {
						return Class.forName(elemTypes.getBinaryName(typeelem).toString(), false, cl);
					} catch (ClassNotFoundException e) {
						MirroredTypeException exc = new MirroredTypeException(
								elemTypes.getTypeMirror(typesig, getEnclosingResolutionElement()));
						exc.initCause(e);
						throw exc;
					}
				}
				throw new MirroredTypeException(elemTypes.getTypeMirror(typesig, getEnclosingResolutionElement()));
			}
			throw new AnnotationTypeMismatchException(method, value.toString());
		} else {
			//unknown target type
			throw new AnnotationTypeMismatchException(method, value.toString());
		}
	}

	private final class RepeatableContainerInvocationHandler<A extends Annotation> implements InvocationHandler {
		private final Class<A> type;
		private final List<AnnotationSignature> signatures;
		private final Method valueMethod;
		private final Supplier<Annotation[]> valueMethodResultSupplier = LazySupplier
				.of(this::calculateValueMethodResult);

		public RepeatableContainerInvocationHandler(Class<A> type, List<AnnotationSignature> signatures,
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

	private final class AnnotationSignatureInvocationHandler<A extends Annotation> implements InvocationHandler {
		private final Class<A> type;
		private final Map<? extends String, ? extends Value> values;

		public AnnotationSignatureInvocationHandler(Class<A> type, Map<? extends String, ? extends Value> values) {
			this.type = type;
			this.values = values;
		}

		private AnnotationSignatureInvocationHandler(Class<A> type, AnnotationSignature signature) {
			this(type, signature.getValues());
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
			Class<?> returntype = method.getReturnType();
			Value result = values.get(member);
			if (result == null) {
				Object defval = method.getDefaultValue();
				if (defval == null) {
					throw new IncompleteAnnotationException(type, member);
				}
				return defval;
			}
			return getValueAsType(result, returntype, method, type.getClassLoader());
		}

		private String toStringImpl() {
			StringBuilder result = new StringBuilder(128);
			result.append('@');
			result.append(type.getName());
			result.append('(');
			for (Iterator<? extends Entry<? extends String, ? extends Value>> it = values.entrySet().iterator(); it
					.hasNext();) {
				Entry<? extends String, ? extends Value> entry = it.next();
				result.append(entry.getKey());
				result.append(" = ");
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
				Value got = values.get(methodname);
				Object val;
				if (got == null) {
					val = m.getDefaultValue();
				} else {
					val = getValueAsType(got, m.getReturnType(), m, type.getClassLoader());
				}
				if (val != null) {
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
				Value got = values.get(member);
				Object ourValue;
				if (got == null) {
					ourValue = m.getDefaultValue();
				} else {
					ourValue = getValueAsType(got, m.getReturnType(), m, type.getClassLoader());
				}
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
	}
}
