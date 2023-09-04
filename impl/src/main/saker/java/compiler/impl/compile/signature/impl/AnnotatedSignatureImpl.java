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
import java.util.Collections;
import java.util.List;

import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.impl.JavaUtil;
import saker.java.compiler.impl.signature.element.AnnotatedSignature;
import saker.java.compiler.impl.signature.element.AnnotationSignature;

public abstract class AnnotatedSignatureImpl implements AnnotatedSignature, Externalizable {
	private static final long serialVersionUID = 1L;

	protected List<? extends AnnotationSignature> annotations;

	/**
	 * For {@link Externalizable}.
	 */
	public AnnotatedSignatureImpl() {
	}

	protected AnnotatedSignatureImpl(List<? extends AnnotationSignature> annotations) {
		this.annotations = annotations == null ? Collections.emptyList() : annotations;
	}

	@Override
	public final List<? extends AnnotationSignature> getAnnotations() {
		return annotations;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerialUtils.writeExternalCollection(out, annotations);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		annotations = SerialUtils.readExternalImmutableList(in);
	}

	@Override
	public int hashCode() {
		return annotations.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AnnotatedSignatureImpl other = (AnnotatedSignatureImpl) obj;
		if (!annotations.equals(other.annotations))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return JavaUtil.annotationsToStringWithSpace(annotations);
	}

}
