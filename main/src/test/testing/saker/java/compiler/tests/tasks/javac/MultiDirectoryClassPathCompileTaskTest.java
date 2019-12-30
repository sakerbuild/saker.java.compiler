package testing.saker.java.compiler.tests.tasks.javac;

import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class MultiDirectoryClassPathCompileTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		CombinedTargetTaskResult res;

		res = runScriptTask("build");

		res = runScriptTask("build");
		assertEquals(getMetric().getRunTaskIdFactories().keySet(), setOf());
	}

}
