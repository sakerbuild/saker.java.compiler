package saker.java.compiler.api.modulepath;

/**
 * Represents a modulepath configuration for the JVM.
 * <p>
 * The interface represents a collection of modulepath configuration. It is a heterogeneous collection of modulepath
 * objects.
 * <p>
 * The modulepath entries may be enumerated by calling {@link #accept(ModulePathVisitor)} with a custom visitor
 * implementation. The possible modulepath types and their properties can be accessed by overriding the appropriate
 * <code>visit</code> method.
 * <p>
 * Clients shouldn't implement this interface.
 * <p>
 * Create a new instance using {@link JavaModulePathBuilder}.
 * 
 * @see ModulePathVisitor
 */
public interface JavaModulePath {
	/**
	 * Checks if this modulepath configuration contains any entries.
	 * 
	 * @return <code>true</code> if this modulepath is empty.
	 */
	public boolean isEmpty();

	/**
	 * Calls the argument visitor for all the enclosed entries in this modulepath.
	 * <p>
	 * If this modulepath is {@linkplain #isEmpty() empty}, then the argument visitor will not be called at all.
	 * <p>
	 * The visitor may be called multiple times if this modulepath contains multiple entries.
	 * 
	 * @param visitor
	 *            The visitor.
	 * @throws NullPointerException
	 *             If the visitor is <code>null</code>.
	 */
	public void accept(ModulePathVisitor visitor) throws NullPointerException;

	@Override
	public int hashCode();

	@Override
	public boolean equals(Object obj);

}