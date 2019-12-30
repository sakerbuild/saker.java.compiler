package saker.java.compiler.impl.compile.handler.incremental.model.mirror;

import java.util.List;

import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.TypeVisitor;

import saker.build.thirdparty.saker.util.StringUtils;
import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;

public class SimpleExecutableType extends SimpleTypeMirror implements ExecutableType {

	private List<? extends TypeVariable> typeVariables;
	private TypeMirror returnType;
	private TypeMirror receiverType;
	private List<? extends TypeMirror> parameterTypes;
	private List<? extends TypeMirror> thrownTypes;

	public SimpleExecutableType(IncrementalElementsTypesBase elemTypes, List<? extends TypeVariable> typeVariables,
			TypeMirror returnType, TypeMirror receiverType, List<? extends TypeMirror> parameterTypes,
			List<? extends TypeMirror> thrownTypes) {
		super(elemTypes);
		this.typeVariables = typeVariables;
		this.returnType = returnType;
		this.receiverType = receiverType;
		this.parameterTypes = parameterTypes;
		this.thrownTypes = thrownTypes;
	}

	@Override
	public TypeKind getKind() {
		return TypeKind.EXECUTABLE;
	}

	@Override
	public <R, P> R accept(TypeVisitor<R, P> v, P p) {
		return v.visitExecutable(this, p);
	}

	@Override
	public List<? extends TypeVariable> getTypeVariables() {
		return typeVariables;
	}

	@Override
	public TypeMirror getReturnType() {
		return returnType;
	}

	@Override
	public List<? extends TypeMirror> getParameterTypes() {
		return parameterTypes;
	}

	@Override
	public TypeMirror getReceiverType() {
		return receiverType;
	}

	@Override
	public List<? extends TypeMirror> getThrownTypes() {
		return thrownTypes;
	}

	@Override
	public String toString() {
		return (typeVariables.isEmpty() ? "" : StringUtils.toStringJoin("<", ", ", typeVariables, "> ")) + returnType
				+ " (" + StringUtils.toStringJoin(", ", parameterTypes) + ")"
				+ (thrownTypes.isEmpty() ? "" : " throws " + StringUtils.toStringJoin(", ", thrownTypes));
	}
}
