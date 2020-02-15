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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.SimpleStructuredObjectTaskResult;
import saker.build.task.utils.StructuredListTaskResult;
import saker.build.task.utils.StructuredTaskResult;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.io.IOUtils;
import saker.build.trace.BuildTrace;
import saker.build.util.data.DataConverterUtils;
import saker.java.compiler.impl.classpath.bundle.BundleClassPathWorkerTaskFactory;
import saker.java.compiler.main.TaskDocs.DocBundleClassPath;
import saker.java.compiler.main.compile.JavaCompilerTaskFactory;
import saker.nest.bundle.BundleIdentifier;
import saker.nest.bundle.BundleKey;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.support.api.dependency.DependencyResolutionTaskOutput;
import saker.nest.support.api.download.DownloadBundleTaskOutput;
import saker.nest.support.api.download.DownloadBundleWorkerTaskOutput;
import saker.nest.support.api.property.BundlePropertyUtils;
import saker.nest.utils.FrontendTaskFactory;

@NestTaskInformation(returnType = @NestTypeUsage(DocBundleClassPath.class))
@NestInformation("Creates a class path reference for the specified saker.nest bundles.\n"
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

	public static final String TASK_NAME = "saker.java.classpath.bundle";

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Object>() {
			@SakerInput(value = { "", "Bundle", "Bundles" }, required = true)
			public Object bundles;

			@Override
			public Object run(TaskContext taskcontext) throws Exception {
				if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
					BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_FRONTEND);
				}
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
						while (it.hasNext()) {
							StructuredTaskResult structuredtaskres = it.next();
							Object taskres = structuredtaskres.toResult(taskcontext);
							String taskresstr = Objects.toString(taskres, null);
							if (taskresstr == null) {
								throw new NullPointerException("Null list task result.");
							}
							BundleIdentifier bundleid = BundleIdentifier.valueOf(taskresstr);
							BundleKey bundlekey = taskcontext.getTaskUtilities().getReportExecutionDependency(
									BundlePropertyUtils.lookupBundleIdentifierToBundleKeyExecutionProperty(bundleid));

							synchronized (bundlekeys) {
								bundlekeys.add(bundlekey);
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
					for (Object o : bundlesit) {
						if (o == null) {
							continue;
						}
						if (o instanceof StructuredTaskResult) {
							o = ((StructuredTaskResult) o).toResult(taskcontext);
						}
						BundleIdentifier bundleid = BundleIdentifier.valueOf(Objects.toString(o));
						BundleKey bundlekey = taskcontext.getTaskUtilities().getReportExecutionDependency(
								BundlePropertyUtils.lookupBundleIdentifierToBundleKeyExecutionProperty(bundleid));

						bundlekeys.add(bundlekey);
					}
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

}
