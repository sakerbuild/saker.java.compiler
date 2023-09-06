package saker.java.compiler.impl.compile.handler.usage;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.FieldSignature;

public class FieldABIInfo implements Comparable<FieldABIInfo>, Externalizable {
	private static final long serialVersionUID = 1L;

	protected String classCanonicalName;
	protected String fieldName;

	/**
	 * For {@link Externalizable}.
	 */
	public FieldABIInfo() {
	}

	private FieldABIInfo(String classCanonicalName, String fieldName) {
		this.classCanonicalName = classCanonicalName;
		this.fieldName = fieldName;
	}

	public static FieldABIInfo create(ClassSignature enclosingclass, FieldSignature signature) {
		if (signature.getConstantValue() != null) {
			return new ConstantFieldABIInfo(enclosingclass.getCanonicalName(), signature.getSimpleName());
		}
		return new FieldABIInfo(enclosingclass.getCanonicalName(), signature.getSimpleName());
	}

	private static class ConstantFieldABIInfo extends FieldABIInfo {
		private static final long serialVersionUID = 1L;

		/**
		 * For {@link Externalizable}.
		 */
		public ConstantFieldABIInfo() {
		}

		protected ConstantFieldABIInfo(String classCanonicalName, String fieldName) {
			super(classCanonicalName, fieldName);
		}

		@Override
		public boolean hasConstantValue() {
			return true;
		}

	}

	public String getClassCanonicalName() {
		return classCanonicalName;
	}

	public String getFieldName() {
		return fieldName;
	}

	public boolean hasConstantValue() {
		return false;
	}

	@Override
	public int compareTo(FieldABIInfo o) {
		int cmp = classCanonicalName.compareTo(o.classCanonicalName);
		if (cmp != 0) {
			return cmp;
		}
		cmp = fieldName.compareTo(o.fieldName);
		if (cmp != 0) {
			return cmp;
		}
		return Boolean.compare(hasConstantValue(), o.hasConstantValue());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((classCanonicalName == null) ? 0 : classCanonicalName.hashCode());
		result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
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
		FieldABIInfo other = (FieldABIInfo) obj;
		if (classCanonicalName == null) {
			if (other.classCanonicalName != null)
				return false;
		} else if (!classCanonicalName.equals(other.classCanonicalName))
			return false;
		if (fieldName == null) {
			if (other.fieldName != null)
				return false;
		} else if (!fieldName.equals(other.fieldName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "["
				+ (classCanonicalName != null ? "classCanonicalName=" + classCanonicalName + ", " : "")
				+ (fieldName != null ? "fieldName=" + fieldName + ", " : "") + "]";
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(classCanonicalName);
		out.writeObject(fieldName);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		classCanonicalName = (String) in.readObject();
		fieldName = (String) in.readObject();
	}
}