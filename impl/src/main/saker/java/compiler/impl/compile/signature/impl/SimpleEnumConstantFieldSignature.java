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
import java.util.List;
import java.util.Set;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

import saker.java.compiler.impl.compat.ElementKindCompatUtils;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

public class SimpleEnumConstantFieldSignature extends FieldSignatureBase implements Externalizable {
	private static final long serialVersionUID = 1L;

	protected TypeSignature type;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleEnumConstantFieldSignature() {
	}

	public SimpleEnumConstantFieldSignature(TypeSignature type, String name) {
		super(name);
		this.type = type;
	}

	@Override
	public final Set<Modifier> getModifiers() {
		return IncrementalElementsTypes.MODIFIERS_PUBLIC_STATIC_FINAL;
	}

	@Override
	public final ElementKind getKind() {
		return ElementKind.ENUM_CONSTANT;
	}

	@Override
	public final byte getKindIndex() {
		return ElementKindCompatUtils.ELEMENTKIND_INDEX_ENUM_CONSTANT;
	}

	@Override
	public final List<? extends AnnotationSignature> getAnnotations() {
		return type.getAnnotations();
	}

	@Override
	public final TypeSignature getTypeSignature() {
		return type;
	}

	@Override
	public boolean isEnumConstant() {
		return true;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(type);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		type = (TypeSignature) in.readObject();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleEnumConstantFieldSignature other = (SimpleEnumConstantFieldSignature) obj;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

}
