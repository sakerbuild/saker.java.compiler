package saker.java.compiler.main.compile.option;

import saker.build.file.path.SakerPath;
import saker.build.task.TaskContext;
import saker.std.api.file.location.ExecutionFileLocation;

final class RelativePathJavaClassPathTaskOption implements JavaClassPathTaskOption {
	private final SakerPath filepath;

	RelativePathJavaClassPathTaskOption(SakerPath filepath) {
		this.filepath = filepath;
	}

	@Override
	public JavaClassPathTaskOption clone() {
		return this;
	}

	@Override
	public void accept(TaskContext taskcontext, ClassPathTaskOptionVisitor visitor) {
		visitor.visitFileLocation(
				ExecutionFileLocation.create(taskcontext.getTaskWorkingDirectoryPath().tryResolve(filepath)));
	}
}