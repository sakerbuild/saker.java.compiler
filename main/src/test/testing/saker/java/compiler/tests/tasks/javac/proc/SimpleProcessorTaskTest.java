package testing.saker.java.compiler.tests.tasks.javac.proc;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class SimpleProcessorTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	//the test also asserts that the processing api and javac classes are available for the loaded processors

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		SakerPath srcpath = SRC_PATH_BASE.resolve("test/Main.java");

		SakerPath binbuilddir = getJavacBuildBinDirectory("src");
		SakerPath genbuilddir = getJavacBuildGeneratedSourceDirectory("src");

		runScriptTask("build");
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/ExtractInterface.class", "test/Main.class",
				"test/MainItf.class");
		assertDirectoryRecursiveContents(genbuilddir, "test", "test/MainItf.java");

		runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());

		files.setFileBytes(srcpath, files.getAllBytes(srcpath).toString().replace("implements MainItf", "/*impl*/")
				.replace("@ExtractInterface", "/*annot*/").getBytes());
		runScriptTask("build");
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/ExtractInterface.class", "test/Main.class");
		assertDirectoryRecursiveContents(genbuilddir);

		files.setFileBytes(srcpath, files.getAllBytes(srcpath).toString().replace("/*impl*/", "implements MainItf")
				.replace("/*annot*/", "@ExtractInterface").getBytes());
		runScriptTask("build");
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/ExtractInterface.class", "test/Main.class",
				"test/MainItf.class");
		assertDirectoryRecursiveContents(genbuilddir, "test", "test/MainItf.java");

		files.setFileBytes(srcpath, files.getAllBytes(srcpath).toString().replace("implements MainItf", "/*impl*/")
				.replace("@ExtractInterface", "/*annot*/").getBytes());
		runScriptTask("build");
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/ExtractInterface.class", "test/Main.class");
		assertDirectoryRecursiveContents(genbuilddir);

		files.setFileBytes(srcpath, files.getAllBytes(srcpath).toString().replace("/*impl*/", "implements MainItf")
				.replace("/*annot*/", "@ExtractInterface").getBytes());
		runScriptTask("build");
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/ExtractInterface.class", "test/Main.class",
				"test/MainItf.class");
		assertDirectoryRecursiveContents(genbuilddir, "test", "test/MainItf.java");

		//test that the processor is reinvoked if an output source file is modified/deleted
		files.delete(genbuilddir.resolve("test/MainItf.java"));
		runScriptTask("build");
		assertEquals(getMetric().getInitializedProcessors(), setOf("test.ExtractProcessor"));
		assertDirectoryRecursiveContents(genbuilddir, "test", "test/MainItf.java");

	}

}
