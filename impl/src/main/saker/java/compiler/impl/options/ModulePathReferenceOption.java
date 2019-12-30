package saker.java.compiler.impl.options;

import saker.java.compiler.api.modulepath.ModulePathVisitor;

public interface ModulePathReferenceOption {
	public void accept(ModulePathVisitor visitor);

	@Override
	public boolean equals(Object obj);

	@Override
	public int hashCode();
}
