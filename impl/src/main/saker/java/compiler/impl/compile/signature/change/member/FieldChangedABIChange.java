package saker.java.compiler.impl.compile.signature.change.member;

import java.util.function.Consumer;

import saker.java.compiler.impl.compile.handler.usage.TopLevelAbiUsage;
import saker.java.compiler.impl.compile.signature.change.AbiChange;

public class FieldChangedABIChange implements AbiChange {
	private String classCanonicalName;
	private String fieldName;

	public FieldChangedABIChange(String classCanonicalName, String fieldName) {
		this.classCanonicalName = classCanonicalName;
		this.fieldName = fieldName;
	}

	@Override
	public boolean affects(TopLevelAbiUsage usage, Consumer<AbiChange> foundchanges) {
		return usage.isReferencesField(classCanonicalName, fieldName);
	}

	@Override
	public String toString() {
		return "Class field changed: " + classCanonicalName + "." + fieldName;
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
		FieldChangedABIChange other = (FieldChangedABIChange) obj;
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

}
