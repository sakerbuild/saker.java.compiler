package testing.saker.java.compiler.tests.tasks.javac.proc;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class BuildErrorGenSourceNotDeletedProcessorTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		SakerPath mainjavapath = PATH_WORKING_DIRECTORY.resolve("src/test/Main.java");

		runScriptTask("build");
		assertEquals(getMetric().getInitializedProcessors(), setOf("test.ResourceGenProcessor"));
		assertDirectoryRecursiveContents(getJavacBuildGeneratedSourceDirectory("main"), "test", "test/MainGen.java");
		assertDirectoryRecursiveContents(getJavacBuildBinDirectory("main"), "test", "test/MainGen.class",
				"test/Annot.class", "test/GenUser.class", "test/Main.class", "test/MainGen.ctxt");
		assertDirectoryRecursiveContents(getJavacBuildResourcesDirectory("main").resolve("RES_OUTPUT"), "test",
				"test/MainGen.txt");

		files.putFile(mainjavapath,
				files.getAllBytes(mainjavapath).toString().replace("test.MainGen", "test.MainGenModified"));
		assertTaskException("saker.java.compiler.api.compile.exc.JavaCompilationFailedException",
				() -> runScriptTask("build"));
		assertEquals(getMetric().getInitializedProcessors(), setOf("test.ResourceGenProcessor"));
		assertDirectoryRecursiveContents(getJavacBuildGeneratedSourceDirectory("main"), "test",
				"test/MainGenModified.java");
		assertDirectoryRecursiveContents(getJavacBuildResourcesDirectory("main").resolve("RES_OUTPUT"), "test",
				"test/MainGenModified.txt");

		files.putFile(mainjavapath,
				files.getAllBytes(mainjavapath).toString().replace("test.MainGenModified", "test.MainGen"));
		runScriptTask("build");
		assertEquals(getMetric().getInitializedProcessors(), setOf("test.ResourceGenProcessor"));
		assertDirectoryRecursiveContents(getJavacBuildGeneratedSourceDirectory("main"), "test", "test/MainGen.java");
		assertDirectoryRecursiveContents(getJavacBuildBinDirectory("main"), "test", "test/MainGen.class",
				"test/Annot.class", "test/GenUser.class", "test/Main.class", "test/MainGen.ctxt");
		assertDirectoryRecursiveContents(getJavacBuildResourcesDirectory("main").resolve("RES_OUTPUT"), "test",
				"test/MainGen.txt");
	}

}
