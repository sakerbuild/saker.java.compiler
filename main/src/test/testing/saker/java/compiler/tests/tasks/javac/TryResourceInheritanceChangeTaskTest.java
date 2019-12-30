package testing.saker.java.compiler.tests.tasks.javac;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class TryResourceInheritanceChangeTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		runScriptTask("build");

		SakerPath ressrcpath = PATH_WORKING_DIRECTORY.resolve("src/test/MyResource.java");
		files.putFile(ressrcpath, files.getAllBytes(ressrcpath).toString().replace("extends AutoCloseable", ""));
		assertTaskException("saker.java.compiler.api.compile.exc.JavaCompilationFailedException",
				() -> runScriptTask("build"));
	}

}
