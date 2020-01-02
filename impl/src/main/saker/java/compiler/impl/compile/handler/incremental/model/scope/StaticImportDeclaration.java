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

public class StaticImportDeclaration implements ImportDeclaration, Externalizable {
	public static final long serialVersionUID = 1L;

	private String path;

	public StaticImportDeclaration() {
	}

	public StaticImportDeclaration(String path) {
		this.path = path;
	}

	@Override
	public String getPath() {
		return path;
	}

	@Override
	public String resolveType(String identifier) {
		if (isStatic()) {
			return null;
		}
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
		if (!isStatic()) {
			return null;
		}
		int lindex = path.lastIndexOf('.');
		String last = path.substring(lindex + 1);
		if ("*".equals(last) || identifier.equals(last)) {
			//wildcard import
			return path.substring(0, lindex);
		}
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
		final int prime = 31;
		int result = 1;
		result = prime * result + ((path == null) ? 0 : path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StaticImportDeclaration other = (StaticImportDeclaration) obj;
		if (path == null) {
			if (other.path != null)
				return false;
		} else if (!path.equals(other.path))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "import static " + path + ";";
	}

	@Override
	public boolean isStatic() {
		return true;
	}

}
