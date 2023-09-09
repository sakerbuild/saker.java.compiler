package saker.java.compiler.impl.compile.handler.usage;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.java.compiler.impl.signature.element.ClassSignature;

final class ClassABIInfo implements Comparable<ClassABIInfo> {
	protected String canonicalName;

	public ClassABIInfo(String canonicalName) {
		this.canonicalName = canonicalName;
	}

	public ClassABIInfo(ClassSignature signature) {
		this(signature.getCanonicalName());
	}

	public static ClassABIInfo createExternal(ObjectInput in) throws ClassNotFoundException, IOException {
		return new ClassABIInfo((String) in.readObject());
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
		return getClass().getSimpleName() + "[" + (canonicalName != null ? "canonicalName=" + canonicalName : "") + "]";
	}

	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(canonicalName);
	}
}