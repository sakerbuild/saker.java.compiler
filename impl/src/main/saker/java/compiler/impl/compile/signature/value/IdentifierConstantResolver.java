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
import java.util.Objects;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

import saker.build.util.data.annotation.ValueType;
import saker.java.compiler.api.processing.SakerElementsTypes;
import saker.java.compiler.impl.compile.signature.type.impl.CanonicalTypeSignatureImpl;
import saker.java.compiler.impl.compile.signature.type.impl.UnknownTypeSignatureImpl;
import saker.java.compiler.impl.signature.type.ResolutionScope;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.signature.value.ConstantValueResolver;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

@ValueType
public class IdentifierConstantResolver implements EnumOrConstantValueResolver, Externalizable {
	private static final long serialVersionUID = 1L;

	private String identifier;

	/**
	 * For {@link Externalizable}.
	 */
	public IdentifierConstantResolver() {
	}

	public IdentifierConstantResolver(String identifier) {
		this.identifier = identifier;
	}

	@Override
	public Object resolve(SakerElementsTypes elemtypes, Element resolutionelement) {
		ResolutionScope scope = ((IncrementalElementsTypes) elemtypes).createResolutionScope(resolutionelement);
		VariableElement resolved = scope.resolveVariable(elemtypes, identifier);
		if (resolved != null) {
			return resolved.getConstantValue();
		}
		return null;
	}

	@Override
	public TypeSignature getType(IncrementalElementsTypes elemtypes, Element resolutionelement) {
		ResolutionScope scope = elemtypes.createResolutionScope(resolutionelement);
		VariableElement variable = scope.resolveVariable(elemtypes, identifier);
		if (variable != null) {
			Element enclosing = variable.getEnclosingElement();
			if (enclosing instanceof TypeElement) {
				return CanonicalTypeSignatureImpl.create(((TypeElement) enclosing).getQualifiedName().toString());
			}
		}
		return UnknownTypeSignatureImpl.create(identifier);
	}

	@Override
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(identifier);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		identifier = in.readUTF();
	}

	@Override
	public String toString() {
		return identifier;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(identifier);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		IdentifierConstantResolver other = (IdentifierConstantResolver) obj;
		if (identifier == null) {
			if (other.identifier != null)
				return false;
		} else if (!identifier.equals(other.identifier))
			return false;
		return true;
	}

	@Override
	public boolean signatureEquals(ConstantValueResolver other) {
		if (!(other instanceof IdentifierConstantResolver)) {
			return false;
		}
		IdentifierConstantResolver o = (IdentifierConstantResolver) other;
		if (!identifier.equals(o.identifier)) {
			return false;
		}
		return true;
	}

}
