package saker.java.compiler.impl.processor;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.annotation.processing.Processor;

import saker.build.file.content.ContentDescriptor;
import saker.build.file.content.HashContentDescriptor;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.ExecutionProperty;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.identifier.TaskIdentifier;
import saker.build.thirdparty.saker.util.ReflectUtils;
import saker.build.trace.BuildTrace;
import saker.java.compiler.api.processor.ProcessorConfiguration;
import saker.java.compiler.api.processor.ProcessorCreationContext;
import saker.java.compiler.api.processor.ProcessorCreator;
import saker.java.compiler.impl.JavaUtil;
import saker.java.compiler.impl.options.SimpleProcessorConfiguration;
import saker.java.compiler.main.processor.BundleProcessorTaskFactory;
import saker.nest.bundle.BundleIdentifier;
import saker.nest.bundle.BundleKey;
import saker.nest.bundle.NestBundleClassLoader;
import saker.nest.bundle.NestBundleStorageConfiguration;
import saker.nest.support.api.property.BundlePropertyUtils;

public class BundleProcessorWorkerTaskFactory
		implements TaskFactory<ProcessorConfiguration>, Task<ProcessorConfiguration>, Externalizable, TaskIdentifier {
	private static final long serialVersionUID = 1L;

	private String processorClassName;
	private BundleIdentifier bundleIdentifier;

	private Boolean aggregating;
	private Boolean consistent;
	private Boolean alwaysRun;

	/**
	 * For {@link Externalizable}.
	 */
	public BundleProcessorWorkerTaskFactory() {
	}

	public BundleProcessorWorkerTaskFactory(String processorClassName, BundleIdentifier bundleIdentifier,
			Boolean aggregating, Boolean consistent, Boolean alwaysRun) {
		this.processorClassName = processorClassName;
		this.bundleIdentifier = bundleIdentifier;
		this.aggregating = aggregating;
		this.consistent = consistent;
		this.alwaysRun = alwaysRun;
	}

	@Override
	public ProcessorConfiguration run(TaskContext taskcontext) throws Exception {
		if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
			String procsimplename = JavaUtil.getClassSimpleNameFromBinaryName(processorClassName);
			BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_WORKER);
			BuildTrace.setDisplayInformation("java.processor:" + procsimplename,
					BundleProcessorTaskFactory.TASK_NAME + ": " + procsimplename);
		}

		String cname = this.processorClassName;
		BundleIdentifier bundleid = this.bundleIdentifier;
		BundleKey bundlekey = taskcontext.getTaskUtilities().getReportExecutionDependency(
				BundlePropertyUtils.lookupBundleIdentifierToBundleKeyExecutionProperty(bundleid));
		ContentDescriptor clhash = taskcontext.getTaskUtilities()
				.getReportExecutionDependency(new ClassLoaderHashExecutionProperty(bundlekey));

		ProcessorCreator proccreator = new BundleClassLoaderProcessorCreator(bundlekey, clhash, cname);
		SimpleProcessorConfiguration result = new SimpleProcessorConfiguration(proccreator);
		if (aggregating != null) {
			result.setAggregating(aggregating);
		}
		if (consistent != null) {
			result.setConsistent(consistent);
		}
		if (alwaysRun != null) {
			result.setAlwaysRun(alwaysRun);
		}
		return result;
	}

	@Override
	public Task<? extends ProcessorConfiguration> createTask(ExecutionContext executioncontext) {
		return this;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(processorClassName);
		out.writeObject(bundleIdentifier);
		out.writeObject(aggregating);
		out.writeObject(consistent);
		out.writeObject(alwaysRun);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		processorClassName = (String) in.readObject();
		bundleIdentifier = (BundleIdentifier) in.readObject();
		aggregating = (Boolean) in.readObject();
		consistent = (Boolean) in.readObject();
		alwaysRun = (Boolean) in.readObject();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bundleIdentifier == null) ? 0 : bundleIdentifier.hashCode());
		result = prime * result + ((processorClassName == null) ? 0 : processorClassName.hashCode());
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
		BundleProcessorWorkerTaskFactory other = (BundleProcessorWorkerTaskFactory) obj;
		if (aggregating == null) {
			if (other.aggregating != null)
				return false;
		} else if (!aggregating.equals(other.aggregating))
			return false;
		if (alwaysRun == null) {
			if (other.alwaysRun != null)
				return false;
		} else if (!alwaysRun.equals(other.alwaysRun))
			return false;
		if (bundleIdentifier == null) {
			if (other.bundleIdentifier != null)
				return false;
		} else if (!bundleIdentifier.equals(other.bundleIdentifier))
			return false;
		if (consistent == null) {
			if (other.consistent != null)
				return false;
		} else if (!consistent.equals(other.consistent))
			return false;
		if (processorClassName == null) {
			if (other.processorClassName != null)
				return false;
		} else if (!processorClassName.equals(other.processorClassName))
			return false;
		return true;
	}

	public static final class BundleClassLoaderProcessorCreator implements ProcessorCreator, Externalizable {
		private static final long serialVersionUID = 1L;

		private BundleKey bundleKey;
		private String className;
		private ContentDescriptor classLoaderHash;

		/**
		 * For {@link Externalizable}.
		 */
		public BundleClassLoaderProcessorCreator() {
		}

		BundleClassLoaderProcessorCreator(BundleKey bundlekey, ContentDescriptor clhash, String cname) {
			this.bundleKey = bundlekey;
			this.classLoaderHash = clhash;
			this.className = cname;
		}

		@Override
		public String getName() {
			return className + "@" + bundleKey.getBundleIdentifier();
		}

		@Override
		public Processor create(ProcessorCreationContext creationcontext) throws Exception {
			NestBundleClassLoader thiscl = (NestBundleClassLoader) this.getClass().getClassLoader();
			NestBundleStorageConfiguration storageconfig = thiscl.getBundleStorageConfiguration();
			ClassLoader bundlecl = storageconfig.getBundleClassLoader(bundleKey);
			Class<? extends Processor> procclass;
			try {
				//XXX we may want to load the class directly from the bundle instead of the whole bundle classpath
				procclass = Class.forName(className, false, bundlecl).asSubclass(Processor.class);
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException(
						"Class " + className + " was not found in bundle: " + bundleKey.getBundleIdentifier(), e);
			} catch (ClassCastException e) {
				throw new IllegalArgumentException("Class " + className + " doesn't implement " + Processor.class, e);
			} catch (Exception e) {
				throw new IllegalArgumentException("Failed to retrieve processor class: " + className + " from bundle: "
						+ bundleKey.getBundleIdentifier(), e);
			}
			try {
				return ReflectUtils.newInstance(procclass);
			} catch (Exception e) {
				throw new IllegalArgumentException("Failed to instantiate processor: " + procclass, e);
			}
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(bundleKey);
			out.writeObject(className);
			out.writeObject(classLoaderHash);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			bundleKey = (BundleKey) in.readObject();
			className = (String) in.readObject();
			classLoaderHash = (ContentDescriptor) in.readObject();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((bundleKey == null) ? 0 : bundleKey.hashCode());
			result = prime * result + ((classLoaderHash == null) ? 0 : classLoaderHash.hashCode());
			result = prime * result + ((className == null) ? 0 : className.hashCode());
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
			BundleClassLoaderProcessorCreator other = (BundleClassLoaderProcessorCreator) obj;
			if (bundleKey == null) {
				if (other.bundleKey != null)
					return false;
			} else if (!bundleKey.equals(other.bundleKey))
				return false;
			if (classLoaderHash == null) {
				if (other.classLoaderHash != null)
					return false;
			} else if (!classLoaderHash.equals(other.classLoaderHash))
				return false;
			if (className == null) {
				if (other.className != null)
					return false;
			} else if (!className.equals(other.className))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "[" + getName() + "]";
		}

	}

	public static class ClassLoaderHashExecutionProperty
			implements ExecutionProperty<ContentDescriptor>, Externalizable {
		private static final long serialVersionUID = 1L;

		private BundleKey bundleKey;

		/**
		 * For {@link Externalizable}.
		 */
		public ClassLoaderHashExecutionProperty() {
		}

		public ClassLoaderHashExecutionProperty(BundleKey bundleKey) {
			this.bundleKey = bundleKey;
		}

		@Override
		public ContentDescriptor getCurrentValue(ExecutionContext executioncontext) throws Exception {
			NestBundleClassLoader thiscl = (NestBundleClassLoader) this.getClass().getClassLoader();
			return HashContentDescriptor.createWithHash(
					((NestBundleClassLoader) thiscl.getBundleStorageConfiguration().getBundleClassLoader(bundleKey))
							.getBundleHashWithClassPathDependencies());
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(bundleKey);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			bundleKey = (BundleKey) in.readObject();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((bundleKey == null) ? 0 : bundleKey.hashCode());
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
			ClassLoaderHashExecutionProperty other = (ClassLoaderHashExecutionProperty) obj;
			if (bundleKey == null) {
				if (other.bundleKey != null)
					return false;
			} else if (!bundleKey.equals(other.bundleKey))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "[" + (bundleKey != null ? "bundleKey=" + bundleKey : "") + "]";
		}

	}
}
