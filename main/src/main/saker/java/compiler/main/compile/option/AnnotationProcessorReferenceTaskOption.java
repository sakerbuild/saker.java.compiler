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
package saker.java.compiler.main.compile.option;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import saker.build.task.TaskContext;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.java.compiler.api.compile.JavaAnnotationProcessor;
import saker.java.compiler.api.compile.JavaCompilerWarningType;
import saker.java.compiler.api.processor.ProcessorConfiguration;
import saker.java.compiler.main.TaskDocs;
import saker.java.compiler.main.TaskDocs.AnnotationProcessorOptionKey;
import saker.java.compiler.main.TaskDocs.AnnotationProcessorOptionValue;
import saker.java.compiler.main.TaskDocs.DocProcessorConfiguration;
import saker.java.compiler.main.processor.ClassPathProcessorTaskFactory;
import saker.nest.scriptinfo.reflection.annot.NestFieldInformation;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;

@NestInformation("Specifies an annotation processor to use during Java compilation.\n"
		+ "Annotation processors are plugins into the Java compiler, in which they can examine the source elements and generate other "
		+ "classes and resources based on them.\n"
		+ "Processors can be configured using the Options field which provide them string based key-value configurations.\n"
		+ "Processors can be created by other tasks in the build system, e.g. "
		+ ClassPathProcessorTaskFactory.TASK_NAME + "(). See the documentation of your processor "
		+ "of interest to view the exact mechanism.\n"
		+ "In order to properly support incremental compilation, it is recommended to properly configure the processor by specifying whether "
		+ "the processor is Aggregating via the associated field.")
@NestFieldInformation(value = "Processor",
		type = @NestTypeUsage(DocProcessorConfiguration.class),
		info = @NestInformation("Specifies the processor that is used during compilation."))
@NestFieldInformation(value = "Options",
		info = @NestInformation("Specifies private options to the processor.\n"
				+ "Unlike the AnnotationProcessorOptions parameter of saker.java.compile(), this specifies string key-value options which are private to "
				+ "this annotation processor. The options specified here will not be visible to others processors used during the same Java compilation."),
		type = @NestTypeUsage(value = Map.class,
				elementTypes = { AnnotationProcessorOptionKey.class, AnnotationProcessorOptionValue.class }))
@NestFieldInformation(value = "AlwaysRun",
		info = @NestInformation(TaskDocs.ANNOTATION_PROCESSOR_ALWAYSRUN),
		type = @NestTypeUsage(boolean.class))
@NestFieldInformation(value = "Consistent",
		info = @NestInformation(TaskDocs.ANNOTATION_PROCESSOR_CONSISTENT),
		type = @NestTypeUsage(boolean.class))
@NestFieldInformation(value = "Aggregating",
		info = @NestInformation(TaskDocs.ANNOTATION_PROCESSOR_AGGREGATING),
		type = @NestTypeUsage(boolean.class))
@NestFieldInformation(value = "SuppressWarnings",
		info = @NestInformation("Specifies the warning types which should be suppressed for this processor.\n"
				+ "Unline the global SuppressWarnings option in saker.java.compile(), the warning suppressions defined in this option will "
				+ "only affect the diagnostics produced by this processor."),
		type = @NestTypeUsage(value = Set.class, elementTypes = JavaCompilerWarningType.class))
public interface AnnotationProcessorReferenceTaskOption {
	public default AnnotationProcessorReferenceTaskOption clone() {
		return new SimpleAnnotationProcessorReferenceTaskOption(this);
	}

	public JavaAnnotationProcessor toJavaAnnotationProcessor(TaskContext taskcontext);

	public ProcessorConfiguration getProcessor();

	public default Map<String, String> getOptions() {
		return Collections.emptyMap();
	}

	public default boolean getAlwaysRun() {
		return ObjectUtils.defaultize(getProcessor().getAlwaysRun(), false);
	}

	public default boolean getConsistent() {
		return ObjectUtils.defaultize(getProcessor().getConsistent(), true);
	}

	public default boolean getAggregating() {
		return ObjectUtils.defaultize(getProcessor().getAggregating(), true);
	}

	public default Collection<String> getSuppressWarnings() {
		return Collections.emptySet();
	}

	public static AnnotationProcessorReferenceTaskOption valueOf(ProcessorConfiguration procconfig) {
		return new SimpleAnnotationProcessorReferenceTaskOption(procconfig);
	}
}
