package testing.saker.java.compiler.tests.tasks.javac;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class FullJavaCompileTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	private static final SakerPath MAIN_JAVA_PATH = SRC_PATH_BASE.resolve("test/Main.java");
	private static final SakerPath SECOND_JAVA_PATH = SRC_PATH_BASE.resolve("test/Second.java");

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		SakerPath binbuilddir = getJavacBuildBinDirectory("src");

		runScriptTask("build");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("src"));
		assertCompiled(MAIN_JAVA_PATH);
		assertReused();
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/Main.class");

		runScriptTask("build");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf());
		assertCompiled();
		assertReused();
		assertEmpty(getMetric().getRunTaskIdFactories());
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/Main.class");

		files.putFile(SECOND_JAVA_PATH, "package test; public class Second { }".getBytes());
		runScriptTask("build");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("src"));
		assertCompiled(MAIN_JAVA_PATH, SECOND_JAVA_PATH);
		assertReused();
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/Main.class", "test/Second.class");

		files.touch(MAIN_JAVA_PATH);
		runScriptTask("build");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("src"));
		assertCompiled(SECOND_JAVA_PATH, MAIN_JAVA_PATH);
		assertReused();
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/Main.class", "test/Second.class");

		files.delete(binbuilddir.resolve("test/Main.class"));
		runScriptTask("build");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("src"));
		assertCompiled(SECOND_JAVA_PATH, MAIN_JAVA_PATH);
		assertReused();
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/Main.class", "test/Second.class");
	}

}
