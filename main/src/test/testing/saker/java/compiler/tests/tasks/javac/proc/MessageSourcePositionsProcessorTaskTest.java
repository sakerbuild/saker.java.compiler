package testing.saker.java.compiler.tests.tasks.javac.proc;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class MessageSourcePositionsProcessorTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		SakerPath procsrc = PATH_WORKING_DIRECTORY.resolve("proc/test/MessagerProcessor.java");
		SakerPath srcjava = PATH_WORKING_DIRECTORY.resolve("src/test/Main.java");
		SakerPath addsrcjava = PATH_WORKING_DIRECTORY.resolve("src/test/Add.java");
		SakerPath addsrcjavax = PATH_WORKING_DIRECTORY.resolve("src/test/Add.javax");

		runScriptTask("build");
		assertEquals(getMetric().getInitializedProcessors(), setOf("test.MessagerProcessor"));
		assertTrue(getMetric().getAllPrintedTaskLines().contains(
				"[saker.java.compile:src]src/test/Main.java:3:19-26: Warning: (test.MessagerProcessor): OUT_MESSAGE_T"));
		assertTrue(getMetric().getAllPrintedTaskLines().contains(
				"[saker.java.compile:src]src/test/Main.java:5:2-9: Warning: (test.MessagerProcessor): OUT_MESSAGE_run"));
		assertTrue(getMetric().getAllPrintedTaskLines().contains(
				"[saker.java.compile:src]src/test/Main.java:9:11-13: Warning: (test.MessagerProcessor): OUT_MESSAGE_VAL_f"));

		files.putFile(addsrcjava, files.getAllBytes(addsrcjavax));
		runScriptTask("build");
		assertEquals(getMetric().getInitializedProcessors(), setOf("test.MessagerProcessor"));
		assertTrue(getMetric().getAllPrintedTaskLines().contains(
				"[saker.java.compile:src]src/test/Main.java:5:2-9: Warning: (test.MessagerProcessor): OUT_MESSAGE_run"));
		assertTrue(getMetric().getAllPrintedTaskLines().contains(
				"[saker.java.compile:src]src/test/Main.java:9:11-13: Warning: (test.MessagerProcessor): OUT_MESSAGE_VAL_f"));
		assertTrue(getMetric().getAllPrintedTaskLines().contains(
				"[saker.java.compile:src]src/test/Add.java:4:11-13: Warning: (test.MessagerProcessor): OUT_MESSAGE_VAL_addf"));

		runScriptTask("build");
		assertEquals(getMetric().getInitializedProcessors(), setOf());

		//modify only the processor to be sure that the source position data is passed to the processor even if the actual files are not reparsed
		files.putFile(procsrc, files.getAllBytes(procsrc).toString().replace("OUT_MESSAGE", "OUT_MOD"));
		runScriptTask("build");
		assertEquals(getMetric().getInitializedProcessors(), setOf("test.MessagerProcessor"));
		assertCompiled(procsrc);
		assertTrue(getMetric().getAllPrintedTaskLines().contains(
				"[saker.java.compile:src]src/test/Main.java:5:2-9: Warning: (test.MessagerProcessor): OUT_MOD_run"));
		assertTrue(getMetric().getAllPrintedTaskLines().contains(
				"[saker.java.compile:src]src/test/Main.java:9:11-13: Warning: (test.MessagerProcessor): OUT_MOD_VAL_f"));
		assertTrue(getMetric().getAllPrintedTaskLines().contains(
				"[saker.java.compile:src]src/test/Add.java:4:11-13: Warning: (test.MessagerProcessor): OUT_MOD_VAL_addf"));

		//insert a new line after the class declaration
		//the warning positions should update even though the processor doesn't rerun
		files.putFile(addsrcjava, files.getAllBytes(addsrcjava).toString().replace("Add {", "Add {\n"));
		runScriptTask("build");
		assertEquals(getMetric().getInitializedProcessors(), setOf());
		assertCompiled(addsrcjava);
		assertTrue(getMetric().getAllPrintedTaskLines().contains(
				"[saker.java.compile:src]src/test/Main.java:5:2-9: Warning: (test.MessagerProcessor): OUT_MOD_run"));
		assertTrue(getMetric().getAllPrintedTaskLines().contains(
				"[saker.java.compile:src]src/test/Main.java:9:11-13: Warning: (test.MessagerProcessor): OUT_MOD_VAL_f"));
		assertTrue(getMetric().getAllPrintedTaskLines().contains(
				"[saker.java.compile:src]src/test/Add.java:5:11-13: Warning: (test.MessagerProcessor): OUT_MOD_VAL_addf"));
	}

}
