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
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.lang.model.element.Modifier;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.JavaUtil;
import saker.java.compiler.impl.compat.KindCompatUtils;
import saker.java.compiler.impl.compile.signature.type.impl.CanonicalTypeSignatureImpl;
import saker.java.compiler.impl.compile.signature.type.impl.TypeReferenceSignatureImpl;
import saker.java.compiler.impl.compile.signature.type.impl.TypeVariableTypeSignatureImpl;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.element.ClassMemberSignature;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.type.ParameterizedTypeSignature;
import saker.java.compiler.impl.signature.type.TypeParameterTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.util.ImmutableModifierSet;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

public abstract class ClassSignatureBase implements ClassSignature, Externalizable {
	private static final long serialVersionUID = 1L;

	protected String packageName;
	protected String name;
	protected short modifierFlags;
	protected List<? extends ClassMemberSignature> members;

	/**
	 * For {@link Externalizable}.
	 */
	public ClassSignatureBase() {
	}

	public ClassSignatureBase(Set<Modifier> modifiers, String packageName, String name,
			List<? extends ClassMemberSignature> members) {
		this.modifierFlags = ImmutableModifierSet.getFlag(modifiers);
		this.packageName = packageName;
		this.name = name;
		this.members = members;
	}

	@Override
	public ParameterizedTypeSignature getTypeSignature() {
		List<? extends TypeParameterTypeSignature> typeParameters = getTypeParameters();
		List<TypeSignature> tparams = JavaTaskUtils.cloneImmutableList(typeParameters,
				tp -> TypeVariableTypeSignatureImpl.create(tp.getVarName()));
		return getTypeSignature(tparams);
	}

	@Override
	public ParameterizedTypeSignature getTypeSignature(List<? extends TypeSignature> typeparameters) {
		if (IncrementalElementsTypes.isClassUnrelatedToEnclosing(this)) {
			return CanonicalTypeSignatureImpl.create(getCanonicalName(), typeparameters);
		}
		return TypeReferenceSignatureImpl.create(getEnclosingSignature().getTypeSignature(), getSimpleName(),
				typeparameters);
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
	public final List<? extends ClassMemberSignature> getMembers() {
		return members;
	}

	@Override
	public final String getPackageName() {
		return packageName;
	}

	@Override
	public Collection<? extends AnnotationSignature> getAnnotations() {
		return Collections.emptyList();
	}

	@Override
	public String getDocComment() {
		return null;
	}

	@Override
	public List<? extends TypeParameterTypeSignature> getTypeParameters() {
		return Collections.emptyList();
	}

	@Override
	public TypeSignature getSuperClass() {
		return null;
	}

	@Override
	public List<? extends TypeSignature> getSuperInterfaces() {
		return Collections.emptyList();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(packageName);
		out.writeUTF(name);
		ImmutableModifierSet.writeExternalFlag(out, modifierFlags);
		SerialUtils.writeExternalCollection(out, members);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		packageName = (String) in.readObject();
		name = in.readUTF();
		modifierFlags = ImmutableModifierSet.readExternalFlag(in);
		members = SerialUtils.readExternalImmutableList(in);
	}

	@Override
	public final int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((packageName == null) ? 0 : packageName.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		ClassSignatureBase other = (ClassSignatureBase) obj;
		if (members == null) {
			if (other.members != null)
				return false;
		} else if (!members.equals(other.members))
			return false;
		if (modifierFlags != other.modifierFlags)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (packageName == null) {
			if (other.packageName != null)
				return false;
		} else if (!packageName.equals(other.packageName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		Collection<? extends AnnotationSignature> annotations = getAnnotations();
		if (!ObjectUtils.isNullOrEmpty(annotations)) {
			sb.append(StringUtils.toStringJoin(" ", annotations));
			sb.append(' ');
		}
		sb.append(JavaUtil.modifiersToStringWithSpace(getModifiers()));
		sb.append(KindCompatUtils.getElementKindName(getKindIndex()).toLowerCase(Locale.ENGLISH));
		sb.append(' ');
		sb.append(getBinaryName());
		return sb.toString();
	}
}
