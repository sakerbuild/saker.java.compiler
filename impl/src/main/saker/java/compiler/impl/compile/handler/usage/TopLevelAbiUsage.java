/*
 * Copyright (C) 2020 Bence Sipka
 *
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
	public static final class ClassABIInfo implements Comparable<ClassABIInfo>, Externalizable {
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

	public static final class MethodABIInfo implements Comparable<MethodABIInfo>, Externalizable {
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

	public static class FieldABIInfo implements Comparable<FieldABIInfo>, Externalizable {
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

	public Map<ClassABIInfo, ? extends AbiUsage> getClasses();

	public Map<MethodABIInfo, ? extends Collection<? extends AbiUsage>> getMethods();

	public Map<FieldABIInfo, ? extends AbiUsage> getFields();

	public AbiUsage getPackageUsage();

	public boolean addABIChangeForEachMember(Function<AbiUsage, Boolean> predicate, Consumer<AbiChange> foundchanges);

//	public boolean addABIChangeForEachClassMember(BiFunction<ClassMemberSignature, ABIUsage, Boolean> predicate, Consumer<ABIChange> foundchanges);

}