package saker.java.compiler.api.classpath;

import saker.java.compiler.api.compile.JavaCompilationWorkerTaskIdentifier;
import saker.sdk.support.api.SDKPathReference;
import saker.std.api.file.location.FileLocation;

/**
 * Builder interface for {@link JavaClassPath}.
 * <p>
 * Clients shouldn't implement this interface.
 * <p>
 * Create a new instance using {@link #newBuilder()}.
 */
public interface JavaClassPathBuilder {
	/**
	 * Adds a class path reference to the builder.
	 * 
	 * @param classpathref
	 *            The reference.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 * @see ClassPathReference
	 */
	public void addClassPath(ClassPathReference classpathref) throws NullPointerException;

	/**
	 * Adds a compilation classpath to the builder.
	 * 
	 * @param compilationworkertaskid
	 *            The task identifier of the compilation worker build task.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 * @see CompilationClassPath
	 */
	public void addCompileClassPath(JavaCompilationWorkerTaskIdentifier compilationworkertaskid)
			throws NullPointerException;

	/**
	 * Adds a file classpath to the builder.
	 * 
	 * @param filelocation
	 *            The file location of the classpath.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 * @see FileClassPath
	 */
	public void addFileClassPath(FileLocation filelocation) throws NullPointerException;

	/**
	 * Adds an SDK classpath to the builder.
	 * 
	 * @param sdkpathreference
	 *            The SDK path reference.
	 * @throws NullPointerException
	 *             If the argument is <code>null</code>.
	 * @see SDKClassPath
	 */
	public void addSDKClassPath(SDKPathReference sdkpathreference) throws NullPointerException;

	/**
	 * Builds the classpath.
	 * <p>
	 * The builder can be reused after this call.
	 * 
	 * @return The created classpath.
	 */
	public JavaClassPath build();

	/**
	 * Creates a new builder.
	 * 
	 * @return The builder.
	 */
	public static JavaClassPathBuilder newBuilder() {
		return new JavaClassPathBuilderImpl();
	}
}