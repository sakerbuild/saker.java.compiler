package saker.java.compiler.impl.compile.handler.usage;

import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.FieldSignature;

public class FieldABIInfo implements Comparable<FieldABIInfo> {

	protected String classCanonicalName;
	protected String fieldName;

	private FieldABIInfo(String classCanonicalName, String fieldName) {
		this.classCanonicalName = classCanonicalName;
		this.fieldName = fieldName;
	}

	static FieldABIInfo create(ClassSignature enclosingclass, FieldSignature signature) {
		String classCanonicalName = enclosingclass.getCanonicalName();
		String fieldName = signature.getSimpleName();
		if (signature.getConstantValue() != null) {
			return createConstant(classCanonicalName, fieldName);
		}
		return create(classCanonicalName, fieldName);
	}

	static FieldABIInfo create(String classCanonicalName, String fieldName) {
		return new FieldABIInfo(classCanonicalName, fieldName);
	}

	static FieldABIInfo createConstant(String classCanonicalName, String fieldName) {
		return new ConstantFieldABIInfo(classCanonicalName, fieldName);
	}

	private static class ConstantFieldABIInfo extends FieldABIInfo {
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
		//order constants first, so serialization can be done in a slightly more efficient way in TopLevelABIUsageImpl
		int cmp = Boolean.compare(hasConstantValue(), o.hasConstantValue());
		if (cmp != 0) {
			return cmp;
		}
		cmp = classCanonicalName.compareTo(o.classCanonicalName);
		if (cmp != 0) {
			return cmp;
		}
		return fieldName.compareTo(o.fieldName);
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

}