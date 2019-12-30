package saker.java.compiler.impl.compile.handler.incremental.model.forwarded.mirror;

import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

import saker.java.compiler.impl.compile.handler.incremental.model.IncrementalElementsTypesBase;

public class ForwardingExecutableType extends ForwardingTypeMirrorBase<ExecutableType> implements ExecutableType {
	private static final AtomicReferenceFieldUpdater<ForwardingExecutableType, TypeMirror> ARFU_returnType = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingExecutableType.class, TypeMirror.class, "returnType");
	private static final AtomicReferenceFieldUpdater<ForwardingExecutableType, TypeMirror> ARFU_receiverType = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingExecutableType.class, TypeMirror.class, "receiverType");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<ForwardingExecutableType, List> ARFU_typeVariables = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingExecutableType.class, List.class, "typeVariables");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<ForwardingExecutableType, List> ARFU_parameterTypes = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingExecutableType.class, List.class, "parameterTypes");
	@SuppressWarnings("rawtypes")
	private static final AtomicReferenceFieldUpdater<ForwardingExecutableType, List> ARFU_thrownTypes = AtomicReferenceFieldUpdater
			.newUpdater(ForwardingExecutableType.class, List.class, "thrownTypes");

	private volatile transient TypeMirror returnType;
	private volatile transient TypeMirror receiverType;
	private volatile transient List<? extends TypeMirror> parameterTypes;
	private volatile transient List<? extends TypeVariable> typeVariables;
	private volatile transient List<? extends TypeMirror> thrownTypes;

	public ForwardingExecutableType(IncrementalElementsTypesBase elemTypes, ExecutableType subject) {
		super(elemTypes, subject);
	}

	@Override
	public List<? extends TypeVariable> getTypeVariables() {
		List<? extends TypeVariable> thistypevariables = this.typeVariables;
		if (thistypevariables != null) {
			return thistypevariables;
		}
		thistypevariables = elemTypes.forwardTypes(subject::getTypeVariables);
		if (ARFU_typeVariables.compareAndSet(this, null, thistypevariables)) {
			return thistypevariables;
		}
		return this.typeVariables;
	}

	@Override
	public TypeMirror getReturnType() {
		TypeMirror thisreturntype = this.returnType;
		if (thisreturntype != null) {
			return thisreturntype;
		}
		thisreturntype = elemTypes.forwardType(subject::getReturnType);
		if (ARFU_returnType.compareAndSet(this, null, thisreturntype)) {
			return thisreturntype;
		}
		return this.returnType;
	}

	@Override
	public List<? extends TypeMirror> getParameterTypes() {
		List<? extends TypeMirror> thisparametertypes = this.parameterTypes;
		if (thisparametertypes != null) {
			return thisparametertypes;
		}
		thisparametertypes = elemTypes.forwardTypes(subject::getParameterTypes);
		if (ARFU_parameterTypes.compareAndSet(this, null, thisparametertypes)) {
			return thisparametertypes;
		}
		return this.parameterTypes;
	}

	@Override
	public TypeMirror getReceiverType() {
		TypeMirror thisreceivertype = this.receiverType;
		if (thisreceivertype != null) {
			return thisreceivertype;
		}
		thisreceivertype = elemTypes.forwardTypeOrNone(subject::getReceiverType);
		if (ARFU_receiverType.compareAndSet(this, null, thisreceivertype)) {
			return thisreceivertype;
		}
		return this.receiverType;
	}

	@Override
	public List<? extends TypeMirror> getThrownTypes() {
		List<? extends TypeMirror> thisthrowntypes = this.thrownTypes;
		if (thisthrowntypes != null) {
			return thisthrowntypes;
		}
		thisthrowntypes = elemTypes.forwardTypes(subject::getThrownTypes);
		if (ARFU_thrownTypes.compareAndSet(this, null, thisthrowntypes)) {
			return thisthrowntypes;
		}
		return this.thrownTypes;
	}

}
