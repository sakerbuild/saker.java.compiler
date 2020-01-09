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
package saker.java.compiler.impl.compile.handler.incremental;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import saker.build.file.DirectoryVisitPredicate;
import saker.build.file.SakerDirectory;
import saker.build.file.SakerFile;
import saker.build.file.content.ContentDescriptor;
import saker.build.file.content.DirectoryContentDescriptor;
import saker.build.file.content.HashContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.SakerPathFiles;
import saker.build.runtime.environment.SakerEnvironment;
import saker.build.runtime.execution.ExecutionContext;
import saker.build.runtime.execution.SakerLog;
import saker.build.task.TaskContext;
import saker.build.task.TaskDependencyFuture;
import saker.build.task.TaskFileDeltas;
import saker.build.task.delta.DeltaType;
import saker.build.task.dependencies.FileCollectionStrategy;
import saker.build.task.utils.TaskUtils;
import saker.build.task.utils.dependencies.RecursiveIgnoreCaseExtensionFileCollectionStrategy;
import saker.build.thirdparty.saker.rmi.connection.RMIVariables;
import saker.build.thirdparty.saker.util.ConcurrentEntryMergeSorter;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.thread.ThreadUtils;
import saker.java.compiler.api.classpath.ClassPathEntry;
import saker.java.compiler.api.classpath.ClassPathReference;
import saker.java.compiler.api.classpath.ClassPathVisitor;
import saker.java.compiler.api.classpath.CompilationClassPath;
import saker.java.compiler.api.classpath.FileClassPath;
import saker.java.compiler.api.classpath.JavaClassPath;
import saker.java.compiler.api.classpath.JavaClassPathBuilder;
import saker.java.compiler.api.classpath.JavaSourceDirectory;
import saker.java.compiler.api.classpath.SDKClassPath;
import saker.java.compiler.api.compile.JavaAnnotationProcessor;
import saker.java.compiler.api.compile.JavaCompilationWorkerTaskIdentifier;
import saker.java.compiler.api.compile.JavaCompilerWorkerTaskOutput;
import saker.java.compiler.api.compile.SakerJavaCompilerUtils;
import saker.java.compiler.api.compile.exc.JavaCompilationFailedException;
import saker.java.compiler.api.modulepath.CompilationModulePath;
import saker.java.compiler.api.modulepath.FileModulePath;
import saker.java.compiler.api.modulepath.JavaModulePath;
import saker.java.compiler.api.modulepath.ModulePathVisitor;
import saker.java.compiler.api.modulepath.SDKModulePath;
import saker.java.compiler.api.option.JavaAddExports;
import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.JavaTaskUtils.LocalDirectoryClassFilesExecutionProperty;
import saker.java.compiler.impl.RemoteJavaRMIProcess;
import saker.java.compiler.impl.compile.ClassPathIDEConfigurationEntry;
import saker.java.compiler.impl.compile.CompileFileTags;
import saker.java.compiler.impl.compile.InternalJavaCompilerOutput;
import saker.java.compiler.impl.compile.ModulePathIDEConfigurationEntry;
import saker.java.compiler.impl.compile.NoSourcesVersionKey;
import saker.java.compiler.impl.compile.VersionKeyUtils;
import saker.java.compiler.impl.compile.handler.CompilationHandler;
import saker.java.compiler.impl.compile.handler.DirectoryClearingFileRemover;
import saker.java.compiler.impl.compile.handler.diagnostic.DiagnosticEntry;
import saker.java.compiler.impl.compile.handler.info.ClassFileData;
import saker.java.compiler.impl.compile.handler.info.ClassHoldingData;
import saker.java.compiler.impl.compile.handler.info.CompilationInfo;
import saker.java.compiler.impl.compile.handler.info.CompilationInfo.ExecutionClassPathStateInfo;
import saker.java.compiler.impl.compile.handler.info.CompilationInfo.LocalClassPathStateInfo;
import saker.java.compiler.impl.compile.handler.info.CompilationInfo.ProcessorData;
import saker.java.compiler.impl.compile.handler.info.FileData;
import saker.java.compiler.impl.compile.handler.info.GeneratedClassFileData;
import saker.java.compiler.impl.compile.handler.info.GeneratedFileOrigin;
import saker.java.compiler.impl.compile.handler.info.GeneratedResourceFileData;
import saker.java.compiler.impl.compile.handler.info.GeneratedSourceFileData;
import saker.java.compiler.impl.compile.handler.info.SourceFileData;
import saker.java.compiler.impl.compile.handler.invoker.IncrementalCompilationDirector;
import saker.java.compiler.impl.compile.handler.invoker.JavaCompilationInvoker;
import saker.java.compiler.impl.compile.handler.invoker.JavaCompilerInvocationContext;
import saker.java.compiler.impl.compile.handler.invoker.ProcessorDetails;
import saker.java.compiler.impl.compile.handler.invoker.ProcessorTriggerDelta;
import saker.java.compiler.impl.compile.signature.change.AbiChange;
import saker.java.compiler.impl.compile.signature.change.ClassAddedABIChange;
import saker.java.compiler.impl.compile.signature.change.ClassAnnotationABIChange;
import saker.java.compiler.impl.compile.signature.change.ClassInheritanceABIChange;
import saker.java.compiler.impl.compile.signature.change.ClassModifierABIChange;
import saker.java.compiler.impl.compile.signature.change.ClassRemovedABIChange;
import saker.java.compiler.impl.compile.signature.change.ClassTypeChange;
import saker.java.compiler.impl.compile.signature.change.ClassTypeParametersAbiChange;
import saker.java.compiler.impl.compile.signature.change.member.FieldAddedABIChange;
import saker.java.compiler.impl.compile.signature.change.member.FieldInitializerABIChange;
import saker.java.compiler.impl.compile.signature.change.member.FieldRemovedABIChange;
import saker.java.compiler.impl.compile.signature.change.member.MethodAddedABIChange;
import saker.java.compiler.impl.compile.signature.change.member.MethodRemovedABIChange;
import saker.java.compiler.impl.compile.util.LocalPathFileContentDescriptorExecutionProperty;
import saker.java.compiler.impl.options.SimpleJavaSourceDirectoryOption;
import saker.java.compiler.impl.sdk.JavaSDKReference;
import saker.java.compiler.impl.signature.element.AnnotatedSignature;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.FieldSignature;
import saker.java.compiler.impl.signature.element.MethodSignature;
import saker.java.compiler.impl.signature.element.ModuleSignature;
import saker.java.compiler.impl.signature.element.ParameterizedSignature;
import saker.java.compiler.impl.signature.element.SignatureNameChecker;
import saker.java.compiler.impl.signature.type.TypeSignature;
import saker.java.compiler.jdk.impl.invoker.InternalIncrementalCompilationInvoker;
import saker.sdk.support.api.SDKPathReference;
import saker.sdk.support.api.SDKReference;
import saker.sdk.support.api.SDKSupportUtils;
import saker.std.api.file.location.ExecutionFileLocation;
import saker.std.api.file.location.FileLocation;
import saker.std.api.file.location.FileLocationVisitor;
import saker.std.api.file.location.LocalFileLocation;
import testing.saker.java.compiler.TestFlag;

public class IncrementalCompilationHandler extends CompilationHandler {
	public static final boolean LOGGING_ENABLED;
	static {
		LOGGING_ENABLED = TestFlag.ENABLED;
	}

	private final class InvocationContextImpl implements JavaCompilerInvocationContext {
		private final NavigableMap<SakerPath, SakerDirectory> passClassPaths;
		private final NavigableMap<SakerPath, SakerDirectory> passBootClassPaths;
		private final LinkedHashMap<String, String> generalProcessorOptions;
		private final Map<ProcessorDetails, JavaAnnotationProcessor> passProcessorReferences;
		private final NavigableMap<String, SakerDirectory> passModulePaths;

		private InvocationContextImpl(NavigableMap<SakerPath, SakerDirectory> passclasspaths,
				NavigableMap<SakerPath, SakerDirectory> passbootclasspaths,
				LinkedHashMap<String, String> generalprocessoroptions,
				Map<ProcessorDetails, JavaAnnotationProcessor> passProcessorReferences,
				NavigableMap<String, SakerDirectory> passmodulepaths) {
			this.passClassPaths = passclasspaths;
			this.passBootClassPaths = passbootclasspaths;
			this.generalProcessorOptions = generalprocessoroptions;
			this.passProcessorReferences = passProcessorReferences;
			this.passModulePaths = passmodulepaths;
		}

		@Override
		public void close() throws IOException {
		}

		@Override
		public boolean isParallelProcessing() {
			return parallelProcessing;
		}

		@Override
		public Collection<String> getOptions() {
			return options;
		}

		@Override
		public Collection<String> getSuppressWarnings() {
			return suppressWarnings;
		}

		@Override
		public SakerDirectory getOutputClassDirectory() {
			return outputClassDirectory;
		}

		@Override
		public SakerDirectory getOutputSourceDirectory() {
			return outputSourceDirectory;
		}

		@Override
		public SakerDirectory getOutputResourceDirectory() {
			return outputResourceDirectory;
		}

		@Override
		public SakerDirectory getOutputNativeHeaderDirectory() {
			return outputNativeHeaderDirectory;
		}

		@Override
		public Collection<SakerDirectory> getClassPathDirectories() {
			return passClassPaths.values();
		}

		@Override
		public Collection<SakerDirectory> getBootClassPathDirectories() {
			return passBootClassPaths.values();
		}

		@Override
		public Map<String, SakerDirectory> getModulePathDirectories() {
			return passModulePaths;
		}

		@Override
		public Map<String, String> getGeneralProcessorOptions() {
			return generalProcessorOptions;
		}

		@Override
		public Map<ProcessorDetails, JavaAnnotationProcessor> getPassProcessorReferences() {
			return passProcessorReferences;
		}

	}

	private final TaskContext taskContext;
	private final ExecutionContext executionContext;
	private final CompilationInfo prevInfo;

	private final SakerDirectory outputBaseDirectory;
	private final SakerDirectory outputClassDirectory;
	private final SakerDirectory outputNativeHeaderDirectory;
	private final SakerDirectory outputSourceDirectory;
	private final SakerDirectory outputResourceDirectory;

	/**
	 * Collection of files that are given as input to this compilation task. These are the files that are listed under
	 * the source directories.
	 */
	private NavigableMap<SakerPath, ? extends SakerFile> presentSourceFiles;
	private NavigableMap<SakerPath, ? extends SakerFile> presentOutputGeneratedResourceFiles = new TreeMap<>();

	private NavigableMap<SakerPath, ? extends SakerFile> changedInputSourceFiles;

	private NavigableMap<SakerPath, ? extends SakerFile> changedOutputClassFiles;
	private NavigableMap<SakerPath, ? extends SakerFile> changedOutputGeneratedClassFiles;
	private NavigableMap<SakerPath, ? extends SakerFile> changedOutputGeneratedSourceFiles;
	private NavigableMap<SakerPath, ? extends SakerFile> changedOutputGeneratedResourceFiles;

	/**
	 * Collection of sources from previous compilation.
	 */
	private NavigableMap<SakerPath, SourceFileData> prevSourceFileDatas;
	/**
	 * Collection of generated sources from previous compilation which haven't been regenerated in this compilation
	 * task.
	 */
	private NavigableMap<SakerPath, GeneratedSourceFileData> unaddedGeneratedSourceFileDatas;
	/**
	 * Collection of generated sources from previous compilation.
	 */
	private NavigableMap<SakerPath, GeneratedSourceFileData> prevGeneratedSourceFileDatas;
	/**
	 * Collection of class files from previous compilation which haven't been regenerated in this compilation task.
	 */
	private NavigableMap<SakerPath, ClassFileData> prevClassFileDatas;
	/**
	 * Collection of class files from previous compilation which haven't been regenerated in this compilation task.
	 */
	private NavigableMap<SakerPath, GeneratedClassFileData> prevGeneratedClassFileDatas;
	/**
	 * Collection of resource files from previous compilation.
	 */
	private NavigableMap<SakerPath, GeneratedResourceFileData> prevGeneratedResourceFileDatas;
	/**
	 * Collection of processor details that was part of the previous compilation.
	 */
	private Map<ProcessorDetails, ProcessorData> prevProcessorDetails;

	private Set<DiagnosticEntry> prevDiagnosticEntries;

	private ClassHoldingData prevModuleInfoFileData;
	private NavigableSet<String> prevCompilationModuleSet;

	private NavigableMap<SakerPath, GeneratedResourceFileData> unaddedGeneratedResourceFileDatas = new TreeMap<>();

	private NavigableMap<SakerPath, CompilationInfo.ExecutionClassPathStateInfo> prevExecutionClassPathStateInfos;
	private NavigableMap<SakerPath, CompilationInfo.LocalClassPathStateInfo> prevLocalClassPathStateInfos;

	private NavigableMap<SakerPath, CompilationInfo.ExecutionClassPathStateInfo> currentExecutionClassPathStateInfos = new TreeMap<>();
	private NavigableMap<SakerPath, CompilationInfo.LocalClassPathStateInfo> currentLocalClassPathStateInfos = new TreeMap<>();

	private NavigableMap<SakerPath, CompilationInfo.ExecutionClassPathStateInfo> prevExecutionModulePathStateInfos;
	private NavigableMap<SakerPath, CompilationInfo.LocalClassPathStateInfo> prevLocalModulePathStateInfos;

	private NavigableMap<SakerPath, CompilationInfo.ExecutionClassPathStateInfo> currentExecutionModulePathStateInfos = new TreeMap<>();
	private NavigableMap<SakerPath, CompilationInfo.LocalClassPathStateInfo> currentLocalModulePathStateInfos = new TreeMap<>();

	protected List<String> options;

	private JavaClassPath classPath;
	private JavaModulePath modulePath;
	private JavaClassPath bootClassPath;

	private String fullCompilationReason = null;

	private DirectoryClearingFileRemover fileRemover = new DirectoryClearingFileRemover();

	private Collection<String> suppressWarnings;
	private String sourceVersionName;
	private String targetVersionName;
	private boolean parallelProcessing;
	private List<String> passParameters;
	private Map<String, String> passAnnotationProcessorOptions;
	private Collection<JavaAnnotationProcessor> passAnnotationProcessors;
	private Collection<JavaAddExports> passAddExports;
	private NavigableMap<String, SakerPath> processorInputLocations;

	private SDKReference compilationJavaSDKReference;

	private Object prevAbiVersionKey;

	private CompilationInfo resultCompilationInfo;

	private SignatureNameChecker methodParameterNameChangeChecker;
	private String prevModuleMainClass;
	private String moduleMainClass;
	private String prevModuleVersion;
	private String moduleVersion;
	private Set<ClassPathIDEConfigurationEntry> classPathIdeConfigurationEntries = new LinkedHashSet<>();
	private Set<ClassPathIDEConfigurationEntry> bootClassPathIdeConfigurationEntries = new LinkedHashSet<>();
	private Set<ModulePathIDEConfigurationEntry> modulePathIdeConfigurationEntries = new LinkedHashSet<>();

	private String outModuleName;
	private NavigableMap<String, SDKReference> sdkReferences;
	private boolean parameterNames;
	private Set<String> debugInfos;

	public IncrementalCompilationHandler(TaskContext taskcontext, SakerDirectory outputbasedirectory,
			SakerDirectory outputClassDirectory, SakerDirectory outputNativeHeaderDirectory,
			SakerDirectory outputSourceDirectory, SakerDirectory outputResourceDirectory, CompilationInfo previnfo,
			NavigableMap<SakerPath, SakerFile> sourcefiles) {
		this.taskContext = taskcontext;
		this.outputBaseDirectory = outputbasedirectory;
		this.executionContext = taskcontext.getExecutionContext();
		this.outputClassDirectory = outputClassDirectory;
		this.outputNativeHeaderDirectory = outputNativeHeaderDirectory;
		this.outputSourceDirectory = outputSourceDirectory;
		this.outputResourceDirectory = outputResourceDirectory;
		this.presentSourceFiles = sourcefiles;
		this.prevInfo = previnfo;
	}

	public CompilationInfo getResultCompilationInfo() {
		return resultCompilationInfo;
	}

	public void setPassOptions(String sourceversionname, String targetversionname, boolean parallelprocessing,
			SDKReference compilationJavaSDKReference, List<String> parameters, JavaClassPath classpath,
			JavaModulePath modulepath, Map<String, String> annotationprocessoroptions,
			Collection<JavaAnnotationProcessor> annotationprocessors, Collection<JavaAddExports> addexports,
			JavaClassPath bootclasspath, Collection<String> suppresswarnings,
			NavigableMap<String, SakerPath> processorInputLocations, String moduleMainClass, String moduleVersion,
			NavigableMap<String, SDKReference> sdkrefs, boolean parameterNames, Set<String> debugInfos) {
		this.moduleMainClass = moduleMainClass;
		this.moduleVersion = moduleVersion;
		this.sdkReferences = sdkrefs;
		this.parameterNames = parameterNames;
		this.debugInfos = debugInfos;
		if (suppresswarnings == null) {
			suppresswarnings = Collections.emptySet();
		}
		if (parameters == null) {
			parameters = Collections.emptyList();
		}
		if (annotationprocessoroptions == null) {
			annotationprocessoroptions = Collections.emptyMap();
		}
		if (annotationprocessors == null) {
			annotationprocessors = Collections.emptyList();
		}
		if (addexports == null) {
			addexports = Collections.emptyList();
		}

		this.suppressWarnings = suppresswarnings;
		this.sourceVersionName = sourceversionname;
		this.targetVersionName = targetversionname;
		this.parallelProcessing = parallelprocessing;

		this.compilationJavaSDKReference = compilationJavaSDKReference;
		this.passParameters = parameters;
		this.classPath = classpath;
		this.modulePath = modulepath;
		this.passAnnotationProcessorOptions = annotationprocessoroptions;
		this.passAnnotationProcessors = annotationprocessors;
		this.passAddExports = addexports;
		//can be null
		this.bootClassPath = bootclasspath;

		this.processorInputLocations = processorInputLocations;
	}

	public void setPresentOutputGeneratedResourceFiles(
			NavigableMap<SakerPath, ? extends SakerFile> presentGeneratedResourceFiles) {
		this.presentOutputGeneratedResourceFiles = presentGeneratedResourceFiles;
	}

	private void clearForFullCompilation(String reason) {
		if (fullCompilationReason != null) {
			//already full compilation
			return;
		}
		fullCompilationReason = reason;
		clearPrevInfoFields();
		clearOutputDirectories();
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void clearPrevInfoFields() {
		//use a tree map as empty map because Collections.empty* doesnt support removeAll 
		NavigableMap emptymap = new TreeMap<>();
		this.prevClassFileDatas = emptymap;
		this.prevSourceFileDatas = emptymap;
		this.prevGeneratedSourceFileDatas = emptymap;
		this.prevGeneratedClassFileDatas = emptymap;
		this.prevGeneratedResourceFileDatas = emptymap;
		this.prevProcessorDetails = new HashMap<>();
		this.prevExecutionClassPathStateInfos = emptymap;
		this.prevLocalClassPathStateInfos = emptymap;
		this.prevExecutionModulePathStateInfos = emptymap;
		this.prevLocalModulePathStateInfos = emptymap;
		this.prevCompilationModuleSet = new TreeSet<>();
		this.prevDiagnosticEntries = new HashSet<>();
		this.prevModuleMainClass = null;
		this.prevModuleVersion = null;
	}

	private void clearOutputDirectories() {
		this.outputClassDirectory.clear();
		this.outputNativeHeaderDirectory.clear();
		this.outputSourceDirectory.clear();
		this.outputResourceDirectory.clear();
	}

	/**
	 * Iterates over two sorted maps and removes them from the collection based on the parameter function result.<br>
	 * <br>
	 * The function iterates over the keys of the base map and finds the matching value in {@code subjectmap}. If the
	 * value with the same key is not found, the function is called with {@code null} argument. If the function returns
	 * {@code true} then the item is removed from the base map. If a value is found in the subject map, then it will be
	 * always removed from the subject map.
	 * 
	 * @param basemap
	 *            The base map.
	 * @param subjectmap
	 *            The subject map.
	 * @param removefunction
	 *            The function returning true if the item should be removed from the base map.
	 */
	private static <T extends Comparable<T>, V1, V2> void iterateRemoverSortedRemoving(SortedMap<T, V1> basemap,
			SortedMap<T, V2> subjectmap, BiFunction<Entry<T, V1>, V2, Boolean> removefunction) {
		Iterator<Entry<T, V1>> baseit = basemap.entrySet().iterator();
		Iterator<Entry<T, V2>> subit = subjectmap.entrySet().iterator();
		while (baseit.hasNext()) {
			Entry<T, V1> item = baseit.next();
			Entry<T, V2> subjectitem = subit.hasNext() ? subit.next() : null;
			while (subjectitem != null) {
				int cmp = item.getKey().compareTo(subjectitem.getKey());
				if (cmp == 0) {
					//found the same item in subject map
					break;
				}
				if (cmp < 0) {
					//item is not found in subject collection
					//keep the subject item, and continue the loop
					boolean remove = removefunction.apply(item, null);
					if (remove) {
						baseit.remove();
					}
					if (!baseit.hasNext()) {
						return;
					}
					item = baseit.next();
					//do not advance subjectitem
					continue;
				}
				subjectitem = subit.hasNext() ? subit.next() : null;
			}
			boolean remove;
			if (subjectitem == null) {
				remove = removefunction.apply(item, null);
			} else {
				remove = removefunction.apply(item, subjectitem.getValue());
				subit.remove();
			}
			if (remove) {
				baseit.remove();
			}
		}
	}

	/**
	 * Same as {@link #iterateRemoverSortedRemoving(SortedMap, SortedMap, BiFunction)} but never removes the item from
	 * the base map.
	 * 
	 * @see {@link #iterateRemoverSortedRemoving(SortedMap, SortedMap, BiFunction)}
	 */
	private static <T extends Comparable<T>, V1, V2> void iterateSortedRemoving(SortedMap<T, V1> basemap,
			SortedMap<T, V2> subjectmap, BiConsumer<Entry<T, V1>, V2> consumer) {
		iterateRemoverSortedRemoving(basemap, subjectmap, (e, i) -> {
			consumer.accept(e, i);
			return false;
		});
	}

	private static boolean canSkipProcessorRunning(Iterable<? extends JavaAnnotationProcessor> processors) {
		for (JavaAnnotationProcessor apr : processors) {
			if (apr.getAlwaysRun()) {
				return false;
			}
		}
		return true;
	}

	private void detectClassPathPassStateChanges(Set<AbiChange> abichanges) {
		detectClassOrModulePathPassStateChanges(abichanges, prevExecutionClassPathStateInfos,
				currentExecutionClassPathStateInfos);
	}

	private boolean isAnyModulePathModuleSigntureChanged() {
		return !ObjectUtils.iterateSortedMapEntriesBreak(prevExecutionModulePathStateInfos,
				currentExecutionModulePathStateInfos, (k, prev, current) -> {
					if (prev == null || current == null) {
						return false;
					}
					Object prevabiversionkey = prev.getAbiVersionKey();
					if (prevabiversionkey != null && prevabiversionkey.equals(current.getAbiVersionKey())) {
						//the ABI versions are the same, meaning the ABI of the module path have not changed. 
						//    this means that we should not find any differences between the module signatures
						//    so we can skip the checks
						return true;
					}
					ModuleSignature prevsig = prev.getModuleSignature();
					ModuleSignature currentsig = current.getModuleSignature();
					if (!ModuleSignature.signatureEquals(prevsig, currentsig)
							|| !AnnotatedSignature.annotationSignaturesEqual(currentsig, prevsig)) {
						return false;
					}
					return true;
				});
	}

	private void detectModulePathPassStateChanges(Set<AbiChange> abichanges) {
		//detect module signature changes
		detectClassOrModulePathPassStateChanges(abichanges, prevExecutionModulePathStateInfos,
				currentExecutionModulePathStateInfos);
	}

	private void detectClassOrModulePathPassStateChanges(Set<AbiChange> abichanges,
			NavigableMap<SakerPath, ExecutionClassPathStateInfo> prevstateinfos,
			NavigableMap<SakerPath, ExecutionClassPathStateInfo> currentstateinfos) {
		//the class signatures maps are checked for nulls. if any of them are nulls, the build is already cleared for full compilation beforehand
		ObjectUtils.iterateSortedMapEntries(prevstateinfos, currentstateinfos, (k, prev, current) -> {
			if (prev == null) {
				//pass added
				NavigableMap<String, ? extends ClassSignature> ccpsignatures = current.getClassSignatures();
				if (ccpsignatures != null) {
					for (ClassSignature c : ccpsignatures.values()) {
						abichanges.add(new ClassAddedABIChange(c));
					}
				}
			} else if (current == null) {
				NavigableMap<String, ? extends ClassSignature> prevcpsignatures = prev.getClassSignatures();
				if (prevcpsignatures != null) {
					for (ClassSignature c : prevcpsignatures.values()) {
						abichanges.add(new ClassRemovedABIChange(c));
					}
				}
			} else {
				Object prevabiversionkey = prev.getAbiVersionKey();
				if (prevabiversionkey != null && prevabiversionkey.equals(current.getAbiVersionKey())) {
					//the ABI versions are the same, meaning the ABI of the classpath have not changed. 
					//    this means that we should not find any differences between the class signatures
					//    so we can skip the checks
					return;
				}
				//TODO only detect, if the corresponding class file has been changed based on changedInputClasspathFiles 
				NavigableMap<String, ? extends ClassSignature> prevcpsignatures = prev.getClassSignatures();
				NavigableMap<String, ? extends ClassSignature> ccpsignatures = current.getClassSignatures();
				if (prevcpsignatures != null && ccpsignatures != null) {
					ObjectUtils.iterateSortedMapEntries(prevcpsignatures, ccpsignatures, (cname, pclass, cclass) -> {
						if (pclass == null) {
							abichanges.add(new ClassAddedABIChange(cclass));
						} else {
							if (cclass == null) {
								abichanges.add(new ClassRemovedABIChange(pclass));
							} else {
								detectChanges(pclass, cclass, methodParameterNameChangeChecker, abichanges::add);
							}
						}
					});
				}
			}
		});
	}

	public static LinkedHashMap<String, String> createGeneralProcessorOptions(List<String> passparameters,
			Map<String, String> passannotationprocessoroptions) {
		//keep insertion order
		LinkedHashMap<String, String> processoroptions = new LinkedHashMap<>();
		if (!ObjectUtils.isNullOrEmpty(passparameters)) {
			for (String param : passparameters) {
				int paramlen = param.length();
				if (param.startsWith("-A") && paramlen > 2) {
					int sepindex = param.indexOf('=');
					String value;
					if (sepindex < 0) {
						sepindex = paramlen;
						value = null;
					} else {
						value = param.substring(sepindex + 1, paramlen);
					}
					String key = param.substring(2, sepindex);
					String prev = processoroptions.put(key, value);
					if (prev != null && !prev.equals(value)) {
						warnAnnotationProcessorRedefinition(key, prev, value);
					}
				}
			}
		}
		if (!ObjectUtils.isNullOrEmpty(passannotationprocessoroptions)) {
			for (Entry<String, String> entry : passannotationprocessoroptions.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				String prev = processoroptions.put(key, value);
				if (prev != null && !prev.equals(value)) {
					warnAnnotationProcessorRedefinition(key, prev, value);
				}
			}
		}
		return processoroptions;
	}

	public static LinkedHashMap<String, String> createProcessorSpecificOptions(
			LinkedHashMap<String, String> generaloptions, JavaAnnotationProcessor procref) {
		Map<String, String> procoptions = procref.getOptions();
		return createProcessorSpecificOptions(generaloptions, procoptions);
	}

	public static LinkedHashMap<String, String> createProcessorSpecificOptions(
			LinkedHashMap<String, String> generaloptions, Map<String, String> procoptions) {
		LinkedHashMap<String, String> result = new LinkedHashMap<>(procoptions);
		if (!ObjectUtils.isNullOrEmpty(generaloptions)) {
			for (Entry<String, String> entry : generaloptions.entrySet()) {
				result.putIfAbsent(entry.getKey(), entry.getValue());
			}
		}
		return result;
	}

	private static void warnAnnotationProcessorRedefinition(String key, String prev, String value) {
		SakerLog.warning().println(
				"Annotation processor option '" + key + "' defined multiple times: '" + prev + "', '" + value + "'.");
	}

	private boolean isProcessorsChanged(Map<ProcessorDetails, JavaAnnotationProcessor> passProcessorReferences) {
		return !passProcessorReferences.keySet().equals(prevProcessorDetails.keySet());
	}

	private static NavigableMap<String, ? extends ClassSignature> mapClassSignaturesToCanonicalNames(
			Collection<? extends ClassSignature> classes) {
		if (classes.isEmpty()) {
			return Collections.emptyNavigableMap();
		}
		NavigableMap<String, ClassSignature> result = new TreeMap<>();
		for (ClassSignature c : classes) {
			result.put(c.getCanonicalName(), c);
		}
		return result;
	}

	private CompilationInfo invokeCompilation(IncrementalCompilationDirector director,
			NavigableMap<SakerPath, SakerFile> units, NavigableMap<SakerPath, SourceFileData> removedsourcefiles,
			Map<ProcessorDetails, ProcessorTriggerDelta> deltatriggeredprocessors, Set<AbiChange> classpathabichanges,
			boolean processordetailschanged, String compilejdkversion, boolean nocommandlineclasspath,
			boolean allowcommandlinebootclasspath) throws IOException, JavaCompilationFailedException {
		if (fullCompilationReason != null) {
			System.out.println("Recompiling all sources. (" + fullCompilationReason + ")");
		}
		director.setCompilationDetails(presentSourceFiles, prevSourceFileDatas, unaddedGeneratedSourceFileDatas,
				prevGeneratedSourceFileDatas, prevGeneratedClassFileDatas, prevGeneratedResourceFileDatas,
				deltatriggeredprocessors, prevProcessorDetails, unaddedGeneratedResourceFileDatas, classpathabichanges,
				processordetailschanged, prevModuleInfoFileData, prevCompilationModuleSet, prevDiagnosticEntries,
				fileRemover, prevClassFileDatas, processorInputLocations, methodParameterNameChangeChecker,
				moduleMainClass, prevModuleMainClass, moduleVersion, prevModuleVersion, nocommandlineclasspath,
				allowcommandlinebootclasspath);
		CompilationInfo result = director.invokeCompilation(units, removedsourcefiles);
		result.setExecutionClassPathStateInfosSignatures(currentExecutionClassPathStateInfos);
		result.setLocalClassPathStateInfosSignatures(currentLocalClassPathStateInfos);
		result.setExecutionModulePathStateInfosSignatures(currentExecutionModulePathStateInfos);
		result.setLocalModulePathStateInfosSignatures(currentLocalModulePathStateInfos);

		result.setOptions(sourceVersionName, targetVersionName, options, compilejdkversion);
		ClassHoldingData modulecf = result.getModuleClassFile();
		if (modulecf != null) {
			ModuleSignature modulesig = modulecf.getModuleSignature();
			if (modulesig != null) {
				this.outModuleName = modulesig.getName();
			}
		}

		//remove present files in the output directory which were not generated by the compilation
		ConcurrentEntryMergeSorter<SakerPath, FileData> alloutputfiles = new ConcurrentEntryMergeSorter<>();
		alloutputfiles.add(result.getClassFiles());
		alloutputfiles.add(result.getGeneratedSourceFiles());
		alloutputfiles.add(result.getGeneratedClassFiles());
		alloutputfiles.add(result.getGeneratedResourceFiles());
		NavigableMap<SakerPath, SakerFile> presentgenfiles = outputBaseDirectory
				.getFilesRecursiveByPath(outputBaseDirectory.getSakerPath(), DirectoryVisitPredicate.subFiles());
		ObjectUtils.iterateOrderedEntryIterators(alloutputfiles.iterator(), presentgenfiles.entrySet().iterator(),
				Comparator.naturalOrder(), (p, gen, present) -> {
					if (gen == null) {
						fileRemover.remove(p, present);
					}
				});

		if (fullCompilationReason != null || prevAbiVersionKey == null || director.hadAbiChanges()) {
			TreeMap<String, byte[]> classhashes = new TreeMap<>();
			MessageDigest hasher = VersionKeyUtils.getMD5();
			for (ClassFileData cf : result.getClassFiles().values()) {
				classhashes.put(cf.getClassBinaryName(), cf.getAbiHash());
			}
			for (GeneratedClassFileData cf : result.getGeneratedClassFiles().values()) {
				classhashes.put(cf.getClassBinaryName(), cf.getAbiHash());
			}
			for (byte[] h : classhashes.values()) {
				hasher.update(h);
			}
			result.setAbiVersionKey(HashContentDescriptor.createWithHash(hasher.digest()));
		} else {
			result.setAbiVersionKey(prevAbiVersionKey);
		}
		{
			TreeMap<String, byte[]> classhashes = new TreeMap<>();
			MessageDigest hasher = VersionKeyUtils.getMD5();
			for (ClassFileData cf : result.getClassFiles().values()) {
				classhashes.put(cf.getClassBinaryName(), cf.getImplementationHash());
			}
			for (GeneratedClassFileData cf : result.getGeneratedClassFiles().values()) {
				classhashes.put(cf.getClassBinaryName(), cf.getImplementationHash());
			}
			for (byte[] h : classhashes.values()) {
				hasher.update(h);
			}
			result.setImplementationVersionKey(HashContentDescriptor.createWithHash(hasher.digest()));
		}
		return result;
	}

	private void detectSourceFileDeltas(NavigableMap<SakerPath, SakerFile> firstroundsourcefiles,
			NavigableMap<SakerPath, SourceFileData> outremovedsourcefiles) {
//		long innanos = System.nanoTime();
		for (Entry<SakerPath, ? extends SakerFile> entry : changedInputSourceFiles.entrySet()) {
			SakerPath sourcepath = entry.getKey();
			SakerFile file = entry.getValue();
			SourceFileData sfd = removeCompiledFilesForSource(sourcepath);
			if (sfd == null) {
				//there was no source file corresponding to this changed input file
				continue;
			}
			if (file == null) {
				//source file removed
				outremovedsourcefiles.put(sourcepath, sfd);
				if (LOGGING_ENABLED && fullCompilationReason == null) {
					SakerLog.log().verbose()
							.println("Java source removed: " + SakerPathFiles.toRelativeString(sourcepath));
				}
				continue;
			}
			firstroundsourcefiles.put(sourcepath, file);
			if (LOGGING_ENABLED && fullCompilationReason == null) {
				SakerLog.log().verbose()
						.println("Java source modified: " + SakerPathFiles.toRelativeString(sourcepath));
			}
		}
		ObjectUtils.iterateSortedMapEntries(presentSourceFiles, prevSourceFileDatas, (sourcepath, file, sfd) -> {
			if (sfd == null) {
				//source file added
				firstroundsourcefiles.put(sourcepath, file);
				removeCompiledFilesForSource(sourcepath);
				if (LOGGING_ENABLED && fullCompilationReason == null) {
					SakerLog.log().verbose()
							.println("Java source added: " + SakerPathFiles.toRelativeString(sourcepath));
				}
			} else if (file == null) {
				//source file removed
				//this can happen if a source directory got removed from the compile pass, but not the source file itself
				SourceFileData outprev = outremovedsourcefiles.putIfAbsent(sourcepath, sfd);
				if (LOGGING_ENABLED && fullCompilationReason == null && outprev == null) {
					SakerLog.log().verbose()
							.println("Java source removed: " + SakerPathFiles.toRelativeString(sourcepath));
				}
			}
		});
	}

	private void detectGeneratedResourceFileDeltas(
			Map<ProcessorDetails, ProcessorTriggerDelta> deltatriggeredprocessors) {
		if (prevGeneratedResourceFileDatas.isEmpty()) {
			//there was no resources generated previously
			return;
		}
		//TODO don't use the present output generated resource files.
		TreeMap<SakerPath, SakerFile> presentgenfiles = new TreeMap<>(presentOutputGeneratedResourceFiles);
		for (Entry<SakerPath, ? extends SakerFile> entry : changedOutputGeneratedResourceFiles.entrySet()) {
			SakerPath path = entry.getKey();
			GeneratedResourceFileData filedata = prevGeneratedResourceFileDatas.get(path);
			if (filedata == null) {
				//no resource was generated at this path previously
				continue;
			}
			presentgenfiles.remove(path);
			GeneratedFileOrigin origins = filedata.getOrigin();
			//change detected at path
			SakerFile file = entry.getValue();
			if (file == null) {
				addDeltaTriggeredProcessor(deltatriggeredprocessors, origins);
				if (LOGGING_ENABLED && fullCompilationReason == null) {
					SakerLog.log().verbose()
							.println("Generated resource file removed: " + SakerPathFiles.toRelativeString(path));
				}
				continue;
			}
			addDeltaTriggeredProcessor(deltatriggeredprocessors, origins);
			if (LOGGING_ENABLED && fullCompilationReason == null) {
				SakerLog.log().verbose()
						.println("Generated resource file changed: " + SakerPathFiles.toRelativeString(path));
			}
		}
		//list of present unchanged gen files
		for (Entry<SakerPath, SakerFile> entry : presentgenfiles.entrySet()) {
			SakerPath path = entry.getKey();
			GeneratedResourceFileData sfd = prevGeneratedResourceFileDatas.get(path);
			if (sfd != null) {
				unaddedGeneratedResourceFileDatas.put(path, sfd);
			}
		}
	}

	private void detectGeneratedSourceFileDeltas(
			Map<ProcessorDetails, ProcessorTriggerDelta> deltatriggeredprocessors) {
		for (Entry<SakerPath, ? extends SakerFile> entry : changedOutputGeneratedSourceFiles.entrySet()) {
			SakerPath path = entry.getKey();
			GeneratedSourceFileData sfd = prevGeneratedSourceFileDatas.get(path);
			if (sfd == null) {
				//the changed output file is not a generated source file
				continue;
			}
			unaddedGeneratedSourceFileDatas.remove(path);
			removeCompiledFilesForSource(sfd);
			SakerFile file = entry.getValue();
			if (file != null) {
				fileRemover.remove(path, file);
			}
			GeneratedFileOrigin origins = sfd.getOrigin();
			addDeltaTriggeredProcessor(deltatriggeredprocessors, origins);
			if (LOGGING_ENABLED && fullCompilationReason == null) {
				SakerLog.log().verbose().println("Generated Java source " + (file == null ? "removed" : "changed")
						+ ": " + SakerPathFiles.toRelativeString(path));
			}
		}
	}

	private void detectClassFileDeltas(NavigableMap<SakerPath, SakerFile> firstroundsourcefiles,
			Map<ProcessorDetails, ProcessorTriggerDelta> deltatriggeredprocessors) {
		//TODO dont create a common map, but solve separately
		TreeMap<SakerPath, SakerFile> commonmap = new TreeMap<>(changedOutputClassFiles);
		commonmap.putAll(changedOutputGeneratedClassFiles);
		for (Entry<SakerPath, ? extends SakerFile> entry : commonmap.entrySet()) {
			SakerPath path = entry.getKey();
			ClassFileData cfd = prevClassFileDatas.get(path);
			if (cfd == null) {
				//no corresponding class file is present for the modified output file
				continue;
			}
			SakerPath sourcepath = cfd.getSourceFile().getPath();
			SakerFile srcfile = presentSourceFiles.get(sourcepath);
			if (srcfile != null) {
				firstroundsourcefiles.put(sourcepath, srcfile);
			}
			if (LOGGING_ENABLED && fullCompilationReason == null) {
				SakerFile file = entry.getValue();
				if (file == null) {
					//class file was removed
					SakerLog.log().verbose()
							.println("Java class file was removed: " + SakerPathFiles.toRelativeString(path));
				} else {
					//class file was modified
					SakerLog.log().verbose()
							.println("Java class file was modified: " + SakerPathFiles.toRelativeString(path));
				}
			}
			GeneratedSourceFileData gensfd = prevGeneratedSourceFileDatas.get(sourcepath);
			if (gensfd != null) {
				if (unaddedGeneratedSourceFileDatas.remove(sourcepath) != null) {
					removeCompiledFilesForSource(gensfd);
				}
				GeneratedFileOrigin origins = gensfd.getOrigin();
				addDeltaTriggeredProcessor(deltatriggeredprocessors, origins);
			}
		}
	}

	private static void addDeltaTriggeredProcessor(
			Map<ProcessorDetails, ProcessorTriggerDelta> deltatriggeredprocessors, GeneratedFileOrigin origins) {
		deltatriggeredprocessors.computeIfAbsent(origins.getProcessorDetails(), x -> new ProcessorTriggerDelta())
				.getTriggeredUnitPaths().addAll(origins.getOriginatingFilePaths());
	}

	private SourceFileData removeCompiledFilesForSource(SakerPath sourcepath) {
		SourceFileData sfd = prevSourceFileDatas.get(sourcepath);
		removeCompiledFilesForSource(sfd);
		return sfd;
	}

	private void removeCompiledFilesForSource(SourceFileData sfd) {
		if (sfd == null) {
			return;
		}
		for (SakerPath path : sfd.getGeneratedClassDatas().keySet()) {
			SakerFile mf = SakerPathFiles.resolveAtPath(taskContext, path);
			prevClassFileDatas.remove(path);
			if (mf != null) {
				fileRemover.remove(path, mf);
			}
		}
	}

	private void createOptions(Set<SakerPath> commandlineclasspaths, Set<SakerPath> commandlinemodulepaths,
			boolean[] nocmdlineclasspath, Set<? extends SakerPath> commandlinebootclasspaths) throws IOException {
		this.options = createOptions(passParameters, sourceVersionName, targetVersionName, passAddExports,
				commandlinebootclasspaths, commandlineclasspaths, commandlinemodulepaths, parameterNames, debugInfos,
				nocmdlineclasspath);
		//options never contain any -proc options, as it is ignored by the createOptions method
		this.options.add("-proc:none");
	}

	@Override
	public Collection<? extends ClassPathIDEConfigurationEntry> getClassPathIDEConfigurationEntries() {
		return classPathIdeConfigurationEntries;
	}

	@Override
	public Collection<? extends ClassPathIDEConfigurationEntry> getBootClassPathIDEConfigurationEntries() {
		return bootClassPathIdeConfigurationEntries;
	}

	@Override
	public Collection<? extends ModulePathIDEConfigurationEntry> getModulePathIDEConfigurationEntries() {
		return modulePathIdeConfigurationEntries;
	}

	@Override
	public String getModuleName() {
		return outModuleName;
	}

	private static Object getAbiVersionKeyFallbackImplementationApplyDependency(TaskDependencyFuture<?> depresult,
			JavaCompilerWorkerTaskOutput output) {
		Object versionkey = output.getAbiVersionKey();
		if (versionkey != null) {
			depresult.setTaskOutputChangeDetector(
					SakerJavaCompilerUtils.getCompilerOutputAbiVersionKeyTaskOutputChangeDetector(versionkey));
		} else {
			versionkey = output.getImplementationVersionKey();
			if (versionkey != null) {
				depresult.setTaskOutputChangeDetector(SakerJavaCompilerUtils
						.getCompilerOutputImplementationVersionKeyTaskOutputChangeDetector(versionkey));
			}
		}
		return versionkey;
	}

	/**
	 * @throws IOException
	 *             thrown if mirroring fails
	 */
	private void collectModulePaths(Map<String, SakerDirectory> resultpassmodulepaths,
			Collection<SakerPath> resultcommandlinemodulepaths) throws IOException {
		if (modulePath == null) {
			return;
		}

		modulePath.accept(new ModulePathVisitor() {
			private Set<FileLocation> handledLocations = new HashSet<>();
			private Set<JavaCompilationWorkerTaskIdentifier> handledWorkerTaskIds = new HashSet<>();

			@Override
			public void visit(CompilationModulePath modulepath) {
				JavaCompilationWorkerTaskIdentifier workertaskid = modulepath.getCompilationWorkerTaskIdentifier();
				if (!handledWorkerTaskIds.add(workertaskid)) {
					//don't get the task result to not install another dependency
					return;
				}
				TaskDependencyFuture<?> depresult = taskContext.getTaskDependencyFuture(workertaskid);
				InternalJavaCompilerOutput output = (InternalJavaCompilerOutput) depresult.getFinished();
				SakerPath path = output.getClassDirectory();
				Object abiversionkey = getAbiVersionKeyFallbackImplementationApplyDependency(depresult, output);
				JavaModulePath outputmodulepath = output.getModulePath();
				depresult.setTaskOutputChangeDetector(
						SakerJavaCompilerUtils.getCompilerOutputModulePathTaskOutputChangeDetector(outputmodulepath));
				String modulename = output.getModuleName();
				if (modulename == null) {
					throw new IllegalArgumentException(
							"Compilation module path doesn't contain module: " + workertaskid.getPassIdentifier());
				}

				SakerDirectory classesdir = taskContext.getTaskUtilities().resolveDirectoryAtPath(path);
				if (classesdir == null) {
					throw ObjectUtils
							.sneakyThrow(new FileNotFoundException("Compilation class directory not found: " + path));
				}

				ModulePathIDEConfigurationEntry ideconfig = new ModulePathIDEConfigurationEntry(
						ExecutionFileLocation.create(path));
				ideconfig.setSourceDirectories(output.getSourceDirectories());
				modulePathIdeConfigurationEntries.add(ideconfig);

				Collection<ClassSignature> cpsignatures = output.getClassSignatures();
				if (cpsignatures == null) {
					//the signatures for the classpath are not available.
					if (abiversionkey != null) {
						//the abi key is available
						//we don't need to report the files as dependencies, as changes in the abi key will result in reinvocation
						//    as the task that produced the abi key will cause a task change delta for this
						currentExecutionClassPathStateInfos.put(path,
								new CompilationInfo.ExecutionClassPathStateInfo(null, null, abiversionkey));

						ExecutionClassPathStateInfo prevcps = ObjectUtils.getMapValue(prevExecutionClassPathStateInfos,
								path);
						if (prevcps == null || !Objects.equals(prevcps.getAbiVersionKey(), abiversionkey)) {
							//if the abi version key changed in any way compared to the previous version, clear for full compilation
							clearForFullCompilation("Modulepath change.");
						}
						SakerDirectory prev = resultpassmodulepaths.put(modulename, classesdir);
						if (prev != null) {
							throw new IllegalArgumentException("Module present in multiple module paths: " + modulename
									+ " in " + classesdir + " and " + prev);
						}
					} else {
						//the abi version key for the classpath is not available.
						//we fall back to reporting the files as dependencies as that is the only way we can detect any changes made to the classpath
						SakerDirectory prev = resultpassmodulepaths.put(modulename, classesdir);
						if (prev != null) {
							throw new IllegalArgumentException("Module present in multiple module paths: " + modulename
									+ " in " + classesdir + " and " + prev);
						}

						FileCollectionStrategy classfileadditiondep = RecursiveIgnoreCaseExtensionFileCollectionStrategy
								.create(path, "." + JavaTaskUtils.EXTENSION_CLASSFILE);
						taskContext.getTaskUtilities().collectFilesReportInputFileAndAdditionDependency(
								CompileFileTags.INPUT_MODULEPATH, classfileadditiondep);
					}
				} else {
					//the class path signatures are available.
					//we don't need to report the files as dependencies, as changes to the signatures will result in reinvocation
					currentExecutionModulePathStateInfos.put(path,
							new CompilationInfo.ExecutionClassPathStateInfo(
									mapClassSignaturesToCanonicalNames(cpsignatures), output.getModuleSignature(),
									abiversionkey));

					ExecutionClassPathStateInfo prevcps = ObjectUtils.getMapValue(prevExecutionModulePathStateInfos,
							path);
					if (prevcps == null || prevcps.getClassSignatures() == null) {
						//as the previous classpath signatures are not available, 
						//  clear for full compilation as we can't reliably detect changes
						clearForFullCompilation("Modulepath change.");
					}
					SakerDirectory prev = resultpassmodulepaths.put(modulename, classesdir);
					if (prev != null) {
						throw new IllegalArgumentException("Module present in multiple module paths: " + modulename
								+ " in " + classesdir + " and " + prev);
					}
				}

				if (outputmodulepath != null) {
					outputmodulepath.accept(this);
				}
			}

			@Override
			public void visit(FileModulePath modulepath) {
				FileLocation filelocation = modulepath.getFileLocation();
				if (!handledLocations.add(filelocation)) {
					return;
				}
				handleFileLocationModulePath(filelocation);
				modulePathIdeConfigurationEntries.add(new ModulePathIDEConfigurationEntry(filelocation));
			}

			@Override
			public void visit(SDKModulePath modulepath) {
				SakerPath path = SDKSupportUtils.getSDKPathReferencePath(modulepath.getSDKPathReference(),
						sdkReferences);
				resultcommandlinemodulepaths.add(path);
				modulePathIdeConfigurationEntries
						.add(new ModulePathIDEConfigurationEntry(LocalFileLocation.create(path)));
			}

			private void handleFileLocationModulePath(FileLocation filelocation) {
				filelocation.accept(new FileLocationVisitor() {
					@Override
					public void visit(ExecutionFileLocation loc) {
						SakerPath path = loc.getPath();
						SakerFile cpfile = taskContext.getTaskUtilities().resolveAtPath(path);
						if (cpfile == null) {
							throw ObjectUtils
									.sneakyThrow(new FileNotFoundException("Class path file not found: " + path));
						}

						try {
							//the abi version key for the classpath is not available.
							//we fall back to reporting the files as dependencies as that is the only way we can detect any changes made to the classpath
							if (cpfile instanceof SakerDirectory) {
								SakerDirectory mpdir = (SakerDirectory) cpfile;
								SakerFile moduleinfofile = mpdir.get("module-info.class");
								if (moduleinfofile == null) {
									throw ObjectUtils.sneakyThrow(new FileNotFoundException(
											"No module-info.class found in module path: " + path));
								}
								Optional<String> modulenameoptional = taskContext.computeFileContentData(moduleinfofile,
										f -> Optional.ofNullable(JavaTaskUtils.getModuleInfoModuleName(f.getBytes())));
								if (!modulenameoptional.isPresent()) {
									throw new IllegalArgumentException(
											"Failed to determine module name from module-info.class: " + path);
								}
								String modulename = modulenameoptional.get();
								SakerDirectory prev = resultpassmodulepaths.put(modulename, (SakerDirectory) cpfile);
								if (prev != null) {
									throw new IllegalArgumentException("Module present in multiple module paths: "
											+ modulename + " in " + cpfile + " and " + prev);
								}

								FileCollectionStrategy classfileadditiondep = RecursiveIgnoreCaseExtensionFileCollectionStrategy
										.create(path, "." + JavaTaskUtils.EXTENSION_CLASSFILE);
								taskContext.getTaskUtilities().collectFilesReportInputFileAndAdditionDependency(
										CompileFileTags.INPUT_MODULEPATH, classfileadditiondep);
							} else {
								taskContext.getTaskUtilities()
										.reportInputFileDependency(CompileFileTags.INPUT_MODULEPATH, cpfile);
								resultcommandlinemodulepaths.add(SakerPath.valueOf(taskContext.mirror(cpfile)));
							}
						} catch (IOException e) {
							throw ObjectUtils.sneakyThrow(e);
						}
					}

					@Override
					public void visit(LocalFileLocation loc) {
						SakerPath path = loc.getLocalPath();
						ContentDescriptor cd = taskContext.getTaskUtilities().getReportExecutionDependency(
								new LocalPathFileContentDescriptorExecutionProperty(taskContext.getTaskId(), path));
						if (cd == null) {
							throw ObjectUtils.sneakyThrow(
									new FileNotFoundException("Module path local file not found: " + path));
						}
						//TODO support ABI key

						NavigableMap<SakerPath, ? extends ContentDescriptor> cpcontentdescriptors;

						if (DirectoryContentDescriptor.INSTANCE.equals(cd)) {
							//the module path denotes a directory
							//add the dependencies on the class files

							NavigableMap<SakerPath, ? extends ContentDescriptor> classfilecontents = taskContext
									.getTaskUtilities()
									.getReportExecutionDependency(new LocalDirectoryClassFilesExecutionProperty(
											taskContext.getTaskId(), path))
									.getContents();
							cpcontentdescriptors = classfilecontents;
						} else {
							cpcontentdescriptors = ImmutableUtils.singletonNavigableMap(path, cd);
						}
						resultcommandlinemodulepaths.add(path);

						CompilationInfo.LocalClassPathStateInfo localstateinfo = new CompilationInfo.LocalClassPathStateInfo(
								cpcontentdescriptors, null);
						currentLocalModulePathStateInfos.put(path, localstateinfo);

						LocalClassPathStateInfo prevcps = ObjectUtils.getMapValue(prevLocalModulePathStateInfos, path);
						if (prevcps == null || !ObjectUtils.mapOrderedEquals(cpcontentdescriptors,
								prevcps.getClassPathFileContents())) {
							clearForFullCompilation("Modulepath change.");
						}
					}
				});
			}
		});
	}

	/**
	 * @throws IOException
	 *             thrown if mirroring fails
	 */
	private void collectBootClassPaths(Map<SakerPath, SakerDirectory> resultpassclasspaths,
			Collection<SakerPath> resultcommandlineclasspaths) throws IOException {
		collectClassPaths(bootClassPath, resultpassclasspaths, resultcommandlineclasspaths,
				CompileFileTags.INPUT_BOOT_CLASSPATH, this.bootClassPathIdeConfigurationEntries,
				"Bootclasspath change.");
	}

	/**
	 * @throws IOException
	 *             thrown if mirroring fails
	 */
	private void collectClassPaths(Map<SakerPath, SakerDirectory> resultpassclasspaths,
			Collection<SakerPath> resultcommandlineclasspaths) throws IOException {
		collectClassPaths(classPath, resultpassclasspaths, resultcommandlineclasspaths, CompileFileTags.INPUT_CLASSPATH,
				this.classPathIdeConfigurationEntries, "Classpath change.");
	}

	private void collectClassPaths(JavaClassPath classPath, Map<SakerPath, SakerDirectory> resultpassclasspaths,
			Collection<SakerPath> resultcommandlineclasspaths, Object classpathtag,
			Collection<ClassPathIDEConfigurationEntry> ideconfigentries, String fullreason) {
		if (classPath == null) {
			return;
		}

		//TODO there may be significant duplicated code in this method
		classPath.accept(new ClassPathVisitor() {
			private Set<FileLocation> handledLocations = new HashSet<>();
			private Set<JavaCompilationWorkerTaskIdentifier> handledWorkerTaskIds = new HashSet<>();

			@Override
			public void visit(ClassPathReference classpath) {
				Collection<? extends ClassPathEntry> entries = classpath.getEntries();
				if (ObjectUtils.isNullOrEmpty(entries)) {
					SakerLog.warning().println("No class path entries found for: " + classpath);
					return;
				}
				for (ClassPathEntry entry : entries) {
					if (entry == null) {
						SakerLog.warning().println("Class path entry is null for: " + classpath);
						continue;
					}
					FileLocation filelocation = entry.getFileLocation();
					if (filelocation == null) {
						SakerLog.warning().println("No class path file location for: " + entry);
						continue;
					}
					if (!handledLocations.add(filelocation)) {
						continue;
					}
					Object abiversionkey = entry.getAbiVersionKey();
					if (abiversionkey == null) {
						//fall back to the implementation version key if the abi version key is not available
						abiversionkey = entry.getImplementationVersionKey();
					}
					handleFileLocationClassPath(filelocation, abiversionkey);

					Collection<? extends JavaSourceDirectory> sourcedirs = entry.getSourceDirectories();
					Collection<JavaSourceDirectory> sourcediroptions;
					if (ObjectUtils.isNullOrEmpty(sourcedirs)) {
						sourcediroptions = null;
					} else {
						sourcediroptions = new LinkedHashSet<>();
						for (JavaSourceDirectory sdir : sourcedirs) {
							sourcediroptions
									.add(new SimpleJavaSourceDirectoryOption(sdir.getDirectory(), sdir.getFiles()));
						}
					}
					ClassPathIDEConfigurationEntry value = new ClassPathIDEConfigurationEntry(filelocation,
							sourcediroptions, entry.getSourceAttachment(), entry.getDocumentationAttachment());
					ideconfigentries.add(value);

					Collection<? extends ClassPathReference> additionalclasspaths = entry
							.getAdditionalClassPathReferences();
					if (!ObjectUtils.isNullOrEmpty(additionalclasspaths)) {
						JavaClassPathBuilder additionalcpbuilder = JavaClassPathBuilder.newBuilder();
						for (ClassPathReference additionalcp : additionalclasspaths) {
							additionalcpbuilder.addClassPath(additionalcp);
						}
						JavaClassPath additionalcp = additionalcpbuilder.build();
						additionalcp.accept(this);
					}
				}
			}

			@Override
			public void visit(CompilationClassPath classpath) {
				JavaCompilationWorkerTaskIdentifier workertaskid = classpath.getCompilationWorkerTaskIdentifier();
				if (!handledWorkerTaskIds.add(workertaskid)) {
					//don't get the task result to not install another dependency
					return;
				}
				TaskDependencyFuture<?> depresult = taskContext.getTaskDependencyFuture(workertaskid);
				InternalJavaCompilerOutput output = (InternalJavaCompilerOutput) depresult.getFinished();
				SakerPath path = output.getClassDirectory();
				Object abiversionkey = getAbiVersionKeyFallbackImplementationApplyDependency(depresult, output);

				JavaClassPath classpathcompilecp = output.getClassPath();
				depresult.setTaskOutputChangeDetector(
						SakerJavaCompilerUtils.getCompilerOutputClassPathTaskOutputChangeDetector(classpathcompilecp));

				SakerDirectory classesdir = taskContext.getTaskUtilities().resolveDirectoryAtPath(path);
				if (classesdir == null) {
					throw ObjectUtils
							.sneakyThrow(new FileNotFoundException("Compilation class directory not found: " + path));
				}

				ClassPathIDEConfigurationEntry ideconfig = new ClassPathIDEConfigurationEntry(
						ExecutionFileLocation.create(path), output.getSourceDirectories(), null, null);
				if (output.hadAnnotationProcessors()) {
					ideconfig.setSourceGenDirectory(output.getSourceGenDirectory());
				}
				ideconfigentries.add(ideconfig);

				Collection<ClassSignature> cpsignatures = output.getClassSignatures();
				if (cpsignatures == null) {
					//the signatures for the classpath are not available.
					if (abiversionkey != null) {
						//the abi key is available
						//we don't need to report the files as dependencies, as changes in the abi key will result in reinvocation
						//    as the task that produced the abi key will cause a task change delta for this
						currentExecutionClassPathStateInfos.put(path,
								new CompilationInfo.ExecutionClassPathStateInfo(null, null, abiversionkey));

						ExecutionClassPathStateInfo prevcps = ObjectUtils.getMapValue(prevExecutionClassPathStateInfos,
								path);
						if (prevcps == null || !Objects.equals(prevcps.getAbiVersionKey(), abiversionkey)) {
							//if the abi version key changed in any way compared to the previous version, clear for full compilation
							clearForFullCompilation(fullreason);
						}
						resultpassclasspaths.put(path, classesdir);
					} else {
						//the abi version key for the classpath is not available.
						//we fall back to reporting the files as dependencies as that is the only way we can detect any changes made to the classpath
						resultpassclasspaths.put(path, classesdir);

						FileCollectionStrategy classfileadditiondep = RecursiveIgnoreCaseExtensionFileCollectionStrategy
								.create(path, "." + JavaTaskUtils.EXTENSION_CLASSFILE);
						taskContext.getTaskUtilities().collectFilesReportInputFileAndAdditionDependency(classpathtag,
								classfileadditiondep);
					}
				} else {
					//the class path signatures are available.
					//we don't need to report the files as dependencies, as changes to the signatures will result in reinvocation
					currentExecutionClassPathStateInfos.put(path,
							new CompilationInfo.ExecutionClassPathStateInfo(
									mapClassSignaturesToCanonicalNames(cpsignatures), output.getModuleSignature(),
									abiversionkey));

					ExecutionClassPathStateInfo prevcps = ObjectUtils.getMapValue(prevExecutionClassPathStateInfos,
							path);
					if (prevcps == null || prevcps.getClassSignatures() == null) {
						//as the previous classpath signatures are not available, 
						//  clear for full compilation as we can't reliably detect changes
						clearForFullCompilation(fullreason);
					}
					resultpassclasspaths.put(path, classesdir);
				}

				if (classpathcompilecp != null) {
					classpathcompilecp.accept(this);
				}
			}

			@Override
			public void visit(FileClassPath classpath) {
				FileLocation filelocation = classpath.getFileLocation();
				if (!handledLocations.add(filelocation)) {
					return;
				}
				handleFileLocationClassPath(filelocation, null);

				ClassPathIDEConfigurationEntry ideconfig = new ClassPathIDEConfigurationEntry(filelocation);
				ideconfigentries.add(ideconfig);
			}

			@Override
			public void visit(SDKClassPath classpath) {
				SDKPathReference sdkpathref = classpath.getSDKPathReference();
				SakerPath path = SDKSupportUtils.getSDKPathReferencePath(sdkpathref, sdkReferences);
				resultcommandlineclasspaths.add(path);

				ClassPathIDEConfigurationEntry ideconfig = new ClassPathIDEConfigurationEntry(
						LocalFileLocation.create(path));
				ideconfigentries.add(ideconfig);
			}

			private void handleFileLocationClassPath(FileLocation filelocation, Object abiversionkey) {
				filelocation.accept(new FileLocationVisitor() {
					@Override
					public void visit(ExecutionFileLocation loc) {
						SakerPath path = loc.getPath();
						SakerFile cpfile = taskContext.getTaskUtilities().resolveAtPath(path);
						if (cpfile == null) {
							throw ObjectUtils
									.sneakyThrow(new FileNotFoundException("Class path file not found: " + path));
						}

						try {
							if (abiversionkey != null) {
								//the abi key is available
								//we don't need to report the files as dependencies, as changes in the abi key will result in reinvocation
								//    as the task that produced the abi key will cause a task change delta for this
								currentExecutionClassPathStateInfos.put(path,
										new CompilationInfo.ExecutionClassPathStateInfo(null, null, abiversionkey));

								ExecutionClassPathStateInfo prevcps = ObjectUtils
										.getMapValue(prevExecutionClassPathStateInfos, path);
								if (prevcps == null || !Objects.equals(prevcps.getAbiVersionKey(), abiversionkey)) {
									//if the abi version key changed in any way compared to the previous version, clear for full compilation
									clearForFullCompilation(fullreason);
								}
								if (cpfile instanceof SakerDirectory) {
									resultpassclasspaths.put(path, (SakerDirectory) cpfile);
								} else {
									resultcommandlineclasspaths.add(SakerPath.valueOf(taskContext.mirror(cpfile)));
								}
							} else {
								//the abi version key for the classpath is not available.
								//we fall back to reporting the files as dependencies as that is the only way we can detect any changes made to the classpath
								if (cpfile instanceof SakerDirectory) {
									resultpassclasspaths.put(path, (SakerDirectory) cpfile);

									FileCollectionStrategy classfileadditiondep = RecursiveIgnoreCaseExtensionFileCollectionStrategy
											.create(path, "." + JavaTaskUtils.EXTENSION_CLASSFILE);
									taskContext.getTaskUtilities().collectFilesReportInputFileAndAdditionDependency(
											classpathtag, classfileadditiondep);
								} else {
									taskContext.getTaskUtilities().reportInputFileDependency(classpathtag, cpfile);
									resultcommandlineclasspaths.add(SakerPath.valueOf(taskContext.mirror(cpfile)));
								}
							}
						} catch (IOException e) {
							throw ObjectUtils.sneakyThrow(e);
						}
					}

					@Override
					public void visit(LocalFileLocation loc) {
						SakerPath path = loc.getLocalPath();
						ContentDescriptor cd = taskContext.getTaskUtilities().getReportExecutionDependency(
								new LocalPathFileContentDescriptorExecutionProperty(taskContext.getTaskId(), path));
						if (cd == null) {
							throw ObjectUtils
									.sneakyThrow(new FileNotFoundException("Class path local file not found: " + path));
						}
						//TODO support ABI key

						NavigableMap<SakerPath, ? extends ContentDescriptor> cpcontentdescriptors;

						if (DirectoryContentDescriptor.INSTANCE.equals(cd)) {
							//the class path denotes a directory
							//add the dependencies on the class files

							NavigableMap<SakerPath, ? extends ContentDescriptor> classfilecontents = taskContext
									.getTaskUtilities()
									.getReportExecutionDependency(new LocalDirectoryClassFilesExecutionProperty(
											taskContext.getTaskId(), path))
									.getContents();
							cpcontentdescriptors = classfilecontents;
						} else {
							cpcontentdescriptors = ImmutableUtils.singletonNavigableMap(path, cd);
						}
						resultcommandlineclasspaths.add(path);

						CompilationInfo.LocalClassPathStateInfo localstateinfo = new CompilationInfo.LocalClassPathStateInfo(
								cpcontentdescriptors, null);
						currentLocalClassPathStateInfos.put(path, localstateinfo);

						LocalClassPathStateInfo prevcps = ObjectUtils.getMapValue(prevLocalClassPathStateInfos, path);
						if (prevcps == null || !ObjectUtils.mapOrderedEquals(cpcontentdescriptors,
								prevcps.getClassPathFileContents())) {
							clearForFullCompilation(fullreason);
						}
					}
				});
			}
		});
		if (prevLocalClassPathStateInfos != null) {
			if (prevLocalClassPathStateInfos.size() != currentLocalClassPathStateInfos.size()) {
				//the number of local class paths changed
				//this can happen if a classpath is removed
				//we need to clear for full compilation here, as this scenario is not detected during visiting
				clearForFullCompilation(fullreason);
			}
		}
	}

	private static void detectReadResourceTriggeredProcessors(TaskFileDeltas inputchanges,
			Set<ProcessorDetails> processors, Map<ProcessorDetails, ProcessorTriggerDelta> deltatriggeredprocessors) {
		for (ProcessorDetails procdetails : processors) {
			if (inputchanges.hasFileDeltaWithTag(new ProcessorReadResourceDependencyTag(procdetails))) {
				deltatriggeredprocessors.computeIfAbsent(procdetails, x -> new ProcessorTriggerDelta())
						.setReadResourceTriggered(true);
			}
		}
	}

	@Override
	public void build() throws Exception {
		TaskFileDeltas inputchanges = taskContext.getFileDeltas(DeltaType.INPUT_FILE_CHANGE);
		TaskFileDeltas inputadditionchanges = taskContext.getFileDeltas(DeltaType.INPUT_FILE_ADDITION);
		TaskFileDeltas outputchanges = taskContext.getFileDeltas(DeltaType.OUTPUT_FILE_CHANGE);

		if (prevInfo == null) {
			clearForFullCompilation("Incremental state missing.");
		} else if (inputchanges.hasFileDeltaWithTag(CompileFileTags.INPUT_CLASSPATH)
				|| inputadditionchanges.hasFileDeltaWithTag(CompileFileTags.INPUT_CLASSPATH)) {
			clearForFullCompilation("Classpath change.");
		} else if (inputchanges.hasFileDeltaWithTag(CompileFileTags.INPUT_BOOT_CLASSPATH)
				|| inputadditionchanges.hasFileDeltaWithTag(CompileFileTags.INPUT_BOOT_CLASSPATH)) {
			clearForFullCompilation("Bootclasspath change.");
		} else if (inputchanges.hasFileDeltaWithTag(CompileFileTags.INPUT_MODULEPATH)
				|| inputadditionchanges.hasFileDeltaWithTag(CompileFileTags.INPUT_MODULEPATH)) {
			clearForFullCompilation("Modulepath change.");
		} else {
			this.prevAbiVersionKey = prevInfo.getAbiVersionKey();
			//concurrent map, as multiple threads may remove from it at the same time
			this.prevClassFileDatas = new ConcurrentSkipListMap<>(prevInfo.getClassFiles());
			this.prevSourceFileDatas = new TreeMap<>(prevInfo.getSourceFiles());
			this.prevGeneratedSourceFileDatas = new TreeMap<>(prevInfo.getGeneratedSourceFiles());
			this.prevGeneratedClassFileDatas = new TreeMap<>(prevInfo.getGeneratedClassFiles());
			this.prevGeneratedResourceFileDatas = new TreeMap<>(prevInfo.getGeneratedResourceFiles());
			this.prevProcessorDetails = new HashMap<ProcessorDetails, ProcessorData>(prevInfo.getProcessorDetails());
			this.prevExecutionClassPathStateInfos = new TreeMap<>(prevInfo.getExecutionClassPathStateInfos());
			this.prevLocalClassPathStateInfos = new TreeMap<>(prevInfo.getLocalClassPathStateInfos());
			this.prevExecutionModulePathStateInfos = new TreeMap<>(prevInfo.getExecutionModulePathStateInfos());
			this.prevLocalModulePathStateInfos = new TreeMap<>(prevInfo.getLocalModulePathStateInfos());
			this.prevModuleInfoFileData = prevInfo.getModuleClassFile();
			this.prevCompilationModuleSet = ObjectUtils.cloneTreeSet(prevInfo.getCompilationModuleSet());
			this.prevDiagnosticEntries = new HashSet<>(prevInfo.getDiagnostics());
			this.prevModuleMainClass = prevInfo.getModuleMainClass();
			this.prevModuleVersion = prevInfo.getModuleVersion();
		}

		//only compare the method parameter names for ABI changes if there are any processors that can use them
		methodParameterNameChangeChecker = this.passAnnotationProcessors.isEmpty()
				? SignatureNameChecker.COMPARE_WITHOUT_NAMES
				: SignatureNameChecker.COMPARE_WITH_NAMES;

		this.changedInputSourceFiles = TaskUtils.collectFilesForTag(inputchanges, CompileFileTags.INPUT_SOURCE);

		this.changedOutputClassFiles = TaskUtils.collectFilesForTag(outputchanges, CompileFileTags.OUTPUT_CLASS);
		this.changedOutputGeneratedClassFiles = TaskUtils.collectFilesForTag(outputchanges,
				CompileFileTags.OUTPUT_GENERATED_CLASS);
		this.changedOutputGeneratedResourceFiles = TaskUtils.collectFilesForTag(outputchanges,
				CompileFileTags.OUTPUT_GENERATED_RESOURCE);
		this.changedOutputGeneratedSourceFiles = TaskUtils.collectFilesForTag(outputchanges,
				CompileFileTags.OUTPUT_GENERATED_SOURCE);

		this.unaddedGeneratedSourceFileDatas = new ConcurrentSkipListMap<>(prevGeneratedSourceFileDatas);

		Map<ProcessorDetails, JavaAnnotationProcessor> passProcessorReferences = new HashMap<>();
		LinkedHashMap<String, String> generalprocessoroptions = createGeneralProcessorOptions(passParameters,
				passAnnotationProcessorOptions);
		Collection<JavaAnnotationProcessor> annotationprocessors = ObjectUtils.nullDefault(passAnnotationProcessors,
				Collections.emptyList());
		if (!annotationprocessors.isEmpty()) {
			for (JavaAnnotationProcessor procref : annotationprocessors) {
				if (procref != null) {
					ProcessorDetails procdetails = new ProcessorDetails(procref,
							createProcessorSpecificOptions(generalprocessoroptions, procref));
					passProcessorReferences.put(procdetails, procref);
				}
			}
		}

		NavigableMap<SakerPath, SakerDirectory> passclasspathdirs = new TreeMap<>();
		NavigableMap<SakerPath, SakerDirectory> passbootclasspathdirs = new TreeMap<>();
		NavigableMap<String, SakerDirectory> passmodulepaths = new TreeMap<>();
		Set<SakerPath> commandlineclasspaths = new LinkedHashSet<>();
		Set<SakerPath> commandlinemodulepaths = new LinkedHashSet<>();
		Set<SakerPath> commandlinebootclasspaths = new LinkedHashSet<>();

		collectClassPaths(passclasspathdirs, commandlineclasspaths);
		collectBootClassPaths(passbootclasspathdirs, commandlinebootclasspaths);
		collectModulePaths(passmodulepaths, commandlinemodulepaths);

		boolean[] nocmdlineclasspath = { false };
		boolean[] allowcmdlinebootclasspath = { bootClassPath == null };
		createOptions(commandlineclasspaths, commandlinemodulepaths, nocmdlineclasspath, commandlinebootclasspaths);

		String compilejdkversion = compilationJavaSDKReference.getProperty(JavaSDKReference.PROPERTY_JAVA_VERSION);
		System.out.println("Performing incremental Java compilation. (JDK version: " + compilejdkversion + ")");

		if (fullCompilationReason == null) {
			if (!Objects.equals(sourceVersionName, prevInfo.getSourceVersion())) {
				clearForFullCompilation("Source version changed.");
			}
			if (!Objects.equals(targetVersionName, prevInfo.getTargetVersion())) {
				clearForFullCompilation("Target version changed.");
			}
			if (!Objects.equals(options, prevInfo.getOptions())) {
				clearForFullCompilation("Javac options changed.");
			}
			if (!Objects.equals(compilejdkversion, prevInfo.getJreVersion())) {
				clearForFullCompilation("Compiler JDK version changed.");
			}
		}

		Set<AbiChange> classpathabichanges = new HashSet<>();
		NavigableMap<SakerPath, SakerFile> firstroundsourcefiles = new ConcurrentSkipListMap<>();
		Map<ProcessorDetails, ProcessorTriggerDelta> deltatriggeredprocessors = new ConcurrentHashMap<>();
		NavigableMap<SakerPath, SourceFileData> removedsourcefiles = new TreeMap<>();

		if (isAnyModulePathModuleSigntureChanged()) {
			clearForFullCompilation("Modulepath change.");
		}

		ThreadUtils.runParallelRunnables(//
				() -> detectClassPathPassStateChanges(classpathabichanges), //
				() -> detectModulePathPassStateChanges(classpathabichanges), //
				() -> detectSourceFileDeltas(firstroundsourcefiles, removedsourcefiles), //
				() -> detectClassFileDeltas(firstroundsourcefiles, deltatriggeredprocessors), //
				() -> detectGeneratedSourceFileDeltas(deltatriggeredprocessors), //
				() -> detectGeneratedResourceFileDeltas(deltatriggeredprocessors), //
				() -> detectReadResourceTriggeredProcessors(inputchanges, passProcessorReferences.keySet(),
						deltatriggeredprocessors)//
		);

		try {
			boolean processordetailschanged = isProcessorsChanged(passProcessorReferences);
			if (firstroundsourcefiles.isEmpty() && Objects.equals(prevModuleMainClass, moduleMainClass)
					&& Objects.equals(prevModuleVersion, moduleVersion)) {
				if (removedsourcefiles.isEmpty() && classpathabichanges.isEmpty() && deltatriggeredprocessors.isEmpty()
						&& canSkipProcessorRunning(annotationprocessors) && !processordetailschanged) {
					SakerLog.log().verbose().println("No Java files changed, skipping compilation.");
					if (prevInfo != null) {
						IncrementalCompilationDirector.printDiagnosticEntries(prevInfo, suppressWarnings,
								passProcessorReferences);
						resultCompilationInfo = prevInfo;
						return;
					}
					IncrementalCompilationInfo result = new IncrementalCompilationInfo();
					result.setAbiVersionKey(NoSourcesVersionKey.INSTANCE);
					result.setImplementationVersionKey(NoSourcesVersionKey.INSTANCE);
					resultCompilationInfo = result;
					return;
				}
			}

			try {
				if (shouldExternallyCompile()) {
					//externally compile
					if (TestFlag.ENABLED) {
						TestFlag.metric().externallyCompiling();
					}
					SakerPath javaexe = compilationJavaSDKReference.getPath(JavaSDKReference.PATH_JAVA_EXE);
					if (javaexe == null) {
						throw new NullPointerException("Java SDK doesn't contain java.exe path. ("
								+ compilationJavaSDKReference + " : " + JavaSDKReference.PATH_JAVA_EXE + ")");
					}
					SakerEnvironment env = executionContext.getEnvironment();
					RemoteJavaCompilerCacheKey key = new RemoteJavaCompilerCacheKey(env, javaexe.toString(),
							env.getEnvironmentJarPath());
					RemoteCompiler remotecompiler = env.getCachedData(key);
					RemoteJavaRMIProcess proc = remotecompiler.getRmiProcess();

					try (RMIVariables vars = proc.getConnection().newVariables();
							InvocationContextImpl invcontext = new InvocationContextImpl(passclasspathdirs,
									passbootclasspathdirs, generalprocessoroptions, passProcessorReferences,
									passmodulepaths)) {

						JavaCompilationInvoker invoker = (JavaCompilationInvoker) vars
								.newRemoteInstance(InternalIncrementalCompilationInvoker.class.getConstructor());
						IncrementalCompilationDirector director = new IncrementalCompilationDirector(taskContext,
								invcontext, invoker);
						resultCompilationInfo = invokeCompilation(director, firstroundsourcefiles, removedsourcefiles,
								deltatriggeredprocessors, classpathabichanges, processordetailschanged,
								invoker.getJavaVersionProperty(), nocmdlineclasspath[0], allowcmdlinebootclasspath[0]);
						return;
					}
				}
				try (InvocationContextImpl invcontext = new InvocationContextImpl(passclasspathdirs,
						passbootclasspathdirs, generalprocessoroptions, passProcessorReferences, passmodulepaths)) {
					InternalIncrementalCompilationInvoker invoker = new InternalIncrementalCompilationInvoker();
					IncrementalCompilationDirector director = new IncrementalCompilationDirector(taskContext,
							invcontext, invoker);
					director.setParserCache(invoker.getParserCache());
					resultCompilationInfo = invokeCompilation(director, firstroundsourcefiles, removedsourcefiles,
							deltatriggeredprocessors, classpathabichanges, processordetailschanged,
							invoker.getJavaVersionProperty(), nocmdlineclasspath[0], allowcmdlinebootclasspath[0]);
					return;
				}
			} catch (JavaCompilationFailedException e) {
				throw e;
			} catch (JavacPrivateAPIError e) {
				throw new JavaCompilationFailedException("Unexpected Javac internal API error. "
						+ "You can turn off internal API usage and incremental compilation by setting BuildIncremental to false on your Java compiler pass. ("
						+ e + ")", e);
			} catch (Exception e) {
				throw new JavaCompilationFailedException("Unexpected error.", e);
			}
		} finally {
			fileRemover.clearDirectories(executionContext, this.outputClassDirectory.getSakerPath());
			fileRemover.clearDirectories(executionContext, this.outputNativeHeaderDirectory.getSakerPath());
			fileRemover.clearDirectories(executionContext, this.outputSourceDirectory.getSakerPath());
			fileRemover.clearDirectories(executionContext, this.outputResourceDirectory.getSakerPath());
		}
	}

	private boolean shouldExternallyCompile() {
		if (TestFlag.ENABLED) {
			if (TestFlag.metric().forceExternalCompilation()) {
				return true;
			}
		}
		return !JavaSDKReference.getCurrent().equals(compilationJavaSDKReference);
	}

	public static void detectChanges(ClassSignature prev, ClassSignature thiz, SignatureNameChecker parameterchecker,
			Consumer<AbiChange> result) {
		if (!ObjectUtils.objectsEquals(thiz.getSuperClass(), prev.getSuperClass(), TypeSignature::signatureEquals)
				|| !ObjectUtils.collectionOrderedEquals(thiz.getSuperInterfaces(), prev.getSuperInterfaces(),
						TypeSignature::signatureEquals)) {
			result.accept(new ClassInheritanceABIChange(thiz));
		}
		if (thiz.getKind() != prev.getKind()) {
			result.accept(new ClassTypeChange(thiz));
		}
		if (!Objects.equals(thiz.getModifiers(), prev.getModifiers())) {
			result.accept(new ClassModifierABIChange(thiz, prev.getModifiers()));
		}
		if (!AnnotatedSignature.annotationSignaturesEqual(thiz, prev)) {
			// annotations were changed on this class
			result.accept(new ClassAnnotationABIChange(thiz));
		}
		if (!ParameterizedSignature.signatureEquals(thiz, prev, parameterchecker)) {
			//TODO if only the type parameter name changed, then it should include a no-effect ABI change to trigger processors, but not recompilations
			result.accept(new ClassTypeParametersAbiChange(thiz));
		}

		NavigableMap<String, FieldSignature> fields = new TreeMap<>();
		NavigableMap<String, Collection<MethodSignature>> methods = new TreeMap<>();
		NavigableMap<String, ClassSignature> types = new TreeMap<>();
		thiz.categorizeEnclosedMemberSignaturesByName(fields, methods, types);

		NavigableMap<String, FieldSignature> prevfields = new TreeMap<>();
		NavigableMap<String, Collection<MethodSignature>> prevmethods = new TreeMap<>();
		NavigableMap<String, ClassSignature> prevtypes = new TreeMap<>();
		prev.categorizeEnclosedMemberSignaturesByName(prevfields, prevmethods, prevtypes);

		detectMemberChanges(fields, methods, types, prevfields, prevmethods, prevtypes, parameterchecker, prev, thiz,
				result);
	}

	private static void detectMemberChanges(NavigableMap<String, FieldSignature> fields,
			NavigableMap<String, Collection<MethodSignature>> methods, NavigableMap<String, ClassSignature> types,
			NavigableMap<String, FieldSignature> prevfields,
			NavigableMap<String, Collection<MethodSignature>> prevmethods,
			NavigableMap<String, ClassSignature> prevtypes, SignatureNameChecker parameterchecker, ClassSignature prev,
			ClassSignature thiz, Consumer<AbiChange> result) {
		ObjectUtils.iterateSortedMapEntries(prevfields, fields, (vname, prevsig, sig) -> {
			if (prevsig == null) {
				result.accept(new FieldAddedABIChange(thiz, sig));
			} else {
				if (sig == null) {
					result.accept(new FieldRemovedABIChange(prev, prevsig));
				} else {
					if (sig.isInitializerChanged(prevsig)) {
						if (!FieldSignature.isOnlyInitializerChanged(sig, prevsig)) {
							result.accept(new FieldRemovedABIChange(prev, prevsig));
							result.accept(new FieldAddedABIChange(thiz, sig));
						} else {
							result.accept(new FieldInitializerABIChange(thiz, sig));
						}
					} else {
						if (!FieldSignature.signatureEquals(sig, prevsig)) {
							result.accept(new FieldRemovedABIChange(prev, prevsig));
							result.accept(new FieldAddedABIChange(thiz, sig));
						}
					}
				}
			}
		});
		ObjectUtils.iterateSortedMapEntries(prevmethods, methods, (methodname, prevnamedmethods, namedmethods) -> {
			if (prevnamedmethods == null) {
				for (MethodSignature sig : namedmethods) {
					result.accept(new MethodAddedABIChange(thiz, sig));
				}
			} else {
				if (namedmethods == null) {
					for (MethodSignature m : prevnamedmethods) {
						result.accept(new MethodRemovedABIChange(prev, m));
					}
				} else {
					outerloop:
					for (Iterator<MethodSignature> it = namedmethods.iterator(); it.hasNext();) {
						MethodSignature method = it.next();
						for (Iterator<MethodSignature> previt = prevnamedmethods.iterator(); previt.hasNext();) {
							MethodSignature prevmethod = previt.next();
							if (MethodSignature.signatureEquals(method, prevmethod, parameterchecker)) {
								//TODO if only the parameter name changed, then it should include a no-effect ABI change to trigger processors, but not recompilations
								previt.remove();
								//found the matching method in previous
								continue outerloop;
							}
						}
						//if we get here we havent found any matching signatures in the previous one
						result.accept(new MethodAddedABIChange(thiz, method));
					}
					if (!prevnamedmethods.isEmpty()) {
						for (MethodSignature m : prevnamedmethods) {
							result.accept(new MethodRemovedABIChange(prev, m));
						}
					}
				}
			}
		});
		ObjectUtils.iterateSortedMapEntries(prevtypes, types, (tname, prevsig, sig) -> {
			if (prevsig == null) {
				result.accept(new ClassAddedABIChange(sig));
			} else {
				if (sig == null) {
					result.accept(new ClassRemovedABIChange(prevsig));
				} else {
					detectChanges(prevsig, sig, parameterchecker, result);
				}
			}
		});
	}
}
