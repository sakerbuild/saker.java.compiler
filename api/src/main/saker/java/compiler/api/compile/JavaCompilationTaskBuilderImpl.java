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
package saker.java.compiler.api.compile;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Supplier;

import saker.build.exception.InvalidPathFormatException;
import saker.build.file.path.SakerPath;
import saker.build.task.TaskDirectoryContext;
import saker.build.task.TaskFactory;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.function.Functionals;
import saker.build.thirdparty.saker.util.function.LazySupplier;
import saker.compiler.utils.api.CompilationIdentifier;
import saker.java.compiler.api.classpath.JavaClassPath;
import saker.java.compiler.api.classpath.JavaSourceDirectory;
import saker.java.compiler.api.modulepath.JavaModulePath;
import saker.java.compiler.api.option.JavaAddExports;
import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.compile.FullWorkerJavaCompilerTaskFactory;
import saker.java.compiler.impl.compile.IncrementalWorkerJavaCompilerTaskFactory;
import saker.java.compiler.impl.compile.WorkerJavaCompilerTaskFactoryBase;
import saker.java.compiler.impl.options.SimpleAddExportsPath;
import saker.java.compiler.impl.options.SimpleAnnotationProcessorReferenceOption;
import saker.java.compiler.impl.options.SimpleJavaSourceDirectoryOption;
import saker.java.compiler.impl.sdk.JavaSDKReference;
import saker.sdk.support.api.SDKDescription;
import saker.sdk.support.api.SDKSupportUtils;

final class JavaCompilationTaskBuilderImpl implements JavaCompilationTaskBuilder {
	private static final NavigableMap<String, SDKDescription> DEFAULT_SDKS_MAP = ImmutableUtils.singletonNavigableMap(
			JavaSDKReference.DEFAULT_SDK_NAME, SakerJavaCompilerUtils.getDefaultJavaSDK(),
			SDKSupportUtils.getSDKNameComparator());

	protected Supplier<CompilationIdentifier> compilationIdentifier;
	protected List<String> parameters;
	protected String sourceVersionName;
	protected String targetVersionName;
	protected boolean parallelProcessing;
	protected NavigableMap<String, String> annotationProcessorOptions;
	protected NavigableSet<String> suppressWarnings;
	protected NavigableMap<String, SDKDescription> sdks = DEFAULT_SDKS_MAP;

	protected NavigableMap<String, SakerPath> processorInputLocations;

	protected Set<JavaSourceDirectory> sourceDirectories = Collections.emptySet();

	protected String moduleMainClass;
	protected String moduleVersion;

	protected Set<JavaAnnotationProcessor> annotationProcessors;
	protected Set<JavaAddExports> addExports;

	protected JavaClassPath classPath;
	protected JavaClassPath bootClassPath;
	protected JavaModulePath modulePath;

	protected boolean parameterNames = true;

	protected boolean generateNativeHeaders = false;
	protected boolean buildIncremental = true;

	protected NavigableSet<String> debugInfo;

	public JavaCompilationTaskBuilderImpl() {
	}

	@Override
	public void setSDKs(Map<String, SDKDescription> sdks) {
		if (sdks == null) {
			this.sdks = DEFAULT_SDKS_MAP;
			return;
		}

		NavigableMap<String, SDKDescription> setsdks;
		setsdks = new TreeMap<>(SDKSupportUtils.getSDKNameComparator());
		setsdks.putAll(DEFAULT_SDKS_MAP);
		setsdks.putAll(sdks);
		setsdks = ImmutableUtils.unmodifiableNavigableMap(setsdks);

		this.sdks = setsdks;
	}

	@Override
	public void setAutomaticCompilationIdentifier(TaskDirectoryContext taskdircontext) {
		SakerPath workingdir = taskdircontext == null ? null : taskdircontext.getTaskWorkingDirectoryPath();
		this.compilationIdentifier = LazySupplier.of(() -> generateCompilationIdentifier(workingdir));
	}

	@Override
	public void setCompilationIdentifier(CompilationIdentifier compilationIdentifier) {
		Objects.requireNonNull(compilationIdentifier, "compilation identifier");
		this.compilationIdentifier = Functionals.valSupplier(CompilationIdentifier.valueOf(compilationIdentifier));
	}

	@Override
	public void setParameters(List<String> parameters) {
		this.parameters = ImmutableUtils.makeImmutableList(parameters);
	}

	@Override
	public void setSourceVersion(Integer version) {
		if (version == null) {
			this.sourceVersionName = null;
			return;
		}
		this.sourceVersionName = "RELEASE_" + version;
	}

	@Override
	public void setTargetVersion(Integer version) {
		if (version == null) {
			this.targetVersionName = null;
			return;
		}
		this.targetVersionName = "RELEASE_" + version;
	}

	@Override
	public void setParallelProcessing(Boolean parallelProcessing) {
		this.parallelProcessing = ObjectUtils.defaultize(parallelProcessing, true);
	}

	@Override
	public void setAnnotationProcessorOptions(Map<String, String> annotationProcessorOptions) {
		this.annotationProcessorOptions = ImmutableUtils.makeImmutableNavigableMap(annotationProcessorOptions);
	}

	@Override
	public void setSuppressWarnings(Collection<String> suppressWarnings) {
		this.suppressWarnings = JavaTaskUtils.makeImmutableIgnoreCaseNullableStringCollection(suppressWarnings);
	}

	@Override
	public void setProcessorInputLocations(Map<String, SakerPath> processorInputLocations) {
		if (processorInputLocations != null) {
			for (Entry<String, SakerPath> entry : processorInputLocations.entrySet()) {
				Objects.requireNonNull(entry.getKey(), "processor input location name");
				SakerPath path = Objects.requireNonNull(entry.getValue(), "processor input location path");
				if (!path.isAbsolute()) {
					throw new InvalidPathFormatException("Processor input location path is not absolute: "
							+ entry.getKey() + " - " + entry.getValue());
				}
			}
		}
		this.processorInputLocations = ImmutableUtils.makeImmutableNavigableMap(processorInputLocations);
	}

	@Override
	public void setModuleMainClass(String moduleMainClass) {
		this.moduleMainClass = moduleMainClass;
	}

	@Override
	public void setModuleVersion(String moduleVersion) {
		this.moduleVersion = moduleVersion;
	}

	@Override
	public void setSourceDirectories(Collection<? extends JavaSourceDirectory> sourceDirectories) {
		if (sourceDirectories == null) {
			this.sourceDirectories = Collections.emptySet();
		} else {
			this.sourceDirectories = JavaTaskUtils.cloneImmutableLinkedHashSet(sourceDirectories,
					SimpleJavaSourceDirectoryOption::new);
		}
	}

	@Override
	public void setClassPath(JavaClassPath classPath) {
		this.classPath = classPath;
	}

	@Override
	public void setBootClassPath(JavaClassPath bootClassPath) {
		this.bootClassPath = bootClassPath;
	}

	@Override
	public void setModulePath(JavaModulePath modulePath) {
		this.modulePath = modulePath;
	}

	@Override
	public void setAddExports(Collection<? extends JavaAddExports> addExports) {
		this.addExports = JavaTaskUtils.cloneImmutableHashSet(addExports, SimpleAddExportsPath::new);
	}

	@Override
	public void setAnnotationProcessors(Collection<? extends JavaAnnotationProcessor> annotationProcessors) {
		this.annotationProcessors = JavaTaskUtils.cloneImmutableLinkedHashSet(annotationProcessors,
				SimpleAnnotationProcessorReferenceOption::new);
	}

	@Override
	public void setGenerateNativeHeaders(Boolean generateNativeHeaders) {
		this.generateNativeHeaders = ObjectUtils.defaultize(generateNativeHeaders, false);
	}

	@Override
	public void setBuildIncremental(Boolean buildIncremental) {
		this.buildIncremental = ObjectUtils.defaultize(buildIncremental, true);
	}

	@Override
	public void setParameterNames(Boolean parameterNames) {
		this.parameterNames = ObjectUtils.defaultize(parameterNames, true);
	}

	@Override
	public void setDebugInfo(Collection<String> debugInfo) {
		this.debugInfo = JavaTaskUtils.makeImmutableIgnoreCaseNullableStringCollection(debugInfo);
	}

	@Override
	public JavaCompilationWorkerTaskIdentifier buildTaskIdentifier() {
		String passidstr;
		if (compilationIdentifier == null) {
			passidstr = generateCompilationIdentifier(null).toString();
		} else {
			passidstr = compilationIdentifier.get().toString();
		}
		return JavaCompilationWorkerTaskIdentifier.create(passidstr);
	}

	@Override
	public TaskFactory<? extends JavaCompilerWorkerTaskOutput> buildTaskFactory() {
		WorkerJavaCompilerTaskFactoryBase workertask;
		if (buildIncremental) {
			IncrementalWorkerJavaCompilerTaskFactory task = new IncrementalWorkerJavaCompilerTaskFactory();
			workertask = task;
			task.setParallelProcessing(parallelProcessing);
			task.setSuppressWarnings(suppressWarnings);
		} else {
			FullWorkerJavaCompilerTaskFactory task = new FullWorkerJavaCompilerTaskFactory();
			workertask = task;
		}
		workertask.setSourceDirectories(sourceDirectories);
		workertask.setClassPath(classPath);
		workertask.setModulePath(modulePath);
		workertask.setBootClassPath(bootClassPath);
		workertask.setParameters(parameters);
		workertask.setSourceVersionName(sourceVersionName);
		workertask.setTargetVersionName(targetVersionName);
		if (!ObjectUtils.isNullOrEmpty(annotationProcessors)) {
			workertask.setAnnotationProcessors(annotationProcessors);
			workertask.setProcessorInputLocations(processorInputLocations);
			workertask.setAnnotationProcessorOptions(annotationProcessorOptions);
		}
		workertask.setModuleMainClass(moduleMainClass);
		workertask.setModuleVersion(moduleVersion);
		workertask.setAddExports(addExports);
		workertask.setGenerateNativeHeaders(generateNativeHeaders);
		workertask.setParameterNames(parameterNames);
		workertask.setDebugInfo(debugInfo);
		workertask.setSDKs(this.sdks);
		return workertask;
	}

	private CompilationIdentifier generateCompilationIdentifier(SakerPath taskworkingdir) {
		Set<String> dirnames = new TreeSet<>();
		for (JavaSourceDirectory srcdir : sourceDirectories) {
			String dirfilename = srcdir.getDirectory().getFileName();
			if (dirfilename != null) {
				dirnames.add(dirfilename);
			}
		}
		String passidstring;
		String wdfilename = taskworkingdir == null ? null : taskworkingdir.getFileName();
		if (wdfilename != null) {
			passidstring = wdfilename + StringUtils.toStringJoin("-", "-", dirnames, null);
		} else {
			passidstring = StringUtils.toStringJoin("-", dirnames);
		}
		if (passidstring.isEmpty()) {
			passidstring = "default";
		}
		return CompilationIdentifier.valueOf(passidstring);
	}

}