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
package saker.java.compiler.impl.compile.signature.value;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import saker.java.compiler.api.processing.SakerElementsTypes;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.signature.value.ConstantValueResolver;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

public class VariableConstantMemberResolver implements EnumOrConstantValueResolver, Externalizable {
	private static final long serialVersionUID = 1L;

	private TypeSignature type;
	private String variableName;

	public VariableConstantMemberResolver() {
	}

	public VariableConstantMemberResolver(TypeSignature type, String variableName) {
		this.type = type;
		this.variableName = variableName;
	}

	@Override
	public Object resolve(SakerElementsTypes elements, Element resolutionelement) {
		IncrementalElementsTypes iet = (IncrementalElementsTypes) elements;
		TypeElement elem = iet.getTypeElement(type, resolutionelement);
		if (elem == null) {
			return null;
		}
		for (Element e : elem.getEnclosedElements()) {
			if (e.getKind() == ElementKind.FIELD && e.getSimpleName().contentEquals(variableName)) {
				return ((VariableElement) e).getConstantValue();
			}
		}
		return null;
	}

	@Override
	public String getIdentifier() {
		return variableName;
	}

	@Override
	public TypeSignature getType(IncrementalElementsTypes elemtypes, Element resolutionelement) {
		return type;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(type);
		out.writeUTF(variableName);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		type = (TypeSignature) in.readObject();
		variableName = in.readUTF();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((variableName == null) ? 0 : variableName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VariableConstantMemberResolver other = (VariableConstantMemberResolver) obj;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (variableName == null) {
			if (other.variableName != null)
				return false;
		} else if (!variableName.equals(other.variableName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return type + "." + variableName;
	}

	@Override
	public boolean signatureEquals(ConstantValueResolver other) {
		if (!(other instanceof VariableConstantMemberResolver)) {
			return false;
		}
		VariableConstantMemberResolver o = (VariableConstantMemberResolver) other;
		if (!variableName.equals(o.variableName)) {
			return false;
		}
		if (!type.signatureEquals(o.type)) {
			return false;
		}
		return true;
	}

}
