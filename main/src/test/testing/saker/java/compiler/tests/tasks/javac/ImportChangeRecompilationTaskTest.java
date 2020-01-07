package testing.saker.java.compiler.tests.tasks.javac;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class ImportChangeRecompilationTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		runScriptTask("build");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("src"));

		SakerPath pathItf = PATH_WORKING_DIRECTORY.resolve("src/test/Itf.java");
		SakerPath pathItfImpl = PATH_WORKING_DIRECTORY.resolve("src/test/ItfImpl.java");
		files.putFile(pathItf, files.getAllBytes(pathItf).toString().replace("import test.p1.ImportedClass;",
				"import test.p2.ImportedClass;"));
		runScriptTask("build");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("src"));
		assertEquals(getMetric().getCompiledFiles(), setOf(pathItf, pathItfImpl));
	}

}
