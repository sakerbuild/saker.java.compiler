package testing.saker.java.compiler.tests.tasks.javac;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class JavaCompileNativeHeaderTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	private static final SakerPath SRC_MAIN_JAVA_PATH = SRC_PATH_BASE.resolve("test/Main.java");
	private static final SakerPath SRC_SECOND_JAVA_PATH = SRC_PATH_BASE.resolve("test/Second.java");
	private static final SakerPath INC_MAIN_JAVA_PATH = PATH_WORKING_DIRECTORY.resolve("incsrc")
			.resolve("test/Main.java");
	private static final SakerPath INC_SECOND_JAVA_PATH = PATH_WORKING_DIRECTORY.resolve("incsrc")
			.resolve("test/Second.java");

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		SakerPath binbuilddir = getJavacBuildBinDirectory("src");
		SakerPath headerbuilddir = getJavacBuildHeaderDirectory("src");
		SakerPath incbinbuilddir = getJavacBuildBinDirectory("incsrc");
		SakerPath incheaderbuilddir = getJavacBuildHeaderDirectory("incsrc");

		runScriptTask("build");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("src", "incsrc"));
		assertCompiled(SRC_MAIN_JAVA_PATH, INC_MAIN_JAVA_PATH);
		assertReused();
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/Main.class");
		assertDirectoryRecursiveContents(headerbuilddir, "test_Main.h");
		assertDirectoryRecursiveContents(incbinbuilddir, "test", "test/Main.class");
		assertDirectoryRecursiveContents(incheaderbuilddir, "test_Main.h");
		assertTrue(files.getAllBytes(headerbuilddir.resolve("test_Main.h"))
				.regionEquals(files.getAllBytes(incheaderbuilddir.resolve("test_Main.h"))));

		runScriptTask("build");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf());
		assertCompiled();
		assertReused();
		assertEmpty(getMetric().getRunTaskIdFactories());
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/Main.class");
		assertDirectoryRecursiveContents(headerbuilddir, "test_Main.h");
		assertDirectoryRecursiveContents(incbinbuilddir, "test", "test/Main.class");
		assertDirectoryRecursiveContents(incheaderbuilddir, "test_Main.h");

		files.putFile(SRC_SECOND_JAVA_PATH,
				"package test; public class Second { @java.lang.annotation.Native public static final int CONST = 3; }"
						.getBytes());
		files.putFile(INC_SECOND_JAVA_PATH,
				"package test; public class Second { @java.lang.annotation.Native public static final int CONST = 3; }"
						.getBytes());
		runScriptTask("build");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("src", "incsrc"));
		assertCompiled(SRC_MAIN_JAVA_PATH, SRC_SECOND_JAVA_PATH, INC_SECOND_JAVA_PATH);
		assertReused(INC_MAIN_JAVA_PATH);
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/Main.class", "test/Second.class");
		assertDirectoryRecursiveContents(headerbuilddir, "test_Main.h", "test_Second.h");
		assertDirectoryRecursiveContents(incbinbuilddir, "test", "test/Main.class", "test/Second.class");
		assertDirectoryRecursiveContents(incheaderbuilddir, "test_Main.h", "test_Second.h");

		files.putFile(SRC_SECOND_JAVA_PATH,
				"package test; public class Second { public static final int CONST = 3; }".getBytes());
		files.putFile(INC_SECOND_JAVA_PATH,
				"package test; public class Second { public static final int CONST = 3; }".getBytes());
		runScriptTask("build");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("src", "incsrc"));
		assertCompiled(SRC_MAIN_JAVA_PATH, SRC_SECOND_JAVA_PATH, INC_SECOND_JAVA_PATH);
		assertReused(INC_MAIN_JAVA_PATH);
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/Main.class", "test/Second.class");
		assertDirectoryRecursiveContents(headerbuilddir, "test_Main.h");
		assertDirectoryRecursiveContents(incbinbuilddir, "test", "test/Main.class", "test/Second.class");
		assertDirectoryRecursiveContents(incheaderbuilddir, "test_Main.h");

		files.delete(headerbuilddir.resolve("test_Main.h"));
		runScriptTask("build");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("src"));
		assertCompiled(SRC_MAIN_JAVA_PATH, SRC_SECOND_JAVA_PATH);
		assertReused();
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/Main.class", "test/Second.class");
		assertDirectoryRecursiveContents(headerbuilddir, "test_Main.h");
		assertDirectoryRecursiveContents(incbinbuilddir, "test", "test/Main.class", "test/Second.class");
		assertDirectoryRecursiveContents(incheaderbuilddir, "test_Main.h");

		files.delete(incheaderbuilddir.resolve("test_Main.h"));
		runScriptTask("build");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("incsrc"));
		assertCompiled();
		assertReused(INC_MAIN_JAVA_PATH, INC_SECOND_JAVA_PATH);
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/Main.class", "test/Second.class");
		assertDirectoryRecursiveContents(headerbuilddir, "test_Main.h");
		assertDirectoryRecursiveContents(incbinbuilddir, "test", "test/Main.class", "test/Second.class");
		assertDirectoryRecursiveContents(incheaderbuilddir, "test_Main.h");
	}
}
