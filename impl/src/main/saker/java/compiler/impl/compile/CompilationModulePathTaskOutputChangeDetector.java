package saker.java.compiler.impl.compile;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

import saker.build.task.dependencies.TaskOutputChangeDetector;
import saker.java.compiler.api.compile.JavaCompilerWorkerTaskOutput;
import saker.java.compiler.api.modulepath.JavaModulePath;

public class CompilationModulePathTaskOutputChangeDetector implements TaskOutputChangeDetector, Externalizable {
	private static final long serialVersionUID = 1L;

	protected JavaModulePath modulePath;

	/**
	 * For {@link Externalizable}.
	 */
	public CompilationModulePathTaskOutputChangeDetector() {
	}

	public CompilationModulePathTaskOutputChangeDetector(JavaModulePath modulePath) {
		this.modulePath = modulePath;
	}

	@Override
	public boolean isChanged(Object taskoutput) {
		JavaCompilerWorkerTaskOutput compileroutput = (JavaCompilerWorkerTaskOutput) taskoutput;
		return !Objects.equals(modulePath, compileroutput.getModulePath());
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(modulePath);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		modulePath = (JavaModulePath) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((modulePath == null) ? 0 : modulePath.hashCode());
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
		CompilationModulePathTaskOutputChangeDetector other = (CompilationModulePathTaskOutputChangeDetector) obj;
		if (modulePath == null) {
			if (other.modulePath != null)
				return false;
		} else if (!modulePath.equals(other.modulePath))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + modulePath + "]";
	}
}
