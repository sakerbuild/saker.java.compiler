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

import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.impl.signature.element.AnnotationSignature.ArrayValue;
import saker.java.compiler.impl.signature.element.AnnotationSignature.Value;

public class ArrayValueImpl implements ArrayValue, Externalizable {
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
		final int prime = 31;
		int result = 1;
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

}
