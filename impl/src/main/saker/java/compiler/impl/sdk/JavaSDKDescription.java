package saker.java.compiler.impl.sdk;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.sdk.support.api.EnvironmentSDKDescription;
import saker.sdk.support.api.IndeterminateSDKDescription;
import saker.sdk.support.api.SDKDescription;
import saker.sdk.support.api.SDKReference;
import saker.sdk.support.api.exc.SDKPropertyNotFoundException;

public class JavaSDKDescription implements IndeterminateSDKDescription, Externalizable {
	private static final long serialVersionUID = 1L;

	private SDKDescription baseSDK;

	/**
	 * For {@link Externalizable}.
	 */
	public JavaSDKDescription() {
	}

	public JavaSDKDescription(SDKDescription baseSDK) {
		this.baseSDK = baseSDK;
	}

	@Override
	public SDKDescription getBaseSDKDescription() {
		return baseSDK;
	}

	@Override
	public SDKDescription pinSDKDescription(SDKReference sdkreference) {
		String prop = null;
		try {
			prop = sdkreference.getProperty(JavaSDKReference.PROPERTY_JAVA_VERSION);
			if (prop == null) {
				throw new SDKPropertyNotFoundException(
						JavaSDKReference.PROPERTY_JAVA_VERSION + " property not found in Java SDK.");
			}
		} catch (SDKPropertyNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new SDKPropertyNotFoundException(
					JavaSDKReference.PROPERTY_JAVA_VERSION + " property not found in Java SDK.", e);
		}
		return EnvironmentSDKDescription
				.create(new JavaSDKReferenceEnvironmentProperty(ImmutableUtils.singletonNavigableSet(prop)));
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(baseSDK);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		baseSDK = SerialUtils.readExternalObject(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((baseSDK == null) ? 0 : baseSDK.hashCode());
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
		JavaSDKDescription other = (JavaSDKDescription) obj;
		if (baseSDK == null) {
			if (other.baseSDK != null)
				return false;
		} else if (!baseSDK.equals(other.baseSDK))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "JavaSDKDescription[" + baseSDK + "]";
	}

}
