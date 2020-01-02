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
package saker.java.compiler.impl.compile.signature.impl;

import java.io.Externalizable;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.java.compiler.impl.compile.signature.type.impl.CanonicalTypeSignatureImpl;
import saker.java.compiler.impl.signature.element.ClassMemberSignature;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.type.CanonicalTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;

public class SimpleAnnotationInterfaceSignature extends ClassSignatureBase {
	private static final List<CanonicalTypeSignature> LIST_SUPER_INTERFACES_JAVA_LANG_ANNOTATION_ANNOTATION = ImmutableUtils
			.singletonList(CanonicalTypeSignatureImpl.INSTANCE_JAVA_LANG_ANNOTATION_ANNOTATION);
	private static final long serialVersionUID = 1L;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleAnnotationInterfaceSignature() {
	}

	public SimpleAnnotationInterfaceSignature(Set<Modifier> modifiers, String packageName, String name,
			List<? extends ClassMemberSignature> members) {
		super(modifiers, packageName, name, members);
	}

	@Override
	public NestingKind getNestingKind() {
		return NestingKind.TOP_LEVEL;
	}

	@Override
	public ClassSignature getEnclosingSignature() {
		return null;
	}

	@Override
	public final ElementKind getKind() {
		return ElementKind.ANNOTATION_TYPE;
	}

	@Override
	public final List<? extends TypeSignature> getSuperInterfaces() {
		return LIST_SUPER_INTERFACES_JAVA_LANG_ANNOTATION_ANNOTATION;
	}
}
