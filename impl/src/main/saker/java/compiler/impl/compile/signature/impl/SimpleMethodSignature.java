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

import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.element.MethodParameterSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;

public class SimpleMethodSignature extends SimpleVoidMethodSignature {
	private static final long serialVersionUID = 1L;

	protected TypeSignature returnType;
	//Note: subclasses may have their own serialization functions, 
	//      so take care when adding new fields

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleMethodSignature() {
	}

	@Override
	public TypeSignature getReturnType() {
		return returnType;
	}

	public SimpleMethodSignature(Set<Modifier> modifiers, List<? extends MethodParameterSignature> parameters,
			TypeSignature returnType, String name) {
		super(modifiers, parameters, name);
		this.returnType = returnType;
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
	public List<? extends AnnotationSignature> getAnnotations() {
		return returnType.getAnnotations();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);

		out.writeObject(returnType);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);

		this.returnType = (TypeSignature) in.readObject();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleMethodSignature other = (SimpleMethodSignature) obj;
		if (returnType == null) {
			if (other.returnType != null)
				return false;
		} else if (!returnType.equals(other.returnType))
			return false;
		return true;
	}
}
