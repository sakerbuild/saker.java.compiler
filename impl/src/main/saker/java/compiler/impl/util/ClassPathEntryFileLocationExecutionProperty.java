package saker.java.compiler.impl.util;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.ExecutionProperty;
import saker.java.compiler.api.classpath.ClassPathEntry;
import saker.std.api.file.location.FileLocation;

public class ClassPathEntryFileLocationExecutionProperty implements ExecutionProperty<FileLocation>, Externalizable {
	private static final long serialVersionUID = 1L;

	private ClassPathEntry entry;

	/**
	 * For {@link Externalizable}.
	 */
	public ClassPathEntryFileLocationExecutionProperty() {
	}

	public ClassPathEntryFileLocationExecutionProperty(ClassPathEntry entry) {
		this.entry = entry;
	}

	@Override
	public FileLocation getCurrentValue(ExecutionContext executioncontext) throws Exception {
		return entry.getFileLocation();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(entry);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		entry = (ClassPathEntry) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((entry == null) ? 0 : entry.hashCode());
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
		ClassPathEntryFileLocationExecutionProperty other = (ClassPathEntryFileLocationExecutionProperty) obj;
		if (entry == null) {
			if (other.entry != null)
				return false;
		} else if (!entry.equals(other.entry))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (entry != null ? "entry=" + entry : "") + "]";
	}

}
