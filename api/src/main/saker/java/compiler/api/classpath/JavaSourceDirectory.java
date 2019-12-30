package saker.java.compiler.api.classpath;

import java.util.Collection;
import java.util.Set;

import saker.build.exception.InvalidPathFormatException;
import saker.build.file.path.SakerPath;
import saker.build.file.path.WildcardPath;
import saker.java.compiler.impl.options.SimpleJavaSourceDirectoryOption;

/**
 * Represents a source directory configuration.
 * <p>
 * The interface provides access to the execution path of the source directory and the files that should be considered.
 * <p>
 * Clients shouldn't implement this interface.
 * <p>
 * Use {@link #create(SakerPath, Collection) create} to create a new instance.
 */
public interface JavaSourceDirectory {
	/**
	 * Gets the absolute build execution path to the source directory.
	 * 
	 * @return The directory path.
	 */
	public SakerPath getDirectory();

	/**
	 * Gets the wildcards that specify the files that are used under the source directory.
	 * <p>
	 * The wildcards are used to match the relative paths of the source files from the {@linkplain #getDirectory()
	 * source directory}.
	 * <p>
	 * If <code>null</code> are specified, then all files are considered that have the <code>.java</code> (ignore-case)
	 * extension. (Note that an empty collection doesn't signal that the defaults should be used.)
	 * 
	 * @return The wildcard for the matching files or <code>null</code> to use the defaults.
	 */
	public Set<? extends WildcardPath> getFiles();

	@Override
	public boolean equals(Object obj);

	@Override
	public int hashCode();

	/**
	 * Creates a new instance for the specified directory.
	 * <p>
	 * Same as:
	 * 
	 * <pre>
	 * {@linkplain #create(SakerPath, Collection) create}(directorypath, null)
	 * </pre>
	 * 
	 * @param directorypath
	 *            The absolute build execution path to the directory.
	 * @return The created configuration.
	 * @throws NullPointerException
	 *             If the directory path is <code>null</code>.
	 * @throws InvalidPathFormatException
	 *             If the path is not absolute.
	 */
	public static JavaSourceDirectory create(SakerPath directorypath)
			throws NullPointerException, InvalidPathFormatException {
		return create(directorypath, null);
	}

	/**
	 * Creates a new instance for the specified directory and wildcard filters.
	 * 
	 * @param directorypath
	 *            The absolute build execution path to the directory.
	 * @param files
	 *            The wildcards that match the source files or <code>null</code> to use the {@linkplain #getFiles()
	 *            defaults}.
	 * @return The created configuration.
	 * @throws NullPointerException
	 *             If the directory path is <code>null</code>.
	 * @throws InvalidPathFormatException
	 *             If the path is not absolute.
	 */
	public static JavaSourceDirectory create(SakerPath directorypath, Collection<? extends WildcardPath> files)
			throws NullPointerException, InvalidPathFormatException {
		return new SimpleJavaSourceDirectoryOption(directorypath, files);
	}
}