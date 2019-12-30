package saker.java.compiler.impl.compile.handler.incremental.model.mirror;

import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVisitor;

import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;

public class SimpleArrayType extends SimpleTypeMirror implements ArrayType {
	private TypeMirror componentType;

	public SimpleArrayType(IncrementalElementsTypesBase elemTypes, TypeMirror componentType) {
		super(elemTypes);
		this.componentType = componentType;
	}

	public static SimpleArrayType erasured(IncrementalElementsTypesBase elemTypes, TypeMirror erasedcomponent) {
		SimpleArrayType result = new SimpleArrayType(elemTypes, erasedcomponent);
		result.erasedType = result;
		return result;
	}

	@Override
	public TypeKind getKind() {
		return TypeKind.ARRAY;
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		return v.visitArray(this, p);
	}

	@Override
	public TypeMirror getComponentType() {
		return componentType;
	}

	@Override
	public String toString() {
		return componentType + "[]";
	}
}
