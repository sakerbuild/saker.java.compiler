package testing.saker.java.compiler.tests.tasks.javac.proc;

import java.nio.file.NoSuchFileException;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class PackageDocCommentProcessorTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		SakerPath outtxtpath = getJavacBuildGeneratedSourceDirectory("src").resolve("out.txt");
		SakerPath packageinfopath = PATH_WORKING_DIRECTORY.resolve("src/test/package-info.java");

		runScriptTask("build");
		assertEquals(getMetric().getInitializedProcessors(), setOf("proc.PackageDocProcessor"));
		//trim to avoid non equality due to whitespace
		assertEquals(files.getAllBytes(outtxtpath).toString().trim(), "Doc");

		files.putFile(packageinfopath, files.getAllBytes(packageinfopath).toString().replace("Doc", "Doc2"));
		runScriptTask("build");
		assertEquals(getMetric().getInitializedProcessors(), setOf("proc.PackageDocProcessor"));
		assertEquals(files.getAllBytes(outtxtpath).toString().trim(), "Doc2");

		//remove comment
		files.putFile(packageinfopath, files.getAllBytes(packageinfopath).toString().replace("/**Doc2*/", "//nodoc"));
		runScriptTask("build");
		assertEquals(getMetric().getInitializedProcessors(), setOf("proc.PackageDocProcessor"));
		assertException(NoSuchFileException.class, () -> files.getAllBytes(outtxtpath));
		
		//re-add comment
		files.putFile(packageinfopath, files.getAllBytes(packageinfopath).toString().replace("//nodoc", "/**Doc3*/"));
		runScriptTask("build");
		assertEquals(getMetric().getInitializedProcessors(), setOf("proc.PackageDocProcessor"));
		assertEquals(files.getAllBytes(outtxtpath).toString().trim(), "Doc3");
	}

}
