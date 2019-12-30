package testing.saker.java.compiler.tests.tasks.javac.proc;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class OriginatingFileDoesntExistProcessorTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	//bug test encountered when working with command line processor 

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		SakerPath npath = PATH_WORKING_DIRECTORY.resolve("src/test/SuperParams.java");
		SakerPath subpath = PATH_WORKING_DIRECTORY.resolve("src/test/SubParams.java");

		runScriptTask("build");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("src"));

		files.moveFile(PATH_WORKING_DIRECTORY.resolve("src/test/ModifiedSuperParams.java"), npath);
		files.putFile(npath, files.getAllBytes(npath).toString().replace("Modified", ""));
		files.putFile(subpath, files.getAllBytes(subpath).toString().replace("Modified", ""));

		runScriptTask("build");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("src"));
	}

}
