package saker.java.compiler.impl.find;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.runtime.environment.EnvironmentProperty;
import saker.build.runtime.environment.SakerEnvironment;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.util.java.JavaTools;

public class CurrentJavaVersionEnvironmentProperty implements EnvironmentProperty<String>, Externalizable {
	private static final long serialVersionUID = 1L;

	public static final CurrentJavaVersionEnvironmentProperty INSTANCE = new CurrentJavaVersionEnvironmentProperty();

	/**
	 * For {@link Externalizable}.
	 * 
	 * @see #INSTANCE
	 */
	public CurrentJavaVersionEnvironmentProperty() {
	}

	@Override
	public String getCurrentValue(SakerEnvironment environment) {
		return JavaTools.getCurrentJavaVersionProperty();
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
