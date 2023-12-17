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
package saker.java.compiler.impl.compile.signature.annot.val;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.impl.signature.element.AnnotationSignature.ArrayValue;
import saker.java.compiler.impl.signature.element.AnnotationSignature.Value;

public final class ArrayValueImpl implements ArrayValue, Externalizable {
	private static final long serialVersionUID = 1L;

	public static final ArrayValue EMPTY_INSTANCE = new ArrayValueImpl(Collections.emptyList());

	private List<? extends Value> values;

	/**
	 * For {@link Externalizable}.
	 */
	public ArrayValueImpl() {
	}

	private ArrayValueImpl(List<? extends Value> values) {
		this.values = values;
	}

	public static ArrayValue create(List<? extends Value> values) {
		if (ObjectUtils.isNullOrEmpty(values)) {
			return EMPTY_INSTANCE;
		}
		if (values.size() == 1) {
			return new SingletonArrayValueImpl(values.get(0));
		}
		return new ArrayValueImpl(values);
	}

	@Override
	public List<? extends Value> getValues() {
		return values;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerialUtils.writeExternalCollection(out, values);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		values = SerialUtils.readExternalImmutableList(in);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(values);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ArrayValueImpl other = (ArrayValueImpl) obj;
		if (values == null) {
			if (other.values != null)
				return false;
		} else if (!values.equals(other.values))
			return false;
		return true;
	}

	@Override
	public String toString() {
		if (values.isEmpty()) {
			return "{}";
		}
		return "{ " + StringUtils.toStringJoin(", ", values) + " }";
	}

	protected static final class SingletonArrayValueImpl implements ArrayValue, Externalizable {
		private static final long serialVersionUID = 1L;

		private Value value;

		/**
		 * For {@link Externalizable}.
		 */
		public SingletonArrayValueImpl() {
		}

		public SingletonArrayValueImpl(Value value) {
			this.value = value;
		}

		@Override
		public List<? extends Value> getValues() {
			return ImmutableUtils.singletonList(value);
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(value);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			value = (Value) in.readObject();
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(value);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SingletonArrayValueImpl other = (SingletonArrayValueImpl) obj;
			if (value == null) {
				if (other.value != null)
					return false;
			} else if (!value.equals(other.value))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "{ " + value + " }";
		}

	}

}
