package saker.java.compiler.impl.compile.signature.annot.val;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

import saker.java.compiler.impl.signature.element.AnnotationSignature.LiteralValue;
import saker.java.compiler.impl.signature.value.ConstantValueResolver;

public class LiteralValueImpl implements LiteralValue, Externalizable {
	private static final long serialVersionUID = 1L;

	private ConstantValueResolver value;

	public LiteralValueImpl() {
	}

	public LiteralValueImpl(ConstantValueResolver value) {
		this.value = value;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(value);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		value = (ConstantValueResolver) in.readObject();
	}

	@Override
	public ConstantValueResolver getValue() {
		return value;
	}

	public void setValue(ConstantValueResolver value) {
		this.value = value;
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
		LiteralValueImpl other = (LiteralValueImpl) obj;
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
}
