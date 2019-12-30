package testing.saker.java.compiler;

import java.util.Set;

import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayOutputStream;

@SuppressWarnings("unused")
public interface JavaCompilerTestMetric {
	public default void javacCompilingFile(SakerPath key) {
	}

	public default void javacReusingFiles(Set<SakerPath> reused) {
	}

	public default void javacDiagnosticReported(String message, String warningtype) {
	}

	public default void javacProcessorSourceGenerated(SakerPath path, UnsyncByteArrayOutputStream content,
			String processorname) {
	}

	public default void javacProcessorClassGenerated(SakerPath path, UnsyncByteArrayOutputStream content,
			String processorname) {
	}

	public default void javacProcessorResourceGenerated(SakerPath path, UnsyncByteArrayOutputStream content,
			String processorname) {
	}

	public default void javacProcessorInitialized(String className) {
	}

	public default void javacCompilingPass(String passidentifier) {
	}

	public default void javacCompilationFinished(Elements elems, Types types, Elements javacelements,
			Types javactypes) {
	}

	public default void javacAddCompilationFinishClosing(Runnable run) {
		run.run();
	}

	public default boolean javacCaresAboutCompilationFinish() {
		return false;
	}

	public default void javacCompilerBootTaskInvoked(String passidstring) {
	}

	public default void externallyCompiling() {
	}

	public default boolean forceExternalCompilation() {
		return false;
	}
}
