package saker.java.compiler.api.processor;

import java.io.IOException;
import java.nio.file.Path;

import saker.build.exception.FileMirroringUnavailableException;
import saker.build.file.DirectoryVisitPredicate;
import saker.build.file.SakerFile;
import saker.build.runtime.execution.ExecutionDirectoryContext;
import saker.build.task.TaskContext;

/**
 * Context interface provided to {@link ProcessorCreator}s when the annotation processors are being instantiated.
 * <p>
 * The context provides access to some features of the build environment in order for processor creators to be able to
 * perform some file management related operations.
 * <p>
 * The interface doesn't provide any dependency management related functionality. If a processor creator needs to report
 * dependencies then it should do that when it is being constructed.
 * <p>
 * Clients shouldn't implement this interface.
 */
public interface ProcessorCreationContext extends ExecutionDirectoryContext {
	/**
	 * @see TaskContext#mirror(SakerFile, DirectoryVisitPredicate)
	 */
	public Path mirror(SakerFile file, DirectoryVisitPredicate synchpredicate)
			throws IOException, NullPointerException, FileMirroringUnavailableException;

	/**
	 * @see TaskContext#mirror(SakerFile)
	 */
	public default Path mirror(SakerFile file)
			throws IOException, NullPointerException, FileMirroringUnavailableException {
		return mirror(file, DirectoryVisitPredicate.everything());
	}
}
