package saker.java.compiler.impl.compile.handler;

import java.io.IOException;
import java.nio.file.Path;
import java.util.NavigableMap;
import java.util.NavigableSet;

import saker.build.exception.FileMirroringUnavailableException;
import saker.build.file.DirectoryVisitPredicate;
import saker.build.file.SakerDirectory;
import saker.build.file.SakerFile;
import saker.build.file.path.SakerPath;
import saker.build.task.TaskContext;
import saker.java.compiler.api.processor.ProcessorCreationContext;

public class ProcessorCreationContextImpl implements ProcessorCreationContext {
	private TaskContext taskContext;

	public ProcessorCreationContextImpl(TaskContext taskContext) {
		this.taskContext = taskContext;
	}

	@Override
	public SakerDirectory getExecutionWorkingDirectory() {
		return taskContext.getExecutionContext().getExecutionWorkingDirectory();
	}

	@Override
	public SakerDirectory getExecutionBuildDirectory() {
		return taskContext.getExecutionContext().getExecutionBuildDirectory();
	}

	@Override
	public NavigableMap<String, ? extends SakerDirectory> getRootDirectories() {
		return taskContext.getExecutionContext().getRootDirectories();
	}

	@Override
	public Path mirror(SakerFile file, DirectoryVisitPredicate synchpredicate)
			throws IOException, NullPointerException, FileMirroringUnavailableException {
		return taskContext.mirror(file, synchpredicate);
	}

	@Override
	public SakerPath getExecutionWorkingDirectoryPath() {
		return taskContext.getExecutionContext().getExecutionWorkingDirectoryPath();
	}

	@Override
	public SakerPath getExecutionBuildDirectoryPath() {
		return taskContext.getExecutionContext().getExecutionBuildDirectoryPath();
	}

	@Override
	public NavigableSet<String> getRootDirectoryNames() {
		return taskContext.getExecutionContext().getRootDirectoryNames();
	}
}