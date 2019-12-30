package testing.saker.java.compiler.tests.tasks.javac;

import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class JavacTaskBooterTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		files.putFile(PATH_WORKING_DIRECTORY.resolve("saker.build"),
				"build { $javac = saker.java.compile(SourceDirectories: src); }");
		runScriptTask("build");
		assertEquals(getMetric().getBootedJavacCompilePasses(), setOf("src"));
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("src"));

		files.putFile(PATH_WORKING_DIRECTORY.resolve("saker.build"),
				"build { $dir = src; $javac = saker.java.compile(SourceDirectories: $dir); }");
		runScriptTask("build");
		assertEquals(getMetric().getBootedJavacCompilePasses(), setOf("src"));
		assertEquals(getMetric().getCompiledJavacPasses(), setOf());
	}

}
