package testing.saker.java.compiler.tests.tasks.javac.proc;

import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class AnnotationMethodsTesterProcessorTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		Throwable failexc = assertTaskException("saker.java.compiler.api.compile.exc.JavaCompilationFailedException",
				() -> runScriptTask("build"));
		//assert that the failure wasn't due to the processor
		assertEquals(failexc.getCause(), null);
		assertEquals(failexc.getSuppressed().length, 0);
		assertEquals(getMetric().getInitializedProcessors(), setOf("test.TesterProcessor"));
	}

}
