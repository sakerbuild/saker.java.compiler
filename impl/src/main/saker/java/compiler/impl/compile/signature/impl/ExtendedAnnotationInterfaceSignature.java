package saker.java.compiler.impl.compile.signature.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.NestingKind;

import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.element.ClassMemberSignature;
import saker.java.compiler.impl.signature.element.ClassSignature;

public class ExtendedAnnotationInterfaceSignature extends SimpleAnnotationInterfaceSignature {
	private static final long serialVersionUID = 1L;

	private transient ClassSignature enclosingClass;

	private List<AnnotationSignature> annotations = Collections.emptyList();
	private String docComment;

	/**
	 * For {@link Externalizable}.
	 */
	public ExtendedAnnotationInterfaceSignature() {
	}

	public ExtendedAnnotationInterfaceSignature(Set<Modifier> modifiers, String packageName, String name,
			List<? extends ClassMemberSignature> members, ClassSignature enclosingClass,
			List<AnnotationSignature> annotations, String docComment) {
		super(modifiers, packageName, name, members);
		this.enclosingClass = enclosingClass;
		this.annotations = annotations;
		this.docComment = docComment;
	}

	@Override
	public final NestingKind getNestingKind() {
		return enclosingClass == null ? NestingKind.TOP_LEVEL : NestingKind.MEMBER;
	}

	@Override
	public ClassSignature getEnclosingSignature() {
		return enclosingClass;
	}

	@Override
	public String getDocComment() {
		return docComment;
	}

	@Override
	public Collection<? extends AnnotationSignature> getAnnotations() {
		return annotations;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(enclosingClass);
		SerialUtils.writeExternalCollection(out, annotations);
		out.writeObject(docComment);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		enclosingClass = (ClassSignature) in.readObject();
		annotations = SerialUtils.readExternalImmutableList(in);
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
		ExtendedAnnotationInterfaceSignature other = (ExtendedAnnotationInterfaceSignature) obj;
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
		return true;
	}

}
