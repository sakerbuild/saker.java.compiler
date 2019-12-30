package testing.saker.java.compiler.tests.tasks.javac.proc;

import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class FilerExceptionsTestingProcessorTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		// the processor itself will check for the valid behaviour
		runScriptTask("build");
		assertEquals(getMetric().getInitializedProcessors(), setOf("test.ExtractProcessor"));
	}

}
