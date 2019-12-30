package testing.saker.java.compiler.tests.tasks.javac.proc;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class MessageRemoveProcessorTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		SakerPath procsrc = PATH_WORKING_DIRECTORY.resolve("proc/test/MessagerProcessor.java");
		SakerPath srcjava = PATH_WORKING_DIRECTORY.resolve("src/test/Main.java");

		runScriptTask("build");
		assertEquals(getMetric().getInitializedProcessors(), setOf("test.MessagerProcessor"));
		assertEquals(getMetric().getReportedDiagnostics(), setOf("OUT_MESSAGE_run"));
		assertTrue(getMetric().getAllPrintedTaskLines().contains(
				"[saker.java.compile:src]src/test/Main.java:4:2-10: Warning: (test.MessagerProcessor): OUT_MESSAGE_run"));

		files.putFile(procsrc, files.getAllBytes(procsrc).toString().replace("OUT_MESSAGE", "OUT_MOD"));
		runScriptTask("build");
		assertEquals(getMetric().getInitializedProcessors(), setOf("test.MessagerProcessor"));
		assertEquals(getMetric().getReportedDiagnostics(), setOf("OUT_MOD_run"));
		assertTrue(getMetric().getAllPrintedTaskLines().contains(
				"[saker.java.compile:src]src/test/Main.java:4:2-10: Warning: (test.MessagerProcessor): OUT_MOD_run"));

		runScriptTask("build");
		assertEquals(getMetric().getInitializedProcessors(), setOf());
		assertEquals(getMetric().getReportedDiagnostics(), setOf());
		assertTrue(getMetric().getAllPrintedTaskLines().contains(
				"[saker.java.compile:src]src/test/Main.java:4:2-10: Warning: (test.MessagerProcessor): OUT_MOD_run"));

		files.putFile(srcjava, files.getAllBytes(srcjava).toString().replace("@Override", "//@Override"));
		runScriptTask("build");
		assertEquals(getMetric().getInitializedProcessors(), setOf("test.MessagerProcessor"));
		assertEquals(getMetric().getReportedDiagnostics(), setOf());

		runScriptTask("build");
		assertEquals(getMetric().getInitializedProcessors(), setOf());
		assertEquals(getMetric().getReportedDiagnostics(), setOf());

		files.putFile(srcjava, files.getAllBytes(srcjava).toString().replace("//@Override", "@Override"));
		runScriptTask("build");
		assertEquals(getMetric().getInitializedProcessors(), setOf("test.MessagerProcessor"));
		assertEquals(getMetric().getReportedDiagnostics(), setOf("OUT_MOD_run"));
		assertTrue(getMetric().getAllPrintedTaskLines().contains(
				"[saker.java.compile:src]src/test/Main.java:4:2-10: Warning: (test.MessagerProcessor): OUT_MOD_run"));
	}

}
