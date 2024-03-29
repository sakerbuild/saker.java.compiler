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
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

import saker.java.compiler.impl.JavaUtil;
import saker.java.compiler.impl.compat.ElementKindCompatUtils;
import saker.java.compiler.impl.compile.signature.type.impl.NoTypeSignatureImpl;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.element.AnnotationSignature.Value;
import saker.java.compiler.impl.signature.element.MethodParameterSignature;
import saker.java.compiler.impl.signature.element.MethodSignature;
import saker.java.compiler.impl.signature.type.TypeParameterSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.util.ImmutableModifierSet;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

public final class SimpleNoArgConstructor implements MethodSignature, Externalizable {
	private static final long serialVersionUID = 1L;

	private static final SimpleNoArgConstructor INSTANCE_MODIFIERS_NONE = new SimpleNoArgConstructor(
			ImmutableModifierSet.FLAG_NONE);
	private static final SimpleNoArgConstructor INSTANCE_MODIFIERS_PUBLIC = new SimpleNoArgConstructor(
			ImmutableModifierSet.FLAG_PUBLIC);
	private static final SimpleNoArgConstructor INSTANCE_MODIFIERS_PROTECTED = new SimpleNoArgConstructor(
			ImmutableModifierSet.FLAG_PROTECTED);
	private static final SimpleNoArgConstructor INSTANCE_MODIFIERS_PRIVATE = new SimpleNoArgConstructor(
			ImmutableModifierSet.FLAG_PRIVATE);

	protected short modifierFlags;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleNoArgConstructor() {
	}

	private SimpleNoArgConstructor(short modifierFlags) {
		this.modifierFlags = modifierFlags;
	}

	public static MethodSignature create(Set<Modifier> modifiers) {
		//based on JLS, only the following modifiers are possible, so cache them
		short modflags = ImmutableModifierSet.getFlag(modifiers);
		switch (modflags) {
			case ImmutableModifierSet.FLAG_NONE:
				return INSTANCE_MODIFIERS_NONE;
			case ImmutableModifierSet.FLAG_PUBLIC:
				return INSTANCE_MODIFIERS_PUBLIC;
			case ImmutableModifierSet.FLAG_PROTECTED:
				return INSTANCE_MODIFIERS_PROTECTED;
			case ImmutableModifierSet.FLAG_PRIVATE:
				return INSTANCE_MODIFIERS_PRIVATE;
			default:
				//if by any chance the modifiers are different, create a new instance
				return new SimpleNoArgConstructor(modflags);
		}
	}

	@Override
	public String getSimpleName() {
		return IncrementalElementsTypes.CONSTRUCTOR_METHOD_NAME;
	}

	@Override
	public Set<Modifier> getModifiers() {
		return ImmutableModifierSet.forFlags(modifierFlags);
	}

	@Override
	public ElementKind getKind() {
		return ElementKind.CONSTRUCTOR;
	}

	@Override
	public final byte getKindIndex() {
		return ElementKindCompatUtils.ELEMENTKIND_INDEX_CONSTRUCTOR;
	}

	@Override
	public List<? extends AnnotationSignature> getAnnotations() {
		return Collections.emptyList();
	}

	@Override
	public String getDocComment() {
		return null;
	}

	@Override
	public List<? extends TypeParameterSignature> getTypeParameters() {
		return Collections.emptyList();
	}

	@Override
	public TypeSignature getReturnType() {
		return NoTypeSignatureImpl.getVoid();
	}

	@Override
	public List<? extends MethodParameterSignature> getParameters() {
		return Collections.emptyList();
	}

	@Override
	public List<? extends TypeSignature> getThrowingTypes() {
		return Collections.emptyList();
	}

	@Override
	public Value getDefaultValue() {
		return null;
	}

	@Override
	public TypeSignature getReceiverParameter() {
		return null;
	}

	@Override
	public boolean isVarArg() {
		return false;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		ImmutableModifierSet.writeExternalFlag(out, modifierFlags);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		modifierFlags = ImmutableModifierSet.readExternalFlag(in);
	}

	private Object readResolve() {
		switch (modifierFlags) {
			case ImmutableModifierSet.FLAG_NONE:
				return INSTANCE_MODIFIERS_NONE;
			case ImmutableModifierSet.FLAG_PUBLIC:
				return INSTANCE_MODIFIERS_PUBLIC;
			case ImmutableModifierSet.FLAG_PROTECTED:
				return INSTANCE_MODIFIERS_PROTECTED;
			case ImmutableModifierSet.FLAG_PRIVATE:
				return INSTANCE_MODIFIERS_PRIVATE;
			default:
				//if by any chance the modifiers are different, create a new instance
				return this;
		}
	}

	@Override
	public int hashCode() {
		return modifierFlags;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleNoArgConstructor other = (SimpleNoArgConstructor) obj;
		if (modifierFlags != other.modifierFlags)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return JavaUtil.modifiersToStringWithSpace(getModifiers()) + IncrementalElementsTypes.CONSTRUCTOR_METHOD_NAME
				+ "()";
	}

}
