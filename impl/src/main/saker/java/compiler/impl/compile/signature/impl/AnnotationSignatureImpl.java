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
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;

public class AnnotationSignatureImpl implements Externalizable, AnnotationSignature {
	private static final long serialVersionUID = 1L;

	private TypeSignature annotationType;
	private Map<String, Value> values;

	/**
	 * For {@link Externalizable}.
	 */
	public AnnotationSignatureImpl() {
	}

	public static AnnotationSignature create(TypeSignature annotationType) {
		return SimpleAnnotationSignature.create(annotationType);
	}

	public static AnnotationSignature create(TypeSignature annotationType, Map<String, Value> values) {
		if (ObjectUtils.isNullOrEmpty(values)) {
			return SimpleAnnotationSignature.create(annotationType);
		}
		return new AnnotationSignatureImpl(annotationType, values);
	}

	private AnnotationSignatureImpl(TypeSignature annotationType, Map<String, Value> values) {
		this.annotationType = annotationType;
		this.values = values;
	}

	@Override
	public TypeSignature getAnnotationType() {
		return annotationType;
	}

	@Override
	public Map<String, Value> getValues() {
		return values;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("@");
		sb.append(annotationType);
		sb.append("(");
		for (Iterator<Entry<String, Value>> it = values.entrySet().iterator(); it.hasNext();) {
			Entry<String, Value> entry = it.next();
			sb.append(entry.getKey());
			sb.append(" = ");
			sb.append(entry.getValue());
			if (it.hasNext()) {
				sb.append(", ");
			}
		}
		sb.append(")");
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((annotationType == null) ? 0 : annotationType.hashCode());
		result = prime * result + ((values == null) ? 0 : values.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AnnotationSignatureImpl other = (AnnotationSignatureImpl) obj;
		if (annotationType == null) {
			if (other.annotationType != null)
				return false;
		} else if (!annotationType.equals(other.annotationType))
			return false;
		if (values == null) {
			if (other.values != null)
				return false;
		} else if (!values.equals(other.values))
			return false;
		return true;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerialUtils.writeExternalMap(out, values);

		out.writeObject(annotationType);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		values = SerialUtils.readExternalImmutableLinkedHashMap(in);

		annotationType = (TypeSignature) in.readObject();
	}

}
