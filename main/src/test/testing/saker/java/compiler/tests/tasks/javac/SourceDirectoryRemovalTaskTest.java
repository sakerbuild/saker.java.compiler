package testing.saker.java.compiler.tests.tasks.javac;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

/**
 * Tests that if a source directory is removed then the affected ABI changes trigger recompilation of dependent files.
 */
@SakerTest
public class SourceDirectoryRemovalTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		SakerPath buildfilepath = PATH_WORKING_DIRECTORY.resolve("saker.build");
		runScriptTask("build");
		assertDirectoryRecursiveContents(getJavacBuildBinDirectory("main"), "test", "test/Main.class", "test2",
				"test2/Main2.class", "test3", "test3/Main3.class");

		files.putFile(buildfilepath, files.getAllBytes(buildfilepath).toString().replace("src3,", "#src3,"));
		runScriptTask("build");
		assertDirectoryRecursiveContents(getJavacBuildBinDirectory("main"), "test", "test/Main.class", "test2",
				"test2/Main2.class");

		files.clearDirectoryRecursively(getJavacBuildBinDirectory("main"));
		runScriptTask("build");
		assertDirectoryRecursiveContents(getJavacBuildBinDirectory("main"), "test", "test/Main.class", "test2",
				"test2/Main2.class");

		files.putFile(buildfilepath, files.getAllBytes(buildfilepath).toString().replace("src,", "#src,"));
		assertTaskException("saker.java.compiler.api.compile.exc.JavaCompilationFailedException",
				() -> runScriptTask("build"));

		files.clearDirectoryRecursively(getJavacBuildBinDirectory("main"));
		assertTaskException("saker.java.compiler.api.compile.exc.JavaCompilationFailedException",
				() -> runScriptTask("build"));

		//the following tests some NPE when a source directory was removed, rebuilt, and class files removed
		
		//put back
		files.putFile(buildfilepath, files.getAllBytes(buildfilepath).toString().replace("#src,", "src,"));
		runScriptTask("build");
		assertDirectoryRecursiveContents(getJavacBuildBinDirectory("main"), "test", "test/Main.class", "test2",
				"test2/Main2.class");

		//remove src2
		files.putFile(buildfilepath, files.getAllBytes(buildfilepath).toString().replace("src2,", "#src2,"));
		runScriptTask("build");
		assertDirectoryRecursiveContents(getJavacBuildBinDirectory("main"), "test", "test/Main.class");

		//clear class files, and rebuild
		files.clearDirectoryRecursively(getJavacBuildBinDirectory("main"));
		runScriptTask("build");
		assertDirectoryRecursiveContents(getJavacBuildBinDirectory("main"), "test", "test/Main.class");
	}

}
