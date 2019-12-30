package saker.java.compiler.main.compile;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import saker.build.task.TaskResultResolver;
import saker.build.task.identifier.TaskIdentifier;
import saker.build.task.utils.SimpleStructuredObjectTaskResult;
import saker.build.task.utils.StructuredTaskResult;
import saker.java.compiler.api.compile.JavaCompilationConfigurationOutput;
import saker.java.compiler.api.compile.JavaCompilationWorkerTaskIdentifier;
import saker.java.compiler.api.compile.JavaCompilerTaskFrontendOutput;
import saker.java.compiler.impl.JavaTaskUtils;
import saker.sdk.support.api.SDKDescription;

public class JavaCompilerTaskFrontendOutputImpl extends SimpleStructuredObjectTaskResult
		implements JavaCompilerTaskFrontendOutput {
	private static final long serialVersionUID = 1L;

	private SDKDescription javaSDK;

	/**
	 * For {@link Externalizable}.
	 */
	public JavaCompilerTaskFrontendOutputImpl() {
	}

	public JavaCompilerTaskFrontendOutputImpl(JavaCompilationWorkerTaskIdentifier taskIdentifier,
			SDKDescription javaSDK) {
		super(taskIdentifier);
		this.javaSDK = javaSDK;
	}

	@Override
	public JavaCompilationWorkerTaskIdentifier getTaskIdentifier() {
		return (JavaCompilationWorkerTaskIdentifier) super.getTaskIdentifier();
	}

	@Override
	public SDKDescription getJavaSDK() {
		return javaSDK;
	}

	@Override
	public StructuredTaskResult getClassDirectory() {
		return new ClassDirectoryConfigResolverTaskResult(
				JavaTaskUtils.createJavaCompilationConfigurationOutputTaskIdentifier(getTaskIdentifier()));
	}

	@Override
	public StructuredTaskResult getHeaderDirectory() {
		return new HeaderDirectoryConfigResolverTaskResult(
				JavaTaskUtils.createJavaCompilationConfigurationOutputTaskIdentifier(getTaskIdentifier()));
	}

	@Override
	public StructuredTaskResult getResourceDirectory() {
		return new ResourceDirectoryConfigResolverTaskResult(
				JavaTaskUtils.createJavaCompilationConfigurationOutputTaskIdentifier(getTaskIdentifier()));
	}

	@Override
	public StructuredTaskResult getSourceGenDirectory() {
		return new SourceGenDirectoryConfigResolverTaskResult(
				JavaTaskUtils.createJavaCompilationConfigurationOutputTaskIdentifier(getTaskIdentifier()));
	}

	@Override
	public StructuredTaskResult getModuleName() {
		return new ModuleNameConfigResolverTaskResult(
				JavaTaskUtils.createJavaCompilationConfigurationOutputTaskIdentifier(getTaskIdentifier()));
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeObject(javaSDK);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		javaSDK = (SDKDescription) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((javaSDK == null) ? 0 : javaSDK.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		JavaCompilerTaskFrontendOutputImpl other = (JavaCompilerTaskFrontendOutputImpl) obj;
		if (javaSDK == null) {
			if (other.javaSDK != null)
				return false;
		} else if (!javaSDK.equals(other.javaSDK))
			return false;
		return true;
	}

	public static final class ClassDirectoryConfigResolverTaskResult extends ConfigFieldResolverStructuredTaskResult {
		private static final long serialVersionUID = 1L;

		/**
		 * For {@link Externalizable}.
		 */
		public ClassDirectoryConfigResolverTaskResult() {
		}

		ClassDirectoryConfigResolverTaskResult(TaskIdentifier configOutputTaskId) {
			super(configOutputTaskId);
		}

		@Override
		protected Object resolveField(JavaCompilationConfigurationOutput config) {
			return config.getClassDirectory();
		}
	}

	public static final class HeaderDirectoryConfigResolverTaskResult extends ConfigFieldResolverStructuredTaskResult {
		private static final long serialVersionUID = 1L;

		/**
		 * For {@link Externalizable}.
		 */
		public HeaderDirectoryConfigResolverTaskResult() {
		}

		HeaderDirectoryConfigResolverTaskResult(TaskIdentifier configOutputTaskId) {
			super(configOutputTaskId);
		}

		@Override
		protected Object resolveField(JavaCompilationConfigurationOutput config) {
			return config.getHeaderDirectory();
		}
	}

	public static final class ResourceDirectoryConfigResolverTaskResult extends ConfigFieldResolverStructuredTaskResult {
		private static final long serialVersionUID = 1L;

		/**
		 * For {@link Externalizable}.
		 */
		public ResourceDirectoryConfigResolverTaskResult() {
		}

		ResourceDirectoryConfigResolverTaskResult(TaskIdentifier configOutputTaskId) {
			super(configOutputTaskId);
		}

		@Override
		protected Object resolveField(JavaCompilationConfigurationOutput config) {
			return config.getResourceDirectory();
		}
	}

	public static final class SourceGenDirectoryConfigResolverTaskResult extends ConfigFieldResolverStructuredTaskResult {
		private static final long serialVersionUID = 1L;

		/**
		 * For {@link Externalizable}.
		 */
		public SourceGenDirectoryConfigResolverTaskResult() {
		}

		SourceGenDirectoryConfigResolverTaskResult(TaskIdentifier configOutputTaskId) {
			super(configOutputTaskId);
		}

		@Override
		protected Object resolveField(JavaCompilationConfigurationOutput config) {
			return config.getSourceGenDirectory();
		}
	}

	public static final class ModuleNameConfigResolverTaskResult extends ConfigFieldResolverStructuredTaskResult {
		private static final long serialVersionUID = 1L;

		/**
		 * For {@link Externalizable}.
		 */
		public ModuleNameConfigResolverTaskResult() {
		}

		ModuleNameConfigResolverTaskResult(TaskIdentifier configOutputTaskId) {
			super(configOutputTaskId);
		}

		@Override
		protected Object resolveField(JavaCompilationConfigurationOutput config) {
			return config.getModuleName();
		}
	}

	private static abstract class ConfigFieldResolverStructuredTaskResult
			implements StructuredTaskResult, Externalizable {
		private static final long serialVersionUID = 1L;

		private TaskIdentifier configOutputTaskId;

		/**
		 * For {@link Externalizable}.
		 */
		public ConfigFieldResolverStructuredTaskResult() {
		}

		public ConfigFieldResolverStructuredTaskResult(TaskIdentifier configOutputTaskId) {
			this.configOutputTaskId = configOutputTaskId;
		}

		@Override
		public Object toResult(TaskResultResolver results) {
			Object output = results.getTaskResult(configOutputTaskId);
			if (!(output instanceof JavaCompilationConfigurationOutput)) {
				throw new IllegalStateException("Invalid Java compilation output for task identifier: "
						+ configOutputTaskId + " with " + output);
			}
			JavaCompilationConfigurationOutput res = (JavaCompilationConfigurationOutput) output;
			//XXX report dependency on the task result?
			return resolveField(res);
		}

		protected abstract Object resolveField(JavaCompilationConfigurationOutput config);

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(configOutputTaskId);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			configOutputTaskId = (TaskIdentifier) in.readObject();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((configOutputTaskId == null) ? 0 : configOutputTaskId.hashCode());
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
			ConfigFieldResolverStructuredTaskResult other = (ConfigFieldResolverStructuredTaskResult) obj;
			if (configOutputTaskId == null) {
				if (other.configOutputTaskId != null)
					return false;
			} else if (!configOutputTaskId.equals(other.configOutputTaskId))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "[" + configOutputTaskId + "]";
		}

	}
}
