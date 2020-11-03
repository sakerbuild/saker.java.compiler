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
package saker.java.compiler.impl.compile;

import java.io.Externalizable;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;

import saker.build.file.SakerDirectory;
import saker.build.file.SakerFile;
import saker.build.file.content.ContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.SakerPathFiles;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.SakerLog;
import saker.build.task.Task;
import saker.build.task.TaskContext;
import saker.build.task.dependencies.FileCollectionStrategy;
import saker.build.task.identifier.TaskIdentifier;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.trace.BuildTrace;
import saker.java.compiler.api.compile.JavaCompilationWorkerTaskIdentifier;
import saker.java.compiler.api.compile.SakerJavaCompilerUtils;
import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.compile.handler.full.FullCompilationHandler;
import saker.java.compiler.impl.sdk.JavaSDKReference;
import saker.sdk.support.api.SDKDescription;
import saker.sdk.support.api.SDKReference;
import saker.sdk.support.api.SDKSupportUtils;
import saker.sdk.support.api.exc.SDKNotFoundException;
import testing.saker.java.compiler.TestFlag;

public class FullWorkerJavaCompilerTaskFactory extends WorkerJavaCompilerTaskFactoryBase {
	private static final long serialVersionUID = 1L;

	/**
	 * For {@link Externalizable}.
	 */
	public FullWorkerJavaCompilerTaskFactory() {
	}

	@Override
	public InternalJavaCompilerOutput run(TaskContext taskcontext) throws Exception {
		return compile(taskcontext);
	}

	private InternalJavaCompilerOutput compile(TaskContext taskcontext) throws IOException, Exception {
		JavaCompilationWorkerTaskIdentifier taskid = (JavaCompilationWorkerTaskIdentifier) taskcontext.getTaskId();
		String stdoutid = SakerJavaCompilerUtils.TASK_NAME_SAKER_JAVA_COMPILE + ":" + taskid.getPassIdentifier();
		taskcontext.setStandardOutDisplayIdentifier(stdoutid);

		if (saker.build.meta.Versions.VERSION_FULL_COMPOUND >= 8_006) {
			BuildTrace.classifyTask(BuildTrace.CLASSIFICATION_WORKER);
			BuildTrace.setDisplayInformation("java:" + taskid.getPassIdentifier(), stdoutid);
		}

		if (TestFlag.ENABLED) {
			TestFlag.metric().javacCompilingPass(taskid.getPassIdentifier());
		}
		final SakerDirectory outputDirectory = SakerPathFiles.requireBuildDirectory(taskcontext)
				.getDirectoryCreate(taskid.getPassIdentifier());

		final SakerDirectory outputClassDirectory = outputDirectory
				.getDirectoryCreate(SakerJavaCompilerUtils.DIR_CLASS_OUTPUT);
		final SakerDirectory outputNativeHeaderDirectory = outputDirectory
				.getDirectoryCreate(SakerJavaCompilerUtils.DIR_NATIVE_HEADER_OUTPUT);
		final SakerDirectory outputSourceDirectory = outputDirectory
				.getDirectoryCreate(SakerJavaCompilerUtils.DIR_SOURCE_OUTPUT);
		final SakerDirectory outputResourceDirectory = outputDirectory
				.getDirectoryCreate(SakerJavaCompilerUtils.DIR_RESOURCE_OUTPUT);

		//clear the directores, as we're full compiling
		outputClassDirectory.clear();
		outputNativeHeaderDirectory.clear();
		outputSourceDirectory.clear();
		outputResourceDirectory.clear();

		Collection<? extends ClassPathIDEConfigurationEntry> classpathentries = null;
		Collection<? extends ClassPathIDEConfigurationEntry> bootclasspathentries = null;
		Collection<? extends ModulePathIDEConfigurationEntry> modulepathentries = null;
		Object abiversionkey;
		Object implementationversionkey;
		NavigableMap<SakerPath, SakerFile> sourcefiles;
		NavigableMap<SakerPath, ContentDescriptor> outputfiles;
		NavigableMap<SakerPath, ContentDescriptor> processoraccessedfiles;
		NavigableMap<String, SDKReference> sdkrefs = toSDKReferences(taskcontext, sdks);
		SDKReference javasdkreference = sdkrefs.get(JavaSDKReference.DEFAULT_SDK_NAME);
		if (javasdkreference == null) {
			throw new SDKNotFoundException("No SDK found with name: " + JavaSDKReference.DEFAULT_SDK_NAME);
		}

		NavigableMap<String, SDKDescription> pinnedsdks = SDKSupportUtils.pinSDKSelection(sdks, sdkrefs);

		String modulename;
		try {
			if (sourceDirectories.isEmpty()) {
				sourcefiles = Collections.emptyNavigableMap();
				outputfiles = Collections.emptyNavigableMap();
				processoraccessedfiles = Collections.emptyNavigableMap();

				SakerLog.info().println("No Java source directories specified.");
				SakerLog.info().println();

				//TODO should fill the classpath entries in this case too
				classpathentries = Collections.emptySet();
				bootclasspathentries = Collections.emptySet();
				modulepathentries = Collections.emptySet();

				abiversionkey = NoSourcesVersionKey.INSTANCE;
				implementationversionkey = NoSourcesVersionKey.INSTANCE;
				modulename = null;
			} else {
				Set<FileCollectionStrategy> sourceAdditionDependencies = createSorceFileCollectionStrategies(
						sourceDirectories);
				sourcefiles = taskcontext.getTaskUtilities().collectFilesReportInputFileAndAdditionDependency(
						CompileFileTags.INPUT_SOURCE, sourceAdditionDependencies);
				//TODO handle here if no sources found?
				FullCompilationHandler compilationhandler = new FullCompilationHandler(taskcontext,
						outputClassDirectory, outputNativeHeaderDirectory, outputSourceDirectory,
						outputResourceDirectory);
				compilationhandler.setPassOptions(sourceVersionName, targetVersionName, javasdkreference, parameters,
						classPath, modulePath, annotationProcessorOptions, annotationProcessors, addExports, addReads,
						bootClassPath, sourcefiles, processorInputLocations, bytecodeManipulation, sdkrefs,
						parameterNames, debugInfo);
				compilationhandler.setGenerateNativeHeaders(this.generateNativeHeaders);

				try {
					compilationhandler.build();
				} catch (Exception e) {
					//synchronize the output directories to make the user able to detect errors in the generated files
					outputSourceDirectory.synchronize();
					outputResourceDirectory.synchronize();
					taskcontext.startTask(JavaTaskUtils.createJavaCompilationConfigurationOutputTaskIdentifier(taskid),
							new JavaCompilationConfigFailedTaskFactory(taskid), null);
					throw e;
				} finally {
					classpathentries = compilationhandler.getClassPathIDEConfigurationEntries();
					bootclasspathentries = compilationhandler.getBootClassPathIDEConfigurationEntries();
					modulepathentries = compilationhandler.getModulePathIDEConfigurationEntries();
				}
				abiversionkey = compilationhandler.getAbiVersionKeyHash();
				implementationversionkey = compilationhandler.getImplementationVersionKeyHash();
				modulename = compilationhandler.getModuleName();

				outputfiles = compilationhandler.getOutputFiles();
				processoraccessedfiles = compilationhandler.getProcessorAccessedFileContents();

			}
		} finally {
			//report the ide configuration even if the compilation failed
			startIdeConfigurationTask(taskcontext, taskid.getPassIdentifier(), javasdkreference,
					ObjectUtils.isNullOrEmpty(annotationProcessors) ? null : outputSourceDirectory.getSakerPath(),
					classpathentries, bootclasspathentries, outputClassDirectory.getSakerPath(), modulepathentries,
					pinnedsdks);
		}

		if (TestFlag.ENABLED) {
			Objects.requireNonNull(abiversionkey, "abiversionkey");
			Objects.requireNonNull(implementationversionkey, "implementationversionkey");
		}

		outputDirectory.synchronize();

		SimpleJavaCompilationOutputConfiguration outputconfig = new SimpleJavaCompilationOutputConfiguration(taskid,
				outputClassDirectory.getSakerPath(), outputNativeHeaderDirectory.getSakerPath(),
				outputResourceDirectory.getSakerPath(), outputSourceDirectory.getSakerPath(), modulename, pinnedsdks);
		taskcontext.startTask(JavaTaskUtils.createJavaCompilationConfigurationOutputTaskIdentifier(taskid),
				new JavaCompilationConfigLiteralTaskFactory(outputconfig), null);

		taskcontext.getTaskUtilities().reportOutputFileDependency(null, outputfiles);
		taskcontext.getTaskUtilities().reportInputFileDependency(null, processoraccessedfiles);

		InternalJavaCompilerOutputImpl output = new InternalJavaCompilerOutputImpl(sourceDirectories, classPath,
				modulePath, abiversionkey, implementationversionkey, outputconfig);
		output.setHadAnnotationProcessors(!ObjectUtils.isNullOrEmpty(annotationProcessors));
		return output;
	}

	private void startIdeConfigurationTask(TaskContext taskcontext, String compilationId, SDKReference javacompilersdk,
			SakerPath processorGenDirectory, Collection<? extends ClassPathIDEConfigurationEntry> classPathEntries,
			Collection<? extends ClassPathIDEConfigurationEntry> bootClassPathEntries, SakerPath outputBinDirectory,
			Collection<? extends ModulePathIDEConfigurationEntry> modulepathentries,
			NavigableMap<String, SDKDescription> pinnedsdks) throws Exception {
		JavaIDEConfigurationReportingTaskFactory task = new JavaIDEConfigurationReportingTaskFactory(compilationId);
		task.setAddExports(addExports);
		task.setAddReads(addReads);
		task.setCompilerJavaVersion(javacompilersdk.getProperty(JavaSDKReference.PROPERTY_JAVA_VERSION));
		task.setSourceDirectories(sourceDirectories);
		task.setProcessorGenDirectory(processorGenDirectory);
		task.setOutputBinDirectory(outputBinDirectory);
		task.setBootClassPathEntries(bootClassPathEntries);
		task.setModulePathEntries(modulepathentries);
		task.setParameters(parameters);
		task.setCompilerInstallLocation(javacompilersdk.getPath(JavaSDKReference.PATH_INSTALL_LOCATION));
		task.setClassPathEntries(classPathEntries);
		task.setSdks(pinnedsdks);

		TaskIdentifier taskid = JavaIDEConfigurationReportingTaskFactory.createTaskIdentifier(compilationId);
		taskcontext.startTask(taskid, task, null);
	}

	@Override
	public Task<? extends InternalJavaCompilerOutput> createTask(ExecutionContext executioncontext) {
		return this;
	}

}