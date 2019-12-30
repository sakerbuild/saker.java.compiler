package saker.java.compiler.impl.options;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

import saker.java.compiler.api.classpath.ClassPathVisitor;
import saker.java.compiler.api.classpath.SDKClassPath;
import saker.java.compiler.api.modulepath.ModulePathVisitor;
import saker.java.compiler.api.modulepath.SDKModulePath;
import saker.sdk.support.api.SDKPathReference;

public class SDKClassAndModulePathReferenceOption
		implements ClassPathReferenceOption, SDKClassPath, ModulePathReferenceOption, SDKModulePath, Externalizable {
	private static final long serialVersionUID = 1L;

	private SDKPathReference pathReference;

	/**
	 * For {@link Externalizable}.
	 */
	public SDKClassAndModulePathReferenceOption() {
	}

	public SDKClassAndModulePathReferenceOption(SDKPathReference pathReference) {
		Objects.requireNonNull(pathReference, "sdk path reference");
		this.pathReference = pathReference;
	}

	@Override
	public void accept(ClassPathVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public void accept(ModulePathVisitor visitor) {
		visitor.visit(this);
	}

	@Override
	public SDKPathReference getSDKPathReference() {
		return pathReference;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(pathReference);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		pathReference = (SDKPathReference) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((pathReference == null) ? 0 : pathReference.hashCode());
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
		SDKClassAndModulePathReferenceOption other = (SDKClassAndModulePathReferenceOption) obj;
		if (pathReference == null) {
			if (other.pathReference != null)
				return false;
		} else if (!pathReference.equals(other.pathReference))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + pathReference + "]";
	}

}
