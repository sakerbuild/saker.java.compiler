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
package saker.java.compiler.impl.compile.signature.value;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

import javax.lang.model.element.Element;

import saker.java.compiler.api.processing.SakerElementsTypes;
import saker.java.compiler.impl.signature.value.ConstantValueResolver;

public class LiteralConstantResolver implements ConstantValueResolver, Externalizable {
	private static final long serialVersionUID = 1L;

	public static final LiteralConstantResolver NULL_RESOLVER = new LiteralConstantResolver(null);

	private Object value;

	public LiteralConstantResolver() {
	}

	private LiteralConstantResolver(Object value) {
		this.value = value;
	}

	public static LiteralConstantResolver create(Object value) {
		if (value == null) {
			return NULL_RESOLVER;
		}
		return new LiteralConstantResolver(value);
	}

	@Override
	public Object resolve(SakerElementsTypes elements, Element resolutionelement) {
		return value;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(value);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		value = in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
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
		LiteralConstantResolver other = (LiteralConstantResolver) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return Objects.toString(value);
	}

	@Override
	public boolean signatureEquals(ConstantValueResolver other) {
		if (!(other instanceof LiteralConstantResolver)) {
			return false;
		}
		LiteralConstantResolver o = (LiteralConstantResolver) other;
		if (!Objects.equals(value, o.value)) {
			return false;
		}
		return true;
	}
}
