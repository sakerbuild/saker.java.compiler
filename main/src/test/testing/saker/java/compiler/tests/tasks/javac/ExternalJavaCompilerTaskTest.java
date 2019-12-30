package testing.saker.java.compiler.tests.tasks.javac;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.java.compiler.CompilerCollectingTestMetric;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class ExternalJavaCompilerTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	private static final SakerPath MAIN_JAVA_PATH = SRC_PATH_BASE.resolve("test/Main.java");
	private static final SakerPath SECOND_JAVA_PATH = SRC_PATH_BASE.resolve("test/Second.java");

	@Override
	protected CompilerCollectingTestMetric createMetricImpl() {
		CompilerCollectingTestMetric result = super.createMetricImpl();
		result.setForceExternalCompilation(true);
		return result;
	}

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		runScriptTask("build");
		assertCompiled(MAIN_JAVA_PATH);
		assertReused();
		assertTrue(getMetric().isHadExternallyCompiled());

		runScriptTask("build");
		assertCompiled();
		assertReused();
		assertEquals(getMetric().getRunTaskIdFactories().keySet(), setOf());

		files.putFile(SECOND_JAVA_PATH, "package test; public class Second { }".getBytes());
		runScriptTask("build");
		assertCompiled(SECOND_JAVA_PATH);
		assertReused(MAIN_JAVA_PATH);
		assertTrue(getMetric().isHadExternallyCompiled());

		files.touch(MAIN_JAVA_PATH);
		runScriptTask("build");
		assertCompiled(MAIN_JAVA_PATH);
		assertReused(SECOND_JAVA_PATH);
	}

}
