package saker.java.compiler.impl.compile.signature.change.member;

import java.util.function.Consumer;

import saker.java.compiler.impl.compile.handler.usage.TopLevelAbiUsage;
import saker.java.compiler.impl.compile.signature.change.AbiChange;

public class NamedMethodModifiedABIChange implements AbiChange {
	protected String classCanonicalName;
	protected String methodName;

	public NamedMethodModifiedABIChange(String classCanonicalName, String methodName) {
		this.classCanonicalName = classCanonicalName;
		this.methodName = methodName;
	}

	@Override
	public boolean affects(TopLevelAbiUsage usage, Consumer<AbiChange> foundchanges) {
		return usage.isReferencesMethod(classCanonicalName, methodName);
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
		NamedMethodModifiedABIChange other = (NamedMethodModifiedABIChange) obj;
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
		return "Method changed: " + classCanonicalName + "." + methodName;
	}

}
