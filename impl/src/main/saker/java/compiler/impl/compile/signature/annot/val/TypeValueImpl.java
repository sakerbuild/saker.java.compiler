package saker.java.compiler.impl.compile.signature.annot.val;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.java.compiler.impl.signature.element.AnnotationSignature.TypeValue;
import saker.java.compiler.impl.signature.type.TypeSignature;

public class TypeValueImpl implements TypeValue, Externalizable {
	private static final long serialVersionUID = 1L;

	private TypeSignature type;

	public TypeValueImpl() {
	}

	public TypeValueImpl(TypeSignature type) {
		this.type = type;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(type);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		type = (TypeSignature) in.readObject();
	}

	@Override
	public TypeSignature getType() {
		return type;
	}

	public void setType(TypeSignature type) {
		this.type = type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		TypeValueImpl other = (TypeValueImpl) obj;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return type.toString() + ".class";
	}

}
