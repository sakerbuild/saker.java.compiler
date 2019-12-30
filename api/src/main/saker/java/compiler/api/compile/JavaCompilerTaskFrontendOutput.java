package saker.java.compiler.api.compile;

import saker.build.file.path.SakerPath;
import saker.build.task.utils.StructuredTaskResult;
import saker.sdk.support.api.SDKDescription;

/**
 * Interface for accessing the results of the frontend build task of the Java compiler task.
 * <p>
 * The frontend task is the one that is instantiated for the build scripts and parses the input arguments. It spawns the
 * worker task that performs the actual compilation.
 * <p>
 * The task provides access to the worker task identifier, the Java SDK that is used by the worker task, and the output
 * locations as structured task results.
 * <p>
 * Users of this interface can access some aspects of the compilation configuration without depending on the worker task
 * and the compilation result. (E.g. the Java SDK that is used.)
 * <p>
 * Clients shouldn't implement this interface.
 */
public interface JavaCompilerTaskFrontendOutput {
	/**
	 * Gets the worker task identifier that performs the compilation.
	 * 
	 * @return The task identifier.
	 */
	public JavaCompilationWorkerTaskIdentifier getTaskIdentifier();

	/**
	 * Gets the SDK description that is used to perform the compilation.
	 * <p>
	 * This is the {@link SDKDescription} that is associated with the <code>Java</code> name.
	 * 
	 * @return The SDK description.
	 */
	public SDKDescription getJavaSDK();

	//SakerPath
	/**
	 * Gets the structured task result for the class output directory of the compilation.
	 * <p>
	 * The result will be a {@link SakerPath}.
	 * <p>
	 * Retrieving the task result will wait for the compilation to be done, but doesn't install a dependency on the
	 * results of the compilation. (I.e. compiled class file contents and others.)
	 * 
	 * @return The class output directory task result.
	 * @see JavaCompilerWorkerTaskOutput#getClassDirectory()
	 * @see SakerJavaCompilerUtils#DIR_CLASS_OUTPUT
	 */
	public StructuredTaskResult getClassDirectory();

	//SakerPath
	/**
	 * Gets the structured task result for the header output directory of the compilation.
	 * <p>
	 * The result will be a {@link SakerPath}.
	 * <p>
	 * Retrieving the task result will wait for the compilation to be done, but doesn't install a dependency on the
	 * results of the compilation. (I.e. compiled class file contents and others.)
	 * 
	 * @return The header output directory task result.
	 * @see JavaCompilerWorkerTaskOutput#getClassDirectory()
	 * @see SakerJavaCompilerUtils#DIR_NATIVE_HEADER_OUTPUT
	 */
	public StructuredTaskResult getHeaderDirectory();

	//SakerPath
	/**
	 * Gets the structured task result for the resource output directory of the compilation.
	 * <p>
	 * The result will be a {@link SakerPath}.
	 * <p>
	 * Retrieving the task result will wait for the compilation to be done, but doesn't install a dependency on the
	 * results of the compilation. (I.e. compiled class file contents and others.)
	 * 
	 * @return The resource directory task result.
	 * @see JavaCompilerWorkerTaskOutput#getClassDirectory()
	 * @see SakerJavaCompilerUtils#DIR_RESOURCE_OUTPUT
	 */
	public StructuredTaskResult getResourceDirectory();

	//SakerPath
	/**
	 * Gets the structured task result for the soruce output directory of the compilation.
	 * <p>
	 * The result will be a {@link SakerPath}.
	 * <p>
	 * Retrieving the task result will wait for the compilation to be done, but doesn't install a dependency on the
	 * results of the compilation. (I.e. compiled class file contents and others.)
	 * 
	 * @return The soruce output directory task result.
	 * @see JavaCompilerWorkerTaskOutput#getClassDirectory()
	 * @see SakerJavaCompilerUtils#DIR_SOURCE_OUTPUT
	 */
	public StructuredTaskResult getSourceGenDirectory();

	//String
	/**
	 * Gets the structured task result for the compile module name of the compilation.
	 * <p>
	 * The result will be a {@link String} or <code>null</code>.
	 * <p>
	 * Retrieving the task result will wait for the compilation to be done, but doesn't install a dependency on the
	 * results of the compilation (only the module name). (I.e. compiled class file contents and others.)
	 * 
	 * @return The module name or <code>null</code> if no module was compiled.
	 * @see JavaCompilerWorkerTaskOutput#getModuleName()
	 */
	public StructuredTaskResult getModuleName();

}
