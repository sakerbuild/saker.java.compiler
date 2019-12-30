package saker.java.compiler.impl.compile.signature.change.member;

import java.util.function.Consumer;

import saker.java.compiler.impl.compile.handler.usage.TopLevelAbiUsage;
import saker.java.compiler.impl.compile.signature.change.AbiChange;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.MethodSignature;

public class MethodRemovedABIChange extends MethodABIChange {

	public MethodRemovedABIChange(ClassSignature enclosingclass, MethodSignature method) {
		super(enclosingclass, method);
	}

	@Override
	public boolean affects(TopLevelAbiUsage usage, Consumer<AbiChange> foundchanges) {
		if (!AbiChange.isVisibleFrom(usage, method, enclosingClass)) {
			return false;
		}
		if (usage.isInheritesFromClass(classCanonicalName)) {
			return true;
		}
		if (usage.isReferencesMethod(classCanonicalName, method.getSimpleName())) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return "Class method removed: " + classCanonicalName + ": " + method;
	}
}
