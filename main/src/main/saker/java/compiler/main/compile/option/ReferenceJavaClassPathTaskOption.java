package saker.java.compiler.main.compile.option;

import saker.build.task.TaskContext;
import saker.java.compiler.api.classpath.ClassPathReference;

final class ReferenceJavaClassPathTaskOption implements JavaClassPathTaskOption {
	private final ClassPathReference classpath;

	ReferenceJavaClassPathTaskOption(ClassPathReference classpath) {
		this.classpath = classpath;
	}

	@Override
	public JavaClassPathTaskOption clone() {
		return this;
	}

	@Override
	public void accept(TaskContext taskcontext, ClassPathTaskOptionVisitor visitor) {
		visitor.visitClassPathReference(classpath);
	}
}