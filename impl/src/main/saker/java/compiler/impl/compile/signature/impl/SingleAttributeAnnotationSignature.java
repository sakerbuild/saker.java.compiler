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
 * {@link AnnotationSignature} that contains a single name-value pair.
 */
public final class SingleAttributeAnnotationSignature extends ValueAttributeSimpleAnnotationSignature {
	private static final long serialVersionUID = 1L;

	protected String name;

	/**
	 * For {@link Externalizable}.
	 */
	public SingleAttributeAnnotationSignature() {
	}

	public SingleAttributeAnnotationSignature(TypeSignature annotationType, String name, Value value) {
		super(annotationType, value);
		this.name = name;
	}

	@Override
	public Map<String, ? extends Value> getValues() {
		return ImmutableUtils.singletonMap(name, value);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeUTF(name);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		name = in.readUTF();
	}

	@Override
	public int hashCode() {
		return super.hashCode() * 31 + Objects.hashCode(name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SingleAttributeAnnotationSignature other = (SingleAttributeAnnotationSignature) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("@");
		sb.append(annotationType);
		sb.append("(");
		sb.append(name);
		sb.append(" = ");
		sb.append(value);
		sb.append(")");
		return sb.toString();
	}
}
