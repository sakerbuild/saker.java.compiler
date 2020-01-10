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
import java.util.Collection;
import java.util.Set;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

import saker.java.compiler.impl.JavaUtil;
import saker.java.compiler.impl.compat.KindCompatUtils;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.element.FieldSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.signature.value.ConstantValueResolver;
import saker.java.compiler.impl.util.ImmutableModifierSet;

public class SimpleFieldSignature implements FieldSignature, Externalizable {
	private static final long serialVersionUID = 1L;

	protected short modifierFlags;
	protected TypeSignature type;
	protected String name;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleFieldSignature() {
	}

	public SimpleFieldSignature(Set<Modifier> modifiers, TypeSignature type, String name) {
		this.modifierFlags = ImmutableModifierSet.getFlag(modifiers);
		this.name = name;
		this.type = type;
	}

	@Override
	public final String getSimpleName() {
		return name;
	}

	@Override
	public final Set<Modifier> getModifiers() {
		return ImmutableModifierSet.forFlags(modifierFlags);
	}

	@Override
	public final ElementKind getKind() {
		return ElementKind.FIELD;
	}

	@Override
	public final byte getKindIndex() {
		return KindCompatUtils.ELEMENTKIND_INDEX_FIELD;
	}

	@Override
	public final Collection<? extends AnnotationSignature> getAnnotations() {
		return type.getAnnotations();
	}

	@Override
	public String getDocComment() {
		return null;
	}

	@Override
	public final TypeSignature getTypeSignature() {
		return type;
	}

	@Override
	public ConstantValueResolver getConstantValue() {
		return null;
	}

	@Override
	public boolean isEnumConstant() {
		return false;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		ImmutableModifierSet.writeExternalFlag(out, modifierFlags);
		out.writeObject(type);
		out.writeUTF(name);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		modifierFlags = ImmutableModifierSet.readExternalFlag(in);
		type = (TypeSignature) in.readObject();
		name = in.readUTF();
	}

	@Override
	public final int hashCode() {
		return getSimpleName().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleFieldSignature other = (SimpleFieldSignature) obj;
		if (modifierFlags != other.modifierFlags)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return JavaUtil.modifiersToStringWithSpace(getModifiers()) + type + " " + name;
	}
}
