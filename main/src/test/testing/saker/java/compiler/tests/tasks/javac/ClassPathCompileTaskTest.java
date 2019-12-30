package testing.saker.java.compiler.tests.tasks.javac;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class ClassPathCompileTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	private static final SakerPath CP_PATH_BASE = PATH_WORKING_DIRECTORY.resolve("cp");

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		SakerPath fieldjavasrcpath = CP_PATH_BASE.resolve("test/Field.java");

		runScriptTask("build");
		assertCompiled(CP_PATH_BASE.resolve("test/Field.java"), SRC_PATH_BASE.resolve("test/Main.java"));
		assertReused();

		runScriptTask("build");
		assertEquals(getMetric().getRunTaskIdFactories().keySet(), setOf());

		files.putFile(fieldjavasrcpath,
				files.getAllBytes(fieldjavasrcpath).toString().replace("logging", "modifiedlogging"));
		runScriptTask("build");
		assertCompiled(CP_PATH_BASE.resolve("test/Field.java"));
	}

}
