package testing.saker.java.compiler.tests.tasks.javac.proc;

import java.util.Map;
import java.util.TreeMap;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.ObjectUtils;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class ProcessorRemovalTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	private boolean include;

	@Override
	protected Map<String, ?> getTaskVariables() {
		TreeMap<String, Object> result = ObjectUtils.newTreeMap(super.getTaskVariables());
		result.put("test.proc.include", include);
		return result;
	}

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		SakerPath binbuilddir = getJavacBuildBinDirectory("src");
		SakerPath genbuilddir = getJavacBuildGeneratedSourceDirectory("src");

		include = true;

		runScriptTask("build");
		assertEquals(getMetric().getInitializedProcessors(), setOf("test.ExtractProcessor"));
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/ExtractInterface.class", "test/Main.class",
				"test/MainItf.class");
		assertDirectoryRecursiveContents(genbuilddir, "test", "test/MainItf.java");

		runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/ExtractInterface.class", "test/Main.class",
				"test/MainItf.class");
		assertDirectoryRecursiveContents(genbuilddir, "test", "test/MainItf.java");

		include = false;
		runScriptTask("build");
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/ExtractInterface.class", "test/Main.class");
		assertDirectoryRecursiveContents(genbuilddir);

		runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());
	}

}
