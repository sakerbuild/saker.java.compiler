package testing.saker.java.compiler.tests.tasks.javac.compatibility;

import testing.saker.SakerTest;

@SakerTest
public class VariousJavacCompatibilityTaskTest extends JavacCompatibilityTestCase {
	//TODO create compatibility checks for testing the Types interface
	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		runScriptTask("build");
	}
}
