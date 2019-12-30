package saker.java.compiler.main.compile.option;

import saker.build.task.TaskContext;
import saker.std.api.file.location.FileLocation;

final class FileLocationJavaClassPathTaskOption implements JavaClassPathTaskOption {
	private final FileLocation fileLocation;

	FileLocationJavaClassPathTaskOption(FileLocation filelocation) {
		this.fileLocation = filelocation;
	}

	@Override
	public JavaClassPathTaskOption clone() {
		return this;
	}

	@Override
	public void accept(TaskContext taskcontext, ClassPathTaskOptionVisitor visitor) {
		visitor.visitFileLocation(fileLocation);
	}
}