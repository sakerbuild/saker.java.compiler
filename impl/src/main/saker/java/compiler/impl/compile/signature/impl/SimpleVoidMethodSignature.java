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

import saker.java.compiler.impl.compat.ElementKindCompatUtils;
import saker.java.compiler.impl.compile.signature.type.impl.NoTypeSignatureImpl;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.element.MethodParameterSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;

public class SimpleVoidMethodSignature extends MethodSignatureBase {
	private static final long serialVersionUID = 1L;

	protected String name;
	//Note: subclasses may have their own serialization functions, 
	//      so take care when adding new fields

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleVoidMethodSignature() {
	}

	@Override
	public TypeSignature getReturnType() {
		return NoTypeSignatureImpl.getVoid();
	}

	public SimpleVoidMethodSignature(Set<Modifier> modifiers, List<? extends MethodParameterSignature> parameters,
			String name) {
		super(modifiers, parameters);
		this.name = name;
	}

	@Override
	public String getSimpleName() {
		return name;
	}

	@Override
	public ElementKind getKind() {
		return ElementKind.METHOD;
	}

	@Override
	public final byte getKindIndex() {
		return ElementKindCompatUtils.ELEMENTKIND_INDEX_METHOD;
	}

	@Override
	public List<? extends AnnotationSignature> getAnnotations() {
		return Collections.emptyList();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);

		out.writeObject(name);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);

		this.name = (String) in.readObject();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleVoidMethodSignature other = (SimpleVoidMethodSignature) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
}
