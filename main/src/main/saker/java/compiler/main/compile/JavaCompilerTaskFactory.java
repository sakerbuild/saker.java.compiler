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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import saker.build.file.path.SakerPath;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.SakerLog;
import saker.build.task.ParameterizableTask;
import saker.build.task.TaskContext;
import saker.build.task.TaskExecutionParameters;
import saker.build.task.TaskFactory;
import saker.build.task.utils.annot.SakerInput;
import saker.build.task.utils.dependencies.EqualityTaskOutputChangeDetector;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.function.Functionals;
import saker.build.trace.BuildTrace;
import saker.compiler.utils.api.CompilationIdentifier;
import saker.compiler.utils.api.CompilerUtils;
import saker.compiler.utils.main.CompilationIdentifierTaskOption;
import saker.java.compiler.api.classpath.JavaSourceDirectory;
import saker.java.compiler.api.compile.JavaAnnotationProcessor;
import saker.java.compiler.api.compile.JavaAnnotationProcessorBuilder;
import saker.java.compiler.api.compile.JavaCompilationTaskBuilder;
import saker.java.compiler.api.compile.JavaCompilationWorkerTaskIdentifier;
import saker.java.compiler.api.compile.JavaCompilerWarningType;
import saker.java.compiler.api.compile.JavaDebugInfoType;
import saker.java.compiler.api.compile.SakerJavaCompilerUtils;
import saker.java.compiler.api.option.JavaAddExports;
import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.main.JavaTaskOptionUtils;
import saker.java.compiler.main.TaskDocs;
import saker.java.compiler.main.TaskDocs.AnnotationProcessorOptionKey;
import saker.java.compiler.main.TaskDocs.AnnotationProcessorOptionValue;
import saker.java.compiler.main.TaskDocs.CompilationParameterOption;
import saker.java.compiler.main.TaskDocs.DocJavaCompilerOutput;
import saker.java.compiler.main.TaskDocs.JavaCompilationLanguageOption;
import saker.java.compiler.main.TaskDocs.ModuleInfoInjectMainClassOption;
import saker.java.compiler.main.TaskDocs.ModuleInfoInjectVersionOption;
import saker.java.compiler.main.TaskDocs.ProcessorInputLocationNameOption;
import saker.java.compiler.main.compile.option.AddExportsPathTaskOption;
import saker.java.compiler.main.compile.option.AnnotationProcessorReferenceTaskOption;
import saker.java.compiler.main.compile.option.JavaClassPathTaskOption;
import saker.java.compiler.main.compile.option.JavaCompilerOptions;
import saker.java.compiler.main.compile.option.JavaSourceDirectoryTaskOption;
import saker.java.compiler.main.compile.option.SimpleJavaCompilerOptions;
import saker.java.compiler.main.compile.option.SourceVersionTaskOption;
import saker.nest.scriptinfo.reflection.annot.NestInformation;
import saker.nest.scriptinfo.reflection.annot.NestParameterInformation;
import saker.nest.scriptinfo.reflection.annot.NestTaskInformation;
import saker.nest.scriptinfo.reflection.annot.NestTypeUsage;
import saker.nest.utils.FrontendTaskFactory;
import saker.sdk.support.api.SDKDescription;
import saker.sdk.support.api.SDKSupportUtils;
import saker.sdk.support.main.option.SDKDescriptionTaskOption;
import testing.saker.java.compiler.TestFlag;

@NestTaskInformation(returnType = @NestTypeUsage(value = DocJavaCompilerOutput.class))
@NestInformation(value = "Compiles Java sources.\n"
		+ "Use the SourceDirectories parameter to specify the directories containing the .java sources to compile.")

@NestParameterInformation(value = "Identifier",
		info = @NestInformation(TaskDocs.COMPILE_IDENTIFIER),
		type = @NestTypeUsage(CompilationIdentifierTaskOption.class))
@NestParameterInformation(value = "Language",
		info = @NestInformation(TaskDocs.COMPILE_LANGUAGE),
		type = @NestTypeUsage(JavaCompilationLanguageOption.class))

@NestParameterInformation(value = "SourceDirectories",
		aliases = { "", "SourceDirectory" },
		info = @NestInformation(TaskDocs.COMPILE_SOURCE_DIRECTORY),
		type = @NestTypeUsage(value = Collection.class, elementTypes = JavaSourceDirectoryTaskOption.class))

@NestParameterInformation(value = "ClassPath",
		info = @NestInformation(TaskDocs.COMPILE_CLASS_PATH),
		type = @NestTypeUsage(value = Collection.class, elementTypes = JavaClassPathTaskOption.class))

@NestParameterInformation(value = "BootClassPath",
		info = @NestInformation(TaskDocs.COMPILE_BOOT_CLASS_PATH),
		type = @NestTypeUsage(value = Collection.class, elementTypes = JavaClassPathTaskOption.class))

@NestParameterInformation(value = "ModulePath",
		info = @NestInformation(TaskDocs.COMPILE_MODULE_PATH),
		type = @NestTypeUsage(value = Collection.class, elementTypes = JavaClassPathTaskOption.class))

@NestParameterInformation(value = "SourceVersion",
		info = @NestInformation(TaskDocs.COMPILE_SOURCE_VERSION),
		type = @NestTypeUsage(value = SourceVersionTaskOption.class))
@NestParameterInformation(value = "TargetVersion",
		info = @NestInformation(TaskDocs.COMPILE_TARGET_VERSION),
		type = @NestTypeUsage(value = SourceVersionTaskOption.class))

@NestParameterInformation(value = "Parameters",
		info = @NestInformation(TaskDocs.COMPILE_PARAMETERS),
		type = @NestTypeUsage(value = List.class, elementTypes = CompilationParameterOption.class))

@NestParameterInformation(value = "AnnotationProcessorOptions",
		info = @NestInformation(TaskDocs.COMPILE_ANNOTATION_PROCESSOR_OPTIONS),
		type = @NestTypeUsage(value = Map.class,
				elementTypes = { AnnotationProcessorOptionKey.class, AnnotationProcessorOptionValue.class }))
@NestParameterInformation(value = "AnnotationProcessors",
		info = @NestInformation(TaskDocs.COMPILE_ANNOTATION_PROCESSORS),
		type = @NestTypeUsage(value = Collection.class, elementTypes = AnnotationProcessorReferenceTaskOption.class))
@NestParameterInformation(value = "ProcessorInputLocations",
		info = @NestInformation(TaskDocs.COMPILE_PROCESSOR_INPUT_LOCATIONS),
		type = @NestTypeUsage(value = Map.class,
				elementTypes = { ProcessorInputLocationNameOption.class, SakerPath.class }))
@NestParameterInformation(value = "GenerateNativeHeaders",
		info = @NestInformation(TaskDocs.COMPILE_GENERATE_NATIVE_HEADERS),
		type = @NestTypeUsage(boolean.class))

@NestParameterInformation(value = "AddExports",
		info = @NestInformation(TaskDocs.COMPILE_ADD_EXPORTS),
		type = @NestTypeUsage(value = Collection.class, elementTypes = { AddExportsPathTaskOption.class }))

@NestParameterInformation(value = "SuppressWarnings",
		info = @NestInformation(TaskDocs.COMPILE_SUPPRESS_WARNINGS),
		type = @NestTypeUsage(value = Set.class, elementTypes = JavaCompilerWarningType.class))

@NestParameterInformation(value = TaskDocs.PARAM_NAME_COMPILER_OPTIONS,
		info = @NestInformation(TaskDocs.COMPILE_COMPILER_OPTIONS),
		type = @NestTypeUsage(value = Collection.class, elementTypes = JavaCompilerOptions.class))

@NestParameterInformation(value = "SDKs",
		info = @NestInformation(TaskDocs.COMPILE_SDKS),
		type = @NestTypeUsage(value = Map.class,
				elementTypes = { saker.sdk.support.main.TaskDocs.DocSdkNameOption.class,
						SDKDescriptionTaskOption.class }))

@NestParameterInformation(value = "ModuleMainClass",
		info = @NestInformation(TaskDocs.COMPILE_MODULE_MAIN_CLASS),
		type = @NestTypeUsage(ModuleInfoInjectMainClassOption.class))
@NestParameterInformation(value = "ModuleVersion",
		info = @NestInformation(TaskDocs.COMPILE_MODULE_VERSION),
		type = @NestTypeUsage(ModuleInfoInjectVersionOption.class))

@NestParameterInformation(value = "ParameterNames",
		info = @NestInformation(TaskDocs.COMPILE_PARAMETER_NAMES),
		type = @NestTypeUsage(boolean.class))
@NestParameterInformation(value = "DebugInfo",
		info = @NestInformation(TaskDocs.COMPILE_DEBUG_INFO),
		type = @NestTypeUsage(value = Collection.class, elementTypes = JavaDebugInfoType.class))

@NestParameterInformation(value = "ParallelProcessing",
		info = @NestInformation(TaskDocs.COMPILE_PARALLEL_PROCESSING),
		type = @NestTypeUsage(boolean.class))

@NestParameterInformation(value = "BuildIncremental",
		info = @NestInformation(TaskDocs.COMPILE_BUILD_INCREMENTAL),
		type = @NestTypeUsage(boolean.class))
@NestParameterInformation(value = "AllowTargetReleaseMismatch",
		info = @NestInformation(TaskDocs.COMPILE_ALLOW_TARGET_RELEASE_MISMATCH),
		type = @NestTypeUsage(boolean.class))
public class JavaCompilerTaskFactory extends FrontendTaskFactory<Object> {
	private static final long serialVersionUID = 1L;

	public static final String TASK_NAME = SakerJavaCompilerUtils.TASK_NAME_SAKER_JAVA_COMPILE;

	//TODO create a test for the following:
	//    aggregating annotation processor, taking annotation @Annot
	//    compile a few classes with @Annot
	//    modify some ABI that is unrelated to the dependent Elements
	//    make sure the annotation processor is not invoked.
	//XXX the implementation version key hashes are calculated twice. once when they are calculated, and 
	//    once when the content descriptors of the class files are determined

	@Override
	public ParameterizableTask<? extends Object> createTask(ExecutionContext executioncontext) {
		return new JavaCompilerTask();
	}

	public static class JavaCompilerTask implements ParameterizableTask<Object> {

		@SakerInput(value = { "", "SourceDirectory", "SourceDirectories" })
		public Collection<JavaSourceDirectoryTaskOption> sourceDirectoriesOption;

		@SakerInput(value = { "ClassPath" })
		public Collection<JavaClassPathTaskOption> classPathOption;

		@SakerInput(value = { "ModulePath" })
		public Collection<JavaClassPathTaskOption> modulePathOption;

		@SakerInput(value = { "BootClassPath" })
		public Collection<JavaClassPathTaskOption> bootClassPathOption;

		@SakerInput(value = { "SourceVersion" })
		public SourceVersionTaskOption sourceVersionOption;

		@SakerInput(value = { "TargetVersion" })
		public SourceVersionTaskOption targetVersionOption;

		@SakerInput(value = { "Parameters" })
		public List<String> parametersOption;

		@SakerInput(value = { "AnnotationProcessorOptions" })
		public Map<String, String> annotationProcessorOptionsOption;

		@SakerInput(value = { "AnnotationProcessors" })
		public Collection<AnnotationProcessorReferenceTaskOption> annotationProcessorsOption;

		@SakerInput(value = { "ProcessorInputLocations" })
		public Map<String, SakerPath> processorInputLocationsOption;

		@SakerInput(value = { "GenerateNativeHeaders" })
		public boolean generateNativeHeadersOption = false;

		@SakerInput(value = { "AddExports" })
		public Collection<AddExportsPathTaskOption> addExportsOption;

		@SakerInput(value = { "SuppressWarnings" })
		public Collection<String> suppressWarningsOption;

		@SakerInput(value = { "SDKs" })
		public Map<String, SDKDescriptionTaskOption> sdksOption;

		@SakerInput(value = { "ModuleMainClass" })
		public String moduleMainClassOption;

		@SakerInput(value = { "ModuleVersion" })
		public String moduleVersionOption;

		@SakerInput(value = { "ParallelProcessing" })
		public Boolean parallelProcessingOption;

		@SakerInput(value = { "BuildIncremental" })
		public Boolean buildIncrementalOption;

		@SakerInput(value = { "ParameterNames" })
		public Boolean parameterNamesOption;

		@SakerInput(value = { "DebugInfo" })
		public Collection<String> debugInfoOption;

		@SakerInput(value = { "Identifier" })
		public CompilationIdentifierTaskOption identifierOption;

		@SakerInput(value = { "Language" })
		public String languageOption;

		@SakerInput(value = { TaskDocs.PARAM_NAME_COMPILER_OPTIONS })
		public Collection<JavaCompilerOptions> compilerOptions = Collections.emptyList();

		@SakerInput(value = { "AllowTargetReleaseMismatch" })
		public Boolean allowTargetReleaseMismatchOption;

		@Override
		public Object run(TaskContext taskcontext) throws Exception {
			if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
				BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_FRONTEND);
			}

			SimpleJavaCompilerOptions options = new SimpleJavaCompilerOptions();
			options.setIdentifier(ObjectUtils.clone(identifierOption, CompilationIdentifierTaskOption::clone));
			options.setLanguage(languageOption);

			options.setSourceDirectories(
					ObjectUtils.cloneArrayList(this.sourceDirectoriesOption, JavaSourceDirectoryTaskOption::clone));

			options.setClassPath(ObjectUtils.cloneArrayList(this.classPathOption, JavaClassPathTaskOption::clone));
			options.setModulePath(ObjectUtils.cloneArrayList(this.modulePathOption, JavaClassPathTaskOption::clone));
			options.setBootClassPath(
					ObjectUtils.cloneArrayList(this.bootClassPathOption, JavaClassPathTaskOption::clone));
			options.setSourceVersion(ObjectUtils.clone(this.sourceVersionOption, SourceVersionTaskOption::clone));
			options.setTargetVersion(ObjectUtils.clone(this.targetVersionOption, SourceVersionTaskOption::clone));
			options.setParameters(ImmutableUtils.makeImmutableList(this.parametersOption));
			options.setAnnotationProcessors(ObjectUtils.cloneArrayList(this.annotationProcessorsOption,
					AnnotationProcessorReferenceTaskOption::clone));
			options.setAnnotationProcessorOptions(ObjectUtils.cloneTreeMap(this.annotationProcessorOptionsOption));
			options.setProcessorInputLocations(ObjectUtils.cloneTreeMap(this.processorInputLocationsOption));
			options.setGenerateNativeHeaders(this.generateNativeHeadersOption);
			options.setAddExports(ObjectUtils.cloneArrayList(this.addExportsOption, AddExportsPathTaskOption::clone));
			options.setSuppressWarnings(
					JavaTaskUtils.makeImmutableIgnoreCaseNullableStringCollection(this.suppressWarningsOption));
			options.setSDKs(ObjectUtils.cloneTreeMap(this.sdksOption, Functionals.identityFunction(),
					SDKDescriptionTaskOption::clone));
			options.setModuleMainClass(this.moduleMainClassOption);
			options.setModuleVersion(this.moduleVersionOption);
			options.setParallelProcessing(this.parallelProcessingOption);
			options.setBuildIncremental(this.buildIncrementalOption);
			options.setDebugInfo(debugInfoOption);
			options.setParameterNames(parameterNamesOption);
			options.setAllowTargetReleaseMismatch(allowTargetReleaseMismatchOption);

			return runPass(taskcontext, options,
					ObjectUtils.cloneArrayList(compilerOptions, SimpleJavaCompilerOptions::new));
		}

		public static Object runPass(TaskContext taskcontext, SimpleJavaCompilerOptions p,
				Collection<? extends JavaCompilerOptions> options) throws Exception {
			if (p.getLanguage() == null) {
				//initialize it if not set by the user
				p.setLanguage("Java");
			}

			CompilationIdentifier passid = CompilationIdentifierTaskOption.getIdentifier(p.getIdentifier());

			for (JavaCompilerOptions opt : options) {
				if (opt == null) {
					continue;
				}
				CompilationIdentifierTaskOption optid = opt.getIdentifier();
				if (!CompilerUtils.canMergeIdentifiers(passid, optid == null ? null : optid.getIdentifier())) {
					continue;
				}
				if (!CompilerUtils.canMergeLanguages(p.getLanguage(), opt.getLanguage())) {
					continue;
				}
				p = new SimpleJavaCompilerOptions(p, opt);
			}

			SakerPath workingdirpath = taskcontext.getTaskWorkingDirectoryPath();

			JavaCompilationTaskBuilder taskbuilder = JavaCompilationTaskBuilder.newBuilder();
			if (Boolean.TRUE.equals(p.getAllowTargetReleaseMismatch())) {
				taskbuilder.setAllowTargetReleaseMismatch(true);
			}

			if (passid != null) {
				taskbuilder.setCompilationIdentifier(passid);
			} else {
				taskbuilder.setAutomaticCompilationIdentifier(taskcontext);
			}
			taskbuilder.setParameters(p.getParameters());
			SourceVersionTaskOption srcversion = p.getSourceVersion();
			if (srcversion != null) {
				taskbuilder.setSourceVersion(srcversion.getVersion());
			}
			SourceVersionTaskOption targetversion = p.getTargetVersion();
			if (targetversion != null) {
				taskbuilder.setTargetVersion(targetversion.getVersion());
			}
			taskbuilder.setParallelProcessing(p.getParallelProcessing());
			taskbuilder.setAnnotationProcessorOptions(p.getAnnotationProcessorOptions());
			taskbuilder.setSuppressWarnings(p.getSuppressWarnings());
			Map<String, SakerPath> procinputlocations = p.getProcessorInputLocations();
			if (!ObjectUtils.isNullOrEmpty(procinputlocations)) {
				Map<String, SakerPath> proclocations = new TreeMap<>();
				for (Entry<String, SakerPath> entry : procinputlocations.entrySet()) {
					proclocations.put(entry.getKey(), workingdirpath.tryResolve(entry.getValue()));
				}
				taskbuilder.setProcessorInputLocations(proclocations);
			}
			taskbuilder.setModuleMainClass(p.getModuleMainClass());
			taskbuilder.setModuleVersion(p.getModuleVersion());
			taskbuilder.setGenerateNativeHeaders(p.getGenerateNativeHeaders());
			taskbuilder.setBuildIncremental(p.getBuildIncremental());

			Collection<JavaSourceDirectoryTaskOption> sourcedirs = p.getSourceDirectories();
			boolean hadsourcedir = false;
			if (!ObjectUtils.isNullOrEmpty(sourcedirs)) {
				Set<JavaSourceDirectory> setsourcedirs = new LinkedHashSet<>();
				for (JavaSourceDirectoryTaskOption srcdir : sourcedirs) {
					if (srcdir == null) {
						continue;
					}
					SakerPath dir = srcdir.getDirectory();
					Objects.requireNonNull(dir, "source directory");
					setsourcedirs.add(JavaSourceDirectory.create(workingdirpath.tryResolve(dir), srcdir.getFiles()));
					hadsourcedir = true;
				}
				taskbuilder.setSourceDirectories(setsourcedirs);
			}
			if (!hadsourcedir) {
				SakerLog.warning().taskScriptPosition(taskcontext)
						.println("No Java source directories specified for compilation.");
			}
			taskbuilder.setClassPath(JavaTaskOptionUtils.createClassPath(taskcontext, p.getClassPath()));
			taskbuilder.setBootClassPath(JavaTaskOptionUtils.createClassPath(taskcontext, p.getBootClassPath()));
			taskbuilder.setModulePath(JavaTaskOptionUtils.createModulePath(taskcontext, p.getModulePath()));
			taskbuilder.setParameterNames(p.getParameterNames());
			taskbuilder.setDebugInfo(p.getDebugInfo());
			Collection<? extends AddExportsPathTaskOption> addexports = p.getAddExports();
			if (!ObjectUtils.isNullOrEmpty(addexports)) {
				Collection<JavaAddExports> setaddexports = new HashSet<>();
				NavigableSet<String> packagebuf = new TreeSet<>();
				NavigableSet<String> targetbuf = new TreeSet<>();
				for (AddExportsPathTaskOption aepopt : addexports) {
					if (aepopt == null) {
						continue;
					}
					JavaAddExports aep = aepopt.toAddExportsPath(taskcontext);
					Collection<String> packages = aep.getPackage();
					Collection<String> target = aep.getTarget();
					packagebuf.clear();
					target.clear();
					if (!ObjectUtils.isNullOrEmpty(packages)) {
						for (String pkgstr : packages) {
							if (ObjectUtils.isNullOrEmpty(pkgstr)) {
								continue;
							}
							packagebuf.add(pkgstr);
						}
					}
					if (!ObjectUtils.isNullOrEmpty(target)) {
						for (String targetstr : target) {
							if (ObjectUtils.isNullOrEmpty(targetstr)) {
								continue;
							}
							targetbuf.add(targetstr);
						}
					}

					setaddexports.add(JavaAddExports.create(aep.getModule(), packagebuf, targetbuf));
				}
				taskbuilder.setAddExports(setaddexports);
			}
			Collection<AnnotationProcessorReferenceTaskOption> annotationprocessors = p.getAnnotationProcessors();
			if (!ObjectUtils.isNullOrEmpty(annotationprocessors)) {
				Collection<JavaAnnotationProcessor> setprocessors = new LinkedHashSet<>();
				for (AnnotationProcessorReferenceTaskOption apropt : annotationprocessors) {
					if (apropt == null) {
						continue;
					}
					JavaAnnotationProcessor apr = apropt.toJavaAnnotationProcessor(taskcontext);
					JavaAnnotationProcessorBuilder apbuilder = JavaAnnotationProcessorBuilder.newBuilder();
					apbuilder.setProcessor(apr.getProcessor());
					apbuilder.setOptions(apr.getOptions());
					apbuilder.setSuppressWarnings(apr.getSuppressWarnings());
					apbuilder.setAggregating(apr.getAggregating());
					apbuilder.setConsistent(apr.getConsistent());
					apbuilder.setAlwaysRun(apr.getAlwaysRun());

					setprocessors.add(apbuilder.build());
				}
				taskbuilder.setAnnotationProcessors(setprocessors);
			}
			Map<String, SDKDescriptionTaskOption> sdks = p.getSDKs();
			NavigableMap<String, SDKDescription> presentsdks = new TreeMap<>(SDKSupportUtils.getSDKNameComparator());
			if (!ObjectUtils.isNullOrEmpty(sdks)) {
				for (Entry<String, SDKDescriptionTaskOption> entry : sdks.entrySet()) {
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
					SDKDescription prev = presentsdks.put(entry.getKey(), desc[0]);
					if (prev != null && !prev.equals(desc[0])) {
						throw new IllegalArgumentException("Multiple different SDKs defined for name: " + entry.getKey()
								+ " with " + prev + " and " + desc[0]);
					}
				}
				taskbuilder.setSDKs(presentsdks);
			}

			TaskFactory<?> workerfactory = taskbuilder.buildTaskFactory();
			JavaCompilationWorkerTaskIdentifier workertaskid = taskbuilder.buildTaskIdentifier();

			if (TestFlag.ENABLED) {
				TestFlag.metric().javacCompilerBootTaskInvoked(workertaskid.getPassIdentifier());
			}

			TaskExecutionParameters workertaskparameters = new TaskExecutionParameters();
			workertaskparameters.setBuildDirectory(SakerPath.valueOf(TASK_NAME));

			taskcontext.startTask(workertaskid, workerfactory, workertaskparameters);

			Object result = new JavaCompilerTaskFrontendOutputImpl(workertaskid);
			taskcontext.reportSelfTaskOutputChangeDetector(new EqualityTaskOutputChangeDetector(result));
			return result;
		}
	}

}
