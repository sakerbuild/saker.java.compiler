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
package saker.java.compiler.impl.classpath.bundle;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.ExecutionProperty;
import saker.build.runtime.execution.SakerLog;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.identifier.TaskIdentifier;
import saker.build.task.utils.SimpleStructuredObjectTaskResult;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.build.thirdparty.saker.util.thread.ThreadUtils;
import saker.build.thirdparty.saker.util.thread.ThreadUtils.ThreadWorkPool;
import saker.build.trace.BuildTrace;
import saker.build.util.property.IDEConfigurationRequiredExecutionProperty;
import saker.java.compiler.api.classpath.ClassPathReference;
import saker.java.compiler.main.classpath.bundle.BundleClassPathTaskFactory;
import saker.nest.bundle.BundleIdentifier;
import saker.nest.bundle.BundleKey;
import saker.nest.bundle.storage.StorageViewKey;
import saker.nest.support.api.property.BundleContentDescriptorPathPropertyValue;
import saker.nest.support.api.property.BundleContentDescriptorPropertyValue;
import saker.nest.support.api.property.BundlePropertyUtils;
import saker.std.api.file.location.FileLocation;
import saker.std.api.file.location.LocalFileLocation;

public class BundleClassPathWorkerTaskFactory
		implements TaskFactory<ClassPathReference>, Task<ClassPathReference>, Externalizable, TaskIdentifier {
	private static final long serialVersionUID = 1L;

	private Set<BundleKey> bundles;

	/**
	 * For {@link Externalizable}.
	 */
	public BundleClassPathWorkerTaskFactory() {
	}

	public BundleClassPathWorkerTaskFactory(Set<BundleKey> bundles) {
		this.bundles = bundles;
	}

	@Override
	public ClassPathReference run(TaskContext taskcontext) throws Exception {
		if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
			BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_WORKER);
			if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_009) {
				Map<String, Object> valmap = new LinkedHashMap<>();
				if (!ObjectUtils.isNullOrEmpty(bundles)) {
					valmap.put("Bundles",
							bundles.stream().map(BundleKey::getBundleIdentifier).map(Object::toString).toArray());
				}
				BuildTrace.setValues(valmap, BuildTrace.VALUE_CATEGORY_TASK);
			}
		}
		taskcontext.setStandardOutDisplayIdentifier(BundleClassPathTaskFactory.TASK_NAME);

		BundleClassPathEntry[] entries = new BundleClassPathEntry[bundles.size()];
		try (ThreadWorkPool workpool = ThreadUtils.newDynamicWorkPool()) {
			int i = 0;
			for (BundleKey bk : bundles) {
				int idx = i++;
				workpool.offer(() -> {
					ExecutionProperty<? extends BundleContentDescriptorPropertyValue> bundleexecprop = BundlePropertyUtils
							.bundleContentDescriptorExecutionProperty(bk);
					BundleContentDescriptorPropertyValue bundlecdpropval = taskcontext.getTaskUtilities()
							.getReportExecutionDependency(bundleexecprop);

					BundleClassPathEntry entry = new BundleClassPathEntry(bk, bundlecdpropval.getContentDescriptor());

					StorageViewKey storageviewkey = bk.getStorageViewKey();
					AttachmentIDEConfigurationBundlePathTaskFactory docattachmenttask = new AttachmentIDEConfigurationBundlePathTaskFactory(
							storageviewkey, BundlePropertyUtils.documentationAttachmentExecutionProperty(bk));
					taskcontext.startTask(docattachmenttask, docattachmenttask, null);
					entry.setDocumentationAttachment(new SimpleStructuredObjectTaskResult(docattachmenttask));

					AttachmentIDEConfigurationBundlePathTaskFactory sourceattachmenttask = new AttachmentIDEConfigurationBundlePathTaskFactory(
							storageviewkey, BundlePropertyUtils.sourceAttachmentExecutionProperty(bk));
					taskcontext.startTask(sourceattachmenttask, sourceattachmenttask, null);
					entry.setSourceAttachment(new SimpleStructuredObjectTaskResult(sourceattachmenttask));

					entries[idx] = entry;
				});
			}
		}

		return new BundlesClassPathReference(ImmutableUtils.makeImmutableLinkedHashSet(entries));
	}

	@Override
	public Task<? extends ClassPathReference> createTask(ExecutionContext executioncontext) {
		return this;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerialUtils.writeExternalCollection(out, bundles);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		bundles = SerialUtils.readExternalImmutableLinkedHashSet(in);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bundles == null) ? 0 : bundles.hashCode());
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
		BundleClassPathWorkerTaskFactory other = (BundleClassPathWorkerTaskFactory) obj;
		if (bundles == null) {
			if (other.bundles != null)
				return false;
		} else if (!bundles.equals(other.bundles))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + "[" + (bundles != null ? "bundles=" + bundles : "") + "]";
	}

	private static final class AttachmentIDEConfigurationBundlePathTaskFactory
			implements TaskFactory<FileLocation>, Task<FileLocation>, Externalizable, TaskIdentifier {
		private static final long serialVersionUID = 1L;

		private StorageViewKey storageViewKey;
		private ExecutionProperty<? extends BundleIdentifier> attachmentExecutionProperty;

		/**
		 * For {@link Externalizable}.
		 */
		public AttachmentIDEConfigurationBundlePathTaskFactory() {
		}

		public AttachmentIDEConfigurationBundlePathTaskFactory(StorageViewKey storageViewKey,
				ExecutionProperty<? extends BundleIdentifier> attachmentExecutionProperty) {
			this.storageViewKey = storageViewKey;
			this.attachmentExecutionProperty = attachmentExecutionProperty;
		}

		@Override
		public FileLocation run(TaskContext taskcontext) throws Exception {
			if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
				BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_META);
			}
			if (!taskcontext.getTaskUtilities()
					.getReportExecutionDependency(IDEConfigurationRequiredExecutionProperty.INSTANCE)) {
				return null;
			}
			BundleIdentifier attachmentbundleid;
			try {
				attachmentbundleid = taskcontext.getTaskUtilities()
						.getReportExecutionDependency(attachmentExecutionProperty);
			} catch (Exception e) {
				SakerLog.warning().println(
						"Failed to load retrieve bundle attachment: " + attachmentExecutionProperty + " (" + e + ")");
				taskcontext.getTaskUtilities().reportIgnoredException(e);
				return null;
			}
			try {
				if (attachmentbundleid == null) {
					return null;
				}
				BundleKey bk = BundleKey.create(storageViewKey, attachmentbundleid);

				ExecutionProperty<? extends BundleContentDescriptorPathPropertyValue> bundleexecprop = BundlePropertyUtils
						.bundleContentDescriptorPathExecutionProperty(bk);
				BundleContentDescriptorPathPropertyValue bundlecdpropval = taskcontext.getTaskUtilities()
						.getReportExecutionDependency(bundleexecprop);

				FileLocation result = LocalFileLocation.create(bundlecdpropval.getLocalPath());

				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			} catch (Exception e) {
				//TODO install a dependency for the failure as well. we need to be reinvoked if the attachment is added
				SakerLog.warning().println("Failed to load attachment bundle: " + attachmentbundleid + " (" + e + ")");
				taskcontext.getTaskUtilities().reportIgnoredException(e);
				return null;
			}
		}

		@Override
		public Task<? extends FileLocation> createTask(ExecutionContext executioncontext) {
			return this;
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeObject(storageViewKey);
			out.writeObject(attachmentExecutionProperty);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			storageViewKey = (StorageViewKey) in.readObject();
			attachmentExecutionProperty = (ExecutionProperty<? extends BundleIdentifier>) in.readObject();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((attachmentExecutionProperty == null) ? 0 : attachmentExecutionProperty.hashCode());
			result = prime * result + ((storageViewKey == null) ? 0 : storageViewKey.hashCode());
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
			BundleClassPathWorkerTaskFactory.AttachmentIDEConfigurationBundlePathTaskFactory other = (BundleClassPathWorkerTaskFactory.AttachmentIDEConfigurationBundlePathTaskFactory) obj;
			if (attachmentExecutionProperty == null) {
				if (other.attachmentExecutionProperty != null)
					return false;
			} else if (!attachmentExecutionProperty.equals(other.attachmentExecutionProperty))
				return false;
			if (storageViewKey == null) {
				if (other.storageViewKey != null)
					return false;
			} else if (!storageViewKey.equals(other.storageViewKey))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return getClass().getSimpleName() + "[" + attachmentExecutionProperty + "]";
		}
	}
}