package testing.saker.java.compiler.tests.only.jdk15.tasks.javac;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class Java15IncrementalScenarioTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		SakerPath shapepath = SRC_PATH_BASE.resolve("test/Shape.java");
		SakerPath circleuserpath = SRC_PATH_BASE.resolve("test/CircleUser.java");
		SakerPath fruitpath = SRC_PATH_BASE.resolve("test/Fruit.java");
		SakerPath applepath = SRC_PATH_BASE.resolve("test/Apple.java");
		SakerPath pearpath = SRC_PATH_BASE.resolve("test/Pear.java");
		SakerPath grapepath = SRC_PATH_BASE.resolve("test/Grape.java");
		SakerPath implicitpermitpath = SRC_PATH_BASE.resolve("test/ImplicitPermit.java");
		SakerPath implicituserpath = SRC_PATH_BASE.resolve("test/ImplicitUser.java");

		runScriptTask("build");
		assertEquals(getMetric().getCompiledFiles(), setOf(shapepath, circleuserpath, fruitpath, applepath, pearpath,
				implicitpermitpath, implicituserpath, grapepath));

		runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());

		files.putFile(shapepath, files.getAllBytes(shapepath).toString().replace("Circle extends Shape", "Circle")
				.replace("permits Circle, ", "permits "));
		runScriptTask("build");
		assertEquals(getMetric().getCompiledFiles(), setOf(shapepath, circleuserpath));

		files.putFile(fruitpath, files.getAllBytes(fruitpath).toString().replace("permits Apple, ", "permits "));
		assertTaskException("saker.java.compiler.api.compile.exc.JavaCompilationFailedException",
				() -> runScriptTask("build"));
		assertEquals(getMetric().getCompiledFiles(), setOf(fruitpath, applepath, pearpath, grapepath));

		files.putFile(applepath, files.getAllBytes(applepath).toString().replace("extends Fruit", ""));
		runScriptTask("build");
		assertEquals(getMetric().getCompiledFiles(), setOf(fruitpath, applepath, pearpath, grapepath));

		//changing a permitted class should cause the recompilation of the parent, so the error can surface
		files.putFile(pearpath, files.getAllBytes(pearpath).toString().replace("extends Fruit", ""));
		assertTaskException("saker.java.compiler.api.compile.exc.JavaCompilationFailedException",
				() -> runScriptTask("build"));
		assertEquals(getMetric().getCompiledFiles(), setOf(fruitpath, pearpath, grapepath));
		
		files.putFile(fruitpath, files.getAllBytes(fruitpath).toString().replace("permits Pear, ", "permits "));
		runScriptTask("build");
		assertEquals(getMetric().getCompiledFiles(), setOf(fruitpath, pearpath, grapepath));

		//this causes a modification in the implicit permits clause of ImplicitPermit
		//even if a class doesn't use ImplicitSub1, an change should be triggered due to the permits clause change
		files.putFile(implicitpermitpath, files.getAllBytes(implicitpermitpath).toString()
				.replace("ImplicitSub1 extends ImplicitPermit", "ImplicitSub1"));
		runScriptTask("build");
		assertEquals(getMetric().getCompiledFiles(), setOf(implicitpermitpath, implicituserpath));
	}

}
