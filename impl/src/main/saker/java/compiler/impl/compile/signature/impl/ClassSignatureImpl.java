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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.compat.KindCompatUtils;
import saker.java.compiler.impl.compile.signature.type.impl.ArrayTypeSignatureImpl;
import saker.java.compiler.impl.compile.signature.type.impl.CanonicalTypeSignatureImpl;
import saker.java.compiler.impl.compile.signature.type.impl.TypeReferenceSignatureImpl;
import saker.java.compiler.impl.compile.signature.type.impl.TypeVariableTypeSignatureImpl;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.element.ClassMemberSignature;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.MethodParameterSignature;
import saker.java.compiler.impl.signature.element.MethodSignature;
import saker.java.compiler.impl.signature.type.ParameterizedTypeSignature;
import saker.java.compiler.impl.signature.type.TypeParameterTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.util.ImmutableModifierSet;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;
import testing.saker.java.compiler.TestFlag;

public final class ClassSignatureImpl extends ExtendedClassSignature {
	private static final long serialVersionUID = 1L;

	private static final List<MethodParameterSignature> ENUM_VALUEOF_PARAMETER_SIGNATURES = ImmutableUtils
			.singletonList(MethodParameterSignatureImpl.create(ImmutableModifierSet.empty(),
					CanonicalTypeSignatureImpl.INSTANCE_JAVA_LANG_STRING, "name"));

	private List<TypeParameterTypeSignature> typeParameters = Collections.emptyList();
	private List<AnnotationSignature> annotations = Collections.emptyList();
	private String docComment;
	private PermittedSubclassesList permittedSubclasses;

	public ClassSignatureImpl() {
	}

	public static ClassSignature create(Set<Modifier> modifiers, String packageName, String name,
			List<? extends ClassMemberSignature> members, ClassSignature enclosingClass,
			TypeSignature superTypeSignature, List<TypeSignature> superInterfaces, ElementKind kind,
			NestingKind nestingKind, List<TypeParameterTypeSignature> typeParameters,
			List<AnnotationSignature> annotations, String docComment) {
		return create(modifiers, packageName, name, members, enclosingClass, superTypeSignature, superInterfaces, kind,
				nestingKind, typeParameters, annotations, docComment, null);
	}

	public static ClassSignature create(Set<Modifier> modifiers, String packageName, String name,
			List<? extends ClassMemberSignature> members, ClassSignature enclosingClass,
			TypeSignature superTypeSignature, List<TypeSignature> superInterfaces, ElementKind kind,
			NestingKind nestingKind, List<TypeParameterTypeSignature> typeParameters,
			List<AnnotationSignature> annotations, String docComment, PermittedSubclassesList permittedsubclasses) {
		if (TestFlag.ENABLED) {
			if ((enclosingClass == null && nestingKind != NestingKind.TOP_LEVEL)
					|| (enclosingClass != null && nestingKind != NestingKind.MEMBER)) {
				throw new AssertionError(enclosingClass + " - " + nestingKind);
			}
		}
		if (kind == ElementKind.ANNOTATION_TYPE) {
			if (enclosingClass == null && docComment == null && ObjectUtils.isNullOrEmpty(annotations)) {
				return new SimpleAnnotationInterfaceSignature(modifiers, packageName, name, members);
			}
			return new ExtendedAnnotationInterfaceSignature(modifiers, packageName, name, members, enclosingClass,
					annotations, docComment);
		}
		if (docComment == null && ObjectUtils.isNullOrEmpty(annotations) && ObjectUtils.isNullOrEmpty(typeParameters)
				&& permittedsubclasses == null) {
			if (superTypeSignature == null && ObjectUtils.isNullOrEmpty(superInterfaces)) {
				return new SimpleClassSignature(modifiers, packageName, name, members, enclosingClass, kind);
			}
			return new ExtendedClassSignature(modifiers, packageName, name, members, enclosingClass, kind,
					superInterfaces, superTypeSignature);
		}
		return new ClassSignatureImpl(modifiers, packageName, name, members, enclosingClass, kind, superInterfaces,
				superTypeSignature, typeParameters, annotations, docComment, permittedsubclasses);
	}

	private ClassSignatureImpl(Set<Modifier> modifiers, String packageName, String name,
			List<? extends ClassMemberSignature> members, ClassSignature enclosingClass, ElementKind kind,
			List<? extends TypeSignature> superInterfaces, TypeSignature superClass,
			List<TypeParameterTypeSignature> typeParameters, List<AnnotationSignature> annotations, String docComment,
			PermittedSubclassesList permittedsubclasses) {
		super(modifiers, packageName, name, members, enclosingClass, kind, superInterfaces, superClass);
		this.typeParameters = typeParameters;
		this.annotations = annotations;
		this.docComment = docComment;
		this.permittedSubclasses = permittedsubclasses;
	}

	@Override
	public String getDocComment() {
		return docComment;
	}

	@Override
	public ParameterizedTypeSignature getTypeSignature() {
		List<TypeSignature> tparams = JavaTaskUtils.cloneImmutableList(typeParameters,
				tp -> TypeVariableTypeSignatureImpl.create(tp.getVarName()));
		return getTypeSignature(tparams);
	}

	@Override
	public ParameterizedTypeSignature getTypeSignature(List<? extends TypeSignature> typeparameters) {
		if (IncrementalElementsTypes.isClassUnrelatedToEnclosing(this)) {
			return CanonicalTypeSignatureImpl.create(getCanonicalName(), typeparameters);
		}
		return TypeReferenceSignatureImpl.create(enclosingClass.getTypeSignature(), getSimpleName(), typeparameters);
	}

	@Override
	public Collection<? extends AnnotationSignature> getAnnotations() {
		return annotations;
	}

	@Override
	public final List<TypeParameterTypeSignature> getTypeParameters() {
		return typeParameters;
	}

	@Override
	public PermittedSubclassesList getPermittedSubclasses() {
		return permittedSubclasses;
	}

	protected static boolean hasAnyConstructor(List<? extends ClassMemberSignature> members) {
		for (ClassMemberSignature mem : members) {
			if (mem.getKindIndex() == KindCompatUtils.ELEMENTKIND_INDEX_CONSTRUCTOR) {
				return true;
			}
		}
		return false;
	}

	public static boolean hasSimpleNoArgMethodWithName(String name, List<? extends ClassMemberSignature> members) {
		for (ClassMemberSignature m : members) {
			if (m.getKindIndex() != KindCompatUtils.ELEMENTKIND_INDEX_METHOD) {
				continue;
			}
			if (!name.equals(m.getSimpleName())) {
				continue;
			}
			MethodSignature ms = (MethodSignature) m;
			if (ms.getParameterCount() != 0) {
				continue;
			}
			return true;
		}
		return false;
	}

	public static void addImplicitMembers(List<ClassMemberSignature> result, ClassSignature thiz) {
		byte kindidx = thiz.getKindIndex();
		if (kindidx == KindCompatUtils.ELEMENTKIND_INDEX_ANNOTATION_TYPE
				|| kindidx == KindCompatUtils.ELEMENTKIND_INDEX_INTERFACE) {
			return;
		}

		//Note: don't add implicit members for records here as we cannot detect duplicate equals(Object) method
		//the implicit members are added in IncrementalTypeElement

		if (!hasAnyConstructor(result)) {
			if (kindidx == KindCompatUtils.ELEMENTKIND_INDEX_RECORD) {
				//don't add here, but in IncrementalTypeElement
			} else {
				Set<Modifier> cmodifiers;
				Set<Modifier> thismodifiers = thiz.getModifiers();
				if (kindidx == KindCompatUtils.ELEMENTKIND_INDEX_ENUM) {
					cmodifiers = IncrementalElementsTypes.MODIFIERS_PRIVATE;
				} else {
					if (thismodifiers.contains(Modifier.PUBLIC)) {
						cmodifiers = IncrementalElementsTypes.MODIFIERS_PUBLIC;
					} else if (thismodifiers.contains(Modifier.PRIVATE)) {
						cmodifiers = IncrementalElementsTypes.MODIFIERS_PRIVATE;
					} else if (thismodifiers.contains(Modifier.PROTECTED)) {
						cmodifiers = IncrementalElementsTypes.MODIFIERS_PROTECTED;
					} else {
						cmodifiers = Collections.emptySet();
					}
				}
				result.add(0, FullMethodSignature.createDefaultConstructor(cmodifiers));
			}
		}

		if (kindidx == KindCompatUtils.ELEMENTKIND_INDEX_ENUM) {
			//we dont need to check if methods exist, as declaring them in an enum results in compilation error
			TypeSignature thistypesig = thiz.getTypeSignature();

			MethodSignature ENUM_VALUEOF_METHOD_SIGNATURE = FullMethodSignature.create("valueOf",
					IncrementalElementsTypes.MODIFIERS_PUBLIC_STATIC, ENUM_VALUEOF_PARAMETER_SIGNATURES,
					Collections.emptyList(), thistypesig, null, ElementKind.METHOD, Collections.emptyList(), null,
					false, null);

			result.add(0, ENUM_VALUEOF_METHOD_SIGNATURE);

			MethodSignature valuessig = FullMethodSignature.create("values",
					IncrementalElementsTypes.MODIFIERS_PUBLIC_STATIC, Collections.emptyList(), Collections.emptyList(),
					ArrayTypeSignatureImpl.create(thistypesig), null, ElementKind.METHOD, Collections.emptyList(), null,
					false, null);
			result.add(0, valuessig);
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);

		SerialUtils.writeExternalCollection(out, annotations);
		SerialUtils.writeExternalCollection(out, typeParameters);
		out.writeObject(permittedSubclasses);
		out.writeObject(docComment);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);

		annotations = SerialUtils.readExternalImmutableList(in);
		typeParameters = SerialUtils.readExternalImmutableList(in);
		permittedSubclasses = (PermittedSubclassesList) in.readObject();
		;
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
		ClassSignatureImpl other = (ClassSignatureImpl) obj;
		if (annotations == null) {
			if (other.annotations != null)
				return false;
		} else if (!annotations.equals(other.annotations))
			return false;
		if (docComment == null) {
			if (other.docComment != null)
				return false;
		} else if (!docComment.equals(other.docComment))
			return false;
		if (permittedSubclasses == null) {
			if (other.permittedSubclasses != null)
				return false;
		} else if (!permittedSubclasses.equals(other.permittedSubclasses))
			return false;
		if (typeParameters == null) {
			if (other.typeParameters != null)
				return false;
		} else if (!typeParameters.equals(other.typeParameters))
			return false;
		return true;
	}

}
