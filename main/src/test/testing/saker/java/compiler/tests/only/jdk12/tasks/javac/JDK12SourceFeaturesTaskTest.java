package testing.saker.java.compiler.tests.only.jdk12.tasks.javac;

import saker.build.util.java.JavaTools;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class JDK12SourceFeaturesTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		assertEquals(JavaTools.getCurrentJavaMajorVersion(), 12);
		
		runScriptTask("build");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("src"));
	}
}
