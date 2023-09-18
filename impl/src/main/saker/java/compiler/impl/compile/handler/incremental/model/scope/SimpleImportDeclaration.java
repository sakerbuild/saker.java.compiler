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
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

import saker.build.util.data.annotation.ValueType;

@ValueType
public class SimpleImportDeclaration implements ImportDeclaration, Externalizable {
	public static final long serialVersionUID = 1L;

	protected String path;

	/**
	 * For {@link Externalizable}.
	 */
	public SimpleImportDeclaration() {
	}

	public SimpleImportDeclaration(String path) {
		this.path = path;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public boolean isStatic() {
		return false;
	}

	@Override
	public String resolveType(String identifier) {
		int lindex = path.lastIndexOf('.');
		String last = path.substring(lindex + 1);
		if ("*".equals(last)) {
			//wildcard import
			return path.substring(0, lindex + 1) + identifier;
		}
		if (identifier.equals(last)) {
			return path;
		}
		return null;
	}

	@Override
	public String resolveMember(String identifier) {
		return null;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(path);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		path = in.readUTF();
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(path);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleImportDeclaration other = (SimpleImportDeclaration) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "import " + path + ";";
	}
}
