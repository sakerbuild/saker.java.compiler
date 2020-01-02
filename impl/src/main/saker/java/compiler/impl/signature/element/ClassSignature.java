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

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.NestingKind;

import saker.build.thirdparty.saker.util.function.Functionals;
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

	public default Collection<? extends FieldSignature> getFields() {
		return getMembers().stream().filter(m -> m instanceof FieldSignature).map(m -> (FieldSignature) m)
				.collect(Collectors.toList());
	}

	public default Collection<? extends ClassSignature> getEnclosedClasses() {
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
			ElementKind kind = member.getKind();
			switch (kind) {
				case ANNOTATION_TYPE:
				case CLASS:
				case ENUM:
				case INTERFACE: {
					types.put(membername, (ClassSignature) member);
					break;
				}
				case CONSTRUCTOR:
				case METHOD:
				case STATIC_INIT:
				case INSTANCE_INIT: {
					methods.computeIfAbsent(membername, Functionals.arrayListComputer()).add((MethodSignature) member);
					break;
				}
				case ENUM_CONSTANT:
				case FIELD: {
					fields.put(membername, (FieldSignature) member);
					break;
				}
				default: {
					throw new IllegalArgumentException("Unknown kind: " + kind);
				}
			}
		}
	}

	public default void categorizeEnclosedMemberSignatures(Collection<? super FieldSignature> fields,
			Collection<? super MethodSignature> methods, Collection<? super ClassSignature> types) {
		for (ClassMemberSignature member : getMembers()) {
			switch (member.getKind()) {
				case ANNOTATION_TYPE:
				case CLASS:
				case ENUM:
				case INTERFACE: {
					types.add((ClassSignature) member);
					break;
				}
				case CONSTRUCTOR:
				case METHOD:
				case STATIC_INIT:
				case INSTANCE_INIT: {
					methods.add((MethodSignature) member);
					break;
				}
				case ENUM_CONSTANT:
				case FIELD: {
					fields.add((FieldSignature) member);
					break;
				}
				default: {
					throw new IllegalArgumentException("Unknown kind: " + member.getKind());
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

}
