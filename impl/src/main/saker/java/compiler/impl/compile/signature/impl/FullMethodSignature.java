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
import java.util.Set;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.impl.compat.ElementKindCompatUtils;
import saker.java.compiler.impl.compile.signature.type.impl.NoTypeSignatureImpl;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.element.MethodParameterSignature;
import saker.java.compiler.impl.signature.element.MethodSignature;
import saker.java.compiler.impl.signature.type.TypeParameterTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;

public final class FullMethodSignature extends MethodSignatureBase {
	private static final long serialVersionUID = 1L;

	protected byte elementKindIndex;
	protected TypeSignature returnType;
	protected String name;
	protected List<TypeSignature> throwsTypes = Collections.emptyList();

	protected List<TypeParameterTypeSignature> typeParameters = Collections.emptyList();

	protected TypeSignature receiverParameter;
	protected boolean varArg;
	protected String docComment;

	/**
	 * For {@link Externalizable}.
	 */
	public FullMethodSignature() {
	}

	public static MethodSignature create(String name, Set<Modifier> modifiers,
			List<MethodParameterSignature> parameters, List<TypeSignature> throwsTypes, TypeSignature returnType,
			AnnotationSignature.Value defaultValue, ElementKind methodKind,
			List<TypeParameterTypeSignature> typeParameters, TypeSignature receiverParameter, boolean varArg,
			String docComment) {
		if (defaultValue != null) {
			if (docComment == null) {
				return new AnnotationAttributeMethodSignature(returnType, name, defaultValue);
			}
			return new DocumentedAnnotationAttributeMethodSignature(returnType, name, defaultValue, docComment);
		}
		if (!varArg && receiverParameter == null) {
			if (methodKind == ElementKind.CONSTRUCTOR) {
				if (NoTypeSignatureImpl.getVoid().equals(returnType)) {
					//no annotations
					if (docComment != null) {
						return new DocumentedExtendedConstructorMethodSignature(modifiers, parameters, typeParameters,
								throwsTypes, docComment);
					}
					if (ObjectUtils.isNullOrEmpty(typeParameters) && ObjectUtils.isNullOrEmpty(throwsTypes)) {
						if (ObjectUtils.isNullOrEmpty(parameters)) {
							return SimpleNoArgConstructor.create(modifiers);
						}
						return new SimpleConstructorMethodSignature(modifiers, parameters);
					}
					return new ExtendedConstructorMethodSignature(modifiers, parameters, typeParameters, throwsTypes);
				}
				//annotated constructor, create full signature
			} else {
				if (docComment != null) {
					return new DocumentedExtendedMethodSignature(modifiers, parameters, returnType, name,
							typeParameters, throwsTypes, docComment);
				}
				if (ObjectUtils.isNullOrEmpty(typeParameters) && ObjectUtils.isNullOrEmpty(throwsTypes)) {
					return new SimpleMethodSignature(modifiers, parameters, returnType, name);
				}
				return new ExtendedMethodSignature(modifiers, parameters, returnType, name, typeParameters,
						throwsTypes);
			}
		}
		return new FullMethodSignature(modifiers, parameters, returnType, name, typeParameters, throwsTypes, methodKind,
				receiverParameter, varArg, docComment);
	}

	public static MethodSignature createDefaultConstructor(Set<Modifier> modifiers) {
		return SimpleNoArgConstructor.create(modifiers);
	}

	private FullMethodSignature(Set<Modifier> modifiers, List<MethodParameterSignature> parameters,
			TypeSignature returnType, String name, List<TypeParameterTypeSignature> typeParameters,
			List<TypeSignature> throwsTypes, ElementKind methodKind, TypeSignature receiverParameter, boolean varArg,
			String docComment) {
		super(modifiers, parameters);
		this.name = name;
		this.throwsTypes = throwsTypes == null ? Collections.emptyList() : throwsTypes;
		this.returnType = returnType;
		this.elementKindIndex = ElementKindCompatUtils.getElementKindIndex(methodKind);
		this.typeParameters = typeParameters == null ? Collections.emptyList() : typeParameters;
		this.receiverParameter = receiverParameter;
		this.varArg = varArg;
		this.docComment = docComment;
	}

	@Override
	public String getDocComment() {
		return docComment;
	}

	public void setDocComment(String docComment) {
		this.docComment = docComment;
	}

	@Override
	public String getSimpleName() {
		return name;
	}

	@Override
	public Collection<? extends AnnotationSignature> getAnnotations() {
		if (returnType == null) {
			return Collections.emptySet();
		}
		return returnType.getAnnotations();
	}

	@Override
	public List<? extends TypeSignature> getThrowingTypes() {
		return throwsTypes;
	}

	@Override
	public TypeSignature getReturnType() {
		return returnType;
	}

	@Override
	public AnnotationSignature.Value getDefaultValue() {
		return null;
	}

	@Override
	public final ElementKind getKind() {
		return ElementKindCompatUtils.getElementKind(elementKindIndex);
	}

	@Override
	public final byte getKindIndex() {
		return elementKindIndex;
	}

	@Override
	public List<? extends TypeParameterTypeSignature> getTypeParameters() {
		return typeParameters;
	}

	@Override
	public TypeSignature getReceiverParameter() {
		return receiverParameter;
	}

	@Override
	public boolean isVarArg() {
		return varArg;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);

		SerialUtils.writeExternalCollection(out, throwsTypes);
		SerialUtils.writeExternalCollection(out, typeParameters);

		out.writeUTF(name);
		out.writeObject(returnType);

		out.writeByte(elementKindIndex);

		out.writeObject(receiverParameter);

		out.writeBoolean(varArg);
		out.writeObject(docComment);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);

		throwsTypes = SerialUtils.readExternalImmutableList(in);
		typeParameters = SerialUtils.readExternalImmutableList(in);

		name = in.readUTF();
		returnType = (TypeSignature) in.readObject();

		elementKindIndex = in.readByte();

		receiverParameter = (TypeSignature) in.readObject();

		varArg = in.readBoolean();
		docComment = (String) in.readObject();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		FullMethodSignature other = (FullMethodSignature) obj;
		if (docComment == null) {
			if (other.docComment != null)
				return false;
		} else if (!docComment.equals(other.docComment))
			return false;
		if (elementKindIndex != other.elementKindIndex)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (receiverParameter == null) {
			if (other.receiverParameter != null)
				return false;
		} else if (!receiverParameter.equals(other.receiverParameter))
			return false;
		if (returnType == null) {
			if (other.returnType != null)
				return false;
		} else if (!returnType.equals(other.returnType))
			return false;
		if (throwsTypes == null) {
			if (other.throwsTypes != null)
				return false;
		} else if (!throwsTypes.equals(other.throwsTypes))
			return false;
		if (typeParameters == null) {
			if (other.typeParameters != null)
				return false;
		} else if (!typeParameters.equals(other.typeParameters))
			return false;
		if (varArg != other.varArg)
			return false;
		return true;
	}

	@Override
	public String toString() {
		//XXX some annotations are added twice
		//once for the method and once for the return type
		Set<Modifier> modifiers = getModifiers();
		return (getAnnotations().isEmpty() ? ""
				: String.join(" ", StringUtils.asStringIterable(getAnnotations())) + " ")
				+ (modifiers.isEmpty() ? "" : String.join(" ", StringUtils.asStringIterable(modifiers)) + " ")
				+ (returnType == null ? "" : returnType + " ")
				+ (ObjectUtils.isNullOrEmpty(typeParameters) ? ""
						: "<" + String.join(", ", StringUtils.asStringIterable(typeParameters)) + "> ")
				+ name + "("
				+ (ObjectUtils.isNullOrEmpty(parameters) ? ""
						: String.join(", ", StringUtils.asStringIterable(parameters)))
				+ ")" + (ObjectUtils.isNullOrEmpty(throwsTypes) ? ""
						: " throws " + StringUtils.toStringJoin(", ", throwsTypes));
	}
}