package saker.java.compiler.impl.compile.signature.change.member;

import java.util.function.Consumer;

import saker.java.compiler.impl.compile.handler.usage.TopLevelAbiUsage;
import saker.java.compiler.impl.compile.signature.change.AbiChange;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;

public class ConstructorABIChange implements AbiChange {
	protected String classCanonicalName;

	public ConstructorABIChange(String classCanonicalName) {
		this.classCanonicalName = classCanonicalName;
	}

	@Override
	public boolean affects(TopLevelAbiUsage usage, Consumer<AbiChange> foundchanges) {
		return usage.isReferencesMethod(classCanonicalName, IncrementalElementsTypes.CONSTRUCTOR_METHOD_NAME);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((classCanonicalName == null) ? 0 : classCanonicalName.hashCode());
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
		ConstructorABIChange other = (ConstructorABIChange) obj;
		if (classCanonicalName == null) {
			if (other.classCanonicalName != null)
				return false;
		} else if (!classCanonicalName.equals(other.classCanonicalName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Constructor changed: " + classCanonicalName;
	}
}
