package saker.java.compiler.impl.compile.handler.info;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.ImmutableUtils;
import saker.build.thirdparty.saker.util.io.SerialUtils;
import saker.java.compiler.impl.compile.handler.diagnostic.DiagnosticEntry;
import saker.java.compiler.impl.compile.handler.invoker.ProcessorDetails;
import saker.java.compiler.impl.signature.element.ClassSignature;
import saker.java.compiler.impl.signature.element.ModuleSignature;
import saker.java.compiler.impl.signature.element.PackageSignature;

public abstract class CompilationInfoBase implements CompilationInfo, Externalizable {
	private static final long serialVersionUID = 1L;

	protected NavigableMap<SakerPath, SourceFileData> sourceFiles = Collections.emptyNavigableMap();
	protected NavigableMap<SakerPath, GeneratedSourceFileData> generatedSourceFiles = new ConcurrentSkipListMap<>();

	protected NavigableMap<SakerPath, ClassFileData> classFiles = Collections.emptyNavigableMap();
	protected NavigableMap<SakerPath, GeneratedClassFileData> generatedClassFiles = new ConcurrentSkipListMap<>();

	protected NavigableMap<SakerPath, GeneratedResourceFileData> generatedResourceFiles = new ConcurrentSkipListMap<>();

	protected Map<? extends ProcessorDetails, ProcessorData> processorDetails = Collections.emptyMap();

	private NavigableMap<SakerPath, ExecutionClassPathStateInfo> executionClassPathStateInfos = Collections
			.emptyNavigableMap();
	private NavigableMap<SakerPath, ? extends LocalClassPathStateInfo> localClassPathStateInfos = Collections
			.emptyNavigableMap();
	private NavigableMap<SakerPath, ExecutionClassPathStateInfo> executionModulePathStateInfos = Collections
			.emptyNavigableMap();
	private NavigableMap<SakerPath, ? extends LocalClassPathStateInfo> localModulePathStateInfos = Collections
			.emptyNavigableMap();

	private ClassHoldingData moduleFileData;

	private NavigableSet<String> compilationModuleSet;

	private Set<DiagnosticEntry> diagnostics = ConcurrentHashMap.newKeySet();

	private NavigableSet<String> unrecognizedProcessorOptions = Collections.emptyNavigableSet();

	private Object abiVersionKey;
	private Object implementationVersionKey;

	private String moduleMainClass;
	private String moduleVersion;

	public CompilationInfoBase() {
	}

	@Override
	public NavigableSet<String> getUnrecognizedProcessorOptions() {
		return unrecognizedProcessorOptions;
	}

	@Override
	public void setUnrecognizedProcessorOptions(NavigableSet<String> options) {
		this.unrecognizedProcessorOptions = new TreeSet<>(options);
	}

	@Override
	public void addDiagnostic(DiagnosticEntry entry) {
		diagnostics.add(entry);
	}

	@Override
	public void addDiagnostics(Collection<DiagnosticEntry> entries) {
		diagnostics.addAll(entries);
	}

	@Override
	public Set<DiagnosticEntry> getDiagnostics() {
		return diagnostics;
	}

	@Override
	public ClassHoldingData getModuleClassFile() {
		return moduleFileData;
	}

	@Override
	public void setModuleClassFile(ClassHoldingData fd) {
		this.moduleFileData = fd;
	}

	@Override
	public NavigableSet<String> getCompilationModuleSet() {
		return compilationModuleSet;
	}

	@Override
	public void setCompilationModuleSet(NavigableSet<String> modulenames) {
		this.compilationModuleSet = modulenames;
	}

	@Override
	public void setSourceFiles(NavigableMap<SakerPath, SourceFileData> sources) {
		this.sourceFiles = sources;
	}

	@Override
	public void putGeneratedSourceFiles(NavigableMap<SakerPath, GeneratedSourceFileData> sources) {
		generatedSourceFiles.putAll(sources);
	}

	@Override
	public void putGeneratedSourceFile(GeneratedSourceFileData source) {
		generatedSourceFiles.put(source.getPath(), source);
	}

	@Override
	public void setClassFiles(NavigableMap<SakerPath, ClassFileData> classes) {
		this.classFiles = classes;
	}

	@Override
	public void putGeneratedClassFile(GeneratedClassFileData classfiledata) {
		generatedClassFiles.put(classfiledata.getPath(), classfiledata);
	}

	@Override
	public void putGeneratedClassFiles(NavigableMap<SakerPath, GeneratedClassFileData> classes) {
		generatedClassFiles.putAll(classes);
	}

	@Override
	public void putGeneratedResourceFile(GeneratedResourceFileData resourcefiledata) {
		generatedResourceFiles.put(resourcefiledata.getPath(), resourcefiledata);
	}

	@Override
	public void putGeneratedResourceFiles(NavigableMap<SakerPath, GeneratedResourceFileData> resources) {
		generatedResourceFiles.putAll(resources);
	}

	@Override
	public NavigableMap<SakerPath, ClassFileData> getClassFiles() {
		return Collections.unmodifiableNavigableMap(classFiles);
	}

	@Override
	public NavigableMap<SakerPath, GeneratedClassFileData> getGeneratedClassFiles() {
		return Collections.unmodifiableNavigableMap(generatedClassFiles);
	}

	@Override
	public NavigableMap<SakerPath, SourceFileData> getSourceFiles() {
		return Collections.unmodifiableNavigableMap(sourceFiles);
	}

	@Override
	public NavigableMap<SakerPath, GeneratedSourceFileData> getGeneratedSourceFiles() {
		return Collections.unmodifiableNavigableMap(generatedSourceFiles);
	}

	@Override
	public NavigableMap<SakerPath, GeneratedResourceFileData> getGeneratedResourceFiles() {
		return Collections.unmodifiableNavigableMap(generatedResourceFiles);
	}

	@Override
	public Map<? extends ProcessorDetails, ProcessorData> getProcessorDetails() {
		return processorDetails;
	}

	@Override
	public void setProcessorDetails(Map<? extends ProcessorDetails, ProcessorData> processors) {
		this.processorDetails = new HashMap<>(processors);
	}

	@Override
	public NavigableMap<SakerPath, ExecutionClassPathStateInfo> getExecutionClassPathStateInfos() {
		return executionClassPathStateInfos;
	}

	@Override
	public void setExecutionClassPathStateInfosSignatures(
			NavigableMap<SakerPath, ExecutionClassPathStateInfo> executionClassPathStateInfos) {
		this.executionClassPathStateInfos = executionClassPathStateInfos;
	}

	@Override
	public NavigableMap<SakerPath, ? extends LocalClassPathStateInfo> getLocalClassPathStateInfos() {
		return localClassPathStateInfos;
	}

	@Override
	public void setLocalClassPathStateInfosSignatures(
			NavigableMap<SakerPath, LocalClassPathStateInfo> localClassPathStateInfos) {
		this.localClassPathStateInfos = localClassPathStateInfos;
	}

	@Override
	public NavigableMap<SakerPath, ? extends LocalClassPathStateInfo> getLocalModulePathStateInfos() {
		return localModulePathStateInfos;
	}

	@Override
	public NavigableMap<SakerPath, ExecutionClassPathStateInfo> getExecutionModulePathStateInfos() {
		return executionModulePathStateInfos;
	}

	@Override
	public void setLocalModulePathStateInfosSignatures(
			NavigableMap<SakerPath, LocalClassPathStateInfo> localModulePathStateInfos) {
		this.localModulePathStateInfos = localModulePathStateInfos;
	}

	@Override
	public void setExecutionModulePathStateInfosSignatures(
			NavigableMap<SakerPath, ExecutionClassPathStateInfo> executionModulePathStateInfos) {
		this.executionModulePathStateInfos = executionModulePathStateInfos;
	}

	@Override
	public Collection<ClassSignature> getRealizedClassSignatures() {
		Collection<ClassSignature> result = new ArrayList<>();
		addRealizedClassSignaturesImpl(result, sourceFiles.values());
		addRealizedClassSignaturesImpl(result, generatedSourceFiles.values());
		addRealizedClassSignaturesImpl(result, generatedClassFiles.values());
		return result;
	}

	@Override
	public Collection<PackageSignature> getRealizedPackageSignatures() {
		Collection<PackageSignature> result = new ArrayList<>();
		addRealizedPackageSignaturesImpl(result, sourceFiles.values());
		addRealizedPackageSignaturesImpl(result, generatedSourceFiles.values());
		addRealizedPackageSignaturesImpl(result, generatedClassFiles.values());
		return result;
	}

	@Override
	public NavigableMap<SakerPath, Collection<ClassSignature>> getRealizedClassSignaturesByPath() {
		NavigableMap<SakerPath, Collection<ClassSignature>> result = new TreeMap<>();
		addRealizedClassSignaturesImpl(result, sourceFiles.values());
		addRealizedClassSignaturesImpl(result, generatedSourceFiles.values());
		addRealizedClassSignaturesImpl(result, generatedClassFiles.values());
		return result;
	}

	@Override
	public NavigableMap<SakerPath, PackageSignature> getRealizedPackageSignaturesByPath() {
		NavigableMap<SakerPath, PackageSignature> result = new TreeMap<>();
		addRealizedPackageSignaturesImpl(result, sourceFiles.values());
		addRealizedPackageSignaturesImpl(result, generatedSourceFiles.values());
		addRealizedPackageSignaturesImpl(result, generatedClassFiles.values());
		return result;
	}

	@Override
	public ModuleSignature getRealizedModuleSignature() {
		ClassHoldingData modulefd = moduleFileData;
		if (modulefd == null) {
			return null;
		}
		return modulefd.getRealizedModuleSignature();
	}

	@Override
	public SakerPath getRealizedModulesignaturePath() {
		ClassHoldingData modulefd = moduleFileData;
		if (modulefd == null) {
			return null;
		}
		return modulefd.getPath();
	}

	private static void addRealizedClassSignaturesImpl(Collection<ClassSignature> result,
			Iterable<? extends ClassHoldingFileData> files) {
		for (ClassHoldingFileData fd : files) {
			result.addAll(fd.getRealizedClassSignatures());
		}
	}

	private static void addRealizedClassSignaturesImpl(Map<SakerPath, Collection<ClassSignature>> result,
			Iterable<? extends ClassHoldingFileData> files) {
		for (ClassHoldingFileData fd : files) {
			Collection<? extends ClassSignature> realizedsignatures = fd.getRealizedClassSignatures();
			if (!realizedsignatures.isEmpty()) {
				//XXX avoid new collection creation? 
				result.put(fd.getPath(), ImmutableUtils.makeImmutableList(realizedsignatures));
			}
		}
	}

	private static void addRealizedPackageSignaturesImpl(Collection<PackageSignature> result,
			Iterable<? extends ClassHoldingFileData> files) {
		for (ClassHoldingFileData fd : files) {
			PackageSignature pack = fd.getRealizedPackageSignature();
			if (pack != null) {
				result.add(pack);
			}
		}
	}

	private static void addRealizedPackageSignaturesImpl(Map<SakerPath, PackageSignature> result,
			Iterable<? extends ClassHoldingFileData> files) {
		for (ClassHoldingFileData fd : files) {
			PackageSignature pack = fd.getRealizedPackageSignature();
			if (pack != null) {
				result.put(fd.getPath(), pack);
			}
		}
	}

	@Override
	public Object getAbiVersionKey() {
		return abiVersionKey;
	}

	@Override
	public void setAbiVersionKey(Object abiVersionKey) {
		this.abiVersionKey = abiVersionKey;
	}

	@Override
	public Object getImplementationVersionKey() {
		return implementationVersionKey;
	}

	@Override
	public void setImplementationVersionKey(Object implementationVersionKey) {
		this.implementationVersionKey = implementationVersionKey;
	}

	@Override
	public void setModuleMainClass(String moduleMainClass) {
		this.moduleMainClass = moduleMainClass;
	}

	@Override
	public String getModuleMainClass() {
		return moduleMainClass;
	}

	@Override
	public void setModuleVersion(String moduleVersion) {
		this.moduleVersion = moduleVersion;
	}

	@Override
	public String getModuleVersion() {
		return moduleVersion;
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		SerialUtils.writeExternalMap(out, sourceFiles);
		SerialUtils.writeExternalMap(out, generatedSourceFiles);
		SerialUtils.writeExternalMap(out, classFiles);
		SerialUtils.writeExternalMap(out, generatedClassFiles);
		SerialUtils.writeExternalMap(out, generatedResourceFiles);

		SerialUtils.writeExternalMap(out, processorDetails);

		SerialUtils.writeExternalMap(out, executionClassPathStateInfos);
		SerialUtils.writeExternalMap(out, localClassPathStateInfos);
		SerialUtils.writeExternalMap(out, executionModulePathStateInfos);
		SerialUtils.writeExternalMap(out, localModulePathStateInfos);

		out.writeObject(moduleFileData);
		out.writeObject(abiVersionKey);
		out.writeObject(implementationVersionKey);
		out.writeObject(moduleMainClass);
		out.writeObject(moduleVersion);

		SerialUtils.writeExternalCollection(out, compilationModuleSet);

		SerialUtils.writeExternalCollection(out, diagnostics);
		SerialUtils.writeExternalCollection(out, unrecognizedProcessorOptions);
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		sourceFiles = SerialUtils.readExternalSortedImmutableNavigableMap(in);
		generatedSourceFiles = SerialUtils.readExternalSortedImmutableNavigableMap(in);
		classFiles = SerialUtils.readExternalSortedImmutableNavigableMap(in);
		generatedClassFiles = SerialUtils.readExternalSortedImmutableNavigableMap(in);
		generatedResourceFiles = SerialUtils.readExternalSortedImmutableNavigableMap(in);

		processorDetails = SerialUtils.readExternalImmutableHashMap(in);

		executionClassPathStateInfos = SerialUtils.readExternalSortedImmutableNavigableMap(in);
		localClassPathStateInfos = SerialUtils.readExternalSortedImmutableNavigableMap(in);
		executionModulePathStateInfos = SerialUtils.readExternalSortedImmutableNavigableMap(in);
		localModulePathStateInfos = SerialUtils.readExternalSortedImmutableNavigableMap(in);

		moduleFileData = (ClassHoldingData) in.readObject();
		abiVersionKey = in.readObject();
		implementationVersionKey = in.readObject();
		moduleMainClass = (String) in.readObject();
		moduleVersion = (String) in.readObject();

		compilationModuleSet = SerialUtils.readExternalSortedImmutableNavigableSet(in);

		diagnostics = SerialUtils.readExternalImmutableHashSet(in);
		unrecognizedProcessorOptions = SerialUtils.readExternalSortedImmutableNavigableSet(in);
	}

}
