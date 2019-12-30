package testing.saker.java.compiler.tests.tasks.javac;

import saker.build.thirdparty.saker.util.ReflectUtils;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;
import testing.saker.java.compiler.tests.tasks.javac.util.SimpleClass;

@SakerTest
public class DirectoryClassPathTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		runScriptTask("build");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("second"));

		runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());

		files.putFile(
				PATH_WORKING_DIRECTORY
						.resolve("firstbin/" + SimpleClass.class.getName().replace('.', '/') + ".class"),
				ReflectUtils.getClassBytesUsingClassLoader(SimpleClass.class));
		runScriptTask("build");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("second"));
	}

}
