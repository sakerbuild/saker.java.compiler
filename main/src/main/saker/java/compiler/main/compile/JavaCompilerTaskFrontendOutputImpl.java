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
package saker.java.compiler.main.compile;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Objects;

import saker.build.task.TaskResultDependencyHandle;
import saker.build.task.TaskResultResolver;
import saker.build.task.dependencies.TaskOutputChangeDetector;
import saker.build.task.identifier.TaskIdentifier;
import saker.build.task.utils.SimpleStructuredObjectTaskResult;
import saker.build.task.utils.StructuredTaskResult;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.api.compile.JavaCompilationConfigurationOutput;
import saker.java.compiler.api.compile.JavaCompilationWorkerTaskIdentifier;
import saker.java.compiler.api.compile.JavaCompilerTaskFrontendOutput;

public class JavaCompilerTaskFrontendOutputImpl extends SimpleStructuredObjectTaskResult
		implements JavaCompilerTaskFrontendOutput {
	private static final long serialVersionUID = 1L;

	/**
	 * For {@link Externalizable}.
	 */
	public JavaCompilerTaskFrontendOutputImpl() {
	}

	public JavaCompilerTaskFrontendOutputImpl(JavaCompilationWorkerTaskIdentifier taskIdentifier) {
		super(taskIdentifier);
	}

	@Override
	public JavaCompilationWorkerTaskIdentifier getTaskIdentifier() {
		return (JavaCompilationWorkerTaskIdentifier) super.getTaskIdentifier();
	}

	@Override
	public StructuredTaskResult getJavaSDK() {
		//TODO the SDK result should be retrieved from a setup task that is started by the compiler worker before compilation
		//     we don't actually need to wait for the compilation to complete before retrieving the SDK description
		//     we can also use the SDK description if the compilation fails.
		return new ConfigFieldResolverStructuredTaskResult(getTaskIdentifier(), new JavaSDKConfigResolverTaskResult());
	}

	@Override
	public StructuredTaskResult getSDKs() {
		//TODO should be through setup instance
		return new ConfigFieldResolverStructuredTaskResult(getTaskIdentifier(), new SDKsConfigResolverTaskResult());
	}

	@Override
	public StructuredTaskResult getClassDirectory() {
		return new ConfigFieldResolverStructuredTaskResult(getTaskIdentifier(),
				new ClassDirectoryConfigResolverTaskResult());
	}

	@Override
	public StructuredTaskResult getHeaderDirectory() {
		return new ConfigFieldResolverStructuredTaskResult(getTaskIdentifier(),
				new HeaderDirectoryConfigResolverTaskResult());
	}

	@Override
	public StructuredTaskResult getResourceDirectory() {
		return new ConfigFieldResolverStructuredTaskResult(getTaskIdentifier(),
				new ResourceDirectoryConfigResolverTaskResult());
	}

	@Override
	public StructuredTaskResult getSourceGenDirectory() {
		return new ConfigFieldResolverStructuredTaskResult(getTaskIdentifier(),
				new SourceGenDirectoryConfigResolverTaskResult());
	}

	@Override
	public StructuredTaskResult getModuleName() {
		return new ConfigFieldResolverStructuredTaskResult(getTaskIdentifier(),
				new ModuleNameConfigResolverTaskResult());
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
	}

	public static final class ClassDirectoryConfigResolverTaskResult extends CompilationTaskFieldResolver {
		private static final long serialVersionUID = 1L;

		/**
		 * For {@link Externalizable}.
		 */
		public ClassDirectoryConfigResolverTaskResult() {
		}

		@Override
		public Object resolveField(JavaCompilationConfigurationOutput config) {
			return config.getClassDirectory();
		}
	}

	public static final class JavaSDKConfigResolverTaskResult extends CompilationTaskFieldResolver {
		private static final long serialVersionUID = 1L;

		/**
		 * For {@link Externalizable}.
		 */
		public JavaSDKConfigResolverTaskResult() {
		}

		@Override
		public Object resolveField(JavaCompilationConfigurationOutput config) {
			return config.getJavaSDK();
		}
	}

	public static final class SDKsConfigResolverTaskResult extends CompilationTaskFieldResolver {
		private static final long serialVersionUID = 1L;

		/**
		 * For {@link Externalizable}.
		 */
		public SDKsConfigResolverTaskResult() {
		}

		@Override
		public Object resolveField(JavaCompilationConfigurationOutput config) {
			return config.getSDKs();
		}
	}

	public static final class HeaderDirectoryConfigResolverTaskResult extends CompilationTaskFieldResolver {
		private static final long serialVersionUID = 1L;

		/**
		 * For {@link Externalizable}.
		 */
		public HeaderDirectoryConfigResolverTaskResult() {
		}

		@Override
		public Object resolveField(JavaCompilationConfigurationOutput config) {
			return config.getHeaderDirectory();
		}
	}

	public static final class ResourceDirectoryConfigResolverTaskResult extends CompilationTaskFieldResolver {
		private static final long serialVersionUID = 1L;

		/**
		 * For {@link Externalizable}.
		 */
		public ResourceDirectoryConfigResolverTaskResult() {
		}

		@Override
		public Object resolveField(JavaCompilationConfigurationOutput config) {
			return config.getResourceDirectory();
		}
	}

	public static final class SourceGenDirectoryConfigResolverTaskResult extends CompilationTaskFieldResolver {
		private static final long serialVersionUID = 1L;

		/**
		 * For {@link Externalizable}.
		 */
		public SourceGenDirectoryConfigResolverTaskResult() {
		}

		@Override
		public Object resolveField(JavaCompilationConfigurationOutput config) {
			return config.getSourceGenDirectory();
		}
	}

	public static final class ModuleNameConfigResolverTaskResult extends CompilationTaskFieldResolver {
		private static final long serialVersionUID = 1L;

		/**
		 * For {@link Externalizable}.
		 */
		public ModuleNameConfigResolverTaskResult() {
		}

		@Override
		public Object resolveField(JavaCompilationConfigurationOutput config) {
			return config.getModuleName();
		}
	}

	private static abstract class CompilationTaskFieldResolver implements Externalizable {
		private static final long serialVersionUID = 1L;

		/**
		 * For {@link Externalizable}.
		 */
		public CompilationTaskFieldResolver() {
		}

		public abstract Object resolveField(JavaCompilationConfigurationOutput output);

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		}

		@Override
		public int hashCode() {
			return getClass().getName().hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return ObjectUtils.isSameClass(this, obj);
		}

		@Override
		public String toString() {
			return getClass().getSimpleName().toString();
		}
	}

	private static class ConfigFieldTaskOutputChangeDetector implements TaskOutputChangeDetector, Externalizable {
		private static final long serialVersionUID = 1L;

		private CompilationTaskFieldResolver fieldResolver;
		private Object expected;

		/**
		 * For {@link Externalizable}.
		 */
		public ConfigFieldTaskOutputChangeDetector() {
		}

		public ConfigFieldTaskOutputChangeDetector(CompilationTaskFieldResolver fieldResolver, Object expected) {
			this.fieldResolver = fieldResolver;
			this.expected = expected;
		}

		@Override
		public boolean isChanged(Object taskoutput) {
			if (!(taskoutput instanceof JavaCompilationConfigurationOutput)) {
				return true;
			}
			JavaCompilationConfigurationOutput res = (JavaCompilationConfigurationOutput) taskoutput;
			Object field = fieldResolver.resolveField(res);
			return !Objects.equals(field, expected);
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(fieldResolver);
			out.writeObject(expected);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			fieldResolver = (CompilationTaskFieldResolver) in.readObject();
			expected = in.readObject();
		}

		@Override
		public String toString() {
			return "ConfigFieldTaskOutputChangeDetector[fieldResolver=" + fieldResolver + ", expected=" + expected
					+ "]";
		}
	}

	private static class ConfigFieldResolverStructuredTaskResult implements StructuredTaskResult, Externalizable {
		private static final long serialVersionUID = 1L;

		private TaskIdentifier configOutputTaskId;
		private CompilationTaskFieldResolver fieldResolver;

		/**
		 * For {@link Externalizable}.
		 */
		public ConfigFieldResolverStructuredTaskResult() {
		}

		public ConfigFieldResolverStructuredTaskResult(TaskIdentifier configOutputTaskId,
				CompilationTaskFieldResolver fieldResolver) {
			this.configOutputTaskId = configOutputTaskId;
			this.fieldResolver = fieldResolver;
		}

		@Override
		public Object toResult(TaskResultResolver results) {
			TaskResultDependencyHandle dephandle = results.getTaskResultDependencyHandle(configOutputTaskId);
			Object output = dephandle.get();
			if (!(output instanceof JavaCompilationConfigurationOutput)) {
				throw new IllegalStateException("Invalid Java compilation output for task identifier: "
						+ configOutputTaskId + " with " + output);
			}
			JavaCompilationConfigurationOutput res = (JavaCompilationConfigurationOutput) output;
			Object field = fieldResolver.resolveField(res);
			dephandle.setTaskOutputChangeDetector(new ConfigFieldTaskOutputChangeDetector(fieldResolver, field));
			return field;
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(configOutputTaskId);
			out.writeObject(fieldResolver);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			configOutputTaskId = (TaskIdentifier) in.readObject();
			fieldResolver = (CompilationTaskFieldResolver) in.readObject();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((configOutputTaskId == null) ? 0 : configOutputTaskId.hashCode());
			result = prime * result + ((fieldResolver == null) ? 0 : fieldResolver.hashCode());
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
			if (fieldResolver == null) {
				if (other.fieldResolver != null)
					return false;
			} else if (!fieldResolver.equals(other.fieldResolver))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "[" + configOutputTaskId + "]";
		}

	}
}
