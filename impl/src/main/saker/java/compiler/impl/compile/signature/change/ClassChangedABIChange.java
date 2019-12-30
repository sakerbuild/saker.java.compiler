package saker.java.compiler.impl.compile.signature.change;

import java.util.function.Consumer;

import saker.java.compiler.impl.compile.handler.usage.TopLevelAbiUsage;
import saker.java.compiler.impl.signature.element.ClassSignature;

public class ClassChangedABIChange implements AbiChange {
	private String classCanonicalName;

	public ClassChangedABIChange(ClassSignature clazz) {
		this(clazz.getCanonicalName());
	}

	public ClassChangedABIChange(String classCanonicalName) {
		this.classCanonicalName = classCanonicalName;
	}

	@Override
	public boolean affects(TopLevelAbiUsage usage, Consumer<AbiChange> foundchanges) {
		return ClassChangedABIChange.affectsReferencedClass(classCanonicalName, usage, foundchanges);
	}

	public static boolean affectsReferencedClass(String classCanonicalName, TopLevelAbiUsage usage,
			Consumer<AbiChange> foundchanges) {
		if (usage.isReferencesClass(classCanonicalName)) {
			usage.addABIChangeForEachMember(u -> u.isReferencesClass(classCanonicalName), foundchanges);
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "Class changed: " + classCanonicalName;
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
		ClassChangedABIChange other = (ClassChangedABIChange) obj;
		if (classCanonicalName == null) {
			if (other.classCanonicalName != null)
				return false;
		} else if (!classCanonicalName.equals(other.classCanonicalName))
			return false;
		return true;
	}

}
