package saker.java.compiler.impl.compile.signature.type.impl;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.compile.signature.impl.AnnotatedSignatureImpl;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignatureVisitor;
import saker.java.compiler.impl.signature.type.TypeVariableTypeSignature;

public class TypeVariableTypeSignatureImpl extends AnnotatedSignatureImpl implements TypeVariableTypeSignature {
	private static final long serialVersionUID = 1L;

	private String variableName;

	public TypeVariableTypeSignatureImpl() {
	}

	public static TypeVariableTypeSignature create(String variableName) {
		return new SimpleTypeVariableTypeSignature(variableName);
	}

	public static TypeVariableTypeSignature create(List<AnnotationSignature> annotations, String variableName) {
		if (ObjectUtils.isNullOrEmpty(annotations)) {
			return create(variableName);
		}
		return new TypeVariableTypeSignatureImpl(annotations, variableName);
	}

	private TypeVariableTypeSignatureImpl(List<AnnotationSignature> annotations, String variableName) {
		super(annotations);
		this.variableName = variableName;
	}

	@Override
	public String getVariableName() {
		return variableName;
	}

	@Override
	public String getSimpleName() {
		return variableName;
	}

	@Override
	public <R, P> R accept(TypeSignatureVisitor<R, P> v, P p) {
		return v.visitTypeVariable(this, p);
	}

	@Override
	public TypeSignature getEnclosingSignature() {
		return null;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeUTF(variableName);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		variableName = in.readUTF();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((variableName == null) ? 0 : variableName.hashCode());
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
		TypeVariableTypeSignatureImpl other = (TypeVariableTypeSignatureImpl) obj;
		if (variableName == null) {
			if (other.variableName != null)
				return false;
		} else if (!variableName.equals(other.variableName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return super.toString() + variableName;
	}

}
