package testing.saker.java.compiler.tests.jdk9.tasks.javac;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class SimpleModulePathTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		SakerPath firstmoduleinfo = PATH_WORKING_DIRECTORY.resolve("first/module-info.java");
		SakerPath secondmoduleinfo = PATH_WORKING_DIRECTORY.resolve("second/module-info.java");
		SakerPath firstclass = PATH_WORKING_DIRECTORY.resolve("first/firstpkg/FirstClass.java");
		SakerPath secondclass = PATH_WORKING_DIRECTORY.resolve("second/secondpkg/SecondClass.java");

		runScriptTask("build");
		assertCompiled(firstmoduleinfo, secondmoduleinfo, firstclass, secondclass);

		runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());

		//assert that the second hasn't been recompiled, as the module info hasn't changed semantically
		files.putFile(firstmoduleinfo, files.getAllBytes(firstmoduleinfo).toString().replace("\n", ""));
		runScriptTask("build");
		assertCompiled(firstmoduleinfo);

		//remove exports directive
		files.putFile(firstmoduleinfo, "module modle.first { }");
		assertTaskException("saker.java.compiler.api.compile.exc.JavaCompilationFailedException",
				() -> runScriptTask("build"));

		//remove reference to the first pkg
		files.putFile(secondclass,
				files.getAllBytes(secondclass).toString().replace("private firstpkg.FirstClass fc;", ""));
		runScriptTask("build");
	}

}
