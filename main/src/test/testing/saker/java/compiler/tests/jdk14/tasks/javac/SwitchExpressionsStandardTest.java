package testing.saker.java.compiler.tests.jdk14.tasks.javac;

import testing.saker.SakerTest;
import testing.saker.java.compiler.tests.tasks.javac.compatibility.JavacCompatibilityTestCase;

@SakerTest
public class SwitchExpressionsStandardTest extends JavacCompatibilityTestCase {
	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		runScriptTask("build");
	}
}
