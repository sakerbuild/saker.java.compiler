package testing.saker.java.compiler.tests.jdk9.tasks.javac;

import testing.saker.SakerTest;
import testing.saker.java.compiler.tests.tasks.javac.compatibility.JavacCompatibilityTestCase;

@SakerTest
public class Java9CompatibilityTaskTest extends JavacCompatibilityTestCase {
	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		runScriptTask("build");
	}
}
