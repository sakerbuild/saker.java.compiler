package saker.java.compiler.impl.compile.handler.usage;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.java.compiler.impl.signature.element.ClassSignature;

final class ClassABIInfo implements Comparable<ClassABIInfo>, Externalizable {
	private static final long serialVersionUID = 1L;

	protected String canonicalName;

	/**
	 * For {@link Externalizable}.
	 */
	public ClassABIInfo() {
	}

	public ClassABIInfo(String canonicalName) {
		this.canonicalName = canonicalName;
	}

	public ClassABIInfo(ClassSignature signature) {
		this(signature.getCanonicalName());
	}

	public String getCanonicalName() {
		return canonicalName;
	}

	@Override
	public int compareTo(ClassABIInfo o) {
		return canonicalName.compareTo(o.canonicalName);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((canonicalName == null) ? 0 : canonicalName.hashCode());
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
		ClassABIInfo other = (ClassABIInfo) obj;
		if (canonicalName == null) {
			if (other.canonicalName != null)
				return false;
		} else if (!canonicalName.equals(other.canonicalName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (canonicalName != null ? "canonicalName=" + canonicalName : "")
				+ "]";
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(canonicalName);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		canonicalName = (String) in.readObject();
	}
}