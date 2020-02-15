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

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import saker.build.runtime.execution.ExecutionContext;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.utils.annot.SakerInput;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.function.Functionals;
import saker.build.trace.BuildTrace;
import saker.java.compiler.api.classpath.JavaClassPath;
import saker.java.compiler.api.processor.ProcessorConfiguration;
import saker.java.compiler.api.processor.ProcessorCreator;
import saker.java.compiler.impl.options.SimpleProcessorConfiguration;
import saker.java.compiler.impl.processor.ProcessorCreatorTaskFactory;
import saker.java.compiler.impl.sdk.CurrentJavaSDKReferenceEnvironmentProperty;
import saker.java.compiler.impl.sdk.JavaSDKReference;
import saker.java.compiler.main.JavaTaskOptionUtils;
import saker.java.compiler.main.TaskDocs;
import saker.java.compiler.main.TaskDocs.DocProcessorConfiguration;
import saker.java.compiler.main.TaskDocs.ProcessorClassNameOption;
import saker.java.compiler.main.compile.JavaCompilerTaskFactory;
import saker.java.compiler.main.compile.option.JavaClassPathTaskOption;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.utils.FrontendTaskFactory;
import saker.sdk.support.api.EnvironmentSDKDescription;
import saker.sdk.support.api.SDKDescription;
import saker.sdk.support.api.SDKSupportUtils;
import saker.sdk.support.main.option.SDKDescriptionTaskOption;

@NestTaskInformation(returnType = @NestTypeUsage(DocProcessorConfiguration.class))
@NestInformation("Loads an annotation processor from the specified class path to be used with "
		+ JavaCompilerTaskFactory.TASK_NAME + "().\n"
		+ "The task will load the class path based on the parameters and create an annotation processor configuration "
		+ "that can be used with the Java compiler task. The specified class path and SDKs have no effect on the "
		+ "configuration of the compilation task.\n"
		+ "The task allows configuring some of the processor aspects, whether it is Aggregating, Consistent, or if it should "
		+ "AlwaysRun. Note that these properties can be overridden when the processor is passed to the compilation task.")

@NestParameterInformation(value = "Class",
		aliases = { "" },
		required = true,
		type = @NestTypeUsage(ProcessorClassNameOption.class),
		info = @NestInformation(TaskDocs.ANNOTATION_PROCESSOR_CLASS_PARAMETER))

@NestParameterInformation(value = "ClassPath",
		info = @NestInformation("The class path to load the processor from.\n"
				+ "The class path can be specified the same way as in " + JavaCompilerTaskFactory.TASK_NAME + "(). "
				+ "It can accept paths to JARs or class directories, wildcards of theirs, outputs of previous "
				+ "compilations, paths based on SDKs, or class paths from other tasks."),
		type = @NestTypeUsage(value = Collection.class, elementTypes = JavaClassPathTaskOption.class))
@NestParameterInformation(value = "SDKs",
		info = @NestInformation("Specifies the SDKs used by the task to resolve class paths.\n"
				+ "If any class path entry is specified that uses a given SDK, then it will be resolved using "
				+ "SDKS defined in this parameter. Works similarly as the SDK parameter in "
				+ JavaCompilerTaskFactory.TASK_NAME + "()."),
		type = @NestTypeUsage(value = Map.class,
				elementTypes = { saker.sdk.support.main.TaskDocs.DocSdkNameOption.class,
						SDKDescriptionTaskOption.class }))
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
public class ClassPathProcessorTaskFactory extends FrontendTaskFactory<ProcessorConfiguration> {
	static final String MAY_BE_OVERRIDDEN_APPEND = "\nThis value may be overriden by the "
			+ JavaCompilerTaskFactory.TASK_NAME + "() AnnotationProcessor parameter.";

	public static final String TASK_NAME = "saker.java.processor";

	private static final long serialVersionUID = 1L;

	public ClassPathProcessorTaskFactory() {
	}

	@Override
	public ParameterizableTask<? extends ProcessorConfiguration> createTask(ExecutionContext executioncontext) {
		return new ClassPathProcessorTask();
	}

	private static final class ClassPathProcessorTask implements ParameterizableTask<ProcessorConfiguration> {
		@SakerInput(value = { "", "Class" }, required = true)
		public String processorClassNameOption;
		@SakerInput(value = "ClassPath")
		public Collection<JavaClassPathTaskOption> classPathOption;
		@SakerInput(value = { "SDKs" })
		public Map<String, SDKDescriptionTaskOption> sdksOption;

		@SakerInput("Aggregating")
		public Boolean aggregatingOption;
		@SakerInput("Consistent")
		public Boolean consistentOption;
		@SakerInput("AlwaysRun")
		public Boolean alwaysRunOption;

		@Override
		public ProcessorConfiguration run(TaskContext taskcontext) throws Exception {
			if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
				BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_FRONTEND);
			}
			if (ObjectUtils.isNullOrEmpty(processorClassNameOption)) {
				taskcontext.abortExecution(new IllegalArgumentException(
						(processorClassNameOption == null ? "null" : "Empty") + " processor class name specified."));
				return null;
			}
			Collection<JavaClassPathTaskOption> classpaths = ObjectUtils.cloneArrayList(this.classPathOption,
					JavaClassPathTaskOption::clone);
			Map<String, SDKDescriptionTaskOption> sdkoptions = ObjectUtils.cloneTreeMap(this.sdksOption,
					Functionals.identityFunction(), SDKDescriptionTaskOption::clone);

			Map<String, SDKDescription> sdks = new TreeMap<>(SDKSupportUtils.getSDKNameComparator());
			if (!ObjectUtils.isNullOrEmpty(sdkoptions)) {
				for (Entry<String, SDKDescriptionTaskOption> entry : sdkoptions.entrySet()) {
					SDKDescriptionTaskOption desctaskoption = entry.getValue();
					if (desctaskoption == null) {
						continue;
					}
					SDKDescription[] desc = { null };
					desctaskoption.accept(new SDKDescriptionTaskOption.Visitor() {
						@Override
						public void visit(SDKDescription description) {
							desc[0] = description;
						}
					});
					SDKDescription prev = sdks.put(entry.getKey(), desc[0]);
					if (prev != null && !prev.equals(desc[0])) {
						taskcontext.abortExecution(
								new IllegalArgumentException("Multiple different SDKs defined for name: "
										+ entry.getKey() + " with " + prev + " and " + desc[0]));
						return null;
					}
				}
			}
			if (!ObjectUtils.containsKey(sdks, JavaSDKReference.DEFAULT_SDK_NAME)) {
				sdks.put(JavaSDKReference.DEFAULT_SDK_NAME,
						EnvironmentSDKDescription.create(CurrentJavaSDKReferenceEnvironmentProperty.INSTANCE));
			}

			JavaClassPath classpath = JavaTaskOptionUtils.createClassPath(taskcontext, classpaths);
			ProcessorCreatorTaskFactory proccreatortaskfactory = new ProcessorCreatorTaskFactory(
					processorClassNameOption, classpath, sdks);
			ProcessorCreator proccreator = taskcontext.getTaskUtilities().runTaskResult(proccreatortaskfactory,
					proccreatortaskfactory);
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
	}
}
