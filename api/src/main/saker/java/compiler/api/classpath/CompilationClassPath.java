package saker.java.compiler.api.classpath;

import saker.java.compiler.api.compile.JavaCompilationWorkerTaskIdentifier;

/**
 * Represents a classpath entry that is backed by the output of a Java compilation task.
 * <p>
 * The interface encloses the task identifier of the compiler task that can be used to retrieve the result of the
 * compilation.
 * <p>
 * The consumers of the compilation classpath may or may not include transitive classpaths that are configured as an
 * input to the compilation. Usually, transitive classpaths are included unless otherwise noted.
 * <p>
 * Clients shouldn't implement this interface.
 * 
 * @see JavaClassPath
 * @see ClassPathVisitor
 * @see JavaClassPathBuilder#addCompileClassPath(JavaCompilationWorkerTaskIdentifier)
 */
public interface CompilationClassPath {
	/**
	 * Gets the task identifier of the Java compiler task that performs the compilation.
	 * 
	 * @return The task identifier.
	 */
	public JavaCompilationWorkerTaskIdentifier getCompilationWorkerTaskIdentifier();
}
