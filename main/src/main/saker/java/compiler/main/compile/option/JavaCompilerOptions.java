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
import java.util.List;
import java.util.Map;
import java.util.Set;

import saker.build.file.path.SakerPath;
import saker.compiler.utils.main.CompilationIdentifierTaskOption;
import saker.java.compiler.api.compile.JavaCompilerWarningType;
import saker.java.compiler.api.compile.JavaDebugInfoType;
import saker.java.compiler.main.TaskDocs;
import saker.java.compiler.main.TaskDocs.AnnotationProcessorOptionKey;
import saker.java.compiler.main.TaskDocs.AnnotationProcessorOptionValue;
import saker.java.compiler.main.TaskDocs.CompilationParameterOption;
import saker.java.compiler.main.TaskDocs.JavaCompilationLanguageOption;
import saker.java.compiler.main.TaskDocs.ModuleInfoInjectMainClassOption;
import saker.java.compiler.main.TaskDocs.ModuleInfoInjectVersionOption;
import saker.java.compiler.main.TaskDocs.ProcessorInputLocationNameOption;
import saker.java.compiler.main.compile.JavaCompilerTaskFactory;
import saker.nest.scriptinfo.reflection.annot.NestFieldInformation;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.sdk.support.main.option.SDKDescriptionTaskOption;

@NestInformation("Describes various options as the input to Java compilation.\n" + "The options can be used in "
		+ JavaCompilerTaskFactory.TASK_NAME + "() " + TaskDocs.PARAM_NAME_COMPILER_OPTIONS
		+ " parameter to specify options which are added to the given compilation based on the compilation Identifier "
		+ "and Language qualifiers.\n"
		+ "The specified values in the CompilerOptions argument is merged into the compilation parameters of the task and "
		+ "the compilation will be executed based on the merged options.")

@NestFieldInformation(value = "Identifier",
		info = @NestInformation(TaskDocs.COMPILE_OPTION_IDENTIFIER),
		type = @NestTypeUsage(CompilationIdentifierTaskOption.class))
@NestFieldInformation(value = "Language",
		info = @NestInformation(TaskDocs.COMPILE_OPTION_LANGUAGE),
		type = @NestTypeUsage(JavaCompilationLanguageOption.class))

@NestFieldInformation(value = "SourceDirectories",
		info = @NestInformation(TaskDocs.COMPILE_SOURCE_DIRECTORY),
		type = @NestTypeUsage(value = Collection.class, elementTypes = JavaSourceDirectoryTaskOption.class))

@NestFieldInformation(value = "ClassPath",
		info = @NestInformation(TaskDocs.COMPILE_CLASS_PATH),
		type = @NestTypeUsage(value = Collection.class, elementTypes = JavaClassPathTaskOption.class))
@NestFieldInformation(value = "BootClassPath",
		info = @NestInformation(TaskDocs.COMPILE_BOOT_CLASS_PATH),
		type = @NestTypeUsage(value = Collection.class, elementTypes = JavaClassPathTaskOption.class))

@NestFieldInformation(value = "ModulePath",
		info = @NestInformation(TaskDocs.COMPILE_MODULE_PATH),
		type = @NestTypeUsage(value = Collection.class, elementTypes = JavaClassPathTaskOption.class))

@NestFieldInformation(value = "SourceVersion",
		info = @NestInformation(TaskDocs.COMPILE_SOURCE_VERSION),
		type = @NestTypeUsage(value = SourceVersionTaskOption.class))
@NestFieldInformation(value = "TargetVersion",
		info = @NestInformation(TaskDocs.COMPILE_TARGET_VERSION),
		type = @NestTypeUsage(value = SourceVersionTaskOption.class))

@NestFieldInformation(value = "Parameters",
		info = @NestInformation(TaskDocs.COMPILE_PARAMETERS),
		type = @NestTypeUsage(value = List.class, elementTypes = CompilationParameterOption.class))

@NestFieldInformation(value = "AnnotationProcessorOptions",
		info = @NestInformation(TaskDocs.COMPILE_ANNOTATION_PROCESSOR_OPTIONS),
		type = @NestTypeUsage(value = Map.class,
				elementTypes = { AnnotationProcessorOptionKey.class, AnnotationProcessorOptionValue.class }))
@NestFieldInformation(value = "AnnotationProcessors",
		info = @NestInformation(TaskDocs.COMPILE_ANNOTATION_PROCESSORS),
		type = @NestTypeUsage(value = Collection.class, elementTypes = AnnotationProcessorReferenceTaskOption.class))
@NestFieldInformation(value = "ProcessorInputLocations",
		info = @NestInformation(TaskDocs.COMPILE_PROCESSOR_INPUT_LOCATIONS),
		type = @NestTypeUsage(value = Map.class,
				elementTypes = { ProcessorInputLocationNameOption.class, SakerPath.class }))
@NestFieldInformation(value = "GenerateNativeHeaders",
		info = @NestInformation(TaskDocs.COMPILE_GENERATE_NATIVE_HEADERS),
		type = @NestTypeUsage(boolean.class))

@NestFieldInformation(value = "AddExports",
		info = @NestInformation(TaskDocs.COMPILE_ADD_EXPORTS),
		type = @NestTypeUsage(value = Collection.class, elementTypes = { AddExportsPathTaskOption.class }))

@NestFieldInformation(value = "SuppressWarnings",
		info = @NestInformation(TaskDocs.COMPILE_SUPPRESS_WARNINGS),
		type = @NestTypeUsage(value = Set.class, elementTypes = JavaCompilerWarningType.class))

@NestFieldInformation(value = "SDKs",
		info = @NestInformation(TaskDocs.COMPILE_SDKS),
		type = @NestTypeUsage(value = Map.class,
				elementTypes = { saker.sdk.support.main.TaskDocs.DocSdkNameOption.class,
						SDKDescriptionTaskOption.class }))

@NestFieldInformation(value = "ModuleMainClass",
		info = @NestInformation(TaskDocs.COMPILE_MODULE_MAIN_CLASS),
		type = @NestTypeUsage(ModuleInfoInjectMainClassOption.class))
@NestFieldInformation(value = "ModuleVersion",
		info = @NestInformation(TaskDocs.COMPILE_MODULE_VERSION),
		type = @NestTypeUsage(ModuleInfoInjectVersionOption.class))

@NestFieldInformation(value = "ParameterNames",
		info = @NestInformation(TaskDocs.COMPILE_PARAMETER_NAMES),
		type = @NestTypeUsage(boolean.class))
@NestFieldInformation(value = "DebugInfo",
		info = @NestInformation(TaskDocs.COMPILE_DEBUG_INFO),
		type = @NestTypeUsage(value = Collection.class, elementTypes = JavaDebugInfoType.class))

@NestFieldInformation(value = "ParallelProcessing",
		info = @NestInformation(TaskDocs.COMPILE_PARALLEL_PROCESSING),
		type = @NestTypeUsage(boolean.class))

@NestFieldInformation(value = "BuildIncremental",
		info = @NestInformation(TaskDocs.COMPILE_BUILD_INCREMENTAL),
		type = @NestTypeUsage(boolean.class))
@NestFieldInformation(value = "AllowTargetReleaseMismatch",
		info = @NestInformation(TaskDocs.COMPILE_ALLOW_TARGET_RELEASE_MISMATCH),
		type = @NestTypeUsage(boolean.class))
@NestFieldInformation(value = "PatchEnablePreview",
		info = @NestInformation(TaskDocs.COMPILE_PATCH_ENABLE_PREVIEW),
		type = @NestTypeUsage(boolean.class))
public interface JavaCompilerOptions {
	public CompilationIdentifierTaskOption getIdentifier();

	public String getLanguage();

	public Collection<JavaSourceDirectoryTaskOption> getSourceDirectories();

	public Collection<JavaClassPathTaskOption> getClassPath();

	public Collection<JavaClassPathTaskOption> getModulePath();

	public Collection<JavaClassPathTaskOption> getBootClassPath();

	public SourceVersionTaskOption getSourceVersion();

	public SourceVersionTaskOption getTargetVersion();

	public List<String> getParameters();

	public Map<String, String> getAnnotationProcessorOptions();

	public Collection<AnnotationProcessorReferenceTaskOption> getAnnotationProcessors();

	public Map<String, SakerPath> getProcessorInputLocations();

	public Boolean getGenerateNativeHeaders();

	public Collection<AddExportsPathTaskOption> getAddExports();

	public Collection<String> getSuppressWarnings();

	public Map<String, SDKDescriptionTaskOption> getSDKs();

	public String getModuleMainClass();

	public String getModuleVersion();

	public Boolean getParameterNames();

	public Collection<String> getDebugInfo();

	public Boolean getParallelProcessing();

	public Boolean getBuildIncremental();

	public Boolean getAllowTargetReleaseMismatch();

	public Boolean getPatchEnablePreview();
}
