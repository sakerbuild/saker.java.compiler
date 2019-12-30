package testing.saker.java.compiler.tests.tasks.javac;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class Release7CompilationTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	//note: the --release option requires the file manager to implement StandardJavaFileManager
	//see Arguments:328
	//But ONLY on java 9. This is fixed in javac on Java 10+

	private static final SakerPath ADDED_PATH = PATH_WORKING_DIRECTORY.resolve("src/test/Added.java");

	
	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		runScriptTask("releasebuild");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("releasebuild"));

		runScriptTask("releasebuild");
		assertEmpty(getMetric().getRunTaskIdFactories());

		files.putFile(ADDED_PATH, "package test; public class Added { }");
		runScriptTask("releasebuild");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("releasebuild"));

		files.delete(ADDED_PATH);

		runScriptTask("sourcetargetbuild");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("sourcetargetbuild"));

		runScriptTask("sourcetargetbuild");
		assertEmpty(getMetric().getRunTaskIdFactories());

		files.putFile(ADDED_PATH, "package test; public class Added { }");
		runScriptTask("sourcetargetbuild");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("sourcetargetbuild"));
		
		files.delete(ADDED_PATH);
		
		runScriptTask("bothbuild");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("bothbuild"));

		runScriptTask("bothbuild");
		assertEmpty(getMetric().getRunTaskIdFactories());

		files.putFile(ADDED_PATH, "package test; public class Added { }");
		runScriptTask("bothbuild");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("bothbuild"));
	}

}
