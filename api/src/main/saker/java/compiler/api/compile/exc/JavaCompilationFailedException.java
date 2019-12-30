package saker.java.compiler.api.compile.exc;

/**
 * Exception signaling that the Java compilation failed for some reason.
 */
public class JavaCompilationFailedException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * @see Exception#Exception(String, Throwable)
	 */
	public JavaCompilationFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @see Exception#Exception(String)
	 */
	public JavaCompilationFailedException(String message) {
		super(message);
	}

	/**
	 * @see Exception#Exception(Throwable)
	 */
	public JavaCompilationFailedException(Throwable cause) {
		super(cause);
	}

}
