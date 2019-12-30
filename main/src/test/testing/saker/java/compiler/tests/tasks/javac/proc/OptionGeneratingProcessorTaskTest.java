package testing.saker.java.compiler.tests.tasks.javac.proc;

import java.util.Map;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.build.tests.TestUtils;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class OptionGeneratingProcessorTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	private String value = null;

	@Override
	protected Map<String, ?> getTaskVariables() {
		return TestUtils.<String, Object>treeMapBuilder().put("test.Value", value).put("test.Include", value != null)
				.build();
	}

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		value = "test";
		runScriptTask("build");

		SakerPath binbuilddir = getJavacBuildBinDirectory("src");
		SakerPath genbuilddir = getJavacBuildGeneratedSourceDirectory("src");

		assertDirectoryRecursiveContents(binbuilddir, "test", "test/Main.class", "option", "option/Value.class");
		assertDirectoryRecursiveContents(genbuilddir, "option", "option/Value.java");
		assertEquals(getMetric().getInitializedProcessors(), setOf("test.OptionGeneratingProcessor"));

		files.touch(SRC_PATH_BASE.resolve("test/Main.java"));
		runScriptTask("build");
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/Main.class", "option", "option/Value.class");
		assertDirectoryRecursiveContents(genbuilddir, "option", "option/Value.java");
		assertEquals(getMetric().getInitializedProcessors(), setOf());
		assertEquals(getMetric().getCompiledFiles(), setOf(SRC_PATH_BASE.resolve("test/Main.java")));

		value = "modified";
		runScriptTask("build");
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/Main.class", "option", "option/Value.class");
		assertDirectoryRecursiveContents(genbuilddir, "option", "option/Value.java");
		assertEquals(getMetric().getInitializedProcessors(), setOf("test.OptionGeneratingProcessor"));
		assertEquals(getMetric().getCompiledFiles(),
				setOf(SRC_PATH_BASE.resolve("test/Main.java"), genbuilddir.resolve("option/Value.java")));

		value = null;
		assertTaskException("saker.java.compiler.api.compile.exc.JavaCompilationFailedException",
				() -> runScriptTask("build"));
		assertDirectoryRecursiveContents(genbuilddir);
	}

}
