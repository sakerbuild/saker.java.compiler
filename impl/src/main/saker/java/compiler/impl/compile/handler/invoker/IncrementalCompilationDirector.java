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
package saker.java.compiler.impl.compile.handler.invoker;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.ServiceConfigurationError;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.processing.FilerException;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;

import saker.build.exception.InvalidPathFormatException;
import saker.build.file.ByteArraySakerFile;
import saker.build.file.FileHandle;
import saker.build.file.SakerDirectory;
import saker.build.file.SakerFile;
import saker.build.file.content.ContentDescriptor;
import saker.build.file.content.DirectoryContentDescriptor;
import saker.build.file.content.HashContentDescriptor;
import saker.build.file.path.SakerPath;
import saker.build.file.provider.SakerPathFiles;
import saker.build.runtime.execution.SakerLog;
import saker.build.task.CommonTaskContentDescriptors;
import saker.build.task.TaskContext;
import saker.build.task.TaskExecutionUtilities;
import saker.build.thirdparty.saker.util.ConcatIterable;
import saker.build.thirdparty.saker.util.ConcurrentPrependAccumulator;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.StringUtils;
import saker.build.thirdparty.saker.util.function.Functionals;
import saker.build.thirdparty.saker.util.io.FileUtils;
import saker.build.thirdparty.saker.util.io.IOUtils;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayOutputStream;
import saker.build.thirdparty.saker.util.thread.ThreadUtils;
import saker.java.compiler.api.compile.JavaAnnotationProcessor;
import saker.java.compiler.api.compile.JavaCompilerWarningType;
import saker.java.compiler.api.compile.exc.JavaCompilationFailedException;
import saker.java.compiler.api.processing.SakerElementsTypes;
import saker.java.compiler.api.processing.SakerFiler;
import saker.java.compiler.api.processing.SakerMessager;
import saker.java.compiler.api.processing.SakerProcessingEnvironment;
import saker.java.compiler.api.processing.SakerRoundEnvironment;
import saker.java.compiler.api.processing.exc.SourceVersionNotFoundException;
import saker.java.compiler.api.processor.ProcessorCreator;
import saker.java.compiler.impl.JavaTaskUtils;
import saker.java.compiler.impl.JavaUtil;
import saker.java.compiler.impl.compat.ElementKindCompatUtils;
import saker.java.compiler.impl.compile.file.JavaCompilerDirectories;
import saker.java.compiler.impl.compile.file.SakerFileWrapperFileObject;
import saker.java.compiler.impl.compile.handler.CompilationHandler;
import saker.java.compiler.impl.compile.handler.DirectoryClearingFileRemover;
import saker.java.compiler.impl.compile.handler.ExternalizableLocation;
import saker.java.compiler.impl.compile.handler.ProcessorCreationContextImpl;
import saker.java.compiler.impl.compile.handler.diagnostic.DiagnosticEntry;
import saker.java.compiler.impl.compile.handler.diagnostic.DiagnosticLocation;
import saker.java.compiler.impl.compile.handler.diagnostic.DiagnosticLocationReference;
import saker.java.compiler.impl.compile.handler.diagnostic.DiagnosticPositionTable;
import saker.java.compiler.impl.compile.handler.diagnostic.PathSignatureDiagnosticLocationReference;
import saker.java.compiler.impl.compile.handler.incremental.AnnotationSetCollector;
import saker.java.compiler.impl.compile.handler.incremental.ByteArrayBufferingJavaFileObject;
import saker.java.compiler.impl.compile.handler.incremental.ClientProcessorException;
import saker.java.compiler.impl.compile.handler.incremental.IncrementalCompilationHandler;
import saker.java.compiler.impl.compile.handler.incremental.IncrementalCompilationInfo;
import saker.java.compiler.impl.compile.handler.incremental.model.elem.DocumentedElement;
import saker.java.compiler.impl.compile.handler.incremental.model.elem.IncrementalAnnotationMirror;
import saker.java.compiler.impl.compile.handler.incremental.model.elem.IncrementalAnnotationValue;
import saker.java.compiler.impl.compile.handler.incremental.model.elem.SignaturedElement;
import saker.java.compiler.impl.compile.handler.incremental.model.scope.ImportDeclaration;
import saker.java.compiler.impl.compile.handler.incremental.model.scope.ImportScope;
import saker.java.compiler.impl.compile.handler.info.ClassFileData;
import saker.java.compiler.impl.compile.handler.info.ClassHoldingData;
import saker.java.compiler.impl.compile.handler.info.ClassHoldingFileData;
import saker.java.compiler.impl.compile.handler.info.CompilationInfo;
import saker.java.compiler.impl.compile.handler.info.CompilationInfo.ProcessorData;
import saker.java.compiler.impl.compile.handler.info.CompilationInfo.ProcessorData.ClassDocReference;
import saker.java.compiler.impl.compile.handler.info.CompilationInfo.ProcessorData.DocReference;
import saker.java.compiler.impl.compile.handler.info.CompilationInfo.ProcessorData.FieldDocReference;
import saker.java.compiler.impl.compile.handler.info.CompilationInfo.ProcessorData.MethodDocReference;
import saker.java.compiler.impl.compile.handler.info.CompilationInfo.ProcessorData.ModuleDocReference;
import saker.java.compiler.impl.compile.handler.info.CompilationInfo.ProcessorData.PackageDocReference;
import saker.java.compiler.impl.compile.handler.info.GeneratedClassFileData;
import saker.java.compiler.impl.compile.handler.info.GeneratedFileOrigin;
import saker.java.compiler.impl.compile.handler.info.GeneratedResourceFileData;
import saker.java.compiler.impl.compile.handler.info.GeneratedSourceFileData;
import saker.java.compiler.impl.compile.handler.info.ProcessorGeneratedClassHoldingFileData;
import saker.java.compiler.impl.compile.handler.info.ProcessorGeneratedFileData;
import saker.java.compiler.impl.compile.handler.info.SignaturePath;
import saker.java.compiler.impl.compile.handler.info.SignatureSourcePositions;
import saker.java.compiler.impl.compile.handler.info.SignatureSourcePositions.Position;
import saker.java.compiler.impl.compile.handler.info.SourceFileData;
import saker.java.compiler.impl.compile.handler.invoker.JavaCompilationInvoker.ABIParseInfo;
import saker.java.compiler.impl.compile.handler.usage.AbiUsage;
import saker.java.compiler.impl.compile.handler.usage.FieldABIInfo;
import saker.java.compiler.impl.compile.handler.usage.TopLevelAbiUsage;
import saker.java.compiler.impl.compile.signature.change.AbiChange;
import saker.java.compiler.impl.compile.signature.change.ClassAddedABIChange;
import saker.java.compiler.impl.compile.signature.change.ClassRemovedABIChange;
import saker.java.compiler.impl.compile.signature.change.ModuleChangeABIChange;
import saker.java.compiler.impl.compile.signature.change.ModulePathABIChange;
import saker.java.compiler.impl.compile.signature.change.PackageAnnotationsChangeABIChange;
import saker.java.compiler.impl.compile.signature.change.PlainPackageInfoAddedABIChange;
import saker.java.compiler.impl.compile.signature.change.member.FieldInitializerABIChange;
import saker.java.compiler.impl.compile.signature.parser.ParserCache;
import saker.java.compiler.impl.options.OutputBytecodeManipulationOption;
import saker.java.compiler.impl.signature.Signature;
import saker.java.compiler.impl.signature.element.AnnotatedSignature;
import saker.java.compiler.impl.signature.element.AnnotationSignature;
import saker.java.compiler.impl.signature.element.AnnotationSignature.Value;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.FieldSignature;
import saker.java.compiler.impl.signature.element.MethodSignature;
import saker.java.compiler.impl.signature.element.ModuleSignature;
import saker.java.compiler.impl.signature.element.PackageSignature;
import saker.java.compiler.impl.signature.element.SignatureNameChecker;
import saker.java.compiler.jdk.impl.JavaCompilationUtils;
import saker.java.compiler.jdk.impl.incremental.model.IncrementalElementsTypes;
import saker.java.compiler.jdk.impl.invoker.ForwardingSakerElementsTypes;
import testing.saker.java.compiler.TestFlag;

public class IncrementalCompilationDirector implements JavaCompilerInvocationDirector {
	//use always the same locale when converting the paths to a lowercase representation for checking if they're regenerated
	private static final Locale LOWERCASE_PATHS_LOCALE = Locale.ENGLISH;

	public static final Element[] EMPTY_ELEMENT_ARRAY = {};

	private final JavaCompilerInvocationContext invocationContext;
	private final JavaCompilationInvoker invoker;
	private final TaskContext taskContext;
	private final TaskExecutionUtilities taskUtils;

	private final Set<AbiChange> allABIChanges = ConcurrentHashMap.newKeySet();
	private final IncrementalCompilationInfo info = new IncrementalCompilationInfo();

	private final Map<ProcessorDetails, JavaAnnotationProcessor> passProcessorReferences;
	private JavaCompilerDirectories directoryPaths;

	private Collection<String> options;

	/**
	 * Collection of files that are given as input to this compilation task. These are the files that are listed under
	 * the source directories.
	 */
	private NavigableMap<SakerPath, ? extends SakerFile> presentSourceFilePaths;

	/**
	 * Collection of sources from previous compilation which haven't been added to this compilation task yet.
	 */
	private NavigableMap<SakerPath, SourceFileData> unaddedSourceFileDatas;
	/**
	 * Collection of sources from previous compilation. Does not contain the sources which have been removed by the
	 * user.
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
	private NavigableMap<SakerPath, GeneratedSourceFileData> prevAllGeneratedSourceFileDatas;
	/**
	 * Collection of class files from previous compilation which haven't been regenerated in this compilation task.
	 */
	private NavigableMap<SakerPath, GeneratedClassFileData> prevAllGeneratedClassFileDatas;
	/**
	 * Collection of resource files from previous compilation.
	 */
	private NavigableMap<SakerPath, GeneratedResourceFileData> prevGeneratedResourceFileDatas;

	private Set<DiagnosticEntry> prevDiagnosticEntries;
	private Map<ProcessorDetails, Set<DiagnosticEntry>> prevOriginDiagnosticEntries;

	private ClassHoldingData prevModuleInfoFileData;
	private NavigableSet<String> prevCompilationModuleSet;

	private Map<ProcessorDetails, ProcessorData> prevProcessorDatas;

	private final Object javacSync = new Object();
	protected IncrementalElementsTypes elemTypes;
	private AnnotationSetCollector rootElementsAnnotationSetCollector;
	private volatile AnnotationSetCollector startRootElementsAnnotationSetCollector;
	private ProcessorCreationContextImpl processorCreationContext;

	private Collection<ProcessorState> processorStates;
	private Map<ProcessorDetails, ProcessorState> processorsByDetails = Collections.emptyMap();
	private boolean processorsFinalRoundsCalled = false;
	private boolean requiresUngeneratedFilesRemoved = true;
	private boolean finalClassPathAdded = false;

	private NavigableMap<SakerPath, ClassHoldingFileData> roundParsedElementDatas = new ConcurrentSkipListMap<>();

	/**
	 * Maps class binary names to source file paths.
	 */
	private NavigableMap<String, SakerPath> generatedClassFileSourceFiles = new ConcurrentSkipListMap<>();

	/**
	 * A map of all the parsed compilation units in the compilation.
	 */
	private final NavigableMap<SakerPath, SourceFileData> totalParsedCompilationUnitsByPath = new ConcurrentSkipListMap<>();
	/**
	 * A map of all user sources parsed in this compilation. Doesn't contain the generated sources.
	 */
	private final NavigableMap<SakerPath, SourceFileData> totalParsedSourcesByPath = new ConcurrentSkipListMap<>();
	/**
	 * A map of all generated sources parsed in this compilation. Doesn't contain the user sources.
	 */
	private final NavigableMap<SakerPath, GeneratedSourceFileData> totalParsedGeneratedSourcesByPath = new ConcurrentSkipListMap<>();

	private volatile Throwable anyProcessingException = null;
	private volatile boolean errorRaised = false;

	private ConcurrentSkipListMap<SakerPath, GeneratedFileOrigin> generatedFileOrigins = new ConcurrentSkipListMap<>();

	private Map<ProcessorDetails, ProcessorTriggerDelta> deltaTriggeredProcessors;

	private NavigableMap<SakerPath, GeneratedResourceFileData> unaddedGeneratedResourceFileDatas;

	private NavigableMap<SakerPath, Set<? extends Element>> startRootElements = new TreeMap<>();

	private NavigableMap<SakerPath, SourceFileData> reusedSourceResultsFromPreviousCompilation = new TreeMap<>();

	private ConcurrentPrependAccumulator<GeneratedSourceFileData> roundRegeneratedSources = new ConcurrentPrependAccumulator<>();
	private NavigableMap<SakerPath, GeneratedSourceFileData> unaddedRegeneratedSources = new ConcurrentSkipListMap<>();

	private Set<SakerPath> parseEnteredSourcePaths = new ConcurrentSkipListSet<>();
	private Set<SakerPath> rootElementsAddedPaths = new TreeSet<>();

	private boolean processorDetailsChanged;
	private boolean sourceCompilationInclusionPending = false;
	private boolean classCompilationInclusionPending = false;

	private ClassHoldingFileData moduleInfoFileData;
	private NavigableSet<String> compilationModuleSet;
	private boolean modulesSupported;

	private boolean firstSourceParsing = true;

	private Map<ProcessorDetails, ProcessorData> docCommentInterestedProcessors = new HashMap<>();

	protected ParserCache cache = new ParserCache();
	private DirectoryClearingFileRemover fileRemover;

	private ConcurrentSkipListMap<SakerPath, ClassFileData> infoPutClassFiles;

	private final Object resourcesOpenedLock = new Object();
	//TODO the resourcesOpened map should contain the initial resources (source files, and classpath class files)
	private Map<OpenedResource, OpenedResourceState> resourcesOpened = new HashMap<>();

	private NavigableMap<String, SakerPath> processorInputLocations;

	private SignatureNameChecker methodParameterNameChangeChecker;

	private boolean noCommandLineClassPath;
	//true by default
	private boolean allowCommandLineBootClassPath = true;

	private String sourceVersionOptionName;
	private String targetVersionOptionName;

	private OutputBytecodeManipulationOption bytecodeManipulation;
	private OutputBytecodeManipulationOption prevBytecodeManipulation;

	public IncrementalCompilationDirector(TaskContext taskContext, JavaCompilerInvocationContext invocationContext,
			JavaCompilationInvoker invoker) {
		this.taskContext = taskContext;
		this.taskUtils = taskContext.getTaskUtilities();
		this.invocationContext = invocationContext;
		this.invoker = invoker;
		this.passProcessorReferences = invocationContext.getPassProcessorReferences();
	}

	public void setCompilationDetails(NavigableMap<SakerPath, ? extends SakerFile> presentSourceFilePaths,
			NavigableMap<SakerPath, SourceFileData> prevSourceFileDatas,
			NavigableMap<SakerPath, GeneratedSourceFileData> unaddedGeneratedSourceFileDatas,
			NavigableMap<SakerPath, GeneratedSourceFileData> prevGeneratedSourceFileDatas,
			NavigableMap<SakerPath, GeneratedClassFileData> prevGeneratedClassFileDatas,
			NavigableMap<SakerPath, GeneratedResourceFileData> prevGeneratedResourceFileDatas,
			Map<ProcessorDetails, ProcessorTriggerDelta> deltatriggeredprocessors,
			Map<ProcessorDetails, ProcessorData> prevProcessorDetails,
			NavigableMap<SakerPath, GeneratedResourceFileData> unaddedGeneratedResourceFileDatas,
			Set<AbiChange> classpathabichanges, boolean processordetailschanged,
			ClassHoldingData prevModuleInfoFileData, NavigableSet<String> prevCompilationModuleSet,
			Set<DiagnosticEntry> prevDiagnosticEntries, DirectoryClearingFileRemover fileremover,
			NavigableMap<SakerPath, ClassFileData> prevClassFileDatas,
			NavigableMap<String, SakerPath> processorInputLocations,
			SignatureNameChecker methodParameterNameChangeChecker,
			OutputBytecodeManipulationOption bytecodeManipulation,
			OutputBytecodeManipulationOption prevBytecodeManipulation, boolean noCommandLineClassPath,
			boolean allowcommandlinebootclasspath, String sourceVersionName, String targetVersionName) {
		this.presentSourceFilePaths = presentSourceFilePaths;
		this.prevSourceFileDatas = prevSourceFileDatas;
		this.unaddedGeneratedSourceFileDatas = unaddedGeneratedSourceFileDatas;
		this.prevAllGeneratedSourceFileDatas = prevGeneratedSourceFileDatas;
		this.prevAllGeneratedClassFileDatas = prevGeneratedClassFileDatas;
		this.prevGeneratedResourceFileDatas = prevGeneratedResourceFileDatas;
		this.deltaTriggeredProcessors = deltatriggeredprocessors;
		this.prevProcessorDatas = prevProcessorDetails;
		this.fileRemover = fileremover;
		this.unaddedGeneratedResourceFileDatas = unaddedGeneratedResourceFileDatas;
		this.processorDetailsChanged = processordetailschanged;
		this.prevModuleInfoFileData = prevModuleInfoFileData;
		this.prevCompilationModuleSet = prevCompilationModuleSet;
		this.prevDiagnosticEntries = prevDiagnosticEntries;
		this.processorInputLocations = processorInputLocations;
		this.methodParameterNameChangeChecker = methodParameterNameChangeChecker;
		this.bytecodeManipulation = bytecodeManipulation;
		this.prevBytecodeManipulation = prevBytecodeManipulation;
		this.noCommandLineClassPath = noCommandLineClassPath;
		this.allowCommandLineBootClassPath = allowcommandlinebootclasspath;
		this.sourceVersionOptionName = sourceVersionName;
		this.targetVersionOptionName = targetVersionName;

		this.infoPutClassFiles = new ConcurrentSkipListMap<>(prevClassFileDatas);

		this.prevOriginDiagnosticEntries = new HashMap<>();
		for (DiagnosticEntry de : prevDiagnosticEntries) {
			GeneratedFileOrigin origin = de.getOrigin();
			if (origin == null) {
				continue;
			}
			this.prevOriginDiagnosticEntries
					.computeIfAbsent(origin.getProcessorDetails(), Functionals.hashSetComputer()).add(de);
		}

		allABIChanges.addAll(classpathabichanges);
	}

	public boolean hadAbiChanges() {
		return !allABIChanges.isEmpty();
	}

	public void setParserCache(ParserCache cache) {
		this.cache = cache;
	}

	private String[] getOptions() {
		return options.toArray(ObjectUtils.EMPTY_STRING_ARRAY);
	}

	private boolean canSkipCompilationEntirely() {
		if (!canSkipUserProcessors()) {
			return false;
		}
		//no units need to be parsed, and no processors have to be run
		return true;
	}

	private void collectDocCommentInterestedProcessors() {
		if (ObjectUtils.isNullOrEmpty(prevProcessorDatas)) {
			return;
		}
		for (Entry<ProcessorDetails, ProcessorData> entry : prevProcessorDatas.entrySet()) {
			ProcessorData prevdata = entry.getValue();
			if (prevdata.isDocCommentInterested()) {
				docCommentInterestedProcessors.put(entry.getKey(), prevdata);
			}
		}
	}

	private void removeInfoPutClassFilesForSourceFileData(ClassHoldingData sfd) {
		sfd.getGeneratedClassDatas().forEach(infoPutClassFiles::remove);
	}

	private SortedSet<SakerPath> analyzeStartingABIChanges() {
		if (allABIChanges.isEmpty() || unaddedSourceFileDatas.isEmpty()) {
			//if there were no changes
			//or all of the files were already added
			return Collections.emptyNavigableSet();
		}
		Collection<AbiChange> roundchanges = new HashSet<>(allABIChanges);
		SortedSet<SakerPath> result = new TreeSet<>();

		Collection<AbiChange> nextroundchanges = new HashSet<>();
		Consumer<AbiChange> adder = c -> {
			if (allABIChanges.add(c)) {
				nextroundchanges.add(c);
			}
		};

		Iterable<? extends SourceFileData> sources = prevSourceFileDatas.values();
		@SuppressWarnings("unchecked")
		Iterable<? extends ProcessorGeneratedClassHoldingFileData> gensources = new ConcatIterable<>(
				ImmutableUtils.asUnmodifiableArrayList(prevAllGeneratedSourceFileDatas.values(),
						prevAllGeneratedClassFileDatas.values()));

		while (!unaddedSourceFileDatas.isEmpty()) {
			for (SourceFileData src : sources) {
				for (AbiChange change : roundchanges) {
					if (change.affects(src.getABIUsage(), adder)) {
						SakerPath srcpath = src.getPath();
						SourceFileData unaddedremoved = unaddedSourceFileDatas.remove(srcpath);
						if (unaddedremoved != null) {
							removeInfoPutClassFilesForSourceFileData(unaddedremoved);
							if (IncrementalCompilationHandler.LOGGING_ENABLED) {
								SakerLog.log().verbose().println("Affected ABI change: (" + change + ") -> "
										+ SakerPathFiles.toRelativeString(srcpath));
							}
							result.add(srcpath);
						}
					}
				}
			}
			for (ProcessorGeneratedClassHoldingFileData src : gensources) {
				GeneratedFileOrigin origin = src.getOrigin();
				for (AbiChange change : roundchanges) {
					if (change.affects(src.getABIUsage(), Functionals.nullConsumer())) {
						//if a change affects a generated file, add the originating files to the processor triggers
						deltaTriggeredProcessors
								.computeIfAbsent(origin.getProcessorDetails(), x -> new ProcessorTriggerDelta())
								.getTriggeredUnitPaths().addAll(origin.getOriginatingFilePaths());
						break;
					}
				}
			}

			if (nextroundchanges.isEmpty()) {
				break;
			}
			roundchanges.clear();
			roundchanges.addAll(nextroundchanges);
			nextroundchanges.clear();
		}
		return result;
	}

	private void removeFromUnaddedSourceFileDatas(Iterable<? extends SakerPath> sourcepaths) {
		for (SakerPath srcpath : sourcepaths) {
			SourceFileData unaddedremoved = unaddedSourceFileDatas.remove(srcpath);
			if (unaddedremoved != null) {
				removeInfoPutClassFilesForSourceFileData(unaddedremoved);
			}
		}
	}

	@Override
	public final CompilationInfo invokeCompilation(NavigableMap<SakerPath, ? extends FileHandle> units,
			NavigableMap<SakerPath, ? extends ClassHoldingFileData> removedsourcefiles)
			throws IOException, JavaCompilationFailedException {
		this.unaddedSourceFileDatas = new ConcurrentSkipListMap<>(prevSourceFileDatas);

		analyzeRemovedProcessors();
		boolean modulesignaturefileremoved = false;

		collectDocCommentInterestedProcessors();

//		Set<SakerPath> unaddedsrcfileskeyset = unaddedSourceFileDatas.keySet();
		removeFromUnaddedSourceFileDatas(units.keySet());
//		unaddedsrcfileskeyset.removeAll(units.keySet());
		if (!removedsourcefiles.isEmpty()) {
			Set<SakerPath> removedsourcefilepaths = removedsourcefiles.keySet();

			prevSourceFileDatas.keySet().removeAll(removedsourcefilepaths);
			removeFromUnaddedSourceFileDatas(removedsourcefilepaths);
//			unaddedsrcfileskeyset.removeAll(removedsourcefilepaths);
			for (ClassHoldingFileData sfd : removedsourcefiles.values()) {
				removeCompiledFilesForSource(sfd);
				for (ClassSignature c : sfd.getClassSignatures()) {
					allABIChanges.add(new ClassRemovedABIChange(c));
				}
				ModuleSignature msig = sfd.getModuleSignature();
				PackageSignature psig = sfd.getPackageSignature();
				if (msig != null) {
					modulesignaturefileremoved = true;
				}
				if (psig != null && !psig.getAnnotations().isEmpty()) {
					addABIChange(new PackageAnnotationsChangeABIChange(psig.getName()));
				}
			}
			analyzeStartingMultiOriginDependencies(removedsourcefilepaths);
		}

		SortedMap<SakerPath, FileHandle> unitpaths = new TreeMap<>(units);
		SortedSet<SakerPath> abichangedsources = analyzeStartingABIChanges();
		if (!abichangedsources.isEmpty()) {
			removeFromUnaddedSourceFileDatas(abichangedsources);
//			unaddedsrcfileskeyset.removeAll(abichangedsources);
			for (SakerPath src : abichangedsources) {
				unitpaths.computeIfAbsent(src, fpath -> SakerPathFiles.resolveAtPath(taskContext, fpath));
			}
		}

		if (!modulesignaturefileremoved && unitpaths.isEmpty() && canSkipCompilationEntirely()
				&& Objects.equals(prevBytecodeManipulation, bytecodeManipulation)) {
			info.setSourceFiles(ImmutableUtils.makeImmutableNavigableMap(prevSourceFileDatas));
			putClassFilesToInfoForSource(prevSourceFileDatas);

			info.putGeneratedSourceFiles(prevAllGeneratedSourceFileDatas);
			putClassFilesToInfoForSource(prevAllGeneratedSourceFileDatas);

			info.setClassFiles(infoPutClassFiles);

			info.putGeneratedClassFiles(prevAllGeneratedClassFileDatas);

			info.putGeneratedResourceFiles(unaddedGeneratedResourceFileDatas);

			info.setProcessorDetails(prevProcessorDatas);

			info.addDiagnostics(prevDiagnosticEntries);

			info.setModuleClassFile(prevModuleInfoFileData);
			info.setCompilationModuleSet(prevCompilationModuleSet);
			info.setBytecodeManipulation(bytecodeManipulation);

			SakerLog.log().verbose().println("No affected Java changes, skipping compilation.");
			SakerLog.log().verbose().println();
			printDiagnosticEntries();
			return info;
		}

		this.options = invocationContext.getOptions();

		directoryPaths = new JavaCompilerDirectories(taskContext);
		directoryPaths.setNoCommandLineClassPath(noCommandLineClassPath);
		directoryPaths.setAllowCommandLineBootClassPath(allowCommandLineBootClassPath);
		directoryPaths.setBytecodeManipulation(bytecodeManipulation);
		directoryPaths.addDirectory(ExternalizableLocation.LOCATION_CLASS_OUTPUT,
				invocationContext.getOutputClassDirectory());
		directoryPaths.addDirectory(ExternalizableLocation.LOCATION_SOURCE_OUTPUT,
				invocationContext.getOutputSourceDirectory());
		directoryPaths.addDirectory(ExternalizableLocation.LOCATION_NATIVE_HEADER_OUTPUT,
				invocationContext.getOutputNativeHeaderDirectory());
		directoryPaths.addDirectory(ExternalizableLocation.LOCATION_CLASS_PATH,
				invocationContext.getClassPathDirectories());
		directoryPaths.addDirectory(ExternalizableLocation.LOCATION_PLATFORM_CLASS_PATH,
				invocationContext.getBootClassPathDirectories());

		directoryPaths.addModulePaths(invocationContext.getModulePathDirectories());

		directoryPaths.setResourceOutputDirectory(invocationContext.getOutputResourceDirectory());
		if (!ObjectUtils.isNullOrEmpty(processorInputLocations)) {
			for (Entry<String, SakerPath> entry : processorInputLocations.entrySet()) {
				String locname = entry.getKey();
				if (!JavaUtil.isValidProcessorInputLocationName(locname)) {
					throw new IllegalArgumentException("Invalid processor input location name: " + locname);
				}
				SakerDirectory dir = this.taskUtils.resolveDirectoryAtPath(entry.getValue());
				if (dir != null) {
					directoryPaths.addDirectory(new ExternalizableLocation(locname, false), dir);
				} else {
					SakerLog.warning().println(
							"Processor input location is not a directory: " + locname + " for " + entry.getValue());
				}
			}
		}

		invoker.initCompilation(this, directoryPaths, this.getOptions(), this.getSourceVersionOptionName(),
				this.getTargetVersionOptionName());
		SakerPathBytes[] unitpathbytes;
		try {
			//always include module-info files if compiling to JDK >= 9
			//use > RELEASE_8 instead of >= RELEASE_9 to be able to properly compile on JDK 8
			modulesSupported = JavaUtil.isModuleSupportingSourceVersion(getSourceVersionName());
			if (modulesSupported) {
				for (Entry<SakerPath, ? extends FileHandle> entry : presentSourceFilePaths.entrySet()) {
					if (JavaTaskUtils.isModuleInfoSource(entry.getValue().getName())) {
						unitpaths.put(entry.getKey(), entry.getValue());
						//do not break compilation, as multiple module-info files might be present, and they should trigger an error
					}
				}
			}
			sourceCompilationInclusionPending = !unitpaths.isEmpty();

			if (IncrementalCompilationHandler.LOGGING_ENABLED) {
				System.out.println("Compile: ");
				for (SakerPath p : unitpaths.keySet()) {
					System.out.println("    " + SakerPathFiles.toRelativeString(p));
				}
				System.out.println();
				System.out.println("ABI changes:");
				allABIChanges.forEach(System.out::println);
				System.out.println();
			}

			parseEnteredSourcePaths.addAll(unitpaths.keySet());

			unitpathbytes = new SakerPathBytes[unitpaths.size()];
			{
				int i = 0;
				for (Entry<SakerPath, ? extends FileHandle> entry : unitpaths.entrySet()) {
					unitpathbytes[i++] = new SakerPathBytes(entry.getKey(), entry.getValue().getBytes());
					if (TestFlag.ENABLED) {
						TestFlag.metric().javacCompilingFile(entry.getKey());
					}
				}
			}
		} catch (Throwable e) {
			//an exception was received after the invoker has been initialized
			//close it, and forward the exception
			IOUtils.addExc(e, IOUtils.closeExc(invoker));
			throw e;
		}

		try {
			//invoking the compilation
			invoker.invokeCompilation(unitpathbytes);
		} catch (Exception | com.sun.tools.javac.util.FatalError | com.sun.tools.javac.util.Abort e) {
			//print the diagnostic entries in case of severe error as well
			//close the invoker as well, as we don't know if it has closed itself or not
			IOUtils.addExc(e, IOUtils.closeExc(invoker));
			try {
				printDiagnosticEntries();
			} catch (Throwable e2) {
				e.addSuppressed(e2);
			}
			throw e;
		}

		if (isAnyErrorRaised()) {
			printDiagnosticEntries();
			if (anyProcessingException != null) {
				throw new JavaCompilationFailedException("Java compilation failed.", anyProcessingException);
			}
			throw new JavaCompilationFailedException("Java compilation failed.");
		}

		SortedMap<SakerPath, ABIParseInfo> abiusages = invoker.getParsedSourceABIUsages();

		//putAll is much slower than building a map from sorted
		ConcurrentSkipListMap<SakerPath, SourceFileData> infoputsrcfiles = new ConcurrentSkipListMap<>(
				unaddedSourceFileDatas);

		ThreadUtils.runParallelRunnables(//
				() -> putABIUsagesToSourceFiles(abiusages, infoputsrcfiles), //
				() -> putABIUsagesToGeneratedSourceFiles(abiusages), //
				() -> CompilationHandler.putResultClassFilesToIncrementalInfo(directoryPaths.getOutputClassFiles(),
						generatedClassFileSourceFiles, totalParsedCompilationUnitsByPath, infoPutClassFiles)//
//
//				() -> putClassFilesToInfoForSource(unaddedSourceFileDatas)//
		);
		info.setSourceFiles(infoputsrcfiles);
		info.setClassFiles(infoPutClassFiles);

		warnUnrecognizedProcessorOptions();
		reuseOriginatedDiagnosticEntries();
		printDiagnosticEntries();

		if (isProcessorsContextInitialized()) {
			Map<ProcessorDetails, ProcessorData> procdatas = new HashMap<>();
			for (ProcessorState proc : processorStates) {
				ProcessorDetails procdetails = proc.getProcessorDetails();
				if (!proc.isInitialized()) {
					ProcessorData prevdata = prevProcessorDatas.get(procdetails);
					if (prevdata != null) {
						procdatas.put(procdetails, prevdata);
						continue;
					}
					//the previous data was not found
					//however, the processor was not initialized.
					//can this happen? anyway, just skip it from the proc datas
					continue;
				}
				NavigableMap<SakerPath, Set<DocReference>> doccommentsignatures = new TreeMap<>();
				for (DocumentedElement<?> elem : proc.referencedDocCommentElements) {
					SakerPath path = elemTypes.getFilePathForElement(elem);
					if (path == null) {
						throw new NullPointerException(path + " for elem " + elem);
					}
					DocReference docref = createDocReference(elem);
					doccommentsignatures.computeIfAbsent(path, Functionals.hashSetComputer()).add(docref);
				}
				ProcessorData prevdata = proc.prevProcessorData;
				if (prevdata != null) {
					for (Entry<SakerPath, ? extends Set<? extends DocReference>> entry : prevdata
							.getDocCommentReferencedSignatures().entrySet()) {
						SakerPath path = entry.getKey();
						if (!proc.analyzedClassFilePaths.contains(path)) {
							doccommentsignatures.computeIfAbsent(path, Functionals.hashSetComputer())
									.addAll(entry.getValue());
						}
					}
				}
				procdatas.put(procdetails, new ProcessorData(proc.getSupportedOptions(), doccommentsignatures,
						proc.getReadResourceFileContents()));
			}
			info.setProcessorDetails(procdatas);
		} else {
			info.setProcessorDetails(prevProcessorDatas);
		}
		info.setModuleClassFile(moduleInfoFileData);
		info.setCompilationModuleSet(compilationModuleSet);
		info.setBytecodeManipulation(bytecodeManipulation);

		if (TestFlag.ENABLED) {
			if (TestFlag.metric().javacCaresAboutCompilationFinish()) {
				if (this.elemTypes == null) {
					initElementsTypes();
					infoputsrcfiles.values().forEach(this.elemTypes::addRootClassFile);
				}
				TestFlag.metric().javacCompilationFinished(this.elemTypes, this.elemTypes, invoker.getElements(),
						invoker.getTypes());
			}
		}

		return info;
	}

	private void reuseOriginatedDiagnosticEntries() {
		for (DiagnosticEntry de : prevDiagnosticEntries) {
			GeneratedFileOrigin origin = de.getOrigin();
			if (origin == null) {
				continue;
			}
			//diagnostic was emitted by a processor
			ProcessorState state = processorsByDetails.get(origin.getProcessorDetails());
			if (state != null) {
				if (!state.isInitialized()) {
					//processor was not called in this compilation, we can reuse the diagnostic
					info.addDiagnostic(de);
				} else {
					//processor was initialized in this compilation
					//if the originating paths were not analyzed, reuse the diagnostic
					//if no originating paths were specified, dont reuse the diagnostic
					Set<SakerPath> originpaths = origin.getOriginatingFilePaths();
					if (!originpaths.isEmpty()) {
						if (!ObjectUtils.containsAny(state.analyzedClassFilePaths, originpaths)) {
							info.addDiagnostic(de);
						} else {
							if (TestFlag.ENABLED) {
								if (!state.analyzedClassFilePaths.containsAll(originpaths)) {
									Set<SakerPath> analyzed = new TreeSet<>(state.analyzedClassFilePaths);
									analyzed.retainAll(originpaths);
									throw new AssertionError(
											"Processor should either analyze all of the originating elements or none of them for diagnostic: "
													+ de + " analyzed: " + state.analyzedClassFilePaths + " expected: "
													+ originpaths);
								}
							}
						}
					}
				}
			} else if (passProcessorReferences.containsKey(origin.getProcessorDetails())) {
				//processors were not called in this round, reuse diagnostic
				info.addDiagnostic(de);
			}
		}
	}

	private void analyzeRemovedProcessors() {
		for (Iterator<ProcessorDetails> procit = prevProcessorDatas.keySet().iterator(); procit.hasNext();) {
			ProcessorDetails ppd = procit.next();
			if (!passProcessorReferences.containsKey(ppd)) {
				//processor removed
				procit.remove();
				//remove the diagnostics generated by this processor
				for (Iterator<DiagnosticEntry> it = prevDiagnosticEntries.iterator(); it.hasNext();) {
					DiagnosticEntry entry = it.next();
					if (entry.getOrigin() != null && entry.getOrigin().getProcessorDetails().equals(ppd)) {
						it.remove();
					}
				}
			}
		}
	}

	private void printDiagnosticEntries() {
		printDiagnosticEntries(info, invocationContext.getSuppressWarnings(), passProcessorReferences);
	}

	public static void printDiagnosticEntries(CompilationInfo info, Collection<String> suppresswarnings,
			Map<ProcessorDetails, JavaAnnotationProcessor> procreferences) {
		Map<ProcessorDetails, Collection<String>> processorsuppresswarnings = new HashMap<>();
		for (Entry<ProcessorDetails, JavaAnnotationProcessor> entry : procreferences.entrySet()) {
			Collection<String> aprsuppress = entry.getValue().getSuppressWarnings();
			if (!ObjectUtils.isNullOrEmpty(aprsuppress)) {
				processorsuppresswarnings.computeIfAbsent(entry.getKey(),
						x -> new TreeSet<>(StringUtils::compareStringsNullFirstIgnoreCase)).addAll(aprsuppress);
			}
		}
		CompilationHandler.printDiagnosticEntries(info, suppresswarnings, processorsuppresswarnings,
				new BasicDiagnosticPositionTable(info));
	}

	private static final class BasicDiagnosticPositionTable implements DiagnosticPositionTable {
		private final CompilationInfo info;

		private BasicDiagnosticPositionTable(CompilationInfo info) {
			this.info = info;
		}

		@Override
		public DiagnosticLocation getForPathSignature(SakerPath path, SignaturePath signature) {
			if (path == null) {
				return DiagnosticLocation.EMPTY_INSTANCE;
			}
			if (signature == null) {
				return DiagnosticLocation.create(path);
			}
			Position pos = getPosition(path, signature);
			if (pos == null) {
				return DiagnosticLocation.create(path);
			}

			int lineNumber = pos.getLineIndex();
			int linePositionStart = pos.getLinePositionIndex();
			int linePositionEnd = pos.getLinePositionIndex() + pos.getLength();
			return DiagnosticLocation.create(path, lineNumber, linePositionStart, linePositionEnd);
		}

		private Position getPosition(SakerPath path, SignaturePath signature) {
			SourceFileData src = info.getSourceFiles().get(path);
			if (src == null) {
				src = info.getGeneratedSourceFiles().get(path);
			}
			if (src == null) {
				return null;
			}
			SignatureSourcePositions srcpositions = src.getSourcePositions();
			return getPathPosition(signature, srcpositions);
		}

		private static Position getPathPosition(SignaturePath signaturepath, SignatureSourcePositions srcpositions) {
			do {
				Position res = srcpositions.getPosition(signaturepath);
				if (res != null) {
					return res;
				}
				signaturepath = signaturepath.getParent();
			} while (signaturepath != null);
			return null;
		}
	}

	private void warnUnrecognizedProcessorOptions() {
		Map<String, String> generaloptions = invocationContext.getGeneralProcessorOptions();
		SortedSet<String> unrecognizedgeneraloptions = new TreeSet<>(generaloptions.keySet());
		if (isProcessorsContextInitialized()) {
			for (ProcessorState p : processorStates) {
				ProcessorDetails procdetails = p.getProcessorDetails();
				if (p.isInitialized()) {
					JavaAnnotationProcessor procref = p.processorReference;
					Set<String> supporteds = p.getSupportedOptions();

					removeAndReportUnrecognizedProcessorOptions(unrecognizedgeneraloptions, procdetails, procref,
							supporteds);
				} else {
					//this processor was skipped
					//we can use the supported options from the previous invocation
					ProcessorData prevdata = prevProcessorDatas.get(procdetails);
					if (prevdata != null) {
						JavaAnnotationProcessor procref = p.processorReference;
						Set<String> supporteds = prevdata.getSupportedOptions();

						removeAndReportUnrecognizedProcessorOptions(unrecognizedgeneraloptions, procdetails, procref,
								supporteds);
					}
				}
			}
		} else {
			for (Entry<ProcessorDetails, JavaAnnotationProcessor> entry : passProcessorReferences.entrySet()) {
				ProcessorDetails procdetails = entry.getKey();
				ProcessorData prevdata = prevProcessorDatas.get(procdetails);
				if (prevdata != null) {
					JavaAnnotationProcessor procref = entry.getValue();
					Set<String> supporteds = prevdata.getSupportedOptions();

					removeAndReportUnrecognizedProcessorOptions(unrecognizedgeneraloptions, procdetails, procref,
							supporteds);
				}
			}
		}

		if (!unrecognizedgeneraloptions.isEmpty()) {
			reportDiagnostic(new DiagnosticEntry(Diagnostic.Kind.WARNING,
					CompilationHandler.createUnrecognizedGeneralProcessorOptionsMessage(unrecognizedgeneraloptions),
					JavaCompilerWarningType.UnrecognizedProcessorOptions));
		}
	}

	public void removeAndReportUnrecognizedProcessorOptions(SortedSet<String> unrecognizedgeneraloptions,
			ProcessorDetails procdetails, JavaAnnotationProcessor procref, Set<String> supportedoptions) {
		unrecognizedgeneraloptions.removeAll(supportedoptions);
		reportUnrecognizedProcessorOptions(procdetails, procref.getOptions().keySet(), supportedoptions);
	}

	private void reportUnrecognizedProcessorOptions(ProcessorDetails procdetails, Set<String> processorlocaloptions,
			Set<String> supportedoptions) {
		if (ObjectUtils.isNullOrEmpty(processorlocaloptions)) {
			return;
		}
		SortedSet<String> procoptions = new TreeSet<>(processorlocaloptions);
		procoptions.removeAll(supportedoptions);
		if (!procoptions.isEmpty()) {
			reportDiagnostic(new DiagnosticEntry(Diagnostic.Kind.WARNING,
					CompilationHandler.createUnrecognizedDirectProcessorOptionsMessage(procoptions,
							procdetails.getProcessorName()),
					new GeneratedFileOrigin(procdetails), JavaCompilerWarningType.UnrecognizedProcessorOptions));
		}
	}

	private void putABIUsagesToGeneratedSourceFiles(SortedMap<SakerPath, ABIParseInfo> usages) {
		ObjectUtils.iterateSortedMapEntries(usages, totalParsedGeneratedSourcesByPath, (path, usage, sfd) -> {
			if (sfd == null) {
				return;
			}
			setABIUsagesForSourceFile(usage, sfd);

			sfd.setOrigin(generatedFileOrigins.get(path));

			info.putGeneratedSourceFile(sfd);
		});
	}

	private void putABIUsagesToSourceFiles(SortedMap<SakerPath, ABIParseInfo> usages,
			Map<SakerPath, SourceFileData> putsrcfilemap) {
		ObjectUtils.iterateSortedMapEntries(usages, totalParsedSourcesByPath, (path, usage, sfd) -> {
			if (sfd == null) {
				return;
			}
			setABIUsagesForSourceFile(usage, sfd);

			putsrcfilemap.put(path, sfd);
		});
	}

	private static void setABIUsagesForSourceFile(ABIParseInfo usage, SourceFileData sfd) {
		sfd.setABIUsage(usage.getUsage());

		sfd.setRealizedClasses(usage.getRealizedClasses());
		sfd.setRealizedPackageSignature(usage.getRealizedPackageSignature());
		sfd.setRealizedModuleSignature(usage.getRealizedModuleSignature());
	}

	protected final String getSourceVersionName() {
		return invoker.getSourceVersionName();
	}

	@Override
	public void addGeneratedClassFilesForSourceFiles(Map<String, SakerPath> classbinarynamesourcefilepaths) {
		generatedClassFileSourceFiles.putAll(classbinarynamesourcefilepaths);
	}

	private boolean isCompilationInclusionPending() {
		return sourceCompilationInclusionPending || classCompilationInclusionPending;
	}

	private final void addSourceForCompilation(String sourcename, SakerPath file) throws IOException {
		if (TestFlag.ENABLED) {
			TestFlag.metric().javacCompilingFile(file);
		}
		SourceFileData unaddedremoved = unaddedSourceFileDatas.remove(file);
		if (unaddedremoved != null) {
			removeInfoPutClassFilesForSourceFileData(unaddedremoved);
		}

		sourceCompilationInclusionPending = true;
		synchronized (javacSync) {
			invoker.addSourceForCompilation(sourcename, file);
		}
	}

	private final void addClassFileForCompilation(String classname, SakerPath file) throws IOException {
		classCompilationInclusionPending = true;
		synchronized (javacSync) {
			invoker.addClassFileForCompilation(classname, file);
		}
	}

	private void putClassFilesToInfoForSource(SourceFileData sfd) {
		infoPutClassFiles.putAll(sfd.getGeneratedClassDatas());
//		info.putClassFiles(sfd.getGeneratedClassDatas());
	}

	private void putClassFilesToInfoForSource(Map<SakerPath, ? extends SourceFileData> sources) {
		for (SourceFileData sfd : sources.values()) {
			infoPutClassFiles.putAll(sfd.getGeneratedClassDatas());
//			info.putClassFiles(sfd.getGeneratedClassDatas());
		}
	}

	private void removeGeneratedSourceFileAndCompiledFiles(GeneratedSourceFileData sfd) {
		SakerPath sourcepath = sfd.getPath();
		SakerFile mfile = taskContext.getTaskUtilities().resolveAtPath(sourcepath);
		if (mfile != null) {
			fileRemover.remove(sourcepath, mfile);
//			mfile.remove();
		}
		removeCompiledFilesForSource(sfd);
	}

	private void removeCompiledFilesForSource(ClassHoldingFileData sfd) {
		if (sfd == null) {
			return;
		}
		removeInfoPutClassFilesForSourceFileData(sfd);
		for (SakerPath cfpath : sfd.getGeneratedClassDatas().keySet()) {
			SakerFile mfile = taskContext.getTaskUtilities().resolveAtPath(cfpath);
			if (mfile != null) {
				fileRemover.remove(cfpath, mfile);
			}
		}
	}

//	private void removeClassFilesForSource(SakerPath sourcepath, Map<SakerPath, ? extends SourceFileData> prevsourcefiledatas) {
//		SourceFileData sfd = prevsourcefiledatas.get(sourcepath);
//		if (sfd != null) {
//			removeClassFilesForSource(sfd);
//		}
//	}

	private static Predicate<String> makeAnnotationNamePredicate(String s) {
		int lastidx = s.length() - 1;
		char lastchar = s.charAt(lastidx);
		if (lastchar == '*') {
			//ends in ".*"
			String start = s.substring(0, lastidx);
			return a -> a.startsWith(start);
		}
		return a -> s.equals(a);
	}

	private Predicate<TypeElement> makeAnnotationPredicate(String s) {
		if (ObjectUtils.isNullOrEmpty(s)) {
			return a -> false;
		}
		int slash = s.indexOf('/');
		if (slash >= 0) {
			if (!modulesSupported) {
				//module path specified, but modules are not supported
				//annotation type can never match
				return Functionals.neverPredicate();
			}
			String module = s.substring(0, slash);
			String path = s.substring(slash + 1);
			Predicate<String> pathpred = makeAnnotationNamePredicate(path);
			return te -> pathpred.test(te.getQualifiedName().toString())
					&& module.equals(JavaCompilationUtils.getModuleNameOf(te));
		}
		Predicate<String> pathpred = makeAnnotationNamePredicate(s);
		return te -> pathpred.test(te.getQualifiedName().toString());
	}

//	private <FDType extends SourceFileData> FDType parseAndFillABIUsage(Entry<? extends CompilationUnitTree, FDType> entry, ABIUsageParser usageparser,
//			Set<DiagnosticEntry> diagnosticentries, ConcurrentSkipListMap<SakerPath, FDType> infoputsourcefiledatas) {
//		CompilationUnitTree unit = entry.getKey();
//		FDType sfd = entry.getValue();
//
//		TopLevelABIUsage parsedusage = usageparser.parse(unit, parsedPathTreeSignatures.get(sfd.getPath()));
//
//		JavaFileObject src = unit.getSourceFile();
//		SortedSet<DiagnosticEntry> diagentries = CompilationHandler.removeDiagnosticsForSourceFile(fileManager, diagnosticentries, src);
//
//		sfd.setDiagnostics(diagentries);
//		sfd.setABIUsage(parsedusage);
//
//		CompilationHandler.setRealizedSignatures(unit, usageparser.getTrees(), sfd);
//
//		infoputsourcefiledatas.put(sfd.getPath(), sfd);
//		return sfd;
//	}
//
//	private void parseAndFillABIUsages(Trees trees, ConcurrentAccumulatorMap<? extends CompilationUnitTree, SourceFileData> parsedunits,
//			ConcurrentAccumulatorMap<? extends CompilationUnitTree, GeneratedSourceFileData> parsedgeneratedunits, Set<DiagnosticEntry> diagnosticentries,
//			ConcurrentSkipListMap<SakerPath, SourceFileData> infoputsourcefiledatas,
//			ConcurrentSkipListMap<SakerPath, GeneratedSourceFileData> infoputgensourcefiledatas) {
//		ABIUsageParser abiusageparser = new ABIUsageParser(trees);
//		ThreadUtils.runParallel(parsedunits, entry -> {
//			parseAndFillABIUsage(entry, abiusageparser, diagnosticentries, infoputsourcefiledatas);
//		});
//		ThreadUtils.runParallel(parsedgeneratedunits, entry -> {
//			GeneratedSourceFileData sfd = parseAndFillABIUsage(entry, abiusageparser, diagnosticentries, infoputgensourcefiledatas);
//			sfd.setOrigins(generatedFileOrigins.get(sfd.getPath()));
//		});
//	}
//
//	private void parseABIUsages(Trees trees) {
//		parseAndFillABIUsages(trees, totalParsedCompilationUnits, totalParsedGeneratedCompilationUnits, diagnosticCollector.getDiagnostics(),
//				infoPutSourceFileDatas, infoPutGeneratedSourceFileDatas);
//	}

//	private <FDType extends SourceFileData> FDType putABIUsages(SakerPath sourcepath, ABIParseInfo usage, Set<DiagnosticEntry> diagnosticentries,
//			ConcurrentSkipListMap<SakerPath, FDType> infoputsources, Map<SakerPath, FDType> parsedsourcesbypath) {
//		FDType sfd = parsedsourcesbypath.get(sourcepath);
//		SortedSet<DiagnosticEntry> diagentries = CompilationHandler.removeDiagnosticsForFile(diagnosticentries, sfd.getPath());
//
//		sfd.setDiagnostics(diagentries);
//		sfd.setABIUsage(usage.usage);
//
//		sfd.setRealizedClasses(usage.realizedClasses);
//		sfd.setRealizedPackageSignature(usage.realizedPackageSignature);
//
//		infoputsources.put(sfd.getPath(), sfd);
//		return sfd;
//	}
//
//	private void handleABIUsages(Map<SakerPath, ABIParseInfo> sourcefileusages, Map<SakerPath, ABIParseInfo> generatedsourcefileusages) {
//		Set<DiagnosticEntry> diagnosticentries = diagnosticCollector.getDiagnostics();
//		for (Entry<SakerPath, ABIParseInfo> entry : sourcefileusages.entrySet()) {
//			ABIParseInfo usage = entry.getValue();
//			SakerPath path = entry.getKey();
//			putABIUsages(path, usage, diagnosticentries, infoPutSourceFileDatas, totalParsedSourcesByPath);
//		}
//		for (Entry<SakerPath, ABIParseInfo> entry : generatedsourcefileusages.entrySet()) {
//			ABIParseInfo usage = entry.getValue();
//			SakerPath path = entry.getKey();
//			GeneratedSourceFileData sfd = putABIUsages(path, usage, diagnosticentries, infoPutGeneratedSourceFileDatas, totalParsedGeneratedSourcesByPath);
//			sfd.setOrigins(generatedFileOrigins.get(sfd.getPath()));
//		}
//	}

	private final class TrackingABIChangeResultHandler implements Consumer<AbiChange> {
		private boolean hadAbiChange = false;

		@Override
		public void accept(AbiChange t) {
			hadAbiChange = true;
			addABIChange(t);
		}
	}

	private static DocReference createDocReference(DocumentedElement<?> elem) {
		switch (elem.getKind()) {
			case ANNOTATION_TYPE:
			case CLASS:
			case ENUM:
			case INTERFACE: {
				ClassSignature sig = (ClassSignature) elem.getSignature();
				return createDocReference(sig);
			}
			case CONSTRUCTOR:
			case METHOD: {
				ClassSignature enclosingclass = (ClassSignature) ((SignaturedElement<?>) elem.getEnclosingElement())
						.getSignature();
				MethodSignature sig = (MethodSignature) elem.getSignature();
				return new MethodDocReference(enclosingclass.getCanonicalName(), sig.getSimpleName());
			}

			case ENUM_CONSTANT:
			case FIELD: {
				ClassSignature enclosingclass = (ClassSignature) ((SignaturedElement<?>) elem.getEnclosingElement())
						.getSignature();
				FieldSignature sig = (FieldSignature) elem.getSignature();
				return new FieldDocReference(enclosingclass.getCanonicalName(), sig.getSimpleName());
			}

			case PACKAGE: {
				PackageSignature sig = (PackageSignature) elem.getSignature();
				return new PackageDocReference(sig.getName());
			}
			default: {
				if (ElementKindCompatUtils.isModuleElementKind(elem.getKind())) {
					ModuleSignature ms = (ModuleSignature) elem.getSignature();
					return new ModuleDocReference(ms.getName());
				}
				throw new IllegalArgumentException("Unrecognized element signature type: " + elem.getKind());
			}
		}
	}

//	private static DocReference createDocReference(DocumentedSignature sig) {
//		//XXX use some kind of visitor
//		if (sig instanceof FieldSignature) {
//			FieldSignature fieldsig = (FieldSignature) sig;
//			return createDocReference(sig, fieldsig);
//		}
//		if (sig instanceof MethodSignature) {
//			MethodSignature methodsig = (MethodSignature) sig;
//			return createDocReference(methodsig);
//		}
//		if (sig instanceof ClassSignature) {
//			ClassSignature classsig = (ClassSignature) sig;
//			return createDocReference(classsig);
//		}
//		if (sig instanceof PackageSignature) {
//			PackageSignature packsig = (PackageSignature) sig;
//			return createDocReference(packsig);
//		}
//		if (sig instanceof ModuleSignature) {
//			ModuleSignature modulesig = (ModuleSignature) sig;
//			return createDocReference(modulesig);
//		}
//		throw new IllegalArgumentException(
//				"Unknown documented signature type: " + ObjectUtils.classOf(sig) + ": " + sig);
//	}

	private static DocReference createDocReference(ModuleSignature modulesig) {
		return new ModuleDocReference(modulesig.getName());
	}

	private static DocReference createDocReference(PackageSignature packsig) {
		return new PackageDocReference(packsig.getName());
	}

	private static DocReference createDocReference(ClassSignature classsig) {
		return new ClassDocReference(classsig.getCanonicalName());
	}

	private static DocReference createMethodDocReference(String classcanonicalname, String methodname) {
		return new MethodDocReference(classcanonicalname, methodname);
	}

//	private static DocReference createDocReference(MethodSignature methodsig) {
//		String enclosingcanoncalname = methodsig.getEnclosingSignature().getCanonicalName();
//		return new MethodDocReference(enclosingcanoncalname, methodsig.getSimpleName());
//	}
//
//	private static DocReference createDocReference(DocumentedSignature sig, FieldSignature fieldsig) {
//		String enclosingcanonicalname = ((FieldSignature) sig).getEnclosingSignature().getCanonicalName();
//		return new FieldDocReference(enclosingcanonicalname, fieldsig.getSimpleName());
//	}

	private static void collectDocCommentChanges(ClassSignature prevsig, ClassSignature sig,
			Set<? super DocReference> changeddocs) {
		if (prevsig == null) {
			changeddocs.add(createDocReference(sig));
			return;
		}
		if (sig == null) {
			changeddocs.add(createDocReference(prevsig));
			return;
		}

		String ccanonicalname = prevsig.getCanonicalName();
		if (!Objects.equals(prevsig.getDocComment(), sig.getDocComment())) {
			changeddocs.add(new ClassDocReference(ccanonicalname));
		}

		SortedMap<String, FieldSignature> fields = new TreeMap<>();
		SortedMap<String, Collection<MethodSignature>> methods = new TreeMap<>();
		SortedMap<String, ClassSignature> types = new TreeMap<>();
		sig.categorizeEnclosedMemberSignaturesByName(fields, methods, types);

		SortedMap<String, FieldSignature> prevfields = new TreeMap<>();
		SortedMap<String, Collection<MethodSignature>> prevmethods = new TreeMap<>();
		SortedMap<String, ClassSignature> prevtypes = new TreeMap<>();
		prevsig.categorizeEnclosedMemberSignaturesByName(prevfields, prevmethods, prevtypes);

		ObjectUtils.iterateSortedMapEntries(prevfields, fields, (vname, pfsig, fsig) -> {
			if (!Objects.equals(pfsig.getDocComment(), fsig.getDocComment())) {
				changeddocs.add(new FieldDocReference(ccanonicalname, vname));
			}
		});

		ObjectUtils.iterateSortedMapEntries(prevmethods, methods, (tname, prevmsig, msig) -> {
			prev_foreach:
			for (MethodSignature prevms : prevmsig) {
				String prevmsdoc = prevms.getDocComment();
				for (Iterator<MethodSignature> mit = msig.iterator(); mit.hasNext();) {
					MethodSignature ms = mit.next();
					if (MethodSignature.signatureEquals(prevms, ms, SignatureNameChecker.COMPARE_WITH_NAMES)) {
						mit.remove();
						if (!Objects.equals(prevmsdoc, ms.getDocComment())) {
							changeddocs.add(createMethodDocReference(ccanonicalname, tname));
							return;
						}
						continue prev_foreach;
					}
				}
				//the method with the same signature was not found
				changeddocs.add(createMethodDocReference(ccanonicalname, tname));
				return;
			}
			if (!msig.isEmpty()) {
				changeddocs.add(createMethodDocReference(ccanonicalname, tname));
			}
		});

		ObjectUtils.iterateSortedMapEntries(prevtypes, types, (tname, prevcsig, csig) -> {
			collectDocCommentChanges(prevcsig, csig, changeddocs);
		});
	}

	private static void collectDocCommentChanges(ClassHoldingFileData prevfd, ClassHoldingFileData fd,
			Set<? super DocReference> changeddocs) {
		PackageSignature pps = prevfd.getPackageSignature();
		PackageSignature cps = fd.getPackageSignature();
		if (pps != null) {
			String cpsdoc = cps == null ? null : cps.getDocComment();
			if (!Objects.equals(pps.getDocComment(), cpsdoc)) {
				changeddocs.add(createDocReference(pps));
			}
		} else if (cps != null) {
			if (cps.getDocComment() != null) {
				changeddocs.add(createDocReference(pps));
			}
		}
		ModuleSignature pms = prevfd.getModuleSignature();
		if (pms != null) {
			if (!Objects.equals(pms.getDocComment(), fd.getModuleSignature().getDocComment())) {
				changeddocs.add(createDocReference(pms));
			}
		}

		ObjectUtils.iterateSortedMapEntries(prevfd.getClasses(), fd.getClasses(), (name, prev, cur) -> {
			collectDocCommentChanges(prev, cur, changeddocs);
		});
	}

	public static SakerPath validateRelativePathName(CharSequence relativeName) {
		if (relativeName.length() == 0) {
			throw new IllegalArgumentException("Empty relative name.");
		}
		SakerPath relativepath = SakerPath.valueOf(relativeName.toString());
		if (!relativepath.isForwardRelative()) {
			throw new IllegalArgumentException("Invalid relative name: " + relativeName);
		}
		return relativepath;
	}

	public static SakerPath toPackageRelativePath(String packagename, String relativename) {
		SakerPath packpath = SakerPath.valueOf(packagename.replace('.', '/'));
		if (!packpath.isRelative()) {
			throw new InvalidPathFormatException("Package name must denote a relative path: " + packagename);
		}
		SakerPath relpath = IncrementalCompilationDirector.validateRelativePathName(relativename);
		SakerPath resourcepath = packpath.resolve(relpath);

		if (resourcepath.getNameCount() == 0) {
			throw new IllegalArgumentException("Empty resource path. (" + packagename + "/" + relativename + ")");
		}
		return resourcepath;
	}

	private boolean detectImportABIChanges(TopLevelAbiUsage usage, ImportDeclaration i) {
		boolean result = false;
		if (i.isWildcard()) {
			//the import is a wildcard
			// we generally can't know what could be imported, therefore a wildcard import should be treated
			// as if it could contain ANY simple names
			// this can affect basically ALL elements in a source file that has at least one simple name
			if (i.isStatic()) {
				//import static some.qualified.Type.*
				//if the import is static, we only care about fields that may have constant values
				//as types are not imported
				//therefore method or class changes are not affected
				for (Entry<FieldABIInfo, ? extends AbiUsage> entry : usage.getFields().entrySet()) {
					FieldABIInfo finfo = entry.getKey();
					if (!finfo.hasConstantValue()) {
						continue;
					}
					if (!entry.getValue().hasAnySimpleVariableIdentifier()) {
						//the field initializer doesn't use any simple variables, therefore the import cannot affect it
						continue;
					}
					result = true;
					addABIChange(new FieldInitializerABIChange(finfo.getClassCanonicalName(), finfo.getFieldName()));
				}
			} else {
				//import some.qualified.Type.*
				//imports a type that can be named anything
				result = usage.addABIChangeForEachMember(AbiUsage::hasAnySimpleTypeIdentifier, this::addABIChange);
			}
		} else {
			String ipath = i.getPath();
			int lastdotidx = ipath.lastIndexOf('.');
			String lastname = ipath.substring(lastdotidx + 1);
			if (i.isStatic()) {
				//import static some.qualified.Type.member
				//the only scenario when this can trigger an ABI change is when the 
				//  constant initializer of a field is changed due to this
				String enclosingtype = ipath.substring(0, lastdotidx);
				if (usage.isReferencesField(enclosingtype, lastname)) {
					for (Entry<FieldABIInfo, ? extends AbiUsage> entry : usage.getFields().entrySet()) {
						FieldABIInfo finfo = entry.getKey();
						if (!finfo.hasConstantValue()) {
							continue;
						}
						if (entry.getValue().isReferencesField(enclosingtype, lastname)) {
							result = true;
							addABIChange(
									new FieldInitializerABIChange(finfo.getClassCanonicalName(), finfo.getFieldName()));
						}
					}
				}
			} else {
				//import some.qualified.Type
				//this change affects all elements that use the simple identifier Type
				if (usage.isSimpleTypePresent(lastname)) {
					result = usage.addABIChangeForEachMember(u -> u.isSimpleTypePresent(lastname), this::addABIChange);
				}
			}
		}
		return result;
	}

	private void determineABIChanges(ClassHoldingFileData prevsfd, ClassHoldingFileData sfd,
			SignatureNameChecker methodparamnamechecker) {
		PackageSignature packsig = sfd.getPackageSignature();
		if (prevsfd == null) {
			for (ClassSignature c : sfd.getClassSignatures()) {
				ClassAddedABIChange abic = new ClassAddedABIChange(c);
				addABIChange(abic);
			}
			if (packsig != null) {
				if (!packsig.getAnnotations().isEmpty()) {
					addABIChange(new PackageAnnotationsChangeABIChange(packsig.getName()));
				} else {
					addABIChange(new PlainPackageInfoAddedABIChange(packsig.getName()));
				}
			}
		} else {
			boolean trackdocs = !docCommentInterestedProcessors.isEmpty();
			boolean[] hadabichange = { false };

			ImportScope previmports = prevsfd.getImportScope();
			ImportScope imports = sfd.getImportScope();

			ObjectUtils.iterateOrderedIterables(previmports.getImportDeclarations(), imports.getImportDeclarations(),
					(pi, i) -> {
						if (pi != null && i != null) {
							//the current import wasn't changed
							//as the imports are compareTo 0, we visit them at the same time
							return;
						}
						//the import changed, either removed or added
						ImportDeclaration id = pi == null ? i : pi;
						boolean hadimportrelatedchange = detectImportABIChanges(prevsfd.getABIUsage(), id);
						if (hadimportrelatedchange) {
							hadabichange[0] = true;
						}
					});

			ObjectUtils.iterateSortedMapEntries(prevsfd.getClasses(), sfd.getClasses(), (k, prevclass, parsedclass) -> {
				if (prevclass != null) {
					if (parsedclass != null) {
						if (trackdocs) {
							TrackingABIChangeResultHandler changehandler = new TrackingABIChangeResultHandler();
							IncrementalCompilationHandler.detectChanges(prevclass, parsedclass, methodparamnamechecker,
									changehandler);
							if (changehandler.hadAbiChange) {
								hadabichange[0] = true;
							}
						} else {
							IncrementalCompilationHandler.detectChanges(prevclass, parsedclass, methodparamnamechecker,
									this::addABIChange);
						}
					} else {
						ClassRemovedABIChange abic = new ClassRemovedABIChange(prevclass);
						addABIChange(abic);
					}
				} else {
					ClassAddedABIChange abic = new ClassAddedABIChange(parsedclass);
					addABIChange(abic);
				}
			});
			PackageSignature prevpacksig = prevsfd.getPackageSignature();
			if (packsig != null) {
				if (prevpacksig == null) {
					//a package-info file was added
					if (!packsig.getAnnotations().isEmpty()) {
						addABIChange(new PackageAnnotationsChangeABIChange(packsig.getName()));
					} else {
						addABIChange(new PlainPackageInfoAddedABIChange(packsig.getName()));
					}
					hadabichange[0] = true;
				} else {
					if (!AnnotatedSignature.annotationSignaturesEqual(prevpacksig, packsig)) {
						addABIChange(new PackageAnnotationsChangeABIChange(packsig.getName()));
						hadabichange[0] = true;
					}
				}
			} else {
				if (prevpacksig != null && !prevpacksig.getAnnotations().isEmpty()) {
					addABIChange(new PackageAnnotationsChangeABIChange(prevpacksig.getName()));
					hadabichange[0] = true;
				}
			}
			if (trackdocs && !hadabichange[0]) {
				Set<? super DocReference> changeddocs = new HashSet<>();
				collectDocCommentChanges(prevsfd, sfd, changeddocs);
				SakerPath sourcepath = sfd.getPath();
				for (Entry<ProcessorDetails, ProcessorData> entry : docCommentInterestedProcessors.entrySet()) {
					Set<? extends DocReference> refs = entry.getValue().getDocCommentReferencedSignatures()
							.get(sourcepath);
					if (refs != null) {
						if (ObjectUtils.containsAny(refs, changeddocs)) {
							if (isProcessorsContextInitialized()) {
								processorsByDetails.get(entry.getKey()).triggerProcessorRootElements.add(sourcepath);
							} else {
								deltaTriggeredProcessors
										.computeIfAbsent(entry.getKey(), x -> new ProcessorTriggerDelta())
										.getTriggeredUnitPaths().add(sourcepath);
							}
							continue;
						}
					}
				}
			}
		}
	}

	private boolean hasProcessors() {
		return !passProcessorReferences.isEmpty();
	}

	@Override
	public boolean isAnyErrorRaised() {
		return errorRaised || anyProcessingException != null;
	}

	private String getSourceVersionOptionName() {
		return sourceVersionOptionName;
	}

	private String getTargetVersionOptionName() {
		return targetVersionOptionName;
	}

	private final class IncrementalRoundEnvironment implements SakerRoundEnvironment {
		private final boolean processingOver;
		private Set<? extends Element> rootElements;
		private Map<TypeElement, Set<Element>> elementsForAnnotationTypes;

		public IncrementalRoundEnvironment(boolean processingOver, AnnotationSetCollector collector) {
			this(processingOver, collector.getRootElements(), collector.getElementsForAnnotationTypes());
		}

		public IncrementalRoundEnvironment(boolean processingOver, Set<? extends Element> rootElements,
				Map<TypeElement, Set<Element>> elementsforannotationnames) {
			this.processingOver = processingOver;
			this.rootElements = rootElements;
			this.elementsForAnnotationTypes = elementsforannotationnames;
		}

		@Override
		public boolean processingOver() {
			return processingOver;
		}

		@Override
		public boolean errorRaised() {
			return isAnyErrorRaised();
		}

		@Override
		public Set<? extends Element> getRootElements() {
			return rootElements;
		}

		@Override
		public Set<? extends Element> getElementsAnnotatedWith(TypeElement a) {
			if (a == null) {
				return Collections.emptySet();
			}
			Set<Element> got = elementsForAnnotationTypes.get(a);
			if (got == null) {
				return Collections.emptySet();
			}
			return ImmutableUtils.unmodifiableSet(got);
		}

		@Override
		public Set<? extends Element> getElementsAnnotatedWith(Class<? extends Annotation> a) {
			//converting the class to a type elements takes modules into account
			return getElementsAnnotatedWith(elemTypes.getClassTypeElement(a));
		}
	}

	private static class DocTrackingSakerElementsTypes extends ForwardingSakerElementsTypes {
		private final ProcessorState procState;

		public DocTrackingSakerElementsTypes(SakerElementsTypes elementsTypes, ProcessorState procState) {
			super(elementsTypes);
			this.procState = procState;
		}

		@Override
		public String getDocComment(Element e) {
			if (e instanceof DocumentedElement) {
				//only need to add it in case this is a DocumentElement
				//as per the implementation in IncrementalElementsTypes8
				procState.referencedDocCommentElements.add((DocumentedElement<?>) e);
			}
			return super.getDocComment(e);
		}

	}

	private final class IncrementalProcessingEnvironment implements SakerProcessingEnvironment {
		protected final IncrementalFiler filer;
		protected final Map<String, String> options;
		protected final String sourceVersionName;
		protected final SakerMessager messager;

		protected final DocTrackingSakerElementsTypes docTrackingElementsTypes;

		public IncrementalProcessingEnvironment(ProcessorState p, String sourceVersionName) {
			this.filer = new IncrementalFiler(p);
			this.options = p.getProcessorSpecificOptions();
			this.sourceVersionName = sourceVersionName;
			this.messager = new IncrementalProcessorMessager(p.getProcessorDetails());
			this.docTrackingElementsTypes = new DocTrackingSakerElementsTypes(elemTypes, p);
		}

		@Override
		public Map<String, String> getOptions() {
			return options;
		}

		@Override
		public SakerMessager getMessager() {
			return messager;
		}

		@Override
		public SakerFiler getFiler() {
			return filer;
		}

		@Override
		public SakerElementsTypes getElementUtils() {
			return docTrackingElementsTypes;
		}

		@Override
		public SakerElementsTypes getTypeUtils() {
			return docTrackingElementsTypes;
		}

		@Override
		public SourceVersion getSourceVersion() {
			try {
				return SourceVersion.valueOf(sourceVersionName);
			} catch (IllegalArgumentException e) {
				throw new SourceVersionNotFoundException(sourceVersionName, e);
			}
		}

		@Override
		public Locale getLocale() {
			//XXX support locale?
			//null if no locale is in effect
			return null;
		}

		@Override
		public String getSourceVersionName() {
			return sourceVersionName;
		}

		//@Override only on Java 13+
		public boolean isPreviewEnabled() {
			//TODO return a valid value for this
			return false;
		}
	}

	private static abstract class SourceClassGeneratedJavaFileObject extends ByteArrayBufferingJavaFileObject {
		protected final String name;
		protected final GeneratedFileOrigin origin;

		public SourceClassGeneratedJavaFileObject(String name, GeneratedFileOrigin origin) {
			this.name = name;
			this.origin = origin;
		}

		@Override
		public abstract Kind getKind();
	}

	private String unmodularizeName(String namestr) throws FilerException {
		int slashidx = namestr.indexOf('/');
		if (slashidx < 0) {
			return namestr;
		}
		if (!modulesSupported) {
			throw new FilerException("Modules are not supported in current source version: " + getSourceVersionName());
		}
		String module = namestr.substring(0, slashidx);
		if (!module.equals(elemTypes.getCurrentModuleName())) {
			throw new FilerException("Cannot access resource in module: " + module);
		}
		return namestr.substring(slashidx + 1);
	}

	private static void validateGeneratedTypeName(String namestr) throws FilerException {
		if (namestr.isEmpty()) {
			throw new FilerException("Empty name.");
		}
		if (namestr.indexOf('\\') >= 0 || namestr.indexOf('/') >= 0) {
			throw new FilerException("Invalid type name: " + namestr);
		}
	}

	private static void validateLocationName(Location location) {
		String name = location.getName();
		if (name == null) {
			throw new NullPointerException("Location name is null: " + location);
		}
		if (name.indexOf('/') >= 0 || name.indexOf('\\') >= 0) {
			throw new IllegalArgumentException("Invalid location name: " + name);
		}
	}

	private final class IncrementalFiler implements SakerFiler {
		protected final ProcessorState processor;
		protected final ConcurrentSkipListMap<SakerPath, ContentDescriptor> processorReadResources = new ConcurrentSkipListMap<>();

		public IncrementalFiler(ProcessorState processor) {
			this.processor = processor;
		}

		private void warnNoOriginatingElements(String name, Element[] originatingElements, GeneratedFileOrigin origin) {
			if (ObjectUtils.isNullOrEmpty(originatingElements)) {
				reportDiagnostic(
						new DiagnosticEntry(Diagnostic.Kind.WARNING, createNoOriginatingElementsWarningMessage(name),
								origin, JavaCompilerWarningType.NoOriginatingElements));
			}
		}

		private void putTypeForWriting(String namestr) throws FilerException {
			OpenedResource openedsourcekey = new OpenedResource(StandardLocation.SOURCE_OUTPUT,
					SakerPath.valueOf(namestr.replace('.', '/') + ".java"));
			OpenedResource openedclasskey = new OpenedResource(StandardLocation.CLASS_OUTPUT,
					SakerPath.valueOf(namestr.replace('.', '/') + ".class"));
			//synchronized map
			synchronized (resourcesOpenedLock) {
				OpenedResourceState cprev = resourcesOpened.putIfAbsent(openedclasskey,
						new OpenedResourceState(this.processor.getProcessorDetails(), true));
				if (cprev != null) {
					throw new FilerException(
							"Type " + namestr + " was already created." + cprev.createByProcessorMessage());
				}
				OpenedResourceState sprev = resourcesOpened.putIfAbsent(openedsourcekey,
						new OpenedResourceState(this.processor.getProcessorDetails(), true));
				if (sprev != null) {
					resourcesOpened.remove(openedclasskey, cprev);
					throw new FilerException(
							"Type " + namestr + " was already created." + sprev.createByProcessorMessage());
				}
			}
		}

		@Override
		public final JavaFileObject createSourceFile(CharSequence name, Element... originatingElements)
				throws IOException {
			Objects.requireNonNull(name, "name");
			String namestr = unmodularizeName(name.toString());
			throwIfModuleInfo(namestr);
			validateGeneratedTypeName(namestr);
			putTypeForWriting(namestr);

			GeneratedFileOrigin origin = makeOrigin(originatingElements);
			warnNoOriginatingElements(namestr, originatingElements, origin);
			return new SourceOutputCollectorJavaFileObject(processor, namestr, origin);
		}

		@Override
		public final JavaFileObject createClassFile(CharSequence name, Element... originatingElements)
				throws IOException {
			Objects.requireNonNull(name, "name");
			String namestr = unmodularizeName(name.toString());
			throwIfModuleInfo(namestr);
			validateGeneratedTypeName(namestr);
			putTypeForWriting(namestr);

			GeneratedFileOrigin origin = makeOrigin(originatingElements);
			warnNoOriginatingElements(namestr, originatingElements, origin);
			return new ClassOutputCollectorJavaFileObject(namestr, origin);
		}

		@Override
		public final FileObject createResource(Location location, CharSequence moduleandpkg, CharSequence relativeName,
				Element... originatingElements) throws IOException, IllegalArgumentException {
			Objects.requireNonNull(location, "location");
			Objects.requireNonNull(moduleandpkg, "package name");
			Objects.requireNonNull(relativeName, "relative name");

			if (!location.isOutputLocation()) {
				throw new IllegalArgumentException(
						"Location is not an output location: " + location + " (" + location.getName() + ")");
			}
			validateLocationName(location);

			String pkgstr = unmodularizeName(moduleandpkg.toString());
			String qname = location + "/" + moduleandpkg + "/" + relativeName;
			GeneratedFileOrigin origin = makeOrigin(originatingElements);
			warnNoOriginatingElements(qname, originatingElements, origin);

			SakerPath resourcepath = toPackageRelativePath(pkgstr, relativeName.toString());

			ExternalizableLocation extlocation = new ExternalizableLocation(location);
			OpenedResourceState prevopenstate;
			synchronized (resourcesOpenedLock) {
				prevopenstate = resourcesOpened.putIfAbsent(new OpenedResource(extlocation, resourcepath),
						new OpenedResourceState(processor.getProcessorDetails(), true));
			}
			if (prevopenstate != null) {
				throw new FilerException(
						"Resource " + resourcepath + " was already opened" + prevopenstate.createByProcessorMessage());
			}

			return new ResourceOutputCollectorFileObject(extlocation, resourcepath, origin);
		}

		@Override
		public final FileObject getResource(Location location, CharSequence moduleandpkg, CharSequence relativeName)
				throws IOException, IllegalArgumentException {
			Objects.requireNonNull(location, "location");
			Objects.requireNonNull(moduleandpkg, "package name");
			Objects.requireNonNull(relativeName, "relative name");

			if (location.isOutputLocation()) {
				//do not open resources from output locations
				throw new FilerException("Failed to open resource from output location: " + location.getName());
			}
			validateLocationName(location);

			String pkgstr = unmodularizeName(moduleandpkg.toString());

			SakerPath resourcepath = toPackageRelativePath(pkgstr, relativeName.toString());

			ExternalizableLocation extlocation = new ExternalizableLocation(location);
			OpenedResourceState prevopenstate;
			synchronized (resourcesOpenedLock) {
				prevopenstate = resourcesOpened.putIfAbsent(new OpenedResource(extlocation, resourcepath),
						new OpenedResourceState(processor.getProcessorDetails(), false));
			}
			if (prevopenstate != null) {
				if (prevopenstate.writing) {
					throw new FilerException("Resource " + resourcepath + " was already opened for writing"
							+ prevopenstate.createByProcessorMessage());
				}
				//else it was opened for reading, meaning that it can be opened for reading again
			}

			Collection<SakerDirectory> locationdirs = directoryPaths.getLocationDirectories(extlocation);
			if (!ObjectUtils.isNullOrEmpty(locationdirs)) {
				for (SakerDirectory dir : locationdirs) {
					SakerFile got = taskUtils.resolveAtRelativePath(dir, resourcepath);
					if (got == null) {
						processorReadResources.putIfAbsent(dir.getSakerPath().resolve(resourcepath),
								CommonTaskContentDescriptors.NOT_PRESENT);
						continue;
					}
					if (got instanceof SakerDirectory) {
						processorReadResources.putIfAbsent(dir.getSakerPath().resolve(resourcepath),
								DirectoryContentDescriptor.INSTANCE);
						continue;
					}
					SakerPath path = got.getSakerPath();
					processorReadResources.putIfAbsent(path, CommonTaskContentDescriptors.PRESENT);
					return new SakerFileWrapperFileObject(got) {
						@Override
						public SakerPath getFileObjectSakerPath() {
							return path;
						}

						@Override
						public String getCharContent(boolean ignoreEncodingErrors) throws IOException {
							processorReadResources.replace(path, CommonTaskContentDescriptors.PRESENT,
									file.getContentDescriptor());
							return super.getCharContent(ignoreEncodingErrors);
						}

						@Override
						public InputStream openInputStream() throws IOException {
							processorReadResources.replace(path, CommonTaskContentDescriptors.PRESENT,
									file.getContentDescriptor());
							return super.openInputStream();
						}

						@Override
						public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
							processorReadResources.replace(path, CommonTaskContentDescriptors.PRESENT,
									file.getContentDescriptor());
							return super.openReader(ignoreEncodingErrors);
						}
					};
				}
			}
			throw new FileNotFoundException(location.getName() + "/" + moduleandpkg + "/" + relativeName);
		}

		public GeneratedFileOrigin makeOrigin(Element... originatingElements) {
			return IncrementalCompilationDirector.this.makeOrigin(processor.getProcessorDetails(), originatingElements);
		}

	}

	private final class ResourceOutputCollectorFileObject extends ByteArrayBufferingJavaFileObject {
		protected final ExternalizableLocation location;
		protected final SakerPath resourcePath;
		protected final GeneratedFileOrigin origin;

		public ResourceOutputCollectorFileObject(ExternalizableLocation location, SakerPath resourcePath,
				GeneratedFileOrigin origin) {
			this.location = location;
			this.resourcePath = resourcePath;
			this.origin = origin;
		}

		@Override
		public Kind getKind() {
			return Kind.OTHER;
		}

		@Override
		protected void closeOutputStream(UnsyncByteArrayOutputStream baos) throws IOException {
			ContentDescriptor contentdesc = HashContentDescriptor.hash(baos::writeTo);

			SakerPath mpath;
			GeneratedResourceFileData rfd;
			SakerFile existing = directoryPaths.getExistingResourceFile(location, resourcePath);
			if (existing == null) {
				mpath = null;
				rfd = null;
			} else {
				mpath = existing.getSakerPath();
				rfd = unaddedGeneratedResourceFileDatas.remove(mpath);
			}
			if (rfd == null || contentdesc.isChanged(existing.getContentDescriptor())) {
				String filename = resourcePath.getFileName();
				SakerFile file = new ByteArraySakerFile(filename, baos.toByteArray());
				directoryPaths.putResourceOutputFile(location, resourcePath, file);
				mpath = file.getSakerPath();

				rfd = new GeneratedResourceFileData(mpath, contentdesc, origin);
			} else {
				//no action required as the existing file is already there

				rfd = new GeneratedResourceFileData(rfd, origin);
			}
			info.putGeneratedResourceFile(rfd);

			GeneratedFileOrigin prevorigin = putFileOrigin(mpath, origin);
			if (prevorigin != null) {
				throw new FilerException(resourcePath + " generated multiple times.");
			}

			if (TestFlag.ENABLED) {
				TestFlag.metric().javacProcessorResourceGenerated(mpath, baos,
						origin.getProcessorDetails().getProcessorName());
			}
		}

	}

	private final class ClassOutputCollectorJavaFileObject extends SourceClassGeneratedJavaFileObject {
		public ClassOutputCollectorJavaFileObject(String name, GeneratedFileOrigin origin) {
			super(name, origin);
		}

		@Override
		public Kind getKind() {
			return Kind.CLASS;
		}

		@Override
		protected void closeOutputStream(UnsyncByteArrayOutputStream baos) throws IOException {
			// TODO implement supporting class file generation

			throw new UnsupportedOperationException("Not yet supported.");

//			++openedGeneratedSourceCount;
//
//			HashContentDescriptor contentdesc = new HashContentDescriptor(baos::writeTo);
//			SakerPath mpath;
//			GeneratedClassFileData cfd;
//			SakerFile existing = fileManager.getExistingSourceClassOutputFile(StandardLocation.CLASS_OUTPUT, name.toString(), Kind.CLASS);
//			if (existing == null) {
//				mpath = null;
//				cfd = null;
//			} else {
//				mpath = existing.getSakerPath();
//				cfd = prevAllGeneratedClassFileDatas.get(mpath);
//			}
//			if (cfd == null || contentdesc.isChanged(existing.getContentDescriptor())) {
//				System.out.println("Generated class file: " + name);
//				++generatedSourceCount;
//				mpath = addChangedGeneratedClassFile(name, baos);
//			} else {
//				classRegeneratedWithSameContents(cfd, origin);
//			}
//
//			GeneratedFileOrigin prevorigin = generatedFileOrigins.put(mpath, origin);
//			if (prevorigin != null) {
//				throw new FilerException(name + " generated multiple times.");
//			}
		}

	}

	private final class SourceOutputCollectorJavaFileObject extends SourceClassGeneratedJavaFileObject {
		private ProcessorState processor;

		public SourceOutputCollectorJavaFileObject(ProcessorState procstate, String name, GeneratedFileOrigin origin) {
			super(name, origin);
			this.processor = procstate;
		}

		@Override
		public Kind getKind() {
			return Kind.SOURCE;
		}

		@Override
		protected void closeOutputStream(UnsyncByteArrayOutputStream baos) throws IOException {
			if (processorsFinalRoundsCalled) {
				reportDiagnostic(new DiagnosticEntry(Diagnostic.Kind.WARNING, createLastRoundWarningMessage(name),
						origin, JavaCompilerWarningType.LastRoundGeneration));
			}
			//set contributed to true, to get the processor called in further rounds
			//this is only important if files were generated during the init call
			processor.contributed = true;

			ContentDescriptor contentdesc = HashContentDescriptor.hash(baos::writeTo);
			SakerFile existing = directoryPaths.getExistingSourceClassOutputFile(StandardLocation.SOURCE_OUTPUT,
					name.toString(), Kind.SOURCE);
			GeneratedSourceFileData sfd;
			SakerPath mpath;
			if (existing == null) {
				mpath = null;
				sfd = null;
			} else {
				mpath = existing.getSakerPath();
				sfd = unaddedGeneratedSourceFileDatas.remove(mpath);
			}

			if (sfd == null || contentdesc.isChanged(existing.getContentDescriptor())) {
				mpath = addChangedGeneratedSourceFile(name, baos);
			} else {
				sfd = new GeneratedSourceFileData(sfd, origin);

				//we can add the source for the next round root element
				//if it is going to be entered due to an ABI change, 
				//then when the source is parsed it is going to be overwritten
				roundParsedElementDatas.put(mpath, sfd);
				roundRegeneratedSources.add(sfd);
			}

			GeneratedFileOrigin prevorigin = putFileOrigin(mpath, origin);
			if (prevorigin != null) {
				throw new FilerException(name + " generated multiple times.");
			}

			if (TestFlag.ENABLED) {
				TestFlag.metric().javacProcessorSourceGenerated(mpath, baos,
						origin.getProcessorDetails().getProcessorName());
			}
		}

	}

	private static Collection<Element> topLevelElementify(Element[] elements, Element additional) {
		Collection<Element> result = topLevelElementify(elements);
		if (additional != null) {
			result.add(IncrementalElementsTypes.getOuterMostEnclosingElement(additional));
		}
		return result;
	}

	private static Collection<Element> topLevelElementify(Element[] elements) {
		Collection<Element> result = new HashSet<>();
		if (!ObjectUtils.isNullOrEmpty(elements)) {
			for (Element e : elements) {
				if (e == null) {
					continue;
				}
				result.add(IncrementalElementsTypes.getOuterMostEnclosingElement(e));
			}
		}
		return result;
	}

	private static GeneratedFileOrigin makeOrigin(ProcessorDetails procdetails) {
		return new GeneratedFileOrigin(procdetails);
	}

	private GeneratedFileOrigin makeOrigin(ProcessorDetails procdetails, Element... originatingElements) {
		return new GeneratedFileOrigin(elemTypes.mapElementsToFiles(topLevelElementify(originatingElements)),
				procdetails);
	}

	private GeneratedFileOrigin makeOrigin(ProcessorDetails procdetails, Element additional,
			Element... originatingElements) {
		return new GeneratedFileOrigin(
				elemTypes.mapElementsToFiles(topLevelElementify(originatingElements, additional)), procdetails);
	}

	private static void throwIfModuleInfo(String namestr) throws FilerException {
		if ("module-info".equals(namestr) || namestr.endsWith(".module-info")) {
			throw new FilerException("Cannot generate module-info file");
		}
	}

	private SakerPath addChangedGeneratedFile(String name, UnsyncByteArrayOutputStream baos, Kind kind,
			StandardLocation location) throws IOException {
		//name is a canonical name in the current module
		//we only allow generating file in the current module only
		String pkg;
		String filename;
		int dot = name.lastIndexOf('.');
		if (dot < 0) {
			pkg = "";
			filename = name + kind.extension;
		} else {
			pkg = name.substring(0, dot);
			filename = name.substring(dot + 1) + kind.extension;
		}

		SakerFile mfile = new ByteArraySakerFile(filename, baos.getBuffer(), 0, baos.size());
		directoryPaths.putJavaSakerFileForOutput(location, pkg, mfile);
		switch (kind) {
			case SOURCE: {
				addSourceForCompilation(name, mfile.getSakerPath());
				break;
			}
			case CLASS: {
				addClassFileForCompilation(name, mfile.getSakerPath());
				break;
			}
			default: {
				throw new IllegalArgumentException("Invalid kind");
			}
		}

		SakerPath result = mfile.getSakerPath();
		parseEnteredSourcePaths.add(result);
		return result;
	}

	public static String createLastRoundWarningMessage(String name) {
		return "File for type '" + name + "' created in the last round will not be subject to annotation processing.";
	}

	public static String createNoOriginatingElementsWarningMessage(String name) {
		return "No originating elements provided for: " + name;
	}

	private SakerPath addChangedGeneratedSourceFile(String name, UnsyncByteArrayOutputStream baos) throws IOException {
		return addChangedGeneratedFile(name, baos, Kind.SOURCE, StandardLocation.SOURCE_OUTPUT);
	}

	private SakerPath addChangedGeneratedClassFile(String name, UnsyncByteArrayOutputStream baos) throws IOException {
		return addChangedGeneratedFile(name, baos, Kind.CLASS, StandardLocation.CLASS_OUTPUT);
	}

	private GeneratedFileOrigin putFileOrigin(SakerPath mpath, GeneratedFileOrigin origin) {
		return generatedFileOrigins.put(mpath, origin);
	}

//	private void classRegeneratedWithSameContents(GeneratedClassFileData cfd, GeneratedFileOrigin origin) {
//		cfd = new GeneratedClassFileData(cfd, origin);
//
//		SakerPath classpath = cfd.getPath();
//
//		roundParsedElementDatas.put(classpath, cfd);
//
//		info.putGeneratedClassFile(cfd);
//	}

	private final class ProcessorState {
		private final JavaAnnotationProcessor processorReference;
		private final ProcessorDetails processorDetails;

		private Processor processor;

		private Collection<SakerPath> triggerProcessorRootElements = new TreeSet<>();
		private AnnotationSetCollector skippedRootElements = new AnnotationSetCollector(elemTypes);
		private AnnotationSetCollector annotationSetCollector;

		private Set<SakerPath> analyzedClassFilePaths = new TreeSet<>();

		private IncrementalProcessingEnvironment processingEnvironment;
		private NavigableSet<String> supportedOptions;
		private Predicate<TypeElement> annotationPredicate;

		private boolean contributed = false;
		private boolean supportsAllWildcardAnnotations = false;
		private boolean notSkippable = false;
		private boolean requiresFullRootElements = false;

		protected final Set<DocumentedElement<?>> referencedDocCommentElements = ConcurrentHashMap.newKeySet();
		private ProcessorData prevProcessorData;

		public ProcessorState(JavaAnnotationProcessor procref, ProcessorDetails processordetails,
				ProcessorData prevProcessorData) {
			this.processorReference = procref;
			this.prevProcessorData = prevProcessorData;

			this.processorDetails = processordetails;
			boolean hadpreviousrun;
			if (prevProcessorData == null) {
				notSkippable = true;
				hadpreviousrun = false;
			} else {
				hadpreviousrun = true;
			}
			if (!procref.getConsistent() || procref.getAggregating() || !hadpreviousrun) {
				//if not consistent, then the processing has to be run on all elements
				//if aggregating, then as well, because if a new element was added, it can generate based on that and a previous arbitrary element
				//if this processor has not been run yet for compilation (or was run with different options)
				triggerProcessorRootElements.addAll(unaddedSourceFileDatas.keySet());
				requiresFullRootElements = true;
			}
			//else consistent and simple processor
			ProcessorTriggerDelta trigger = deltaTriggeredProcessors.get(processorDetails);
			if (trigger != null) {
				notSkippable = true;
				if (trigger.isReadResourceTriggered()) {
					//if a read resource has been changed, run the processor for the full type set
					//   as a resource change can completely change the behaviour of the processor
					triggerProcessorRootElements.addAll(unaddedSourceFileDatas.keySet());
					requiresFullRootElements = true;
				}
				triggerProcessorRootElements.addAll(trigger.getTriggeredUnitPaths());
			}
		}

		public boolean shouldIncludeFromPreviousCompilation(ProcessorGeneratedFileData gsfd) {
			if (!processorReference.getConsistent() || processorReference.getAggregating()) {
				return false;
			}
			Collection<? extends ClassHoldingFileData> originfiles = gsfd.getOrigin().getOriginatingFileDatas();
			if (originfiles.isEmpty()) {
				return false;
			}
			for (ClassHoldingData ofd : originfiles) {
				if (analyzedClassFilePaths.contains(ofd.getPath())) {
					return false;
				}
			}
			return true;
		}

		public Map<String, String> getProcessorSpecificOptions() {
			return processorDetails.getOptions();
		}

		public ProcessorDetails getProcessorDetails() {
			return processorDetails;
		}

		public boolean isInitialized() {
			return this.processingEnvironment != null;
		}

		public void init() {
			if (isInitialized()) {
				return;
			}
			if (TestFlag.ENABLED) {
				TestFlag.metric().javacProcessorInitialized(this.processorDetails.getProcessorName());
			}

			String processingsourceversionname = getSourceVersionName();

			this.processingEnvironment = new IncrementalProcessingEnvironment(this, processingsourceversionname);

			this.annotationSetCollector = new AnnotationSetCollector(elemTypes);

			Set<String> clientprocessoroptions = null;
			Set<String> supportedAnnotationTypes = null;
			SourceVersion supportedsourceversion = null;

			System.out.println("Initializing processor: " + processorDetails.getProcessorName());
			try {
				ProcessorCreator creator = processorReference.getProcessor().getCreator();
				this.processor = creator.create(processorCreationContext);
				if (this.processor == null) {
					throw new NullPointerException("Annotation processor creator returned null: " + creator);
				}
			} catch (Exception | StackOverflowError | AssertionError | LinkageError | ServiceConfigurationError
					| OutOfMemoryError e) {
				throw new ClientProcessorException(
						"Failed to instantiate processor: " + processorDetails.getProcessorName(), e, processorDetails);
			}
			try {
				processor.init(processingEnvironment);
			} catch (Exception | StackOverflowError | AssertionError | LinkageError | ServiceConfigurationError
					| OutOfMemoryError e) {
				throw new ClientProcessorException("Failed to call init(ProcessingEnvironment) on Processor: "
						+ processorDetails.getProcessorName(), e, processorDetails);
			}

			try {
				clientprocessoroptions = processor.getSupportedOptions();
				if (clientprocessoroptions == null) {
					reportDiagnostic(new DiagnosticEntry(Diagnostic.Kind.WARNING,
							"Processor returned null for getSupportedOptions().", makeOrigin(processorDetails),
							JavaCompilerWarningType.ProcessorCallResult));
				}
			} catch (Exception | StackOverflowError | AssertionError | LinkageError | ServiceConfigurationError
					| OutOfMemoryError e) {
				reportDiagnostic(new DiagnosticEntry(Diagnostic.Kind.WARNING,
						"Failed to call getSupportedOptions(). (" + e + ")", makeOrigin(processorDetails),
						JavaCompilerWarningType.ProcessorCallResult));
				taskContext.getTaskUtilities().reportIgnoredException(e);
			}
			try {
				supportedAnnotationTypes = processor.getSupportedAnnotationTypes();
				if (supportedAnnotationTypes == null) {
					reportDiagnostic(new DiagnosticEntry(Diagnostic.Kind.WARNING,
							"Processor returned null for getSupportedAnnotationTypes().", makeOrigin(processorDetails),
							JavaCompilerWarningType.ProcessorCallResult));
				}
			} catch (Exception | StackOverflowError | AssertionError | LinkageError | ServiceConfigurationError
					| OutOfMemoryError e) {
				reportDiagnostic(new DiagnosticEntry(Diagnostic.Kind.WARNING,
						"Failed to call getSupportedAnnotationTypes(). (" + e + ")", makeOrigin(processorDetails),
						JavaCompilerWarningType.ProcessorCallResult));
			}
			try {
				supportedsourceversion = processor.getSupportedSourceVersion();
				if (supportedsourceversion == null) {
					reportDiagnostic(new DiagnosticEntry(Diagnostic.Kind.WARNING,
							"Processor returned null for getSupportedSourceVersion().", makeOrigin(processorDetails),
							JavaCompilerWarningType.ProcessorCallResult));
				} else {
					if (JavaUtil.compareSourceVersionEnumNames(supportedsourceversion.name(),
							processingsourceversionname) < 0) {
						reportDiagnostic(new DiagnosticEntry(Diagnostic.Kind.WARNING,
								"Supported source version '" + supportedsourceversion + "' from annotation processor '"
										+ processorDetails.getProcessorName() + "' is less than '"
										+ processingsourceversionname + "'",
								makeOrigin(processorDetails), JavaCompilerWarningType.LessProcessorSourceVersion));
					}
				}
			} catch (Exception | StackOverflowError | AssertionError | LinkageError | ServiceConfigurationError
					| OutOfMemoryError e) {
				reportDiagnostic(new DiagnosticEntry(Diagnostic.Kind.WARNING,
						"Failed to call getSupportedSourceVersion(). (" + e + ")", makeOrigin(processorDetails),
						JavaCompilerWarningType.ProcessorCallResult));
			}

			this.supportedOptions = ObjectUtils.isNullOrEmpty(clientprocessoroptions) ? Collections.emptyNavigableSet()
					: ImmutableUtils.makeImmutableNavigableSet(clientprocessoroptions);

			if (ObjectUtils.isNullOrEmpty(supportedAnnotationTypes)) {
				annotationPredicate = Functionals.neverPredicate();
			} else if (supportedAnnotationTypes.contains("*")) {
				supportsAllWildcardAnnotations = true;
				annotationPredicate = Functionals.alwaysPredicate();
			} else {
				Iterator<String> it = supportedAnnotationTypes.iterator();
				String first = it.next();
				annotationPredicate = makeAnnotationPredicate(first);
				while (it.hasNext()) {
					annotationPredicate = annotationPredicate.or(makeAnnotationPredicate(it.next()));
				}
			}

//			Map<String, String> procoptions = processorReference.getOptions();
//			if (procoptions == null) {
//				procoptions = Collections.emptySortedMap();
//			}
//			SortedSet<String> directunrecognizeds = new TreeSet<>(procoptions.keySet());
//			directunrecognizeds.removeAll(supportedOptions);
//			if (!directunrecognizeds.isEmpty()) {
//				if (!shouldSuppressWarning(JavaCompilerWarningType.UnrecognizedProcessorOptions)) {
//					reportDiagnostic(new DiagnosticEntry(Diagnostic.Kind.WARNING,
//							CompilationHandler.createUnrecognizedProcessorOptionsMessage(directunrecognizeds, processor.getClass()),
//							makeOrigin(processorDetails)));
//				}
//			}
		}

		private boolean canSkipProcessing() {
			if (contributed || notSkippable || !processorReference.getConsistent() || processorReference.getAlwaysRun()
					|| !triggerProcessorRootElements.isEmpty()) {
				return false;
			}
			//we can skip processing even for aggregating processors if there were no abi changes
			return allABIChanges.isEmpty();
		}

		public Set<? extends TypeElement> callRound(AnnotationSetCollector rootcollector) {
			if (canSkipProcessing()) {
				if (!processorReference.getConsistent() || processorReference.getAggregating()) {
					skippedRootElements.add(rootcollector);
				}
				return null;
			}
			init();

			this.annotationSetCollector.rebaseOn(rootcollector);
			if (!this.skippedRootElements.isEmpty()) {
				this.annotationSetCollector.add(this.skippedRootElements);
				this.skippedRootElements.clear();
			}
			if (!triggerProcessorRootElements.isEmpty()) {
				for (SakerPath trigger : triggerProcessorRootElements) {
					Set<? extends Element> elems = elemTypes.getRootClassFileElements(trigger);
					if (elems != null) {
						this.annotationSetCollector.collect(trigger, elems);
					}
				}
				this.analyzedClassFilePaths.addAll(triggerProcessorRootElements);
				this.triggerProcessorRootElements.clear();
			}
			if (requiresFullRootElements) {
				this.annotationSetCollector.add(getStartRootElementsAnnotationSetCollector());
				requiresFullRootElements = false;
			}

			analyzedClassFilePaths.addAll(this.annotationSetCollector.getPaths());

			if (this.annotationSetCollector.isEmpty()) {
				//can happen, but very rarely
				//even if we contributed, we got nothing to do, as the processor is not called with any elements
				return null;
			}

			Set<TypeElement> processorannotations = new HashSet<>();
			for (TypeElement at : this.annotationSetCollector.getAnnotationTypes()) {
				if (recognizesAnnotation(at)) {
					processorannotations.add(at);
				}
			}
			if (supportsAllWildcardAnnotations || !processorannotations.isEmpty() || contributed) {
				IncrementalRoundEnvironment processorroundenv = new IncrementalRoundEnvironment(false,
						this.annotationSetCollector);
				boolean consumed = process(processorannotations, processorroundenv);
				if (consumed) {
					return processorannotations;
				}
			}
			return null;
		}

		public void callFinalRound(RoundEnvironment processorendenv) {
			if (!contributed) {
				return;
			}
			System.out.println("Calling processor final round: " + processorDetails.getProcessorName());
			try {
				processor.process(Collections.emptySet(), processorendenv);
			} catch (Exception | StackOverflowError | AssertionError | LinkageError | ServiceConfigurationError
					| OutOfMemoryError e) {
				throw new ClientProcessorException(
						"Failed to call process(Set<? extends TypeElement>, RoundEnvironment).", e, processorDetails);
			}
		}

		private boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
			contributed = true;

			System.out.println("Calling processor: " + processorDetails.getProcessorName());
			boolean result;
			try {
				result = processor.process(annotations, roundEnv);
			} catch (Exception | StackOverflowError | AssertionError | LinkageError | ServiceConfigurationError
					| OutOfMemoryError e) {
				throw new ClientProcessorException(
						"Failed to call process(Set<? extends TypeElement>, RoundEnvironment).", e, processorDetails);
			}
			return result;
		}

		private boolean recognizesAnnotation(TypeElement annottype) {
			return annotationPredicate.test(annottype);
		}

		public NavigableSet<String> getSupportedOptions() {
			return supportedOptions;
		}

		public NavigableMap<SakerPath, ContentDescriptor> getReadResourceFileContents() {
			IncrementalProcessingEnvironment procenv = processingEnvironment;
			if (procenv == null) {
				return Collections.emptyNavigableMap();
			}
			return procenv.filer.processorReadResources;
		}

		@Override
		public String toString() {
			return processorDetails.getProcessorName();
		}
	}

	private final class IncrementalProcessorMessager implements SakerMessager {
		private ProcessorDetails processor;

		public IncrementalProcessorMessager(ProcessorDetails processor) {
			this.processor = processor;
		}

		private void analyzeMessage(Diagnostic.Kind kind) {
			if (kind == Diagnostic.Kind.ERROR) {
				errorRaised = true;
			}
		}

		@Override
		public void printMessage(Diagnostic.Kind kind, CharSequence msg) {
			printMessage(kind, msg, null, null, null, EMPTY_ELEMENT_ARRAY);
		}

		@Override
		public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e) {
			printMessage(kind, msg, e, null, (AnnotationValue) null, EMPTY_ELEMENT_ARRAY);
		}

		@Override
		public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a) {
			printMessage(kind, msg, e, a, (AnnotationValue) null, EMPTY_ELEMENT_ARRAY);
		}

		@Override
		public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a,
				AnnotationValue v) {
			printMessage(kind, msg, e, a, v, EMPTY_ELEMENT_ARRAY);
		}

		@Override
		public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element... originatingElements) {
			printMessage(kind, msg, null, null, null, originatingElements);
		}

		@Override
		public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e, Element... originatingElements) {
			printMessage(kind, msg, e, null, (AnnotationValue) null, originatingElements);
		}

		@Override
		public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a,
				Element... originatingElements) {
			printMessage(kind, msg, e, a, (AnnotationValue) null, originatingElements);
		}

		@Override
		public void printMessage(Diagnostic.Kind kind, CharSequence msg, Element e, AnnotationMirror a,
				AnnotationValue v, Element... originatingElements) {
			analyzeMessage(kind);
			reportDiagnostic(createDiagnosticEntry(kind, msg.toString(), e, a, v,
					makeOrigin(processor, e, originatingElements)));
		}

		private SignaturePath getDiagnosticPositionSignaturePath(SignatureSourcePositions positions, Element e,
				AnnotationMirror a, AnnotationValue v) {
			if (positions == null) {
				return null;
			}
			SignaturePath path = null;
			{
				SignaturePath elemitpath = null;
				while (e instanceof SignaturedElement) {
					SignaturedElement<?> incelem = (SignaturedElement<?>) e;
					ElementKind incelemkind = incelem.getKind();
					if (incelemkind == ElementKind.PACKAGE && path != null) {
						//don't include the package in the signature path if there are already child paths
						break;
					}
					Signature s = incelem.getSignature();
					if (s != null) {
						if (IncrementalElementsTypes.ELEMENT_KIND_TYPES.contains(incelemkind)) {
							s = SignaturePath.getClassSignature((ClassSignature) s);
						} else if (incelemkind == ElementKind.PARAMETER) {
							//parameter special handling, use index signature
							SignaturedElement<?> enclosing = (SignaturedElement<?>) incelem.getEnclosingElement();
							MethodSignature methodsig = (MethodSignature) enclosing.getSignature();
							int idx = methodsig.getParameters().indexOf(s);
							s = SignaturePath.getMethodParameterIndexSignature(idx);
						}
						SignaturePath npath = new SignaturePath(s);
						if (path == null) {
							path = npath;
						} else {
							elemitpath.setParent(npath);
						}
						elemitpath = npath;
					}
					e = incelem.getEnclosingElement();
				}
			}
			if (path != null) {
				if (v instanceof IncrementalAnnotationValue) {
					SignaturePath valpath = ((IncrementalAnnotationValue) v).getAnnotationSignaturePath();
					if (valpath != null) {
						return valpath.cloneWithPrefixed(path);
					}
				}
				if (a instanceof IncrementalAnnotationMirror) {
					SignaturePath valpath = ((IncrementalAnnotationMirror) a).getAnnotationSignaturePath();
					if (valpath != null) {
						return valpath.cloneWithPrefixed(path);
					}
				}
			}
			return path;
		}

		private DiagnosticEntry createDiagnosticEntry(Diagnostic.Kind kind, String msg, Element e, AnnotationMirror a,
				AnnotationValue v, GeneratedFileOrigin origin) {

			DiagnosticLocationReference location = null;
			if (e != null) {
				ClassHoldingData elemfd = elemTypes.getFileDataForElement(e);
				if (elemfd != null) {
					SakerPath path = elemfd.getPath();
					if (path != null) {
						SignatureSourcePositions positions = elemfd.getSourcePositions();
						SignaturePath diagpath = getDiagnosticPositionSignaturePath(positions, e, a, v);
						location = new PathSignatureDiagnosticLocationReference(path, diagpath);
					}
				}
			}
			DiagnosticEntry result = new DiagnosticEntry(kind, location, msg, origin,
					JavaCompilerWarningType.ClientProcessorMessage);
			return result;
		}
	}

	@Override
	public void reportDiagnostic(DiagnosticEntry entry) {
		if (entry.getKind() == Diagnostic.Kind.ERROR) {
			errorRaised = true;
		}
		info.addDiagnostic(entry);
	}

	private boolean isProcessorsContextInitialized() {
		return this.processorStates != null;
	}

	private void initProcessorContext() {
		if (isProcessorsContextInitialized()) {
			//already inited
			return;
		}
		initElementsTypes();
		this.rootElementsAnnotationSetCollector = new AnnotationSetCollector(elemTypes);
		this.processorCreationContext = new ProcessorCreationContextImpl(taskContext);

		for (SourceFileData sfd : prevSourceFileDatas.values()) {
			if (!unaddedSourceFileDatas.containsKey(sfd.getPath())) {
				continue;
			}
			Set<? extends Element> elems = elemTypes.addRootClassFile(sfd);
			startRootElements.put(sfd.getPath(), elems);
		}

		processorStates = new ArrayList<>(passProcessorReferences.size());
		processorsByDetails = new HashMap<>();
		for (Entry<ProcessorDetails, JavaAnnotationProcessor> entry : passProcessorReferences.entrySet()) {
			ProcessorDetails details = entry.getKey();
			JavaAnnotationProcessor apr = entry.getValue();
			ProcessorData prevprocdata = prevProcessorDatas.get(details);
			ProcessorState state = new ProcessorState(apr, details, prevprocdata);
			processorStates.add(state);
			processorsByDetails.put(state.getProcessorDetails(), state);
		}
	}

	private void initElementsTypes() {
		this.elemTypes = JavaCompilationUtils.createElementsTypes(invoker.getElements(), javacSync,
				CompilationContextInformation.of(invoker), cache);
		if (moduleInfoFileData != null) {
			this.elemTypes.initCompilationModule(moduleInfoFileData);
		} else {
			this.elemTypes.initCompilationModuleNotSpecified();
		}
	}

	private AnnotationSetCollector getStartRootElementsAnnotationSetCollector() {
		if (startRootElementsAnnotationSetCollector == null) {
			synchronized (this) {
				if (startRootElementsAnnotationSetCollector == null) {
					AnnotationSetCollector asc = new AnnotationSetCollector(elemTypes);
					for (Entry<SakerPath, Set<? extends Element>> elems : startRootElements.entrySet()) {
						asc.collect(elems.getKey(), elems.getValue());
					}
					startRootElementsAnnotationSetCollector = asc;
					return asc;
				}
			}
		}
		return startRootElementsAnnotationSetCollector;
	}

	private boolean canSkipUserProcessors() {
		if (!deltaTriggeredProcessors.isEmpty()) {
			return false;
		}
		if (processorDetailsChanged) {
			//if the processor details changed from previous compilation
			return false;
		}
		for (JavaAnnotationProcessor procref : passProcessorReferences.values()) {
			if (!procref.getConsistent() || procref.getAlwaysRun()) {
				return false;
			}
		}
		//only consistent processors
		//no need to call processing if there were no ABI changes
		if (allABIChanges.isEmpty()) {
			return true;
		}

		//we got some ABI changes, we can skip processing if we only have simple (non-aggregating) processors
		//and no sources or class files were parsed before 

		if (!totalParsedCompilationUnitsByPath.isEmpty()) {
			return false;
		}
		for (JavaAnnotationProcessor procref : passProcessorReferences.values()) {
			if (procref.getAggregating()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void runCompilationRounds() {
		while (compilationRound()) {
			//loop
		}
	}

	private boolean compilationRound() {
		if (anyProcessingException != null || errorRaised) {
			return false;
		}
		try {
			parseEnteredSourcesRound();
			if (isCompilationInclusionPending()) {
				//wait next round until calling processors
				return true;
			}
			//no file changes detected
			processors_block:
			if (!processorsFinalRoundsCalled && hasProcessors()) {
				if (!isProcessorsContextInitialized()) {
					if (canSkipUserProcessors()) {
						removeUngeneratedFilesWithSkippedProcessors();

						requiresUngeneratedFilesRemoved = false;
						processorsFinalRoundsCalled = true;
						break processors_block;
					}
					initProcessorContext();
				}
				callProcessorRounds();
				if (anyProcessingException != null) {
					return false;
				}
				if (isCompilationInclusionPending()) {
					return true;
				}
				if (analyzeMultiOriginDependencies()) {
					return true;
				}
				//no sources were generated, call the final round
				callProcessorFinalRounds();
				if (anyProcessingException != null) {
					return false;
				}
				if (isCompilationInclusionPending()) {
					//if anything was generated in the last round, return as we dont want to add the classpath directory yet as a next round will occurr
					return true;
				}
			}

			if (requiresUngeneratedFilesRemoved) {
				requiresUngeneratedFilesRemoved = false;

				//check if any generated sources were not regenerated and detect ABI changes
				//e.g. if a generated class was removed that could break user classes

				removeUngeneratedFiles();
				if (isCompilationInclusionPending()) {
					return true;
				}
			}
			if (!finalClassPathAdded) {
				finalClassPathAdded = true;
				//add the output class directory as class path to make sure the class files from the previous compilation are reused
				//we have to add it before the final round, else javac won't list the directories again
				reusedSourceResultsFromPreviousCompilation.putAll(unaddedSourceFileDatas);
				reusedSourceResultsFromPreviousCompilation.putAll(unaddedRegeneratedSources);

				for (GeneratedSourceFileData sfd : unaddedRegeneratedSources.values()) {
					info.putGeneratedSourceFile(sfd);
					putClassFilesToInfoForSource(sfd);
				}

				addReusedClassFilesFromPreviousCompilation(reusedSourceResultsFromPreviousCompilation.values());
			}
		} catch (Exception | StackOverflowError | AssertionError | LinkageError | ServiceConfigurationError
				| OutOfMemoryError e) {
			setProcessingException(e);
		}
		return false;
	}

	private void addReusedClassFilesFromPreviousCompilation(Collection<? extends ClassHoldingData> reusedclasses) {
		if (TestFlag.ENABLED) {
			TestFlag.metric().javacReusingFiles(
					reusedclasses.stream().map(ClassHoldingData::getPath).collect(Collectors.toSet()));
		}
		invoker.addClassFilesFromPreviousCompilation(new PreviousCompilationClassInfo(reusedclasses));

		NavigableMap<SakerPath, Collection<DiagnosticEntry>> pathentries = new TreeMap<>();
		for (DiagnosticEntry entry : prevDiagnosticEntries) {
			DiagnosticLocationReference locref = entry.getLocationReference();
			if (locref == null) {
				continue;
			}
			SakerPath path = locref.getPath();
			if (path != null) {
				pathentries.computeIfAbsent(path, Functionals.arrayListComputer()).add(entry);
			}
		}
//		NavigableMap<SakerPath, Collection<DiagnosticEntry>> pathentries = ObjectUtils.toSortedMultiValueMap(prevDiagnosticEntries, de -> de.path);
		for (ClassHoldingData chd : reusedclasses) {
			Collection<DiagnosticEntry> entries = pathentries.get(chd.getPath());
			if (ObjectUtils.isNullOrEmpty(entries)) {
				continue;
			}
			for (DiagnosticEntry entry : entries) {
				GeneratedFileOrigin origin = entry.getOrigin();
				if (origin != null) {
					ProcessorDetails originprocdetails = origin.getProcessorDetails();
					ProcessorState procstate = processorsByDetails.get(originprocdetails);
					if (procstate != null && procstate.isInitialized()
							&& procstate.analyzedClassFilePaths.contains(chd.getPath())) {
						//do not include the diagnostic, as it was not re-reported
						continue;
					}
				}
				info.addDiagnostic(entry);
			}
		}
	}

	private void removeUngeneratedFilesWithSkippedProcessors() {
		if (!unaddedGeneratedSourceFileDatas.isEmpty()) {
			info.putGeneratedSourceFiles(unaddedGeneratedSourceFileDatas);
			putClassFilesToInfoForSource(unaddedGeneratedSourceFileDatas);
			reusedSourceResultsFromPreviousCompilation.putAll(unaddedGeneratedSourceFileDatas);
			unaddedGeneratedSourceFileDatas.clear();
		}
		if (!unaddedGeneratedResourceFileDatas.isEmpty()) {
			info.putGeneratedResourceFiles(unaddedGeneratedResourceFileDatas);
			unaddedGeneratedResourceFileDatas.clear();
		}
	}

	private void removeUngeneratedFiles() {
		if (!unaddedGeneratedSourceFileDatas.isEmpty()) {
			for (Entry<SakerPath, GeneratedSourceFileData> entry : unaddedGeneratedSourceFileDatas.entrySet()) {
				SakerPath path = entry.getKey();
				GeneratedSourceFileData gsfd = entry.getValue();
				ProcessorState procstate = processorsByDetails.get(gsfd.getOrigin().getProcessorDetails());
				if (procstate != null) {
					if (!procstate.isInitialized() || procstate.shouldIncludeFromPreviousCompilation(gsfd)) {
						//processor has not been called for processing any of the elements
						//it was totally skipped without even examining the annotations present
						//therefor the generated files are up to date
						//OR
						//we are a simple processor and the causing element was not analyzed
						info.putGeneratedSourceFile(gsfd);
						putClassFilesToInfoForSource(gsfd);

						reusedSourceResultsFromPreviousCompilation.put(path, gsfd);
						continue;
					}
				}
				//TODO remove processor warnings too
				removeGeneratedSourceFileAndCompiledFiles(gsfd);

				for (ClassSignature clazz : gsfd.getClassSignatures()) {
					addABIChange(new ClassRemovedABIChange(clazz));
				}
			}
		}
		if (!unaddedGeneratedResourceFileDatas.isEmpty()) {
			for (Entry<SakerPath, GeneratedResourceFileData> entry : unaddedGeneratedResourceFileDatas.entrySet()) {
				SakerPath path = entry.getKey();
				GeneratedResourceFileData gsfd = entry.getValue();
				ProcessorState procstate = processorsByDetails.get(gsfd.getOrigin().getProcessorDetails());
				if (procstate != null) {
					if (!procstate.isInitialized() || procstate.shouldIncludeFromPreviousCompilation(gsfd)) {
						info.putGeneratedResourceFile(gsfd);
						continue;
					}
				}
				//TODO remove processor warnings too
				SakerFile mfile = SakerPathFiles.resolveAtPath(taskContext, path);
				if (mfile != null) {
					//null check just in case
					fileRemover.remove(path, mfile);
//					mfile.remove();
				}
			}
		}
	}

	private void addABIChange(Collection<? extends AbiChange> changes) {
		if (changes.isEmpty()) {
			return;
		}
		for (AbiChange c : changes) {
			addABIChange(c);
		}
	}

	private void addABIChange(AbiChange change) {
		if (allABIChanges.add(change)) {
			try {
				if (IncrementalCompilationHandler.LOGGING_ENABLED) {
					System.out.println("    " + change);
				}
				if (unaddedSourceFileDatas.isEmpty()) {
					return;
				}
				for (SourceFileData src : prevSourceFileDatas.values()) {
					if (change.affects(src.getABIUsage(), this::addABIChange)) {
						SakerPath srcpath = src.getPath();
						SourceFileData unaddedremoved = unaddedSourceFileDatas.remove(srcpath);
						if (unaddedremoved != null) {
							addABIChangedSourceForCompilation(change, src);
						}
					}
				}
				for (Iterator<? extends SourceFileData> it = unaddedRegeneratedSources.values().iterator(); it
						.hasNext();) {
					SourceFileData src = it.next();
					if (change.affects(src.getABIUsage(), this::addABIChange)) {
						addABIChangedSourceForCompilation(change, src);

						it.remove();
					}
				}
				for (Iterator<GeneratedSourceFileData> it = unaddedGeneratedSourceFileDatas.values().iterator(); it
						.hasNext();) {
					GeneratedSourceFileData src = it.next();
					if (change.affects(src.getABIUsage(), this::addABIChange)) {
						GeneratedFileOrigin origins = src.getOrigin();
						for (ClassHoldingFileData ofile : origins.getOriginatingFileDatas()) {
							//we need to check that the origin file still exists before re-adding

							switch (ofile.getKind()) {
								case CLASS: {
									//XXX support class files 
									throw new UnsupportedOperationException(
											"Unsupported file kind to add for compilation: " + ofile.getKind());
								}
								case SOURCE: {
									SourceFileData unaddedremoved = unaddedSourceFileDatas.remove(ofile.getPath());
									if (unaddedremoved == null) {
										unaddedremoved = unaddedRegeneratedSources.remove(ofile.getPath());
									}
									if (unaddedremoved != null) {
										addABIChangedSourceForCompilation(change, ofile);
										break;
									}
									break;
								}
								default: {
									throw new IllegalStateException(
											"Invalid file kind to add for compilation: " + ofile.getKind());
								}
							}
						}
						it.remove();
						removeGeneratedSourceFileAndCompiledFiles(src);
					}
				}

				//TODO check change against the already added files in Elements
				//if the change affects it, invalidate the cached data of the corresponding Elements
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
	}

//	private void detectABIChangeInCollection(AbiChange change, Collection<? extends SourceFileData> sources) {
//		for (Iterator<? extends SourceFileData> it = sources.iterator(); it.hasNext();) {
//			SourceFileData src = it.next();
//			if (change.affects(src.getABIUsage(), this::addABIChange)) {
//				try {
//					addABIChangedSourceForCompilation(change, src);
//				} catch (IOException e) {
//					throw new UncheckedIOException(e);
//				}
//
//				it.remove();
//			}
//		}
//	}

	private void handleRegeneratedSources() throws IOException {
		if (roundRegeneratedSources.isEmpty()) {
			return;
		}
		Set<AbiChange> addedchanges = new HashSet<>();
		outer:
		for (Iterator<GeneratedSourceFileData> it = roundRegeneratedSources.clearAndIterator(); it.hasNext();) {
			GeneratedSourceFileData sfd = it.next();
			for (AbiChange change : allABIChanges) {
				if (!change.affects(sfd.getABIUsage(), addedchanges::add)) {
					continue;
				}
				addABIChangedSourceForCompilation(change, sfd);
				continue outer;
			}
			unaddedRegeneratedSources.put(sfd.getPath(), sfd);
		}
		addABIChange(addedchanges);
	}

	private void callProcessorRounds() {
		Map<SakerPath, Set<? extends Element>> rootelements = new TreeMap<>();

		while (true) {
			for (ClassHoldingFileData fd : roundParsedElementDatas.values()) {
				// make sure we only add an element only once to the root elements
				// if a processor generates a same source as in the previous round, (ergo it doesnt get parsed again) 
				// but then it gets recompiled a round later because of an ABI change,
				// then it would be added twice to the root elements
				SakerPath fdpath = fd.getPath();
				if (rootElementsAddedPaths.add(fdpath)) {
					//we can skip adding the file to the elemtypes
					//because if the file was not modified by the user, then the signature is the same as the previous one
					//if it was modified by the user, then it is added in the first round and the signature is still correct
					Set<? extends Element> elems = elemTypes.addRootClassFile(fd);
					rootelements.put(fdpath, elems);
				}
			}
			roundParsedElementDatas.clear();
			if (moduleInfoFileData != null) {
				SakerPath minfopath = moduleInfoFileData.getPath();
				if (rootElementsAddedPaths.add(minfopath)) {
					//add the module element to the root elements if any
					Element currentme = elemTypes.getCurrentModuleElement();
					if (currentme != null) {
						rootelements.put(minfopath, ImmutableUtils.singletonSet(currentme));
					}
				}
			}
			//collect after adding every source files, as it can result in an error otherwise:
			//    trying to resolve a typename that is going to be added by an other file 
			rootElementsAnnotationSetCollector.clear();
			for (Entry<SakerPath, Set<? extends Element>> elems : rootelements.entrySet()) {
				rootElementsAnnotationSetCollector.collect(elems.getKey(), elems.getValue());
			}

			try {
				if (invocationContext.isParallelProcessing()) {
					ThreadUtils.runParallelItems(processorStates, p -> {
						p.callRound(rootElementsAnnotationSetCollector);
						//no consuming
					});
				} else {
					for (ProcessorState p : processorStates) {
						Set<? extends TypeElement> consumed = p.callRound(rootElementsAnnotationSetCollector);
						if (consumed != null) {
							rootElementsAnnotationSetCollector.removeAnnotationTypes(consumed);
						}
					}
				}
				handleRegeneratedSources();
			} catch (Throwable e) {
				setProcessingException(e);
				break;
			}

			if (isCompilationInclusionPending()) {
				break;
			}
			if (roundParsedElementDatas.isEmpty()) {
				//there were no generated sources at all
				break;
			}
			rootelements.clear();
			//else continue the loop and call the processors again with the additional elements
		}
	}

	private void analyzeStartingMultiOriginDependencies(Set<SakerPath> removedsources) {
		for (ProcessorDetails procdetails : passProcessorReferences.keySet()) {
			Set<DiagnosticEntry> entries = prevOriginDiagnosticEntries.get(procdetails);
			if (ObjectUtils.isNullOrEmpty(entries)) {
				continue;
			}
			for (DiagnosticEntry de : entries) {
				Set<SakerPath> originpaths = de.getOrigin().getOriginatingFilePaths();
				if (ObjectUtils.containsAny(removedsources, originpaths)) {
					deltaTriggeredProcessors.computeIfAbsent(procdetails, x -> new ProcessorTriggerDelta())
							.getTriggeredUnitPaths().addAll(originpaths);
				}
			}
		}
		analyzeStartingMultiOriginFileDependencies(removedsources, prevAllGeneratedSourceFileDatas);
		analyzeStartingMultiOriginFileDependencies(removedsources, prevAllGeneratedClassFileDatas);
		analyzeStartingMultiOriginFileDependencies(removedsources, prevGeneratedResourceFileDatas);
	}

	private void analyzeStartingMultiOriginFileDependencies(Set<SakerPath> removedsources,
			Map<SakerPath, ? extends ProcessorGeneratedFileData> files) {
		if (files.isEmpty()) {
			return;
		}
		for (ProcessorGeneratedFileData fd : files.values()) {
			GeneratedFileOrigin origin = fd.getOrigin();
			ProcessorDetails procdetails = origin.getProcessorDetails();
			if (!passProcessorReferences.containsKey(procdetails)) {
				continue;
			}
			Set<SakerPath> originpaths = origin.getOriginatingFilePaths();
			if (ObjectUtils.containsAny(originpaths, removedsources)) {
				if (removedsources.containsAll(originpaths)) {
					//if all of the origins were removed for the generated file, do not trigger the processor as it wouldnt regenerate it
					//we can remove the generated resource
					SakerPath filepath = fd.getPath();
					SakerFile mf = SakerPathFiles.resolveAtPath(taskContext, filepath);
					if (mf != null) {
						fileRemover.remove(filepath, mf);
//						mf.remove();
					}
				} else {
					deltaTriggeredProcessors.computeIfAbsent(procdetails, x -> new ProcessorTriggerDelta())
							.getTriggeredUnitPaths().addAll(originpaths);
				}
			}
		}
	}

	private boolean analyzeMultiOriginFileDependencies(ProcessorState p,
			Map<SakerPath, ? extends ProcessorGeneratedFileData> files) {
		//XXX can we make this more efficient
		boolean result = false;
		for (ProcessorGeneratedFileData fd : files.values()) {
			GeneratedFileOrigin origin = fd.getOrigin();
			NavigableMap<SakerPath, ? extends ClassHoldingFileData> originfiles = origin.getOriginatingFiles();
			if (ObjectUtils.containsAny(p.analyzedClassFilePaths, originfiles.keySet())) {
				for (Entry<SakerPath, ? extends ClassHoldingData> entry : originfiles.entrySet()) {
					ClassHoldingData chd = entry.getValue();
					if (chd.getOrigin() != null) {
						//do not include generated files
						continue;
					}
					SakerPath path = entry.getKey();
					if (!p.analyzedClassFilePaths.contains(path)) {
						p.triggerProcessorRootElements.add(path);
						result = true;
					}
				}
			}
		}
		return result;
	}

	private boolean analyzeMultiOriginDependencies() {
		boolean result = false;
		for (ProcessorState p : processorStates) {
			if (!p.isInitialized() || p.analyzedClassFilePaths.isEmpty()) {
				//processor was not initialized, it has not analyzed any files
				return false;
			}
			ProcessorDetails procdetails = p.getProcessorDetails();
			Set<DiagnosticEntry> entries = prevOriginDiagnosticEntries.get(procdetails);
			if (!ObjectUtils.isNullOrEmpty(entries)) {
				for (DiagnosticEntry de : entries) {
					NavigableMap<SakerPath, ? extends ClassHoldingFileData> originfiles = de.getOrigin()
							.getOriginatingFiles();
					if (ObjectUtils.containsAny(p.analyzedClassFilePaths, originfiles.keySet())) {
						for (Entry<SakerPath, ? extends ClassHoldingData> entry : originfiles.entrySet()) {
							ClassHoldingData chd = entry.getValue();
							if (chd.getOrigin() != null) {
								//do not include generated files
								continue;
							}
							SakerPath path = entry.getKey();
							if (!p.analyzedClassFilePaths.contains(path)) {
								p.triggerProcessorRootElements.add(path);
								result = true;
							}
						}
					}
				}
			}
			result |= analyzeMultiOriginFileDependencies(p, prevAllGeneratedSourceFileDatas);
			result |= analyzeMultiOriginFileDependencies(p, prevAllGeneratedClassFileDatas);
			result |= analyzeMultiOriginFileDependencies(p, prevGeneratedResourceFileDatas);
		}
		return result;
	}

	private void callProcessorFinalRounds() {
		if (processorsFinalRoundsCalled) {
			throw new IllegalStateException("Final rounds already called.");
		}

		processorsFinalRoundsCalled = true;

		IncrementalRoundEnvironment processorendenv = new IncrementalRoundEnvironment(true, Collections.emptySet(),
				Collections.emptyMap());
		try {
			if (invocationContext.isParallelProcessing()) {
				ThreadUtils.runParallelItems(processorStates, p -> {
					p.callFinalRound(processorendenv);
				});
			} else {
				for (ProcessorState p : processorStates) {
					p.callFinalRound(processorendenv);
				}
			}
			handleRegeneratedSources();
		} catch (Throwable e) {
			setProcessingException(e);
		}
	}

	private void setProcessingException(Throwable e) {
		e.printStackTrace();
		if (anyProcessingException == null) {
			anyProcessingException = e;
		} else {
			anyProcessingException.addSuppressed(e);
		}
	}

	private SourceFileData handleParsedFileSignature(NavigableMap<String, ? extends ClassSignature> classes,
			PackageSignature packagesignature, ImportScope importscope, ModuleSignature modulesignature,
			ContentDescriptor contentdescriptor, SakerPath filepath, SignatureSourcePositions sourcepositions) {
		SourceFileData resultsfd;
		SourceFileData prevsfd;

		GeneratedFileOrigin origin = generatedFileOrigins.get(filepath);
		if (origin != null) {
			GeneratedSourceFileData gsfd = new GeneratedSourceFileData(filepath, contentdescriptor, classes,
					packagesignature, importscope, origin);
			resultsfd = gsfd;

			prevsfd = prevAllGeneratedSourceFileDatas.get(filepath);
			unaddedGeneratedSourceFileDatas.remove(filepath);
			removeCompiledFilesForSource(prevsfd);
			totalParsedGeneratedSourcesByPath.put(filepath, gsfd);
		} else {
			resultsfd = new SourceFileData(filepath, contentdescriptor, classes, packagesignature, importscope);
			prevsfd = prevSourceFileDatas.get(filepath);
			removeCompiledFilesForSource(prevsfd);
			totalParsedSourcesByPath.put(filepath, resultsfd);
		}
		totalParsedCompilationUnitsByPath.put(filepath, resultsfd);

		resultsfd.setModuleSignature(modulesignature);

		determineABIChanges(prevsfd, resultsfd, methodParameterNameChangeChecker);
		resultsfd.setSourcePositions(sourcepositions);
		return resultsfd;
	}

	private void parseEnteredSourcesRound() {
		if (sourceCompilationInclusionPending) {
			sourceCompilationInclusionPending = false;
			Collection<? extends ClassHoldingData> parsedsources = invoker.parseRoundAddedSources();
			if (!parsedsources.isEmpty()) {
				for (ClassHoldingData cfd : parsedsources) {
					SakerPath entrypath = cfd.getPath();
					ContentDescriptor filecontent = SakerPathFiles.resolveAtPath(taskContext, entrypath)
							.getContentDescriptor();

					NavigableMap<String, ? extends ClassSignature> classes = cfd.getClasses();
					PackageSignature packagesignature = cfd.getPackageSignature();
					SourceFileData sfd = handleParsedFileSignature(classes, packagesignature, cfd.getImportScope(),
							cfd.getModuleSignature(), filecontent, entrypath, cfd.getSourcePositions());

					if (sfd.getModuleSignature() != null) {
						if (moduleInfoFileData != null) {
							throw new IllegalStateException("Multiple module-info files: "
									+ moduleInfoFileData.getPath() + " - " + sfd.getPath());
						}
						moduleInfoFileData = sfd;
					} else {
						roundParsedElementDatas.put(entrypath, sfd);
					}
				}
			}
		}

		if (classCompilationInclusionPending) {
			classCompilationInclusionPending = false;
//			Collection<? extends ClassHoldingData> parsedclasses = invoker.parseRoundAddedClassFiles();
//			if (!parsedclasses.isEmpty()) {
//				for (ClassHoldingData cfd : parsedclasses) {
//					//TODO include generated class files
//				}
//			}
		}
		if (firstSourceParsing) {
			firstSourceParsing = false;
			if (modulesSupported) {
				compilationModuleSet = invoker.getCompilationModuleSet();
				if (!Objects.equals(compilationModuleSet, prevCompilationModuleSet)) {
					//ABI change will affect all source files and trigger a full build
					addABIChange(new ModulePathABIChange(prevCompilationModuleSet, compilationModuleSet));
				} else if (prevModuleInfoFileData != moduleInfoFileData) {
					//at most only one of them is not null
					if (prevModuleInfoFileData == null || moduleInfoFileData == null) {
						//if one of them is null
						//or the signature changed

						//add a module change ABI change
						addABIChange(new ModuleChangeABIChange());
					} else {
						ModuleSignature currentsig = moduleInfoFileData.getModuleSignature();
						ModuleSignature prevsig = prevModuleInfoFileData.getModuleSignature();
						if (!ModuleSignature.signatureEquals(currentsig, prevsig)
								|| !AnnotatedSignature.annotationSignaturesEqual(currentsig, prevsig)) {
							addABIChange(new ModuleChangeABIChange());
						}
					}
				}
			}
		}
	}

	private void addABIChangedSourceForCompilation(AbiChange change, ClassHoldingFileData srcdata) throws IOException {
		SakerPath filesakerpath = srcdata.getPath();
		if (!parseEnteredSourcePaths.add(filesakerpath)) {
			//already added
			return;
		}
		removeInfoPutClassFilesForSourceFileData(srcdata);
		if (IncrementalCompilationHandler.LOGGING_ENABLED) {
			SakerLog.log().verbose().println(
					"Affected ABI change: (" + change + ") -> " + SakerPathFiles.toRelativeString(filesakerpath));
		}
		String filename = FileUtils.removeExtension(filesakerpath.getFileName());
		final String packname = srcdata.getPackageName();
		String createfilename = (packname == null ? "" : packname + ".") + filename;
		addSourceForCompilation(createfilename, filesakerpath);
	}

//	private SourceFileData handleParsedFileSignature(CompilationUnitTree cunittree, SortedMap<String, ? extends ModifiableClassSignature> classes,
//			PackageSignature packagesignature, String packagename, ContentDescriptor contentdescriptor, SakerPath filepath,
//			Map<? extends Tree, ? extends Signature> treesignatures) {
//		SourceFileData resultsfd;
//		SourceFileData prevsfd;
//
//		GeneratedFileOrigin origin = generatedFileOrigins.get(filepath);
//		if (origin != null) {
//			GeneratedSourceFileData gsfd = new GeneratedSourceFileData(filepath, contentdescriptor, classes, packagesignature, packagename, origin);
//			resultsfd = gsfd;
//
//			prevsfd = prevAllGeneratedSourceFileDatas.get(filepath);
//			unaddedGeneratedSourceFileDatas.remove(filepath);
//			removeClassFilesForSource(filepath, prevAllGeneratedSourceFileDatas);
//			totalParsedGeneratedSourcesByPath.put(filepath, gsfd);
//		} else {
//			resultsfd = new SourceFileData(filepath, contentdescriptor, classes, packagesignature, packagename);
//			prevsfd = prevSourceFileDatas.get(filepath);
//			removeClassFilesForSource(filepath, prevSourceFileDatas);
//			totalParsedSourcesByPath.put(filepath, resultsfd);
//		}
//		totalParsedCompilationUnitsByPath.put(filepath, resultsfd);
//		totalAddedClassHolders.add(resultsfd);
//
//		determineABIChanges(prevsfd, resultsfd);
//		return resultsfd;
//	}
//
//	private SourceFileData handleParsedFileSignature(CompilationUnitTree cunittree, ParseContext parsedsignature, SakerFile msrcfile, SakerPath filepath) {
//		SortedMap<String, ModifiableClassSignature> classes = parsedsignature.getClasses();
//		PackageSignature packagesignature = parsedsignature.getPackageSignature();
//		String packagename = parsedsignature.getPackageName();
//		ContentDescriptor contentdescriptor = msrcfile.getContentDescriptor();
//		Map<? extends Tree, ? extends Signature> treesignatures = parsedsignature.getTreeSignatures();
//
//		return handleParsedFileSignature(cunittree, classes, packagesignature, packagename, contentdescriptor, filepath, treesignatures);
//	}
//
//	private void handleAnalyzedGeneratedClassFile(GeneratedClassFileData cfd) {
//		SakerPath path = cfd.getPath();
//		GeneratedClassFileData prevcfd = prevAllGeneratedClassFileDatas.get(path);
//		determineABIChanges(prevcfd, cfd);
//	}

	private final static class OpenedResource {
		private String location;
		private SakerPath resourcePath;

		public OpenedResource(Location location, SakerPath resourcePath) {
			this.location = location.getName().toLowerCase(LOWERCASE_PATHS_LOCALE);
			this.resourcePath = resourcePath.toLowerCase(LOWERCASE_PATHS_LOCALE);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((location == null) ? 0 : location.hashCode());
			result = prime * result + ((resourcePath == null) ? 0 : resourcePath.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			OpenedResource other = (OpenedResource) obj;
			if (location == null) {
				if (other.location != null)
					return false;
			} else if (!location.equals(other.location))
				return false;
			if (resourcePath == null) {
				if (other.resourcePath != null)
					return false;
			} else if (!resourcePath.equals(other.resourcePath))
				return false;
			return true;
		}
	}

	private final static class OpenedResourceState {
		private ProcessorDetails processor;
		private boolean writing;

		public OpenedResourceState(ProcessorDetails processor, boolean writing) {
			this.processor = processor;
			this.writing = writing;
		}

		public ProcessorDetails getProcessor() {
			return processor;
		}

		public String createByProcessorMessage() {
			if (processor == null) {
				return ".";
			}
			return " by processor: " + processor.getProcessorName();
		}
	}
}
