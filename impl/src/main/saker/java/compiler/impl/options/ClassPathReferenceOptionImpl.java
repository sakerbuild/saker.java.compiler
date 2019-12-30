package saker.java.compiler.impl.options;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

import saker.java.compiler.api.classpath.ClassPathReference;
import saker.java.compiler.api.classpath.ClassPathVisitor;

public class ClassPathReferenceOptionImpl implements ClassPathReferenceOption, Externalizable {
	private static final long serialVersionUID = 1L;

	private ClassPathReference reference;

	/**
	 * For {@link Externalizable}.
	 */
	public ClassPathReferenceOptionImpl() {
	}

	public ClassPathReferenceOptionImpl(ClassPathReference reference) {
		Objects.requireNonNull(reference, "classpath reference");
		this.reference = reference;
	}

	@Override
	public void accept(ClassPathVisitor visitor) {
		visitor.visit(reference);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(reference);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		reference = (ClassPathReference) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((reference == null) ? 0 : reference.hashCode());
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
		ClassPathReferenceOptionImpl other = (ClassPathReferenceOptionImpl) obj;
		if (reference == null) {
			if (other.reference != null)
				return false;
		} else if (!reference.equals(other.reference))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (reference != null ? "reference=" + reference : "") + "]";
	}

}
