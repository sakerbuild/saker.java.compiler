package saker.java.compiler.impl.compile.signature.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.java.compiler.impl.signature.element.AnnotationSignature.Value;
import saker.java.compiler.impl.signature.type.TypeSignature;

public class DocumentedAnnotationAttributeMethodSignature extends AnnotationAttributeMethodSignature {
	private static final long serialVersionUID = 1L;

	protected String docComment;

	/**
	 * For {@link Externalizable}.
	 */
	public DocumentedAnnotationAttributeMethodSignature() {
	}

	public DocumentedAnnotationAttributeMethodSignature(TypeSignature returnType, String name, Value defaultValue,
			String docComment) {
		super(returnType, name, defaultValue);
		this.docComment = docComment;
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
	public String getDocComment() {
		return docComment;
	}
}
