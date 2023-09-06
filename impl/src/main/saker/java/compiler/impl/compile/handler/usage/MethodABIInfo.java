package saker.java.compiler.impl.compile.handler.usage;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.MethodSignature;

final class MethodABIInfo implements Comparable<MethodABIInfo>, Externalizable {
	private static final long serialVersionUID = 1L;

	protected String classCanonicalName;
	protected String methodName;

	/**
	 * For {@link Externalizable}.
	 */
	public MethodABIInfo() {
	}

	public MethodABIInfo(String classCanonicalName, String methodName) {
		this.classCanonicalName = classCanonicalName;
		this.methodName = methodName;
	}

	public MethodABIInfo(ClassSignature enclosingclass, MethodSignature method) {
		this(enclosingclass.getCanonicalName(), method.getSimpleName());
	}

	public String getClassCanonicalName() {
		return classCanonicalName;
	}

	public String getMethodName() {
		return methodName;
	}

	@Override
	public int compareTo(MethodABIInfo o) {
		int cmp = classCanonicalName.compareTo(o.classCanonicalName);
		if (cmp != 0) {
			return cmp;
		}
		return methodName.compareTo(o.methodName);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((classCanonicalName == null) ? 0 : classCanonicalName.hashCode());
		result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
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
		MethodABIInfo other = (MethodABIInfo) obj;
		if (classCanonicalName == null) {
			if (other.classCanonicalName != null)
				return false;
		} else if (!classCanonicalName.equals(other.classCanonicalName))
			return false;
		if (methodName == null) {
			if (other.methodName != null)
				return false;
		} else if (!methodName.equals(other.methodName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "["
				+ (classCanonicalName != null ? "classCanonicalName=" + classCanonicalName + ", " : "")
				+ (methodName != null ? "methodName=" + methodName : "") + "]";
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(classCanonicalName);
		out.writeObject(methodName);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		classCanonicalName = (String) in.readObject();
		methodName = (String) in.readObject();
	}
}