package testing.saker.java.compiler.tests.tasks.javac;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class FullDeterministicAbiVersionKeyTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	private static final SakerPath CP_PATH_BASE = PATH_WORKING_DIRECTORY.resolve("cp");

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		SakerPath fieldjavasrcpath = CP_PATH_BASE.resolve("test/Field.java");

		runScriptTask("build");
		assertCompiled(CP_PATH_BASE.resolve("test/Field.java"), SRC_PATH_BASE.resolve("test/Main.java"));
		assertReused();
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("cp", "main"));

		runScriptTask("build");
		assertEquals(getMetric().getRunTaskIdFactories().keySet(), setOf());
		assertEquals(getMetric().getCompiledJavacPasses(), setOf());

		files.putFile(fieldjavasrcpath,
				files.getAllBytes(fieldjavasrcpath).toString().replace("logging", "modifiedlogging"));
		runScriptTask("build");
		assertCompiled(CP_PATH_BASE.resolve("test/Field.java"));
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("cp"));

		files.putFile(fieldjavasrcpath,
				files.getAllBytes(fieldjavasrcpath).toString().replace("//intplaceholder", "public int i;"));
		runScriptTask("build");
		assertCompiled(CP_PATH_BASE.resolve("test/Field.java"), SRC_PATH_BASE.resolve("test/Main.java"));
		assertReused();
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("cp", "main"));

		//add an anonymous class to the classpath, and make sure that the ABI version key doesnt change
		files.putFile(fieldjavasrcpath, files.getAllBytes(fieldjavasrcpath).toString().replace("\"anonreplace\"",
				"new Runnable(){ public void run() { } }"));
		runScriptTask("build");
		assertCompiled(CP_PATH_BASE.resolve("test/Field.java"));
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("cp"));
	}

}
