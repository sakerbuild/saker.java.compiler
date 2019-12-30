package testing.saker.java.compiler.tests.tasks.javac;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class PackageAnnotChangeTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	//checks that a package annotation change causes the recompilation of referencing files
	//it is required, as the compiler may issue new warnings for it
	//note: currently due to a limitation of the ABI usage tracking, the sources in subpackages are rebuilt too

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		SakerPath packinfopath = PATH_WORKING_DIRECTORY.resolve("src/test/package-info.java");

		runScriptTask("build");

		files.putFile(packinfopath, "@Deprecated package test;");
		runScriptTask("build");
		assertEquals(getMetric().getCompiledFiles(),
				setOf(packinfopath, PATH_WORKING_DIRECTORY.resolve("src/test/Main.java"),
						PATH_WORKING_DIRECTORY.resolve("src/test/sub/Sub.java"),
						PATH_WORKING_DIRECTORY.resolve("src/other/Other.java"),
						PATH_WORKING_DIRECTORY.resolve("src/other/WcImport.java")));
	}

}
