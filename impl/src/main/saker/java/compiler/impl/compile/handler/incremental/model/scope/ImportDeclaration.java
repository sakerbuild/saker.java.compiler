/*
 * Copyright (C) 2020 Bence Sipka
 *
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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