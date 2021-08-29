package testing.saker.java.compiler.tests.tasks.javac;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

/**
 * It was encountered once that removing the @Deprecated annotation from an interface field did not remove the warning
 * from the source files in which it was used.
 * <p>
 * It is caused by the deprecated tag being present in the javadoc, but not on the field. Therefore as there is no ABI
 * change, the dependent source files are not recompiled.
 */
@SakerTest
public class DeprecatedAnnotationRemovalTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	private static final SakerPath MAIN_JAVA_PATH = SRC_PATH_BASE.resolve("test/Main.java");
	private static final SakerPath ITF_JAVA_PATH = SRC_PATH_BASE.resolve("test/Itf.java");

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		runScriptTask("build");
		assertCompiled(MAIN_JAVA_PATH, ITF_JAVA_PATH);
		assertTrue(getMetric().getAllPrintedTaskLines().contains(
				"[saker.java.compile:src]src/test/Main.java:5:39-47: Warning: FIELD in test.Itf has been deprecated"));
		assertTrue(getMetric().getAllPrintedTaskLines().contains(
				"[saker.java.compile:src]src/test/Main.java:6:39-51: Warning: DOC_FIELD in test.Itf has been deprecated"));

		files.putFile(ITF_JAVA_PATH, files.getAllBytes(ITF_JAVA_PATH).toString().replace("@Deprecated", ""));
		runScriptTask("build");
		assertCompiled(MAIN_JAVA_PATH, ITF_JAVA_PATH);
		assertFalse(getMetric().getAllPrintedTaskLines().contains(
				"[saker.java.compile:src]src/test/Main.java:5:39-47: Warning: FIELD in test.Itf has been deprecated"));
		assertTrue(getMetric().getAllPrintedTaskLines().contains(
				"[saker.java.compile:src]src/test/Main.java:6:39-51: Warning: DOC_FIELD in test.Itf has been deprecated"));

		//TODO Fix this, issue #15 https://github.com/sakerbuild/saker.java.compiler/issues/15
//		files.putFile(ITF_JAVA_PATH, files.getAllBytes(ITF_JAVA_PATH).toString().replace("@deprecated doc_dep", ""));
//		runScriptTask("build");
//		assertCompiled(MAIN_JAVA_PATH, ITF_JAVA_PATH);
//		assertFalse(getMetric().getAllPrintedTaskLines().contains(
//				"[saker.java.compile:src]src/test/Main.java:5:39-47: Warning: FIELD in test.Itf has been deprecated"));
//		assertFalse(getMetric().getAllPrintedTaskLines().contains(
//				"[saker.java.compile:src]src/test/Main.java:6:39-51: Warning: DOC_FIELD in test.Itf has been deprecated"));
	}
}