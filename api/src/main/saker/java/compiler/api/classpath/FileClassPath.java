package saker.java.compiler.api.classpath;

import saker.std.api.file.location.FileLocation;

/**
 * Represents a classpath entry that is backed by a file location.
 * <p>
 * The backing file may locate a JAR or directory, the interface doesn't specify a requirement on its nature.
 * <p>
 * Clients shouldn't implement this interface.
 * 
 * @see JavaClassPath
 * @see ClassPathVisitor
 * @see JavaClassPathBuilder#addFileClassPath(FileLocation)
 */
public interface FileClassPath {
	/**
	 * Gets the file of this classpath.
	 * 
	 * @return The file location.
	 */
	public FileLocation getFileLocation();
}
