package saker.java.compiler.impl.compile.signature.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;
import java.util.Objects;

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.type.TypeSignature;

/**
 * {@link AnnotationSignature} that contains a single attribute with the name <code>"value"</code>
 */
public class ValueAttributeSimpleAnnotationSignature extends SimpleAnnotationSignature {
	private static final long serialVersionUID = 1L;

	protected AnnotationSignature.Value value;

	/**
	 * For {@link Externalizable}.
	 */
	public ValueAttributeSimpleAnnotationSignature() {
	}

	public ValueAttributeSimpleAnnotationSignature(TypeSignature annotationType, Value value) {
		super(annotationType);
		this.value = value;
	}

	@Override
	public Map<String, ? extends Value> getValues() {
		return ImmutableUtils.singletonMap("value", value);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(value);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		value = (Value) in.readObject();
	}

	@Override
	public int hashCode() {
		return super.hashCode() * 31 + Objects.hashCode(value);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ValueAttributeSimpleAnnotationSignature other = (ValueAttributeSimpleAnnotationSignature) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("@");
		sb.append(annotationType);
		sb.append("(");
		sb.append(value);
		sb.append(")");
		return sb.toString();
	}
}
