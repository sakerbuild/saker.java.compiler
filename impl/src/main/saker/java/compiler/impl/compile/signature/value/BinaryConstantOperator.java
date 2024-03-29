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
import java.util.function.BiFunction;

import javax.lang.model.element.Element;

import com.sun.source.tree.Tree.Kind;

import saker.build.util.data.annotation.ValueType;
import saker.java.compiler.api.processing.SakerElementsTypes;
import saker.java.compiler.impl.signature.value.ConstantValueResolver;
import saker.java.compiler.impl.util.operators.BinaryOperators;

@ValueType
public class BinaryConstantOperator implements ConstantValueResolver, Externalizable {
	private static final long serialVersionUID = 1L;

	private ConstantValueResolver left;
	private ConstantValueResolver right;
	private String operatorName;

	public BinaryConstantOperator() {
	}

	public BinaryConstantOperator(ConstantValueResolver left, ConstantValueResolver right, Kind operator) {
		this.left = left;
		this.right = right;
		this.operatorName = operator.name();
	}

	@Override
	public Object resolve(SakerElementsTypes elements, Element resolutionelement) {
		Object l = left.resolve(elements, resolutionelement);
		if (l == null) {
			return null;
		}
		Object r = right.resolve(elements, resolutionelement);
		if (r == null) {
			return null;
		}
		BiFunction<Object, Object, Object> op = BinaryOperators.getOperatorFunction(operatorName, l, r);
		if (op == null) {
			return null;
		}
		return op.apply(l, r);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(operatorName);
		out.writeObject(left);
		out.writeObject(right);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		operatorName = in.readUTF();
		left = (ConstantValueResolver) in.readObject();
		right = (ConstantValueResolver) in.readObject();
	}

	@Override
	public int hashCode() {
		return (Objects.hashCode(left) * 31 + Objects.hashCode(right) * 61) * 97 + Objects.hashCode(operatorName);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BinaryConstantOperator other = (BinaryConstantOperator) obj;
		if (left == null) {
			if (other.left != null)
				return false;
		} else if (!left.equals(other.left))
			return false;
		if (operatorName != other.operatorName)
			return false;
		if (right == null) {
			if (other.right != null)
				return false;
		} else if (!right.equals(other.right))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return left + " " + operatorName + " " + right;
	}

	@Override
	public boolean signatureEquals(ConstantValueResolver other) {
		if (!(other instanceof BinaryConstantOperator)) {
			return false;
		}
		BinaryConstantOperator o = (BinaryConstantOperator) other;
		if (!Objects.equals(operatorName, o.operatorName)) {
			return false;
		}
		if (!left.signatureEquals(o.left)) {
			return false;
		}
		if (!right.signatureEquals(o.right)) {
			return false;
		}
		return true;
	}
}
