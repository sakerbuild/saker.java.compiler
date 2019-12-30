package saker.java.compiler.impl.options;

import saker.java.compiler.api.classpath.ClassPathVisitor;

public interface ClassPathReferenceOption {
	public void accept(ClassPathVisitor visitor);

	@Override
	public boolean equals(Object obj);

	@Override
	public int hashCode();

}