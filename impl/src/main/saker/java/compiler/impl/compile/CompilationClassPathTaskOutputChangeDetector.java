package saker.java.compiler.impl.compile;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

import saker.build.task.dependencies.TaskOutputChangeDetector;
import saker.java.compiler.api.classpath.JavaClassPath;
import saker.java.compiler.api.compile.JavaCompilerWorkerTaskOutput;

public class CompilationClassPathTaskOutputChangeDetector implements TaskOutputChangeDetector, Externalizable {
	private static final long serialVersionUID = 1L;

	protected JavaClassPath classPath;

	/**
	 * For {@link Externalizable}.
	 */
	public CompilationClassPathTaskOutputChangeDetector() {
	}

	public CompilationClassPathTaskOutputChangeDetector(JavaClassPath classPath) {
		this.classPath = classPath;
	}

	@Override
	public boolean isChanged(Object taskoutput) {
		JavaCompilerWorkerTaskOutput compileroutput = (JavaCompilerWorkerTaskOutput) taskoutput;
		return !Objects.equals(classPath, compileroutput.getClassPath());
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(classPath);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		classPath = (JavaClassPath) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((classPath == null) ? 0 : classPath.hashCode());
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
		CompilationClassPathTaskOutputChangeDetector other = (CompilationClassPathTaskOutputChangeDetector) obj;
		if (classPath == null) {
			if (other.classPath != null)
				return false;
		} else if (!classPath.equals(other.classPath))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + classPath + "]";
	}
}
