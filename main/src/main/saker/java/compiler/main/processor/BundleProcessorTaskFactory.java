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
package saker.java.compiler.main.processor;

import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.SimpleStructuredObjectTaskResult;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.trace.BuildTrace;
import saker.java.compiler.impl.processor.BundleProcessorWorkerTaskFactory;
import saker.java.compiler.main.TaskDocs;
import saker.java.compiler.main.TaskDocs.DocProcessorConfiguration;
import saker.java.compiler.main.TaskDocs.ProcessorClassNameOption;
import saker.java.compiler.main.compile.JavaCompilerTaskFactory;
import saker.nest.bundle.BundleIdentifier;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
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
public class BundleProcessorTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = "saker.java.processor.bundle";

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new ParameterizableTask<Object>() {
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
			public Object run(TaskContext taskcontext) throws Exception {
				BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_FRONTEND);
				if (ObjectUtils.isNullOrEmpty(processorClassNameOption)) {
					taskcontext.abortExecution(
							new IllegalArgumentException((processorClassNameOption == null ? "null" : "Empty")
									+ " processor class name specified."));
					return null;
				}

				BundleProcessorWorkerTaskFactory task = new BundleProcessorWorkerTaskFactory(processorClassNameOption,
						bundleIdentifierOption, aggregatingOption, consistentOption, alwaysRunOption);
				taskcontext.startTask(task, task, null);
				SimpleStructuredObjectTaskResult result = new SimpleStructuredObjectTaskResult(task);
				taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
				return result;
			}
		};
	}

}
