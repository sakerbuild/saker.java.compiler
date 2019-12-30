package saker.java.compiler.api.classpath;

/**
 * Visitor interface for examining classpath entries.
 * <p>
 * The visitor declares methods for visiting various types of classpath. It can be used by calling
 * {@link JavaClassPath#accept(ClassPathVisitor)} with a custom visitor implementation.
 * <p>
 * All of the declared methods in this interface are <code>default</code> and throw an
 * {@link UnsupportedOperationException} by default. Additional <code>visit</code> methods may be added to this
 * interface with similar default implementations.
 * <p>
 * Clients are recommended to implement this interface.
 */
public interface ClassPathVisitor {
	/**
	 * Visits a class path reference.
	 * 
	 * @param classpath
	 *            The classpath entry.
	 */
	public default void visit(ClassPathReference classpath) {
		throw new UnsupportedOperationException("Unsupported class path: " + classpath);
	}

	/**
	 * Visits a compilation classpath.
	 * 
	 * @param classpath
	 *            The classpath entry.
	 */
	public default void visit(CompilationClassPath classpath) {
		throw new UnsupportedOperationException("Unsupported class path: " + classpath);
	}

	/**
	 * Visits a file classpath.
	 * 
	 * @param classpath
	 *            The classpath entry.
	 */
	public default void visit(FileClassPath classpath) {
		throw new UnsupportedOperationException("Unsupported class path: " + classpath);
	}

	/**
	 * Visits an SDK classpath.
	 * 
	 * @param classpath
	 *            The classpath entry.
	 */
	public default void visit(SDKClassPath classpath) {
		throw new UnsupportedOperationException("Unsupported class path: " + classpath);
	}
}
