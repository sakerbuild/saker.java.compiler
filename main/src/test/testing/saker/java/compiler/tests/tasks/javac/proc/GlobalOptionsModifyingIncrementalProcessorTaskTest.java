package testing.saker.java.compiler.tests.tasks.javac.proc;

import java.util.Map;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.build.tests.TestUtils;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class GlobalOptionsModifyingIncrementalProcessorTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	private String option = null;

	@Override
	protected Map<String, ?> getTaskVariables() {
		return TestUtils.<String, Object>treeMapBuilder().put("test.Option", option).build();
	}

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		SakerPath binbuilddir = getJavacBuildBinDirectory("src");
		SakerPath genbuilddir = getJavacBuildGeneratedSourceDirectory("src");
		SakerPath src = PATH_WORKING_DIRECTORY.resolve("src/test/Main.java");

		option = null;

		runScriptTask("build");
		assertTrue(getMetric().getInitializedProcessors().contains("test.InitProcessor"));
		assertDirectoryRecursiveContents(binbuilddir, "output", "output/Generated.class", "test", "test/Main.class");
		assertDirectoryRecursiveContents(genbuilddir, "output", "output/Generated.java");

		runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());

		option = "opt";

		runScriptTask("build");
		assertTrue(getMetric().getInitializedProcessors().contains("test.InitProcessor"));
		assertCompiled(src, genbuilddir.resolve("output/Generated.java"));
	}

}
