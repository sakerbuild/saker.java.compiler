package testing.saker.java.compiler.tests.jdk9.tasks.javac;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class TransitiveModulePathTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		SakerPath firstmoduleinfo = PATH_WORKING_DIRECTORY.resolve("first/module-info.java");
		SakerPath secondmoduleinfo = PATH_WORKING_DIRECTORY.resolve("second/module-info.java");
		SakerPath thirdmoduleinfo = PATH_WORKING_DIRECTORY.resolve("third/module-info.java");
		SakerPath firstclass = PATH_WORKING_DIRECTORY.resolve("first/firstpkg/FirstClass.java");
		SakerPath secondclass = PATH_WORKING_DIRECTORY.resolve("second/secondpkg/SecondClass.java");
		SakerPath thirdclass = PATH_WORKING_DIRECTORY.resolve("third/thirdpkg/ThirdClass.java");

		assertTaskException("saker.java.compiler.api.compile.exc.JavaCompilationFailedException",
				() -> runScriptTask("build"));
		assertCompiled(firstmoduleinfo, secondmoduleinfo, thirdmoduleinfo, firstclass, secondclass,
				thirdclass);

		files.putFile(secondmoduleinfo, files.getAllBytes(secondmoduleinfo).toString().replace("requires modle.first;",
				"requires transitive modle.first;"));
		runScriptTask("build");
		assertCompiled(secondmoduleinfo, thirdmoduleinfo, secondclass, thirdclass);
	}

}
