package saker.java.compiler.main.compile.option;

import saker.build.file.path.WildcardPath;
import saker.build.task.TaskContext;

final class WildcardPathJavaClassPathTaskOption implements JavaClassPathTaskOption {
	private final WildcardPath path;

	WildcardPathJavaClassPathTaskOption(WildcardPath path) {
		this.path = path;
	}

	@Override
	public JavaClassPathTaskOption clone() {
		return this;
	}

	@Override
	public void accept(TaskContext taskcontext, ClassPathTaskOptionVisitor visitor) {
		visitor.visitWildcard(path);
	}
}