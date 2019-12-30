package saker.java.compiler.api.modulepath;

import saker.java.compiler.api.compile.JavaCompilationWorkerTaskIdentifier;

/**
 * Represents a modulepath entry that is backed by the output of a Java compilation task.
 * <p>
 * The interface encloses the task identifier of the compiler task that can be used to retrieve the result of the
 * compilation.
 * <p>
 * The consumers of the compilation modulepath may or may not include transitive modulepaths that are configured as an
 * input to the compilation. Usually, transitive modulepaths are included unless otherwise noted.
 * <p>
 * Clients shouldn't implement this interface.
 * 
 * @see JavaModulePath
 * @see ModulePathVisitor
 * @see JavaModulePathBuilder#addCompileModulePath(JavaCompilationWorkerTaskIdentifier)
 */
public interface CompilationModulePath {
	/**
	 * Gets the task identifier of the Java compiler task that performs the compilation.
	 * 
	 * @return The task identifier.
	 */
	public JavaCompilationWorkerTaskIdentifier getCompilationWorkerTaskIdentifier();
}
