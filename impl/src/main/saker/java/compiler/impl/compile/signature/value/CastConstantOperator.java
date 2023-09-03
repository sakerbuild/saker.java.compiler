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
import java.util.function.Function;

import javax.lang.model.element.Element;

import saker.build.util.data.annotation.ValueType;
import saker.java.compiler.api.processing.SakerElementsTypes;
import saker.java.compiler.impl.signature.value.ConstantValueResolver;
import saker.java.compiler.impl.util.operators.CastOperators;

@ValueType
public class CastConstantOperator implements ConstantValueResolver, Externalizable {
	private static final long serialVersionUID = 1L;

	private ConstantValueResolver subject;
	private Class<?> clazz;

	public CastConstantOperator() {
	}

	public CastConstantOperator(ConstantValueResolver subject, Class<?> clazz) {
		this.subject = subject;
		this.clazz = clazz;
	}

	@Override
	public Object resolve(SakerElementsTypes elements, Element resolutionelement) {
		Object sub = subject.resolve(elements, resolutionelement);
		if (sub == null) {
			return null;
		}
		Function<Object, ?> op = CastOperators.getOperatorFunction(sub, clazz);
		if (op == null) {
			return null;
		}
		return op.apply(sub);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(clazz);
		out.writeObject(subject);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		clazz = (Class<?>) in.readObject();
		subject = (ConstantValueResolver) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
		result = prime * result + ((subject == null) ? 0 : subject.hashCode());
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
		CastConstantOperator other = (CastConstantOperator) obj;
		if (clazz == null) {
			if (other.clazz != null)
				return false;
		} else if (!clazz.equals(other.clazz))
			return false;
		if (subject == null) {
			if (other.subject != null)
				return false;
		} else if (!subject.equals(other.subject))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "((" + clazz.getSimpleName() + ") " + subject + ")";
	}

	@Override
	public boolean signatureEquals(ConstantValueResolver other) {
		if (!(other instanceof CastConstantOperator)) {
			return false;
		}
		CastConstantOperator o = (CastConstantOperator) other;
		if (clazz != o.clazz) {
			return false;
		}
		if (!subject.signatureEquals(o.subject)) {
			return false;
		}
		return true;
	}

}
