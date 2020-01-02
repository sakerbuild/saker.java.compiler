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
import javax.lang.model.element.NestingKind;

import saker.java.compiler.impl.signature.element.ClassMemberSignature;
import saker.java.compiler.impl.signature.element.ClassSignature;

public class SimpleClassSignature extends ClassSignatureBase {
	private static final long serialVersionUID = 1L;

	protected transient ClassSignature enclosingClass;
	protected ElementKind kind;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleClassSignature() {
	}

	public SimpleClassSignature(Set<Modifier> modifiers, String packageName, String name,
			List<? extends ClassMemberSignature> members, ClassSignature enclosingClass, ElementKind kind) {
		super(modifiers, packageName, name, members);
		this.enclosingClass = enclosingClass;
		this.kind = kind;
	}

	@Override
	public final NestingKind getNestingKind() {
		return enclosingClass == null ? NestingKind.TOP_LEVEL : NestingKind.MEMBER;
	}

	@Override
	public final ClassSignature getEnclosingSignature() {
		return enclosingClass;
	}

	@Override
	public final ElementKind getKind() {
		return kind;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(enclosingClass);
		out.writeObject(kind);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		enclosingClass = (ClassSignature) in.readObject();
		kind = (ElementKind) in.readObject();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleClassSignature other = (SimpleClassSignature) obj;
		if (kind != other.kind)
			return false;
		return true;
	}

}
