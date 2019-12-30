package saker.java.compiler.impl.sdk;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.runtime.environment.EnvironmentProperty;
import saker.build.runtime.environment.SakerEnvironment;
import saker.build.thirdparty.saker.util.ObjectUtils;

public final class CurrentJavaSDKReferenceEnvironmentProperty
		implements EnvironmentProperty<JavaSDKReference>, Externalizable {
	private static final long serialVersionUID = 1L;

	public static final CurrentJavaSDKReferenceEnvironmentProperty INSTANCE = new CurrentJavaSDKReferenceEnvironmentProperty();

	/**
	 * For {@link Externalizable}.
	 */
	public CurrentJavaSDKReferenceEnvironmentProperty() {
	}

	@Override
	public JavaSDKReference getCurrentValue(SakerEnvironment environment) throws Exception {
		return JavaSDKReference.getCurrent();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
	}

	@Override
	public int hashCode() {
		return getClass().getName().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return ObjectUtils.isSameClass(this, obj);
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[]";
	}

}
