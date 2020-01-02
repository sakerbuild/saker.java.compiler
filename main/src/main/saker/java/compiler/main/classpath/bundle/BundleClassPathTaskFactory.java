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
package saker.java.compiler.main.classpath.bundle;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.ExecutionProperty;
import saker.build.runtime.execution.SakerLog;
import saker.build.task.ParameterizableTask;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.TaskFactory;
import saker.build.task.identifier.TaskIdentifier;
import saker.build.task.utils.SimpleStructuredObjectTaskResult;
import saker.build.task.utils.StructuredListTaskResult;
import saker.build.task.utils.StructuredTaskResult;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.io.IOUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.build.thirdparty.saker.util.thread.ThreadUtils;
import saker.build.thirdparty.saker.util.thread.ThreadUtils.ThreadWorkPool;
import saker.build.util.data.DataConverterUtils;
import saker.build.util.property.IDEConfigurationRequiredExecutionProperty;
import saker.java.compiler.api.classpath.ClassPathReference;
import saker.java.compiler.main.TaskDocs.DocBundleClassPath;
import saker.java.compiler.main.compile.JavaCompilerTaskFactory;
import saker.nest.bundle.BundleIdentifier;
import saker.nest.bundle.BundleKey;
import saker.nest.bundle.storage.StorageViewKey;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.support.api.dependency.DependencyResolutionTaskOutput;
import saker.nest.support.api.download.DownloadBundleTaskOutput;
import saker.nest.support.api.download.DownloadBundleWorkerTaskOutput;
import saker.nest.support.api.property.BundleContentDescriptorPathPropertyValue;
import saker.nest.support.api.property.BundleContentDescriptorPropertyValue;
import saker.nest.support.api.property.BundlePropertyUtils;
import saker.nest.utils.FrontendTaskFactory;
import saker.std.api.file.location.FileLocation;
import saker.std.api.file.location.LocalFileLocation;

@NestTaskInformation(returnType = @NestTypeUsage(DocBundleClassPath.class))
@NestInformation("Creates a class path reference for the specified Nest bundles.\n"
		+ "This task creates a class path to be used with tasks that accept the class path inputs the same way as "
		+ JavaCompilerTaskFactory.TASK_NAME + "() task.\n"
		+ "If the build is configured in a way that it requires IDE configurations to be reported, the task will fetch the "
		+ "appropriate source bundles and reports the accordingly.\n"
		+ "This task doesn't perform any dependency resolution for the specified bundles.")
@NestParameterInformation(value = "Bundles",
		required = true,
		type = @NestTypeUsage(value = Collection.class, elementTypes = BundleIdentifier.class),
		info = @NestInformation("Specifies the bundles to create the class path for.\n"
				+ "The value may be one more multiple bundle identifiers, or the output of the nest.dependency.resolve() task."))
public class BundleClassPathTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Object>() {
			@SakerInput(value = { "", "Bundle", "Bundles" }, required = true)
			public Object bundles;

			@Override
			public Object run(TaskContext taskcontext) throws Exception {
				//XXX abort the execution when appropriate
				return collectBundleKeys(taskcontext);
			}

			private Object startBundleKeysTask(TaskContext taskcontext, Set<BundleKey> bundlekeys) {
				if (bundlekeys.isEmpty()) {
					throw new IllegalArgumentException("No bundles specified.");
				}
				BundleClassPathWorkerTaskFactory worker = new BundleClassPathWorkerTaskFactory(bundlekeys);
				taskcontext.startTask(worker, worker, null);
				SimpleStructuredObjectTaskResult result = new SimpleStructuredObjectTaskResult(worker);
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			}

			private Object collectBundleKeys(TaskContext taskcontext) throws Exception {
				if (bundles == null) {
					throw new NullPointerException("No bundles specified.");
				}
				if (bundles instanceof StructuredTaskResult) {
					if (bundles instanceof StructuredListTaskResult) {
						//expect to be bundle identifiers
						Set<BundleKey> bundlekeys = new LinkedHashSet<>();
						StructuredListTaskResult bundlesstructuredlist = (StructuredListTaskResult) bundles;
						Iterator<? extends StructuredTaskResult> it = bundlesstructuredlist.resultIterator();
						if (it.hasNext()) {
							try (ThreadWorkPool workpool = ThreadUtils.newDynamicWorkPool()) {
								do {
									StructuredTaskResult structuredtaskres = it.next();
									workpool.offer(() -> {
										Object taskres = structuredtaskres.toResult(taskcontext);
										String taskresstr = Objects.toString(taskres, null);
										if (taskresstr == null) {
											throw new NullPointerException("Null list task result.");
										}
										BundleIdentifier bundleid = BundleIdentifier.valueOf(taskresstr);
										BundleKey bundlekey = taskcontext.getTaskUtilities()
												.getReportExecutionDependency(BundlePropertyUtils
														.lookupBundleIdentifierToBundleKeyExecutionProperty(bundleid));

										synchronized (bundlekeys) {
											bundlekeys.add(bundlekey);
										}
									});
								} while (it.hasNext());
							}
						}
						return startBundleKeysTask(taskcontext, bundlekeys);
					}
					StructuredTaskResult structuredbundles = (StructuredTaskResult) bundles;
					bundles = structuredbundles.toResult(taskcontext);
				}
				if (bundles instanceof Object[]) {
					bundles = ImmutableUtils.makeImmutableList((Object[]) bundles);
				}
				if (bundles instanceof Iterable<?>) {
					Set<BundleKey> bundlekeys = new LinkedHashSet<>();
					Iterable<?> bundlesit = (Iterable<?>) bundles;
					ThreadUtils.runParallelItems(bundlesit, o -> {
						if (o == null) {
							return;
						}
						BundleIdentifier bundleid = BundleIdentifier.valueOf(Objects.toString(o));
						BundleKey bundlekey = taskcontext.getTaskUtilities().getReportExecutionDependency(
								BundlePropertyUtils.lookupBundleIdentifierToBundleKeyExecutionProperty(bundleid));

						synchronized (bundlekeys) {
							bundlekeys.add(bundlekey);
						}
					});
					return startBundleKeysTask(taskcontext, bundlekeys);
				}
				Exception adaptexc = null;
				try {
					Object adapted = DataConverterUtils.adaptInterface(this.getClass().getClassLoader(), bundles);
					if (adapted instanceof DownloadBundleTaskOutput) {
						Set<BundleKey> bundlekeys = new LinkedHashSet<>();
						DownloadBundleTaskOutput downloadoutput = (DownloadBundleTaskOutput) adapted;
						StructuredListTaskResult downloadresults = downloadoutput.getDownloadResults();
						List<?> downloadresultlist = downloadresults.toResult(taskcontext);
						for (Object o : downloadresultlist) {
							DownloadBundleWorkerTaskOutput dlout = (DownloadBundleWorkerTaskOutput) DataConverterUtils
									.adaptInterface(this.getClass().getClassLoader(), o);
							BundleKey bundlekey = dlout.getBundleKey();
							bundlekeys.add(bundlekey);
						}
						//TODO use the downloaded bundle paths
						//TODO we don't have to download the bundles in this case again. just the attachments if necessary
						return startBundleKeysTask(taskcontext, bundlekeys);
					}
					//TODO handle localize output
					if (adapted instanceof DependencyResolutionTaskOutput) {
						return startBundleKeysTask(taskcontext, ImmutableUtils
								.makeImmutableLinkedHashSet(((DependencyResolutionTaskOutput) adapted).getBundles()));
					}
				} catch (Exception e) {
					adaptexc = e;
				}
				try {
					//interpret as a bundle identifier
					BundleIdentifier bundleid = BundleIdentifier.valueOf(Objects.toString(bundles));
					BundleKey bundlekey = taskcontext.getTaskUtilities().getReportExecutionDependency(
							BundlePropertyUtils.lookupBundleIdentifierToBundleKeyExecutionProperty(bundleid));

					return startBundleKeysTask(taskcontext, ImmutableUtils.singletonSet(bundlekey));
				} catch (Exception e) {
					throw IOUtils.addExc(e, adaptexc);
				}
			}
		};
	}

	private static class BundleClassPathWorkerTaskFactory
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

						BundleClassPathEntry entry = new BundleClassPathEntry(bk,
								bundlecdpropval.getContentDescriptor());

						AttachmentIDEConfigurationBundlePathTaskFactory docattachmenttask = new AttachmentIDEConfigurationBundlePathTaskFactory(
								bk.getStorageViewKey(),
								BundlePropertyUtils.documentationAttachmentExecutionProperty(bk));
						taskcontext.startTask(docattachmenttask, docattachmenttask, null);
						entry.setDocumentationAttachment(new SimpleStructuredObjectTaskResult(docattachmenttask));

						AttachmentIDEConfigurationBundlePathTaskFactory sourceattachmenttask = new AttachmentIDEConfigurationBundlePathTaskFactory(
								bk.getStorageViewKey(), BundlePropertyUtils.sourceAttachmentExecutionProperty(bk));
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
			AttachmentIDEConfigurationBundlePathTaskFactory other = (AttachmentIDEConfigurationBundlePathTaskFactory) obj;
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
