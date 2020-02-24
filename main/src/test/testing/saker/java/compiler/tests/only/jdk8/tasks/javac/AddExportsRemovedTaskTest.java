package testing.saker.java.compiler.tests.only.jdk8.tasks.javac;

import saker.build.util.java.JavaTools;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class AddExportsRemovedTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		assertEquals(JavaTools.getCurrentJavaMajorVersion(), 8);

		runScriptTask("build");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("src"));
	}

}
