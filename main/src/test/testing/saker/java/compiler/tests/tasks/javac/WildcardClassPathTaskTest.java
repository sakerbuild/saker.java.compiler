package testing.saker.java.compiler.tests.tasks.javac;

import saker.build.thirdparty.saker.util.ReflectUtils;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;
import testing.saker.java.compiler.tests.tasks.javac.util.SimpleClass;
import testing.saker.java.compiler.tests.tasks.javac.util.SimpleSecondClass;

@SakerTest
public class WildcardClassPathTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		files.putFile(
				PATH_WORKING_DIRECTORY.resolve("cp1").resolve(SimpleClass.class.getName().replace('.', '/') + ".class"),
				ReflectUtils.getClassBytesUsingClassLoader(SimpleClass.class));
		files.putFile(
				PATH_WORKING_DIRECTORY.resolve("cp2")
						.resolve(SimpleSecondClass.class.getName().replace('.', '/') + ".class"),
				ReflectUtils.getClassBytesUsingClassLoader(SimpleSecondClass.class));

		runScriptTask("build");
	}

}
