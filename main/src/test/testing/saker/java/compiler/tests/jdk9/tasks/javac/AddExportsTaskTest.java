package testing.saker.java.compiler.tests.jdk9.tasks.javac;

import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class AddExportsTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		runScriptTask("cmdline");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("cmdline"));
		runScriptTask("cmdline");
		assertEmpty(getMetric().getRunTaskIdFactories());
		
		runScriptTask("fields");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("fields"));
		runScriptTask("fields");
		assertEmpty(getMetric().getRunTaskIdFactories());
		
		runScriptTask("options");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("options"));
		runScriptTask("options");
		assertEmpty(getMetric().getRunTaskIdFactories());
		
		runScriptTask("full");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("full"));
		runScriptTask("full");
		assertEmpty(getMetric().getRunTaskIdFactories());
	}

}
