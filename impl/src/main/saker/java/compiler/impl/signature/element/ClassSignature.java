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
package saker.java.compiler.impl.signature.element;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.lang.model.element.NestingKind;

import saker.build.thirdparty.saker.util.function.Functionals;
import saker.java.compiler.impl.compat.ElementKindCompatUtils;
import saker.java.compiler.impl.signature.type.ParameterizedTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;

public interface ClassSignature extends ClassMemberSignature, ParameterizedSignature, ClassSignatureHeader {

	public String getPackageName();

	@Override
	public NestingKind getNestingKind();

	@Override
	public ClassSignature getEnclosingSignature();

	public TypeSignature getSuperClass();

	public List<? extends TypeSignature> getSuperInterfaces();

	public List<? extends ClassMemberSignature> getMembers();

	public PermittedSubclassesList getPermittedSubclasses();

	public ParameterizedTypeSignature getTypeSignature();

	public ParameterizedTypeSignature getTypeSignature(List<? extends TypeSignature> typeparameters);

	@Override
	public default String getBinaryName() {
		ClassSignature enclosing = getEnclosingSignature();
		String name = getSimpleName();
		String packagename = getPackageName();
		if (packagename == null) {
			if (enclosing == null) {
				return name;
			}
			return enclosing.getBinaryName() + "$" + name;
		}
		if (enclosing == null) {
			return packagename + "." + name;
		}
		return enclosing.getBinaryName() + "$" + name;
	}

	public default String getCanonicalName() {
		ClassSignature enclosing = getEnclosingSignature();
		String name = getSimpleName();
		String packagename = getPackageName();
		if (packagename == null) {
			if (enclosing == null) {
				return name;
			}
			return enclosing.getCanonicalName() + "." + name;
		}
		if (enclosing == null) {
			return packagename + "." + name;
		}
		return enclosing.getCanonicalName() + "." + name;
	}

	public default Collection<? extends MethodSignature> getMethods() {
		return getMembers().stream().filter(m -> m instanceof MethodSignature).map(m -> (MethodSignature) m)
				.collect(Collectors.toList());
	}

	public default Collection<? extends MethodSignature> getConstructors() {
		return getMembers().stream().filter(m -> m.getKindIndex() == ElementKindCompatUtils.ELEMENTKIND_INDEX_CONSTRUCTOR)
				.map(m -> (MethodSignature) m).collect(Collectors.toList());
	}

	public default Collection<? extends FieldSignature> getFields() {
		return getMembers().stream().filter(m -> m instanceof FieldSignature).map(m -> (FieldSignature) m)
				.collect(Collectors.toList());
	}

	public default Collection<? extends ClassSignature> getEnclosedTypes() {
		return getMembers().stream().filter(m -> m instanceof ClassSignature).map(m -> (ClassSignature) m)
				.collect(Collectors.toList());
	}

	public default Collection<? extends MethodSignature> getMethods(String name) {
		return getMembers().stream().filter(m -> m instanceof MethodSignature && m.getSimpleName().equals(name))
				.map(m -> (MethodSignature) m).collect(Collectors.toList());
	}

	public default void categorizeEnclosedMemberSignaturesByName(Map<? super String, ? super FieldSignature> fields,
			Map<? super String, Collection<MethodSignature>> methods,
			Map<? super String, ? super ClassSignature> types) {
		for (ClassMemberSignature member : getMembers()) {
			String membername = member.getSimpleName();
			switch (member.getKindIndex()) {
				case ElementKindCompatUtils.ELEMENTKIND_INDEX_ANNOTATION_TYPE:
				case ElementKindCompatUtils.ELEMENTKIND_INDEX_CLASS:
				case ElementKindCompatUtils.ELEMENTKIND_INDEX_ENUM:
				case ElementKindCompatUtils.ELEMENTKIND_INDEX_INTERFACE: {
					types.put(membername, (ClassSignature) member);
					break;
				}
				case ElementKindCompatUtils.ELEMENTKIND_INDEX_CONSTRUCTOR:
				case ElementKindCompatUtils.ELEMENTKIND_INDEX_METHOD:
				case ElementKindCompatUtils.ELEMENTKIND_INDEX_STATIC_INIT:
				case ElementKindCompatUtils.ELEMENTKIND_INDEX_INSTANCE_INIT: {
					methods.computeIfAbsent(membername, Functionals.arrayListComputer()).add((MethodSignature) member);
					break;
				}
				case ElementKindCompatUtils.ELEMENTKIND_INDEX_ENUM_CONSTANT:
				case ElementKindCompatUtils.ELEMENTKIND_INDEX_FIELD: {
					fields.put(membername, (FieldSignature) member);
					break;
				}
				case ElementKindCompatUtils.ELEMENTKIND_INDEX_RECORD_COMPONENT: {
					//TODO should we use a different map instead of fields?
					fields.put(membername, (FieldSignature) member);
					break;
				}
				default: {
					throw new IllegalArgumentException(
							"Unknown kind: " + ElementKindCompatUtils.getElementKindName(member.getKindIndex()));
				}
			}
		}
	}

	public default boolean hasMethod(Predicate<MethodSignature> predicate) {
		return getMembers().stream().anyMatch(m -> m instanceof MethodSignature && predicate.test((MethodSignature) m));
	}

	public static SortedMap<String, Collection<MethodSignature>> getMethodsByName(ClassSignature sig) {
		SortedMap<String, Collection<MethodSignature>> result = new TreeMap<>();
		for (ClassMemberSignature m : sig.getMembers()) {
			if (m instanceof MethodSignature) {
				MethodSignature ms = (MethodSignature) m;
				result.computeIfAbsent(ms.getSimpleName(), Functionals.arrayListComputer()).add(ms);
			}
		}
		return result;
	}

	public static SortedMap<String, ? extends FieldSignature> getFieldsByName(ClassSignature sig) {
		SortedMap<String, FieldSignature> result = new TreeMap<>();
		for (ClassMemberSignature member : sig.getMembers()) {
			if (member instanceof FieldSignature) {
				result.put(member.getSimpleName(), (FieldSignature) member);
			}
		}
		return result;
	}

	public interface PermittedSubclassesList {
		public void accept(Visitor visitor);
		
		public default boolean signatureEquals(PermittedSubclassesList other) {
			return this.equals(other);
		}

		public interface Visitor {
			public void visitExplicit(List<? extends TypeSignature> types);

			public void visitUnspecified();
		}
	}
}
