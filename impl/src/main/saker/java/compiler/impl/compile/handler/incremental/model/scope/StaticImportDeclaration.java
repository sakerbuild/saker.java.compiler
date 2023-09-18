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

import java.io.Externalizable;

import saker.build.util.data.annotation.ValueType;

@ValueType
public final class StaticImportDeclaration extends SimpleImportDeclaration {
	public static final long serialVersionUID = 1L;

	/**
	 * For {@link Externalizable}.
	 */
	public StaticImportDeclaration() {
	}

	public StaticImportDeclaration(String path) {
		super(path);
	}

	@Override
	public boolean isStatic() {
		return true;
	}

	@Override
	public String resolveType(String identifier) {
		return null;
	}

	@Override
	public String resolveMember(String identifier) {
		int lindex = path.lastIndexOf('.');
		String last = path.substring(lindex + 1);
		if ("*".equals(last) || identifier.equals(last)) {
			//wildcard import
			return path.substring(0, lindex);
		}
		return null;
	}

	@Override
	public String toString() {
		return "import static " + path + ";";
	}
}
