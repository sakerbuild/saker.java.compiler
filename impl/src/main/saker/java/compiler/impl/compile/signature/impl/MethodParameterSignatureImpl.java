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

import javax.lang.model.element.Modifier;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.JavaUtil;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.element.MethodParameterSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.util.ImmutableModifierSet;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

public class MethodParameterSignatureImpl implements MethodParameterSignature, Externalizable {
	private static final long serialVersionUID = 1L;

	protected TypeSignature type;
	protected String name;

	public MethodParameterSignatureImpl() {
	}

	public static MethodParameterSignature create(Set<Modifier> modifiers, TypeSignature type, String name) {
		if (ObjectUtils.isNullOrEmpty(modifiers)) {
			return new MethodParameterSignatureImpl(type, name);
		}
		if (modifiers.equals(IncrementalElementsTypes.MODIFIERS_FINAL)) {
			return new FinalMethodParameterSignatureImpl(type, name);
		}
		//other modifiers are not allowed on parameters. However, as erroneous declarations can be present, handle it
		return new FullMethodParameterSignatureImpl(modifiers, type, name);
	}

	protected MethodParameterSignatureImpl(TypeSignature type, String name) {
		this.type = type;
		this.name = name;
	}

	@Override
	public Set<Modifier> getModifiers() {
		return ImmutableModifierSet.empty();
	}

	@Override
	public List<? extends AnnotationSignature> getAnnotations() {
		return type.getAnnotations();
	}

	@Override
	public final TypeSignature getTypeSignature() {
		return type;
	}

	@Override
	public final String getSimpleName() {
		return name;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(name);
		out.writeObject(type);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		name = in.readUTF();
		type = (TypeSignature) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		MethodParameterSignatureImpl other = (MethodParameterSignatureImpl) obj;
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
