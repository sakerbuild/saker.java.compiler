package saker.java.compiler.impl.compile.signature.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.List;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.element.PackageSignature;

public class PackageSignatureImpl extends AnnotatedSignatureImpl implements PackageSignature {
	private static final long serialVersionUID = 1L;

	private String name;
	private String docComment;

	/**
	 * For {@link Externalizable}.
	 */
	public PackageSignatureImpl() {
	}

	private PackageSignatureImpl(String name, String docComment) {
		super(Collections.emptyList());
		this.name = name;
		this.docComment = docComment;
	}

	private PackageSignatureImpl(List<AnnotationSignature> annotations, String name, String docComment) {
		super(annotations);
		this.name = name;
		this.docComment = docComment;
	}

	public static PackageSignature create(String name) {
		return new SimplePackageSignature(name);
	}

	public static PackageSignature create(String name, String docComment) {
		if (docComment == null) {
			return create(name);
		}
		return new PackageSignatureImpl(name, docComment);
	}

	public static PackageSignature create(List<AnnotationSignature> annotations, String name, String docComment) {
		if (docComment == null && ObjectUtils.isNullOrEmpty(annotations)) {
			return create(name);
		}
		return new PackageSignatureImpl(annotations, name, docComment);
	}

	@Override
	public String getDocComment() {
		return docComment;
	}

	public void setDocComment(String docComment) {
		this.docComment = docComment;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return super.toString() + "package " + name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		PackageSignatureImpl other = (PackageSignatureImpl) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(name);
		out.writeObject(docComment);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		name = (String) in.readObject();
		docComment = (String) in.readObject();
	}

}
