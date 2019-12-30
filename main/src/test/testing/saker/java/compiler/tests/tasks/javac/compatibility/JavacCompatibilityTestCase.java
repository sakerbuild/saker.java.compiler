package testing.saker.java.compiler.tests.tasks.javac.compatibility;

import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

public abstract class JavacCompatibilityTestCase extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	@Override
	protected JavacElementCompatibilityCollectingTestMetric createMetricImpl() {
		return new JavacElementCompatibilityCollectingTestMetric();
	}

	@Override
	protected JavacElementCompatibilityCollectingTestMetric getMetric() {
		return (JavacElementCompatibilityCollectingTestMetric) super.getMetric();
	}
}
