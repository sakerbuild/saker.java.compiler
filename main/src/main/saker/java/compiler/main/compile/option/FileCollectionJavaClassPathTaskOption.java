package saker.java.compiler.main.compile.option;

import saker.build.task.TaskContext;
import saker.std.api.file.location.FileCollection;
import saker.std.api.file.location.FileLocation;

final class FileCollectionJavaClassPathTaskOption implements JavaClassPathTaskOption {
	private final FileCollection fileCollection;

	FileCollectionJavaClassPathTaskOption(FileCollection fileCollection) {
		this.fileCollection = fileCollection;
	}

	@Override
	public JavaClassPathTaskOption clone() {
		return this;
	}

	@Override
	public void accept(TaskContext taskcontext, ClassPathTaskOptionVisitor visitor) {
		for (FileLocation fl : fileCollection) {
			visitor.visitFileLocation(fl);
		}
	}
}