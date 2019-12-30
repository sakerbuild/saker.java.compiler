package saker.java.compiler.impl.compile.signature.value;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;
import java.util.function.BiFunction;

import javax.lang.model.element.Element;

import com.sun.source.tree.Tree.Kind;

import saker.java.compiler.api.processing.SakerElementsTypes;
import saker.java.compiler.impl.signature.value.ConstantValueResolver;
import saker.java.compiler.impl.util.operators.BinaryOperators;

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
		final int prime = 31;
		int result = 1;
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + ((operatorName == null) ? 0 : operatorName.hashCode());
		result = prime * result + ((right == null) ? 0 : right.hashCode());
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
//		return left + " " + JavaUtil.operatorToString(operatorName) + " " + right;
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
