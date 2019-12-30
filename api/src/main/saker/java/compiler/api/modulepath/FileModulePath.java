package saker.java.compiler.api.modulepath;

import saker.std.api.file.location.FileLocation;

/**
 * Represents a modulepath entry that is backed by a file location.
 * <p>
 * The backing file may locate a JAR or directory, the interface doesn't specify a requirement on its nature.
 * <p>
 * Clients shouldn't implement this interface.
 * 
 * @see JavaModulePath
 * @see ModulePathVisitor
 * @see JavaModulePathBuilder#addFileModulePath(FileLocation)
 */
public interface FileModulePath {
	/**
	 * Gets the file of this modulepath.
	 * 
	 * @return The file location.
	 */
	public FileLocation getFileLocation();
}
