package saker.java.compiler.impl.compile;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collections;
import java.util.Set;

import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.java.compiler.api.compile.JavaCompilationConfigurationOutput;

public class JavaCompilationConfigLiteralTaskFactory implements TaskFactory<JavaCompilationConfigurationOutput>,
		Task<JavaCompilationConfigurationOutput>, Externalizable {
	private static final long serialVersionUID = 1L;

	private JavaCompilationConfigurationOutput outputConfiguration;

	/**
	 * For {@link Externalizable}.
	 */
	public JavaCompilationConfigLiteralTaskFactory() {
	}

	public JavaCompilationConfigLiteralTaskFactory(JavaCompilationConfigurationOutput outputConfiguration) {
		this.outputConfiguration = outputConfiguration;
	}

	@Override
	public Set<String> getCapabilities() {
		return Collections.singleton(CAPABILITY_SHORT_TASK);
	}

	@Override
	public Task<? extends JavaCompilationConfigurationOutput> createTask(ExecutionContext executioncontext) {
		return this;
	}

	@Override
	public JavaCompilationConfigurationOutput run(TaskContext taskcontext) throws Exception {
		taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(outputConfiguration));
		return outputConfiguration;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(outputConfiguration);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		outputConfiguration = (JavaCompilationConfigurationOutput) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((outputConfiguration == null) ? 0 : outputConfiguration.hashCode());
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
		JavaCompilationConfigLiteralTaskFactory other = (JavaCompilationConfigLiteralTaskFactory) obj;
		if (outputConfiguration == null) {
			if (other.outputConfiguration != null)
				return false;
		} else if (!outputConfiguration.equals(other.outputConfiguration))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "JavaCompilationConfigLiteralTaskFactory["
				+ (outputConfiguration != null ? "outputConfiguration=" + outputConfiguration : "") + "]";
	}

}
