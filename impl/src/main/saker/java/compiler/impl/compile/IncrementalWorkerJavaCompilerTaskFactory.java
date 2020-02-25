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
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Collection;
import java.util.Collections;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.processing.Processor;

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
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.TransformingNavigableMap;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.build.thirdparty.saker.util.thread.ThreadUtils;
import saker.build.thirdparty.saker.util.thread.ThreadUtils.ThreadWorkPool;
import saker.build.trace.BuildTrace;
import saker.java.compiler.api.compile.JavaAnnotationProcessor;
import saker.java.compiler.api.compile.JavaCompilationWorkerTaskIdentifier;
import saker.java.compiler.api.compile.JavaCompilerWarningType;
import saker.java.compiler.api.compile.SakerJavaCompilerUtils;
import saker.java.compiler.api.processor.ProcessorCreationContext;
import saker.java.compiler.api.processor.ProcessorCreator;
import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.compile.handler.incremental.IncrementalCompilationHandler;
import saker.java.compiler.impl.compile.handler.incremental.IncrementalCompilationInfo;
import saker.java.compiler.impl.compile.handler.incremental.NativeHeaderGeneratorProcessor;
import saker.java.compiler.impl.compile.handler.incremental.ProcessorReadResourceDependencyTag;
import saker.java.compiler.impl.compile.handler.info.CompilationInfo;
import saker.java.compiler.impl.compile.handler.info.CompilationInfo.ProcessorData;
import saker.java.compiler.impl.compile.handler.info.FileData;
import saker.java.compiler.impl.compile.handler.invoker.ProcessorDetails;
import saker.java.compiler.impl.options.SimpleAnnotationProcessorReferenceOption;
import saker.java.compiler.impl.options.SimpleProcessorConfiguration;
import saker.java.compiler.impl.sdk.JavaSDKReference;
import saker.sdk.support.api.IndeterminateSDKDescription;
import saker.sdk.support.api.SDKDescription;
import saker.sdk.support.api.SDKReference;
import saker.sdk.support.api.SDKSupportUtils;
import saker.sdk.support.api.exc.SDKNotFoundException;
import testing.saker.java.compiler.TestFlag;

public class IncrementalWorkerJavaCompilerTaskFactory extends WorkerJavaCompilerTaskFactoryBase {
	private static final long serialVersionUID = 1L;

	protected boolean parallelProcessing;
	protected Collection<String> suppressWarnings;

	/**
	 * For {@link Externalizable}.
	 */
	public IncrementalWorkerJavaCompilerTaskFactory() {
	}

	public void setParallelProcessing(boolean parallelProcessing) {
		this.parallelProcessing = parallelProcessing;
	}

	public void setSuppressWarnings(Collection<String> suppressWarnings) {
		this.suppressWarnings = suppressWarnings;
	}

	@Override
	public int getRequestedComputationTokenCount() {
		return 1;
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

		CompilationInfo previnfo = taskcontext.getPreviousTaskOutput(CompilationInfo.class, CompilationInfo.class);
		if (previnfo == null) {
			outputDirectory.clear();
		}

		final SakerDirectory outputClassDirectory = outputDirectory
				.getDirectoryCreate(SakerJavaCompilerUtils.DIR_CLASS_OUTPUT);
		final SakerDirectory outputNativeHeaderDirectory = outputDirectory
				.getDirectoryCreate(SakerJavaCompilerUtils.DIR_NATIVE_HEADER_OUTPUT);
		final SakerDirectory outputSourceDirectory = outputDirectory
				.getDirectoryCreate(SakerJavaCompilerUtils.DIR_SOURCE_OUTPUT);
		final SakerDirectory outputResourceDirectory = outputDirectory
				.getDirectoryCreate(SakerJavaCompilerUtils.DIR_RESOURCE_OUTPUT);

		CompilationInfo info;
		Collection<? extends ClassPathIDEConfigurationEntry> classpathentries = null;
		Collection<? extends ClassPathIDEConfigurationEntry> bootclasspathentries = null;
		Collection<? extends ModulePathIDEConfigurationEntry> modulepathentries = null;
		NavigableMap<String, SDKReference> sdkrefs = toSDKReferences(taskcontext, sdks);
		SDKReference javasdkreference = sdkrefs.get(JavaSDKReference.DEFAULT_SDK_NAME);
		if (javasdkreference == null) {
			throw new SDKNotFoundException("No SDK found with name: " + JavaSDKReference.DEFAULT_SDK_NAME);
		}

		NavigableMap<String, SDKDescription> pinnedsdks = new TreeMap<>(SDKSupportUtils.getSDKNameComparator());
		for (Entry<String, SDKReference> entry : sdkrefs.entrySet()) {
			String sdkname = entry.getKey();
			SDKDescription desc = sdks.get(sdkname);
			if (desc instanceof IndeterminateSDKDescription) {
				desc = ((IndeterminateSDKDescription) desc).pinSDKDescription(entry.getValue());
			}
			pinnedsdks.put(sdkname, desc);
		}

		String modulename;
		try {
			if (sourceDirectories.isEmpty()) {
				SakerLog.info().println("No Java source directories specified.");
				// clean info
				info = new IncrementalCompilationInfo();

				//clear the directores 
				outputClassDirectory.clear();
				outputNativeHeaderDirectory.clear();
				outputSourceDirectory.clear();
				outputResourceDirectory.clear();
				//TODO should fill the classpath entries in this case too
				classpathentries = Collections.emptySet();
				bootclasspathentries = Collections.emptySet();
				modulepathentries = Collections.emptySet();

				info.setAbiVersionKey(NoSourcesVersionKey.INSTANCE);
				info.setImplementationVersionKey(NoSourcesVersionKey.INSTANCE);
				modulename = null;
			} else {
				Set<FileCollectionStrategy> sourceAdditionDependencies = createSorceFileCollectionStrategies(
						sourceDirectories);
				NavigableMap<SakerPath, SakerFile> sourcefiles = taskcontext.getTaskUtilities()
						.collectFilesReportInputFileAndAdditionDependency(CompileFileTags.INPUT_SOURCE,
								sourceAdditionDependencies);
				//TODO handle here if no sources found?
				IncrementalCompilationHandler compilationhandler = new IncrementalCompilationHandler(taskcontext,
						outputDirectory, outputClassDirectory, outputNativeHeaderDirectory, outputSourceDirectory,
						outputResourceDirectory, previnfo, sourcefiles);
				Set<JavaAnnotationProcessor> processorreferences = ObjectUtils.newLinkedHashSet(annotationProcessors);
				if (generateNativeHeaders) {
					processorreferences.add(NATIVE_HEADER_GENERATOR_PROCESSOR_REFERENCE);
				}
				compilationhandler.setPassOptions(sourceVersionName, targetVersionName, parallelProcessing,
						javasdkreference, parameters, classPath, modulePath, annotationProcessorOptions,
						processorreferences, addExports, bootClassPath, suppressWarnings, processorInputLocations,
						moduleMainClass, moduleVersion, sdkrefs, parameterNames, debugInfo);

				//TODO remove using the present output generated resource files
				compilationhandler.setPresentOutputGeneratedResourceFiles(
						taskcontext.getPreviousOutputDependencies(CompileFileTags.OUTPUT_GENERATED_RESOURCE));

				try {
					compilationhandler.build();
					info = compilationhandler.getResultCompilationInfo();
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
				modulename = compilationhandler.getModuleName();
			}
		} finally {
			//report the ide configuration even if the compilation failed
			startIdeConfigurationTask(taskcontext, taskid.getPassIdentifier(), javasdkreference,
					ObjectUtils.isNullOrEmpty(annotationProcessors) ? null : outputSourceDirectory.getSakerPath(),
					classpathentries, bootclasspathentries, outputClassDirectory.getSakerPath(), modulepathentries,
					pinnedsdks);
		}

		Object abiversionkey = info.getAbiVersionKey();
		Object implementationversionkey = info.getImplementationVersionKey();

		if (TestFlag.ENABLED) {
			Objects.requireNonNull(abiversionkey, "abiversionkey");
			Objects.requireNonNull(implementationversionkey, "implementationversionkey");
		}

		taskcontext.setTaskOutput(CompilationInfo.class, info);

		SimpleJavaCompilationOutputConfiguration outputconfig = new SimpleJavaCompilationOutputConfiguration(taskid,
				outputClassDirectory.getSakerPath(), outputNativeHeaderDirectory.getSakerPath(),
				outputResourceDirectory.getSakerPath(), outputSourceDirectory.getSakerPath(), modulename, pinnedsdks);

		InternalJavaCompilerOutputImpl output = new InternalJavaCompilerOutputImpl(sourceDirectories, classPath,
				modulePath, abiversionkey, implementationversionkey, outputconfig);
		output.setHadAnnotationProcessors(!ObjectUtils.isNullOrEmpty(annotationProcessors));

		try (ThreadWorkPool pool = ThreadUtils.newFixedWorkPool("javac-")) {
			pool.offer(() -> {
				outputDirectory.synchronize();
				taskcontext.startTask(JavaTaskUtils.createJavaCompilationConfigurationOutputTaskIdentifier(taskid),
						new JavaCompilationConfigLiteralTaskFactory(outputconfig), null);
			});
			pool.offer(() -> reportOutputFileDependencies(taskcontext, CompileFileTags.OUTPUT_CLASS,
					info.getClassFiles()));
			pool.offer(() -> reportOutputFileDependencies(taskcontext, CompileFileTags.OUTPUT_GENERATED_CLASS,
					info.getGeneratedClassFiles()));
			pool.offer(() -> reportOutputFileDependencies(taskcontext, CompileFileTags.OUTPUT_GENERATED_SOURCE,
					info.getGeneratedSourceFiles()));
			pool.offer(() -> reportOutputFileDependencies(taskcontext, CompileFileTags.OUTPUT_GENERATED_RESOURCE,
					info.getGeneratedResourceFiles()));
			pool.offer(() -> {
				output.setPackageSignatures(info.getRealizedPackageSignatures());
				output.setClassSignatures(info.getRealizedClassSignatures());
				output.setModuleSignature(info.getRealizedModuleSignature());
			});
			for (Entry<? extends ProcessorDetails, ProcessorData> entry : info.getProcessorDetails().entrySet()) {
				NavigableMap<SakerPath, ContentDescriptor> readrescontents = entry.getValue()
						.getReadResourceFileContents();
				if (!ObjectUtils.isNullOrEmpty(readrescontents)) {
					ProcessorDetails procdetails = entry.getKey();
					pool.offer(() -> {
						taskcontext.getTaskUtilities().reportInputFileDependency(
								new ProcessorReadResourceDependencyTag(procdetails), readrescontents);
					});
				}
			}
			return output;
		}
	}

	static void reportInputFileDependencies(TaskContext taskcontext, Object tag,
			NavigableMap<SakerPath, ? extends FileData> dependencies) {
		taskcontext.getTaskUtilities().reportInputFileDependency(tag,
				new FileDataContentDecriptorTransformingNavigableMap(dependencies));
	}

	static void reportOutputFileDependencies(TaskContext taskcontext, Object tag,
			NavigableMap<SakerPath, ? extends FileData> dependencies) {
		taskcontext.getTaskUtilities().reportOutputFileDependency(tag,
				new FileDataContentDecriptorTransformingNavigableMap(dependencies));
	}

	private static final class FileDataContentDecriptorTransformingNavigableMap
			extends TransformingNavigableMap<SakerPath, FileData, SakerPath, ContentDescriptor> {
		private FileDataContentDecriptorTransformingNavigableMap(
				NavigableMap<? extends SakerPath, ? extends FileData> map) {
			super(map);
		}

		protected static ContentDescriptor transformValue(FileData value) {
			ContentDescriptor result = value.getFileContent();
			if (TestFlag.ENABLED) {
				Objects.requireNonNull(result);
			}
			return result;
		}

		@Override
		protected Entry<SakerPath, ContentDescriptor> transformEntry(SakerPath key, FileData value) {
			return ImmutableUtils.makeImmutableMapEntry(key, transformValue(value));
		}
	}

	private void startIdeConfigurationTask(TaskContext taskcontext, String compilationId, SDKReference javacompilersdk,
			SakerPath processorGenDirectory, Collection<? extends ClassPathIDEConfigurationEntry> classPathEntries,
			Collection<? extends ClassPathIDEConfigurationEntry> bootClassPathEntries, SakerPath outputBinDirectory,
			Collection<? extends ModulePathIDEConfigurationEntry> modulepathentries,
			NavigableMap<String, SDKDescription> pinnedsdks) throws Exception {
		JavaIDEConfigurationReportingTaskFactory task = new JavaIDEConfigurationReportingTaskFactory(compilationId);
		task.setAddExports(addExports);
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

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		super.writeExternal(out);
		out.writeBoolean(parallelProcessing);
		SerialUtils.writeExternalCollection(out, suppressWarnings);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		super.readExternal(in);
		parallelProcessing = in.readBoolean();
		suppressWarnings = SerialUtils.readExternalImmutableNavigableSet(in,
				StringUtils::compareStringsNullFirstIgnoreCase);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (parallelProcessing ? 1231 : 1237);
		result = prime * result + ((suppressWarnings == null) ? 0 : suppressWarnings.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		IncrementalWorkerJavaCompilerTaskFactory other = (IncrementalWorkerJavaCompilerTaskFactory) obj;
		if (parallelProcessing != other.parallelProcessing)
			return false;
		if (suppressWarnings == null) {
			if (other.suppressWarnings != null)
				return false;
		} else if (!suppressWarnings.equals(other.suppressWarnings))
			return false;
		return true;
	}

	private static final SimpleAnnotationProcessorReferenceOption NATIVE_HEADER_GENERATOR_PROCESSOR_REFERENCE;
	static {
		//suppress the less processor source version warnings as if the compilation is being done with an 
		// external process to a more recent source version, then the compilation director warns
		NATIVE_HEADER_GENERATOR_PROCESSOR_REFERENCE = new SimpleAnnotationProcessorReferenceOption(
				new SimpleProcessorConfiguration(NativeHeaderGeneratorProcessorCreator.INSTANCE), null,
				ImmutableUtils.makeImmutableNavigableSet(
						new String[] { JavaCompilerWarningType.LessProcessorSourceVersion }));
		NATIVE_HEADER_GENERATOR_PROCESSOR_REFERENCE.setAggregating(false);
	}

	private static enum NativeHeaderGeneratorProcessorCreator implements ProcessorCreator {
		INSTANCE;

		@Override
		public String getName() {
			return NativeHeaderGeneratorProcessor.class.getName();
		}

		@Override
		public Processor create(ProcessorCreationContext taskcontext) {
			return new NativeHeaderGeneratorProcessor();
		}
	}

}