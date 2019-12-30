package testing.saker.java.compiler.tests.tasks.javac;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class EnclosingOuterClassConstructorTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	private static final SakerPath MAIN_JAVA_PATH = SRC_PATH_BASE.resolve("test/Main.java");
	private static final SakerPath SUBINNERCLASS_JAVA_PATH = SRC_PATH_BASE.resolve("test/SubInnerClass.java");

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		//assert compilation success
		runScriptTask("build");
		assertCompiled(MAIN_JAVA_PATH, SUBINNERCLASS_JAVA_PATH);
		assertReused();
	}

}
