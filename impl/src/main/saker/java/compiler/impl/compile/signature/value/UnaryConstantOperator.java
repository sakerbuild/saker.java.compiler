package saker.java.compiler.impl.compile.signature.value;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;
import java.util.function.Function;

import javax.lang.model.element.Element;

import com.sun.source.tree.Tree.Kind;

import saker.java.compiler.api.processing.SakerElementsTypes;
import saker.java.compiler.impl.signature.value.ConstantValueResolver;
import saker.java.compiler.impl.util.operators.UnaryOperators;

public class UnaryConstantOperator implements ConstantValueResolver, Externalizable {
	private static final long serialVersionUID = 1L;

	private ConstantValueResolver subject;
	private String operatorName;

	public UnaryConstantOperator() {
	}

	public UnaryConstantOperator(ConstantValueResolver subject, Kind operator) {
		this.subject = subject;
		this.operatorName = operator.name();
	}

	@Override
	public Object resolve(SakerElementsTypes elements, Element resolutionelement) {
		Object sub = subject.resolve(elements, resolutionelement);
		if (sub == null) {
			return null;
		}
		Function<Object, Object> op = UnaryOperators.getOperatorFunction(operatorName, sub);
		if (op == null) {
			return null;
		}
		return op.apply(sub);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(operatorName);
		out.writeObject(subject);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		operatorName = in.readUTF();
		subject = (ConstantValueResolver) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((operatorName == null) ? 0 : operatorName.hashCode());
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
		UnaryConstantOperator other = (UnaryConstantOperator) obj;
		if (operatorName != other.operatorName)
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
//		return JavaUtil.operatorToString(operatorName) + subject.toString();
		return operatorName + "(" + subject + ")";
	}

	@Override
	public boolean signatureEquals(ConstantValueResolver other) {
		if (!(other instanceof UnaryConstantOperator)) {
			return false;
		}
		UnaryConstantOperator o = (UnaryConstantOperator) other;
		if (!Objects.equals(operatorName, o.operatorName)) {
			return false;
		}
		if (!subject.signatureEquals(o.subject)) {
			return false;
		}
		return true;
	}
}
