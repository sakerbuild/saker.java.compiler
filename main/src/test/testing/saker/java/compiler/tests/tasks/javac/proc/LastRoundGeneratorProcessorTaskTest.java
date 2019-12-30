package testing.saker.java.compiler.tests.tasks.javac.proc;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class LastRoundGeneratorProcessorTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	private static String createLastRoundWarningMessage(String name) {
		return "File for type '" + name + "' created in the last round will not be subject to annotation processing.";
	}

	private static String createNoOriginatingElementsWarningMessage(String name) {
		return "No originating elements provided for: " + name;
	}

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		SakerPath addedpath = SRC_PATH_BASE.resolve("test/Added.java");

		SakerPath binbuilddir = getJavacBuildBinDirectory("src");
		SakerPath genbuilddir = getJavacBuildGeneratedSourceDirectory("src");

		runScriptTask("build");
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/Annot.class", "test/Main.class", "output",
				"output/MainImpl.class");
		assertDirectoryRecursiveContents(genbuilddir, "output", "output/MainImpl.java");
		assertEquals(getMetric().getReportedDiagnostics(), setOf(createLastRoundWarningMessage("output.MainImpl"),
				createNoOriginatingElementsWarningMessage("output.MainImpl")));

		files.putFile(addedpath, "package test; @Annot public class Added { }");
		runScriptTask("build");
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/Annot.class", "test/Main.class", "test/Added.class",
				"output", "output/AddedImpl.class");
		assertDirectoryRecursiveContents(genbuilddir, "output", "output/AddedImpl.java");
		assertEquals(getMetric().getReportedDiagnostics(), setOf(createLastRoundWarningMessage("output.AddedImpl"),
				createNoOriginatingElementsWarningMessage("output.AddedImpl")));

		//no change from before
		runScriptTask("build");
		assertEquals(getMetric().getRunTaskIdFactories().keySet(), setOf());

		//annotation removed
		files.putFile(addedpath, "package test; public class Added { }");
		runScriptTask("build");
		assertEquals(getMetric().getReportedDiagnostics(), setOf());
		assertEquals(getMetric().getInitializedProcessors(), setOf("test.LastRoundGeneratorProcessor"));
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/Annot.class", "test/Main.class",
				"test/Added.class");
		assertDirectoryRecursiveContents(genbuilddir);

		files.putFile(addedpath, "package test; public class Added { @Annot public static class Inner { } }");
		runScriptTask("build");
		assertEquals(getMetric().getReportedDiagnostics(), setOf(createLastRoundWarningMessage("output.InnerImpl"),
				createNoOriginatingElementsWarningMessage("output.InnerImpl")));
		assertEquals(getMetric().getInitializedProcessors(), setOf("test.LastRoundGeneratorProcessor"));
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/Annot.class", "test/Main.class", "test/Added.class",
				"test/Added$Inner.class", "output", "output/InnerImpl.class");
		assertDirectoryRecursiveContents(genbuilddir, "output", "output/InnerImpl.java");

		files.putFile(addedpath, "package test; public class Added { @Annot public static class Modified { } }");
		runScriptTask("build");
		assertEquals(getMetric().getReportedDiagnostics(), setOf(createLastRoundWarningMessage("output.ModifiedImpl"),
				createNoOriginatingElementsWarningMessage("output.ModifiedImpl")));
		assertEquals(getMetric().getInitializedProcessors(), setOf("test.LastRoundGeneratorProcessor"));
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/Annot.class", "test/Main.class", "test/Added.class",
				"test/Added$Modified.class", "output", "output/ModifiedImpl.class");
		assertDirectoryRecursiveContents(genbuilddir, "output", "output/ModifiedImpl.java");
	}

}
