package testing.saker.java.compiler.tests.tasks.javac.proc;

import java.util.Map;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.build.tests.TestUtils;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class InitGeneratingProcessorTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	private boolean include = true;
	private String option = null;

	@Override
	protected Map<String, ?> getTaskVariables() {
		return TestUtils.<String, Object>treeMapBuilder().put("test.Include", include).put("test.Option", option)
				.build();
	}

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		SakerPath binbuilddir = getJavacBuildBinDirectory("src");
		SakerPath genbuilddir = getJavacBuildGeneratedSourceDirectory("src");

		option = null;
		include = true;

		runScriptTask("build");
		assertTrue(getMetric().getInitializedProcessors().contains("test.InitProcessor"));
		assertDirectoryRecursiveContents(binbuilddir, "output", "output/Generated.class", "test", "test/Main.class");
		assertDirectoryRecursiveContents(genbuilddir, "output", "output/Generated.java");

		runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());

		SakerPath src = PATH_WORKING_DIRECTORY.resolve("src/test/Main.java");
		files.putFile(src, files.getAllBytes(src).toString().replace("//in-method", "System.out.println(123);"));
		runScriptTask("build");
		assertEmpty(getMetric().getInitializedProcessors());
		assertCompiled(src);
		assertDirectoryRecursiveContents(binbuilddir, "output", "output/Generated.class", "test", "test/Main.class");
		assertDirectoryRecursiveContents(genbuilddir, "output", "output/Generated.java");

		option = "opt";
		runScriptTask("build");
		assertTrue(getMetric().getInitializedProcessors().contains("test.InitProcessor"));
		assertCompiled(src, genbuilddir.resolve("output/Generated.java"));
		assertDirectoryRecursiveContents(binbuilddir, "output", "output/Generated.class", "test", "test/Main.class");
		assertDirectoryRecursiveContents(genbuilddir, "output", "output/Generated.java");

		include = false;

		//+ output.Generated.OPTIONVALUE
		files.putFile(src, files.getAllBytes(src).toString().replace("+ output.Generated.OPTIONVALUE", ""));
		runScriptTask("build");
		assertEmpty(getMetric().getInitializedProcessors());
		assertCompiled(src);
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/Main.class");
		assertDirectoryRecursiveContents(genbuilddir);
	}

}
