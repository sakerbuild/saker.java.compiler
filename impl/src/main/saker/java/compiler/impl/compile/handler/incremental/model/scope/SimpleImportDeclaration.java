package saker.java.compiler.impl.compile.handler.incremental.model.scope;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class SimpleImportDeclaration implements ImportDeclaration, Externalizable {
	public static final long serialVersionUID = 1L;

	private String path;

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

	@Override
	public boolean isStatic() {
		return false;
	}

}
