package saker.java.compiler.impl.compile.handler.incremental.model;

import javax.lang.model.type.TypeMirror;

public class IsAssignableTypeVisitor extends IsSubTypeVisitor {

	public IsAssignableTypeVisitor(IncrementalElementsTypesBase incrementalElementsTypes) {
		super(incrementalElementsTypes);
	}

	@Override
	protected boolean isRawDeclaredTypesCompatible(boolean leftraw, boolean rightraw) {
		//if any of the types are raw, consider them assignable
		return true;
	}

	@Override
	protected boolean isDeclaredEnclosingTypesCompatible(TypeMirror leftenclosing, TypeMirror rightenclosing) {
		return elemTypes.isAssignable(leftenclosing, rightenclosing);
	}

	@Override
	protected boolean callWithArguments(TypeMirror t, TypeMirror p) {
		return elemTypes.isAssignable(t, p);
	}
}