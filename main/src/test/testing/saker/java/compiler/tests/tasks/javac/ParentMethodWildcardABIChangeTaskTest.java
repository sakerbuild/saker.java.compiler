package testing.saker.java.compiler.tests.tasks.javac;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class ParentMethodWildcardABIChangeTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	private static final SakerPath TESTIMPL_PATH = SRC_PATH_BASE.resolve("test/TestImpl.java");
	private static final SakerPath TESTITF_PATH = SRC_PATH_BASE.resolve("test/TestItf.java");

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		runScriptTask("build");
		assertCompiled(TESTITF_PATH, TESTIMPL_PATH);
		assertReused();

		files.putFile(TESTITF_PATH,
				files.getAllBytes(TESTITF_PATH).toString().replace("? extends Runnable", "? extends Thread"));
		assertTaskException("saker.java.compiler.api.compile.exc.JavaCompilationFailedException",
				() -> runScriptTask("build"));
		assertCompiled(TESTITF_PATH, TESTIMPL_PATH);
		assertReused();

		files.putFile(TESTIMPL_PATH,
				files.getAllBytes(TESTIMPL_PATH).toString().replace("? extends Runnable", "? extends Thread"));
		runScriptTask("build");
		assertCompiled(TESTITF_PATH, TESTIMPL_PATH);
		assertReused();
	}

}
