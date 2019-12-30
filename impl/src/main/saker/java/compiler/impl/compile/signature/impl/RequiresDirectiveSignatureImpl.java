package saker.java.compiler.impl.compile.signature.impl;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.java.compiler.impl.signature.element.ModuleSignature.RequiresDirectiveSignature;
import saker.java.compiler.impl.signature.type.NameSignature;

public class RequiresDirectiveSignatureImpl implements RequiresDirectiveSignature, Externalizable {
	private static final long serialVersionUID = 1L;

	private boolean staticDirective;
	private boolean transitiveDirective;
	private NameSignature dependencyModule;

	/**
	 * For {@link Externalizable}.
	 */
	public RequiresDirectiveSignatureImpl() {
	}

	public RequiresDirectiveSignatureImpl(boolean staticDirective, boolean transitiveDirective,
			NameSignature dependencyModule) {
		this.staticDirective = staticDirective;
		this.transitiveDirective = transitiveDirective;
		this.dependencyModule = dependencyModule;
	}

	@Override
	public boolean isStatic() {
		return staticDirective;
	}

	@Override
	public boolean isTransitive() {
		return transitiveDirective;
	}

	@Override
	public NameSignature getDependencyModule() {
		return dependencyModule;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeBoolean(staticDirective);
		out.writeBoolean(transitiveDirective);
		out.writeObject(dependencyModule);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		staticDirective = in.readBoolean();
		transitiveDirective = in.readBoolean();
		dependencyModule = (NameSignature) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dependencyModule == null) ? 0 : dependencyModule.hashCode());
		result = prime * result + (staticDirective ? 1231 : 1237);
		result = prime * result + (transitiveDirective ? 1231 : 1237);
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
		RequiresDirectiveSignatureImpl other = (RequiresDirectiveSignatureImpl) obj;
		if (dependencyModule == null) {
			if (other.dependencyModule != null)
				return false;
		} else if (!dependencyModule.equals(other.dependencyModule))
			return false;
		if (staticDirective != other.staticDirective)
			return false;
		if (transitiveDirective != other.transitiveDirective)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "requires " + (staticDirective ? "static " : "") + (transitiveDirective ? "transitive " : "")
				+ dependencyModule;
	}
}