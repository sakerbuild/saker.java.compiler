package testing.saker.java.compiler.tests.tasks.javac.proc;

import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class ProcessorInputLocationTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		// the processor itself will check for the valid behaviour
		runTest("");

		runTest("inc");
	}

	private void runTest(String passprefix) throws Throwable {
		String targetname = passprefix + "build";
		files.putFile(PATH_WORKING_DIRECTORY.resolve("workdirres.txt"), "wdres");

		System.out.println("---- ProcessorInputLocationTest.runTest() running for: " + targetname);

		runScriptTask(targetname);
		assertEquals(files.getAllBytes(getJavacBuildHeaderDirectory(passprefix + "src").resolve("file.txt")).toString(),
				"wdres");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf(passprefix + "proc", passprefix + "src"));

		runScriptTask(targetname);
		assertEquals(files.getAllBytes(getJavacBuildHeaderDirectory(passprefix + "src").resolve("file.txt")).toString(),
				"wdres");
		assertEmpty(getMetric().getRunTaskIdFactories());

		files.putFile(PATH_WORKING_DIRECTORY.resolve("workdirres.txt"), "wdmodified");
		runScriptTask(targetname);
		assertEquals(files.getAllBytes(getJavacBuildHeaderDirectory(passprefix + "src").resolve("file.txt")).toString(),
				"wdmodified");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf(passprefix + "src"));

		files.putFile(PATH_WORKING_DIRECTORY.resolve("workexistence.txt"), "x");
		runScriptTask(targetname);
		assertEquals(files.getAllBytes(getJavacBuildHeaderDirectory(passprefix + "src").resolve("file.txt")).toString(),
				"wdmodified");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf(passprefix + "src"));

		files.delete(PATH_WORKING_DIRECTORY.resolve("workexistence.txt"));
		runScriptTask(targetname);
		assertEquals(files.getAllBytes(getJavacBuildHeaderDirectory(passprefix + "src").resolve("file.txt")).toString(),
				"wdmodified");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf(passprefix + "src"));
	}

}
