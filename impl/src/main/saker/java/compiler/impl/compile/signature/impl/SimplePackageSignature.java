package saker.java.compiler.impl.compile.signature.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Collections;

import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.element.PackageSignature;

public class SimplePackageSignature implements PackageSignature, Externalizable {
	private static final long serialVersionUID = 1L;

	private String name;

	/**
	 * For {@link Externalizable}.
	 */
	public SimplePackageSignature() {
	}

	public SimplePackageSignature(String name) {
		this.name = name;
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
	public String getName() {
		return name;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(name);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		name = in.readUTF();
	}

	@Override
	public String toString() {
		return "package " + name;
	}
}
