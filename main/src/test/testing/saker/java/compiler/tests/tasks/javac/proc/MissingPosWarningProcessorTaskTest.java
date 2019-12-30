package testing.saker.java.compiler.tests.tasks.javac.proc;

import java.util.Set;

import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class MissingPosWarningProcessorTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		runScriptTask("build");
		assertEquals(getMetric().getInitializedProcessors(), setOf("test.MessagerProcessor"));
		Set<?> allprintedlines = getMetric().getAllPrintedTaskLines();
		assertTrue(allprintedlines.contains(
				"[saker.java.compile:src]src/test/Main.java:3:1-32: Warning: (test.MessagerProcessor): OUT_MESSAGE_Main.<init>"));
		assertTrue(allprintedlines.contains(
				"[saker.java.compile:src]src/test/package-info.java:1:1-8: Warning: (test.MessagerProcessor): OUT_MESSAGE_test@MyAnnot"));
		//it was tested that on JDK 12, javac reports the second version. Which is fine.
		assertTrue(allprintedlines.contains(
				"[saker.java.compile:src]src/test/package-info.java:2:9-12: Warning: (test.MessagerProcessor): OUT_MESSAGE_test")

				|| allprintedlines.contains(
						"[saker.java.compile:src]src/test/package-info.java:1:1-23: Warning: (test.MessagerProcessor): OUT_MESSAGE_test"));
	}

}
