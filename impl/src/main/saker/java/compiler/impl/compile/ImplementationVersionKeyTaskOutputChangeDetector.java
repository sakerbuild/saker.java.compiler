package saker.java.compiler.impl.compile;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

import saker.build.task.dependencies.TaskOutputChangeDetector;
import saker.java.compiler.api.compile.JavaCompilerWorkerTaskOutput;

public class ImplementationVersionKeyTaskOutputChangeDetector implements TaskOutputChangeDetector, Externalizable {
	private static final long serialVersionUID = 1L;

	protected Object implementationVersionKey;

	/**
	 * For {@link Externalizable}.
	 */
	public ImplementationVersionKeyTaskOutputChangeDetector() {
	}

	public ImplementationVersionKeyTaskOutputChangeDetector(Object implementationVersionKey) {
		Objects.requireNonNull(implementationVersionKey, "implementationbersionkey");
		this.implementationVersionKey = implementationVersionKey;
	}

	@Override
	public boolean isChanged(Object taskoutput) {
		JavaCompilerWorkerTaskOutput compileroutput = (JavaCompilerWorkerTaskOutput) taskoutput;
		Object currentversionkey = compileroutput.getImplementationVersionKey();
		return currentversionkey == null || !currentversionkey.equals(implementationVersionKey);
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(implementationVersionKey);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		implementationVersionKey = in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((implementationVersionKey == null) ? 0 : implementationVersionKey.hashCode());
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
		ImplementationVersionKeyTaskOutputChangeDetector other = (ImplementationVersionKeyTaskOutputChangeDetector) obj;
		if (implementationVersionKey == null) {
			if (other.implementationVersionKey != null)
				return false;
		} else if (!implementationVersionKey.equals(other.implementationVersionKey))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "["
				+ (implementationVersionKey != null ? "implementationVersionKey=" + implementationVersionKey : "")
				+ "]";
	}
}
