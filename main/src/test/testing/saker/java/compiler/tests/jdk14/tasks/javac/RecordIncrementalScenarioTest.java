package testing.saker.java.compiler.tests.jdk14.tasks.javac;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class RecordIncrementalScenarioTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		SakerPath mainpath = SRC_PATH_BASE.resolve("test/Main.java");
		SakerPath myrecordpath = SRC_PATH_BASE.resolve("test/MyRecord.java");
		SakerPath fielduserpath = SRC_PATH_BASE.resolve("test/FieldUser.java");

		runScriptTask("build");
		assertEquals(getMetric().getCompiledFiles(), setOf(mainpath, myrecordpath, fielduserpath));

		files.putFile(myrecordpath, files.getAllBytes(myrecordpath).toString().replace("int i", "int i, int j"));
		assertTaskException("saker.java.compiler.api.compile.exc.JavaCompilationFailedException",
				() -> runScriptTask("build"));

		files.putFile(mainpath, files.getAllBytes(mainpath).toString().replace("(1)", "(1, 2)"));
		runScriptTask("build");
		assertEquals(getMetric().getCompiledFiles(), setOf(mainpath, myrecordpath));

		//remove the field i
		//update the main file accordingly
		files.putFile(myrecordpath, files.getAllBytes(myrecordpath).toString().replace("int i, ", ""));
		files.putFile(mainpath, files.getAllBytes(mainpath).toString().replace("(1, 2)", "(2)"));
		assertTaskException("saker.java.compiler.api.compile.exc.JavaCompilationFailedException",
				() -> runScriptTask("build"));

		files.putFile(fielduserpath, files.getAllBytes(fielduserpath).toString().replace(".i()", ".j()"));
		runScriptTask("build");
	}

}
