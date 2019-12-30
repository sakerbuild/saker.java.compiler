package testing.saker.java.compiler.tests.tasks.javac.proc;

import testing.saker.SakerTest;
import testing.saker.java.compiler.CompilerCollectingTestMetric;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class ExternalJavaCompilerProcessorTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	@Override
	protected CompilerCollectingTestMetric createMetricImpl() {
		CompilerCollectingTestMetric result = super.createMetricImpl();
		result.setForceExternalCompilation(true);
		return result;
	}

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		runScriptTask("build");
	}

}
