package saker.java.compiler.main.compile.option;

import saker.build.task.TaskContext;
import saker.java.compiler.impl.compile.InternalJavaCompilerOutput;

final class CompilationOutputJavaClassPathTaskOption implements JavaClassPathTaskOption {
	private final InternalJavaCompilerOutput output;

	CompilationOutputJavaClassPathTaskOption(InternalJavaCompilerOutput output) {
		this.output = output;
	}

	@Override
	public JavaClassPathTaskOption clone() {
		return this;
	}

	@Override
	public void accept(TaskContext taskcontext, ClassPathTaskOptionVisitor visitor) {
		visitor.visitCompileClassPath(output.getCompilationTaskIdentifier());
	}
}