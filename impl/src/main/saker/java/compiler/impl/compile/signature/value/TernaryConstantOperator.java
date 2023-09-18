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

import saker.build.util.data.annotation.ValueType;
import saker.java.compiler.api.processing.SakerElementsTypes;
import saker.java.compiler.impl.signature.value.ConstantValueResolver;

@ValueType
public class TernaryConstantOperator implements ConstantValueResolver, Externalizable {
	private static final long serialVersionUID = 1L;

	private ConstantValueResolver condition;
	private ConstantValueResolver onTrue;
	private ConstantValueResolver onFalse;

	public TernaryConstantOperator() {
	}

	public TernaryConstantOperator(ConstantValueResolver condition, ConstantValueResolver onTrue,
			ConstantValueResolver onFalse) {
		this.condition = condition;
		this.onTrue = onTrue;
		this.onFalse = onFalse;
	}

	@Override
	public Object resolve(SakerElementsTypes elements, Element resolutionelement) {
		Object cond = condition.resolve(elements, resolutionelement);
		if (cond instanceof Boolean) {
			if ((Boolean) cond) {
				return onTrue.resolve(elements, resolutionelement);
			}
			return onFalse.resolve(elements, resolutionelement);
		}
		return null;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(condition);
		out.writeObject(onTrue);
		out.writeObject(onFalse);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		condition = (ConstantValueResolver) in.readObject();
		onTrue = (ConstantValueResolver) in.readObject();
		onFalse = (ConstantValueResolver) in.readObject();
	}

	@Override
	public int hashCode() {
		return (Objects.hashCode(onTrue) * 31 + Objects.hashCode(onFalse) * 61) * 97 + Objects.hashCode(condition);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TernaryConstantOperator other = (TernaryConstantOperator) obj;
		if (condition == null) {
			if (other.condition != null)
				return false;
		} else if (!condition.equals(other.condition))
			return false;
		if (onFalse == null) {
			if (other.onFalse != null)
				return false;
		} else if (!onFalse.equals(other.onFalse))
			return false;
		if (onTrue == null) {
			if (other.onTrue != null)
				return false;
		} else if (!onTrue.equals(other.onTrue))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "(" + condition + " ? " + onTrue + " : " + onFalse + ")";
	}

	@Override
	public boolean signatureEquals(ConstantValueResolver other) {
		if (!(other instanceof TernaryConstantOperator)) {
			return false;
		}
		TernaryConstantOperator o = (TernaryConstantOperator) other;
		if (!condition.signatureEquals(o.condition)) {
			return false;
		}
		if (!onTrue.signatureEquals(o.onTrue)) {
			return false;
		}
		if (!onFalse.signatureEquals(o.onFalse)) {
			return false;
		}
		return true;
	}
}
