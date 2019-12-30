package testing.saker.java.compiler;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayOutputStream;
import testing.saker.build.tests.CollectingTestMetric;
import testing.saker.java.compiler.JavaCompilerTestMetric;

public class CompilerCollectingTestMetric extends CollectingTestMetric implements JavaCompilerTestMetric {
	public static class ProcessorInfo {
		protected String className;
		protected Set<SakerPath> generatedSources = new ConcurrentSkipListSet<>();
		protected Set<SakerPath> generatedClassFiles = new ConcurrentSkipListSet<>();
		protected Set<SakerPath> generatedResources = new ConcurrentSkipListSet<>();

		public ProcessorInfo(String className) {
			this.className = className;
		}
	}

	protected Set<SakerPath> compiledFiles = new ConcurrentSkipListSet<>();
	protected Set<SakerPath> reusedFiles = new ConcurrentSkipListSet<>();
	protected Map<String, ProcessorInfo> processorInfos = new ConcurrentSkipListMap<>();
	protected Set<String> initializedProcessors = new ConcurrentSkipListSet<>();
	protected Set<String> reportedDiagnostics = new ConcurrentSkipListSet<>();
	protected Set<String> compiledJavacPasses = new ConcurrentSkipListSet<>();
	protected Set<String> bootedJavacCompilePasses = new ConcurrentSkipListSet<>();
	protected boolean hadExternallyCompiled;
	protected boolean forceExternalCompilation;

	@Override
	public void javacCompilingFile(SakerPath key) {
		compiledFiles.add(key);
	}

	@Override
	public void javacCompilingPass(String passidentifier) {
		compiledJavacPasses.add(passidentifier);
	}

	@Override
	public void javacCompilerBootTaskInvoked(String passidstring) {
		bootedJavacCompilePasses.add(passidstring);
	}

	@Override
	public void externallyCompiling() {
		this.hadExternallyCompiled = true;
	}

	@Override
	public boolean forceExternalCompilation() {
		return forceExternalCompilation;
	}

	public void setForceExternalCompilation(boolean forceExternalCompilation) {
		this.forceExternalCompilation = forceExternalCompilation;
	}

	@Override
	public void javacReusingFiles(Set<SakerPath> reused) {
		reusedFiles.addAll(reused);
	}

	@Override
	public void javacProcessorSourceGenerated(SakerPath path, UnsyncByteArrayOutputStream content,
			String processorclassname) {
		processorInfos.computeIfAbsent(processorclassname, ProcessorInfo::new).generatedSources.add(path);
	}

	@Override
	public void javacProcessorClassGenerated(SakerPath path, UnsyncByteArrayOutputStream content,
			String processorclassname) {
		processorInfos.computeIfAbsent(processorclassname, ProcessorInfo::new).generatedClassFiles.add(path);
	}

	@Override
	public void javacProcessorResourceGenerated(SakerPath path, UnsyncByteArrayOutputStream content,
			String processorclassname) {
		processorInfos.computeIfAbsent(processorclassname, ProcessorInfo::new).generatedResources.add(path);
	}

	@Override
	public void javacProcessorInitialized(String className) {
		initializedProcessors.add(className);
	}

	@Override
	public void javacDiagnosticReported(String message, String warningtype) {
		reportedDiagnostics.add(message);
	}

	public boolean isHadExternallyCompiled() {
		return hadExternallyCompiled;
	}

	public Set<String> getCompiledJavacPasses() {
		return compiledJavacPasses;
	}

	public Set<String> getBootedJavacCompilePasses() {
		return bootedJavacCompilePasses;
	}

	public Set<SakerPath> getCompiledFiles() {
		return compiledFiles;
	}

	public Set<SakerPath> getReusedFiles() {
		return reusedFiles;
	}

	public Map<String, ProcessorInfo> getProcessorInfos() {
		return processorInfos;
	}

	public Set<String> getInitializedProcessors() {
		return initializedProcessors;
	}

	public Set<String> getReportedDiagnostics() {
		return reportedDiagnostics;
	}

}
