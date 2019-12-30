package saker.java.compiler.main.processor;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.annotation.processing.Processor;

import saker.build.file.content.ContentDescriptor;
import saker.build.file.content.HashContentDescriptor;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.ExecutionProperty;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.annot.SakerInput;
import saker.build.thirdparty.saker.util.ReflectUtils;
import saker.java.compiler.api.processor.ProcessorConfiguration;
import saker.java.compiler.api.processor.ProcessorCreationContext;
import saker.java.compiler.api.processor.ProcessorCreator;
import saker.java.compiler.impl.options.SimpleProcessorConfiguration;
import saker.java.compiler.main.TaskDocs;
import saker.java.compiler.main.TaskDocs.DocProcessorConfiguration;
import saker.java.compiler.main.TaskDocs.ProcessorClassNameOption;
import saker.java.compiler.main.compile.JavaCompilerTaskFactory;
import saker.nest.bundle.BundleIdentifier;
import saker.nest.bundle.BundleKey;
import saker.nest.bundle.NestBundleClassLoader;
import saker.nest.bundle.NestBundleStorageConfiguration;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.support.api.property.BundlePropertyUtils;
import saker.nest.utils.FrontendTaskFactory;

@NestTaskInformation(returnType = @NestTypeUsage(DocProcessorConfiguration.class))
@NestInformation("Loads an annotation processor from the specified saker.nest bundle. \n"
		+ "The returned configuration can be used with " + JavaCompilerTaskFactory.TASK_NAME + "().\n"
		+ "The task will look up the specified bundle and load the given processor class.")

@NestParameterInformation(value = "Class",
		aliases = { "" },
		required = true,
		type = @NestTypeUsage(ProcessorClassNameOption.class),
		info = @NestInformation(TaskDocs.ANNOTATION_PROCESSOR_CLASS_PARAMETER))

@NestParameterInformation(value = "Bundle",
		required = true,
		type = @NestTypeUsage(BundleIdentifier.class),
		info = @NestInformation("Specifies the bundle from which the annotation processor should be loaded.\n"
				+ "The parameter takes the bundle identifier of the bundle which whill be looked up according to "
				+ "the current bundle storage configuration.\n"
				+ "The bundle identifier is not required to have a version qualifier."))

@NestParameterInformation(value = "AlwaysRun",
		info = @NestInformation(TaskDocs.ANNOTATION_PROCESSOR_ALWAYSRUN
				+ ClassPathProcessorTaskFactory.MAY_BE_OVERRIDDEN_APPEND),
		type = @NestTypeUsage(boolean.class))
@NestParameterInformation(value = "Consistent",
		info = @NestInformation(TaskDocs.ANNOTATION_PROCESSOR_CONSISTENT
				+ ClassPathProcessorTaskFactory.MAY_BE_OVERRIDDEN_APPEND),
		type = @NestTypeUsage(boolean.class))
@NestParameterInformation(value = "Aggregating",
		info = @NestInformation(TaskDocs.ANNOTATION_PROCESSOR_AGGREGATING
				+ ClassPathProcessorTaskFactory.MAY_BE_OVERRIDDEN_APPEND),
		type = @NestTypeUsage(boolean.class))
public class BundleProcessorTaskFactory extends FrontendTaskFactory<ProcessorConfiguration> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "saker.java.processor.bundle";

	@Override
	public ParameterizableTask<? extends ProcessorConfiguration> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<ProcessorConfiguration>() {
			@SakerInput(value = { "", "Class" }, required = true)
			public String processorClassNameOption;
			@SakerInput(value = { "Bundle" }, required = true)
			public BundleIdentifier bundleIdentifierOption;

			@SakerInput("Aggregating")
			public Boolean aggregatingOption;
			@SakerInput("Consistent")
			public Boolean consistentOption;
			@SakerInput("AlwaysRun")
			public Boolean alwaysRunOption;

			@Override
			public ProcessorConfiguration run(TaskContext taskcontext) throws Exception {
				String cname = this.processorClassNameOption;
				BundleIdentifier bundleid = this.bundleIdentifierOption;
				BundleKey bundlekey = taskcontext.getTaskUtilities().getReportExecutionDependency(
						BundlePropertyUtils.lookupBundleIdentifierToBundleKeyExecutionProperty(bundleid));
				ContentDescriptor clhash = taskcontext.getTaskUtilities()
						.getReportExecutionDependency(new ClassLoaderHashExecutionProperty(bundlekey));

				ProcessorCreator proccreator = new BundleClassLoaderProcessorCreator(bundlekey, clhash, cname);
				SimpleProcessorConfiguration result = new SimpleProcessorConfiguration(proccreator);
				if (aggregatingOption != null) {
					result.setAggregating(aggregatingOption);
				}
				if (consistentOption != null) {
					result.setConsistent(consistentOption);
				}
				if (alwaysRunOption != null) {
					result.setAlwaysRun(alwaysRunOption);
				}
				return result;
			}
		};
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
