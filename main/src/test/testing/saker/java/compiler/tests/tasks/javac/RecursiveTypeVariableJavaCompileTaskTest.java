package testing.saker.java.compiler.tests.tasks.javac;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class RecursiveTypeVariableJavaCompileTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		SakerPath mainjavapath = SRC_PATH_BASE.resolve("test/Main.java");
		SakerPath callerjavapath = SRC_PATH_BASE.resolve("test/Caller.java");

		runScriptTask("build");
		assertEquals(getMetric().getCompiledFiles(), setOf(mainjavapath, callerjavapath));

		files.putFile(mainjavapath, files.getAllBytes(mainjavapath).toString().replace("//replace",
				"//replace\npublic static <T extends E, E extends List<T>> void function(double d) {}"));
		runScriptTask("build");
		assertEquals(getMetric().getCompiledFiles(), setOf(mainjavapath, callerjavapath));
	}

}
