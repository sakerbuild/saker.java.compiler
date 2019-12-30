package saker.java.compiler.impl.compile.handler.incremental.model.scope;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.NavigableSet;

public class EmptyImportScope implements ImportScope, Externalizable {
	private static final long serialVersionUID = 1L;

	public static final ImportScope EMPTY_SCOPE_INSANCE = new EmptyImportScope(null);

	private String packageName;

	public EmptyImportScope() {
	}

	public EmptyImportScope(String packageName) {
		this.packageName = packageName;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(packageName);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		packageName = (String) in.readObject();
	}

	@Override
	public String getPackageName() {
		return packageName;
	}

	@Override
	public NavigableSet<? extends ImportDeclaration> getImportDeclarations() {
		return Collections.emptyNavigableSet();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((packageName == null) ? 0 : packageName.hashCode());
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
		EmptyImportScope other = (EmptyImportScope) obj;
		if (packageName == null) {
			if (other.packageName != null)
				return false;
		} else if (!packageName.equals(other.packageName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "EmptyImportScope [" + (packageName != null ? "packageName=" + packageName : "") + "]";
	}

}
