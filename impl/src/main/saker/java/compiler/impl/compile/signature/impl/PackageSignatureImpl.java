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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.element.PackageSignature;
import saker.java.compiler.impl.util.JavaSerialUtils;

public final class PackageSignatureImpl extends AnnotatedSignatureImpl implements PackageSignature {
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
		JavaSerialUtils.writeOpenEndedList(annotations, out);
		out.writeObject(name);
		out.writeObject(docComment);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		ArrayList<AnnotationSignature> annotations = new ArrayList<>();
		this.annotations = annotations;
		this.name = (String) JavaSerialUtils.readOpenEndedList(AnnotationSignature.class, annotations, in);
		this.docComment = (String) in.readObject();
	}

}
