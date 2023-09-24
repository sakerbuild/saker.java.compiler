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
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;

public final class AnnotationSignatureImpl implements Externalizable, AnnotationSignature {
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
		if (values.size() == 1) {
			Entry<String, Value> entry = values.entrySet().iterator().next();
			String name = entry.getKey();
			Value value = entry.getValue();
			if ("value".equals(name)) {
				return new ValueAttributeSimpleAnnotationSignature(annotationType, value);
			}
			return new SingleAttributeAnnotationSignature(annotationType, name, value);
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
		Iterator<Entry<String, Value>> it = values.entrySet().iterator();
		if (it.hasNext()) {
			sb.append("(");
			while (true) {
				Entry<String, Value> entry = it.next();
				sb.append(entry.getKey());
				sb.append(" = ");
				sb.append(entry.getValue());
				if (it.hasNext()) {
					sb.append(", ");
				} else {
					break;
				}
			}
			sb.append(")");
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(annotationType) * 31 + Objects.hashCode(values);
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
		//the map iterator order should be the same, because annotation processors may (althought shouldnt) rely on that
		if (!ObjectUtils.mapOrderedEquals(values, other.values))
			return false;
		return true;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		if (values != null) {
			for (Entry<String, Value> entry : values.entrySet()) {
				out.writeObject(entry.getKey());
				out.writeObject(entry.getValue());
			}
		}

		out.writeObject(annotationType);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		this.values = new LinkedHashMap<>();
		while (true) {
			Object k = in.readObject();
			if (!(k instanceof String)) {
				this.annotationType = (TypeSignature) k;
				break;
			}
			Object v = in.readObject();
			this.values.put((String) k, (Value) v);
		}
	}

}
