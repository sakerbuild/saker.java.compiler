package saker.java.compiler.impl.compile.handler.incremental.model.scope;

public interface ImportDeclaration extends Comparable<ImportDeclaration> {
	public String getPath();

	public boolean isStatic();

	/**
	 * Tries to resolve the identifier against this import declaration. Returns with the resolved name. In case of
	 * wildcard imports it always returns a value. In case of specific import it only returns non-null when the
	 * identifier matches the last part of the path;
	 * 
	 * @param identifier
	 *            The identifier to resolve.
	 * @return The resolved qualified name or null.
	 */
	public String resolveType(String identifier);

	/**
	 * Tries to resolve the identifier against this import declaration. Returns with the resolved type qualified name.
	 * In case of wildcard imports it always returns a value. In case of specific import it only returns non-null when
	 * the identifier matches the last part of the path;
	 * 
	 * @param identifier
	 *            The identifier to search for.
	 * @return The type canonical name which can contain the identifier.
	 */
	public String resolveMember(String identifier);

	@Override
	public default int compareTo(ImportDeclaration o) {
		int cmp = Boolean.compare(isStatic(), o.isStatic());
		if (cmp != 0) {
			return cmp;
		}
		return getPath().compareTo(o.getPath());
	}

	public default boolean isWildcard() {
		return getPath().endsWith(".*");
	}

	@Override
	public boolean equals(Object obj);

	@Override
	public int hashCode();
}