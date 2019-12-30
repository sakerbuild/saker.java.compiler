package saker.java.compiler.impl.compile.signature.type.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.List;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.impl.compile.signature.impl.AnnotatedSignatureImpl;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.type.ParameterizedTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;

public class TypeReferenceSignatureImpl extends AnnotatedSignatureImpl implements ParameterizedTypeSignature {
	private static final long serialVersionUID = 1L;

	private ParameterizedTypeSignature enclosingSignature;
	private String simpleName;
	private List<? extends TypeSignature> typeParameters = Collections.emptyList();

	/**
	 * For {@link Externalizable}.
	 */
	public TypeReferenceSignatureImpl() {
	}

	public static ParameterizedTypeSignature create(ParameterizedTypeSignature enclosingSignature, String simpleName) {
		if (enclosingSignature == null) {
			return CanonicalTypeSignatureImpl.create(simpleName);
		}
		return new SimpleTypeReferenceSignature(enclosingSignature, simpleName);
	}

	public static ParameterizedTypeSignature create(ParameterizedTypeSignature enclosingSignature, String simpleName,
			List<? extends TypeSignature> typeParameters) {
		if (ObjectUtils.isNullOrEmpty(typeParameters)) {
			return create(enclosingSignature, simpleName);
		}
		if (enclosingSignature == null) {
			return CanonicalTypeSignatureImpl.create(simpleName, typeParameters);
		}
		return new SimpleParameterizedTypeReferenceSignature(enclosingSignature, simpleName, typeParameters);
	}

	public static ParameterizedTypeSignature create(List<? extends AnnotationSignature> annotations,
			ParameterizedTypeSignature enclosingSignature, String simpleName, List<? extends TypeSignature> typeParameters) {
		if (ObjectUtils.isNullOrEmpty(annotations)) {
			return create(enclosingSignature, simpleName, typeParameters);
		}
		return new TypeReferenceSignatureImpl(annotations, enclosingSignature, simpleName, typeParameters);
	}

	public static ParameterizedTypeSignature create(List<? extends AnnotationSignature> annotations,
			ParameterizedTypeSignature enclosingSignature, String simpleName) {
		if (ObjectUtils.isNullOrEmpty(annotations)) {
			return create(enclosingSignature, simpleName);
		}
		return new TypeReferenceSignatureImpl(annotations, enclosingSignature, simpleName, Collections.emptyList());
	}

	private TypeReferenceSignatureImpl(List<? extends AnnotationSignature> annotations,
			ParameterizedTypeSignature enclosingSignature, String simpleName,
			List<? extends TypeSignature> typeParameters) {
		super(annotations);
		this.enclosingSignature = enclosingSignature;
		this.simpleName = simpleName;
		this.typeParameters = typeParameters;
	}

	@Override
	public ParameterizedTypeSignature getEnclosingSignature() {
		return enclosingSignature;
	}

	@Override
	public List<? extends TypeSignature> getTypeParameters() {
		return typeParameters;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);

		out.writeObject(enclosingSignature);
		out.writeUTF(simpleName);
		SerialUtils.writeExternalCollection(out, typeParameters);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);

		enclosingSignature = (ParameterizedTypeSignature) in.readObject();
		simpleName = in.readUTF();
		typeParameters = SerialUtils.readExternalImmutableList(in);
	}

	@Override
	public String getSimpleName() {
		return simpleName;
	}

	@Override
	public String toString() {
		return super.toString()
				+ (getEnclosingSignature() == null ? getCanonicalName()
						: getEnclosingSignature().toString() + "." + getSimpleName())
				+ (ObjectUtils.isNullOrEmpty(typeParameters) ? ""
						: StringUtils.toStringJoin("<", ", ", typeParameters, ">"));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((enclosingSignature == null) ? 0 : enclosingSignature.hashCode());
		result = prime * result + ((simpleName == null) ? 0 : simpleName.hashCode());
		result = prime * result + ((typeParameters == null) ? 0 : typeParameters.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TypeReferenceSignatureImpl other = (TypeReferenceSignatureImpl) obj;
		if (enclosingSignature == null) {
			if (other.enclosingSignature != null)
				return false;
		} else if (!enclosingSignature.equals(other.enclosingSignature))
			return false;
		if (simpleName == null) {
			if (other.simpleName != null)
				return false;
		} else if (!simpleName.equals(other.simpleName))
			return false;
		if (typeParameters == null) {
			if (other.typeParameters != null)
				return false;
		} else if (!typeParameters.equals(other.typeParameters))
			return false;
		return true;
	}

}
