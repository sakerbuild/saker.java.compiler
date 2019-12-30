package saker.java.compiler.impl.compile.signature.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Modifier;

import saker.java.compiler.impl.signature.element.MethodParameterSignature;
import saker.java.compiler.impl.signature.type.TypeParameterTypeSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;

public class DocumentedExtendedMethodSignature extends ExtendedMethodSignature {
	private static final long serialVersionUID = 1L;

	protected String docComment;

	/**
	 * For {@link Externalizable}.
	 */
	public DocumentedExtendedMethodSignature() {
	}

	public DocumentedExtendedMethodSignature(Set<Modifier> modifiers,
			List<? extends MethodParameterSignature> parameters, TypeSignature returnType, String name,
			List<? extends TypeParameterTypeSignature> typeParameters, List<? extends TypeSignature> throwsTypes,
			String docComment) {
		super(modifiers, parameters, returnType, name, typeParameters, throwsTypes);
		this.docComment = docComment;
	}

	@Override
	public String getDocComment() {
		return docComment;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeUTF(docComment);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		docComment = in.readUTF();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		DocumentedExtendedMethodSignature other = (DocumentedExtendedMethodSignature) obj;
		if (docComment == null) {
			if (other.docComment != null)
				return false;
		} else if (!docComment.equals(other.docComment))
			return false;
		return true;
	}
}
