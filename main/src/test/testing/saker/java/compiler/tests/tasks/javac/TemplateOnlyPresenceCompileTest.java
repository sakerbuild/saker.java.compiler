package testing.saker.java.compiler.tests.tasks.javac;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class TemplateOnlyPresenceCompileTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	private static final SakerPath MAIN_JAVA_PATH = SRC_PATH_BASE.resolve("test/Main.java");
	private static final SakerPath USER_JAVA_PATH = SRC_PATH_BASE.resolve("test/User.java");

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		runScriptTask("build");
		assertCompiled(MAIN_JAVA_PATH, USER_JAVA_PATH);

		//modify some method implementation
		files.putFile(MAIN_JAVA_PATH, files.getAllBytes(MAIN_JAVA_PATH).toString().replace("System.out", "System.err"));
		runScriptTask("build");
		assertCompiled(MAIN_JAVA_PATH);

		files.putFile(MAIN_JAVA_PATH, files.getAllBytes(MAIN_JAVA_PATH).toString().replace("implements Runnable", ""));
		runScriptTask("build");
		assertCompiled(MAIN_JAVA_PATH, USER_JAVA_PATH);
	}

}
