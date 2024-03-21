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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.lang.model.element.Modifier;

import saker.build.thirdparty.saker.util.StringUtils;
import saker.java.compiler.impl.JavaUtil;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.element.AnnotationSignature.Value;
import saker.java.compiler.impl.signature.element.MethodParameterSignature;
import saker.java.compiler.impl.signature.element.MethodSignature;
import saker.java.compiler.impl.signature.type.TypeParameterSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.util.ImmutableModifierSet;
import saker.java.compiler.impl.util.JavaSerialUtils;

public abstract class MethodSignatureBase implements MethodSignature, Externalizable {
	private static final long serialVersionUID = 1L;

	protected short modifierFlags;
	protected List<? extends MethodParameterSignature> parameters;
	//Note: subclasses may have their own serialization functions, 
	//      so take care when adding new fields

	/**
	 * For {@link Externalizable}.
	 */
	public MethodSignatureBase() {
	}

	protected MethodSignatureBase(Set<Modifier> modifiers, List<? extends MethodParameterSignature> parameters) {
		this(ImmutableModifierSet.getFlag(modifiers), parameters == null ? Collections.emptyList() : parameters);
	}

	protected MethodSignatureBase(short modifierFlags, List<? extends MethodParameterSignature> parameters) {
		this.modifierFlags = modifierFlags;
		this.parameters = parameters;
	}

	@Override
	public final Set<Modifier> getModifiers() {
		return ImmutableModifierSet.forFlags(modifierFlags);
	}

	@Override
	public final List<? extends MethodParameterSignature> getParameters() {
		return parameters;
	}

	@Override
	public List<? extends AnnotationSignature> getAnnotations() {
		return getReturnType().getAnnotations();
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
		JavaSerialUtils.writeOpenEndedMethodParameterList(parameters, out);
		ImmutableModifierSet.writeExternalObjectFlag(out, modifierFlags);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		ArrayList<MethodParameterSignature> parameters = new ArrayList<>();
		this.parameters = parameters;
		this.modifierFlags = ImmutableModifierSet
				.fromExternalObjectFlag(JavaSerialUtils.readOpenEndedMethodParameterList(parameters, in));
	}

	@Override
	public final int hashCode() {
		return Objects.hashCode(getSimpleName()) * 31 + Objects.hashCode(getParameters());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MethodSignatureBase other = (MethodSignatureBase) obj;
		if (modifierFlags != other.modifierFlags)
			return false;
		if (parameters == null) {
			if (other.parameters != null)
				return false;
		} else if (!parameters.equals(other.parameters))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return JavaUtil.modifiersToStringWithSpace(getModifiers()) + getReturnType() + " " + getSimpleName() + "("
				+ StringUtils.toStringJoin(", ", getParameters()) + ")";
	}

}
