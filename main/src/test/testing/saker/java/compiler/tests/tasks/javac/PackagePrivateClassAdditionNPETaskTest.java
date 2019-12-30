package testing.saker.java.compiler.tests.tasks.javac;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class PackagePrivateClassAdditionNPETaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	//bug that happened in doc change detection when generating headers, and a top level package private class is added
	// in the source file
	
	private static final SakerPath MAIN_JAVA_PATH = SRC_PATH_BASE.resolve("test/Main.java");

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		runScriptTask("build");

		files.putFile(MAIN_JAVA_PATH, files.getAllBytes(MAIN_JAVA_PATH).toString().replace("//add", "class X { }"));
		runScriptTask("build");
	}

}
