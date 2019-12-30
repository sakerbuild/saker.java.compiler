package testing.saker.java.compiler.tests.tasks.javac.proc;

import java.util.Map;
import java.util.TreeMap;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.ObjectUtils;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class ProcessorErrorRemovalTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	//test for a bug 
//	Steps to reproduce:
//
//		Use a processor, compile successfully, reference the generated class.
//		Remove the processor from the compilation pass
//		Compile, it will fail
//		Remove the reference to generated class.
//		compile again, it should succeed.
//		compile again, and again, the compilation task will be reinvoked again and again.

	private boolean include;

	@Override
	protected Map<String, ?> getTaskVariables() {
		TreeMap<String, Object> result = ObjectUtils.newTreeMap(super.getTaskVariables());
		result.put("test.proc.include", include);
		return result;
	}

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		SakerPath srcpath = SRC_PATH_BASE.resolve("test/Main.java");

		SakerPath binbuilddir = getJavacBuildBinDirectory("src");
		SakerPath genbuilddir = getJavacBuildGeneratedSourceDirectory("src");

		include = true;

		runScriptTask("build");
		assertEquals(getMetric().getInitializedProcessors(), setOf("test.ExtractProcessor"));
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/ExtractInterface.class", "test/Main.class",
				"test/MainItf.class");
		assertDirectoryRecursiveContents(genbuilddir, "test", "test/MainItf.java");

		runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/ExtractInterface.class", "test/Main.class",
				"test/MainItf.class");
		assertDirectoryRecursiveContents(genbuilddir, "test", "test/MainItf.java");

		include = false;
		assertTaskException("saker.java.compiler.api.compile.exc.JavaCompilationFailedException",
				() -> runScriptTask("build"));

		files.putFile(srcpath,
				files.getAllBytes(srcpath).toString().replace("implements MainItf", "/*implements MainItf*/"));
		runScriptTask("build");
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/ExtractInterface.class", "test/Main.class");
		assertDirectoryRecursiveContents(genbuilddir);
		System.out.println("ProcessorErrorRemovalTaskTest.runJavacTestImpl() reused: " + getMetric().getReusedFiles());

		//THE BUG OCCURRS HERE:
		//the compilation task was reinvoked. test that it's not.
		//the delta was triggered:
		//    delta OUTPUT_CLASS [bd:/saker.java.compile/src/bin/test/MainItf.class]
		//    [saker.java.compile:src]Java class file was removed: bd:/saker.java.compile/src/bin/test/MainItf.class
		runScriptTask("build");
		System.out.println("ProcessorErrorRemovalTaskTest.runJavacTestImpl() " + getMetric().getRunTaskIdDeltas());
		assertEmpty(getMetric().getRunTaskIdFactories());
	}

}
