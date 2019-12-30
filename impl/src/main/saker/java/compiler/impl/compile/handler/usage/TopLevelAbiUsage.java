package saker.java.compiler.impl.compile.handler.usage;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import saker.java.compiler.impl.compile.signature.change.AbiChange;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.FieldSignature;
import saker.java.compiler.impl.signature.element.MethodSignature;

public interface TopLevelAbiUsage extends AbiUsage {
	public static class ClassABIInfo implements Comparable<ClassABIInfo>, Externalizable {
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
			return "ClassABIInfo [" + (canonicalName != null ? "canonicalName=" + canonicalName : "") + "]";
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

	public static class MethodABIInfo implements Comparable<MethodABIInfo>, Externalizable {
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
			cmp = methodName.compareTo(o.methodName);
			if (cmp != 0) {
				return cmp;
			}
			return 0;
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
			return "MethodABIInfo ["
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

	public static class FieldABIInfo implements Comparable<FieldABIInfo>, Externalizable {
		private static final long serialVersionUID = 1L;

		protected String classCanonicalName;
		protected String fieldName;
		protected boolean hasConstantValue;

		/**
		 * For {@link Externalizable}.
		 */
		public FieldABIInfo() {
		}

		public FieldABIInfo(String classCanonicalName, String fieldName, boolean hasConstantValue) {
			this.classCanonicalName = classCanonicalName;
			this.fieldName = fieldName;
			this.hasConstantValue = hasConstantValue;
		}

		public FieldABIInfo(ClassSignature enclosingclass, FieldSignature signature) {
			this(enclosingclass.getCanonicalName(), signature.getSimpleName(), signature.getConstantValue() != null);
		}

		public String getClassCanonicalName() {
			return classCanonicalName;
		}

		public String getFieldName() {
			return fieldName;
		}

		public boolean hasConstantValue() {
			return hasConstantValue;
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
			cmp = Boolean.compare(hasConstantValue, o.hasConstantValue);
			if (cmp != 0) {
				return cmp;
			}
			return 0;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((classCanonicalName == null) ? 0 : classCanonicalName.hashCode());
			result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
			result = prime * result + (hasConstantValue ? 1231 : 1237);
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
			if (hasConstantValue != other.hasConstantValue)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "FieldABIInfo ["
					+ (classCanonicalName != null ? "classCanonicalName=" + classCanonicalName + ", " : "")
					+ (fieldName != null ? "fieldName=" + fieldName + ", " : "") + "hasConstantValue="
					+ hasConstantValue + "]";
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(classCanonicalName);
			out.writeObject(fieldName);
			out.writeBoolean(hasConstantValue);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			classCanonicalName = (String) in.readObject();
			fieldName = (String) in.readObject();
			hasConstantValue = in.readBoolean();
		}

	}

	public Map<ClassABIInfo, ? extends AbiUsage> getClasses();

	public Map<MethodABIInfo, ? extends Collection<? extends AbiUsage>> getMethods();

	public Map<FieldABIInfo, ? extends AbiUsage> getFields();

	public AbiUsage getPackageUsage();

	public boolean addABIChangeForEachMember(Function<AbiUsage, Boolean> predicate, Consumer<AbiChange> foundchanges);

//	public boolean addABIChangeForEachClassMember(BiFunction<ClassMemberSignature, ABIUsage, Boolean> predicate, Consumer<ABIChange> foundchanges);

}