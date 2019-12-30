package testing.saker.java.compiler.tests.tasks.javac.proc;

import java.util.Locale;
import java.util.Map;

import javax.tools.JavaFileManager.Location;
import javax.tools.StandardLocation;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.build.tests.TestUtils;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class OutputChangeProcessorTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	private String locationName;
	private SakerPath testMainResPath;

	@Override
	protected Map<String, ?> getTaskVariables() {
		return TestUtils.<String, Object>treeMapBuilder().put("test.LocationName", locationName).build();
	}

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		runWithLocation(StandardLocation.locationFor("RES_OUTPUT"));
		runWithLocation(StandardLocation.CLASS_OUTPUT);
	}

	private void runWithLocation(Location location) throws Throwable {
		SakerPath addjavapath = PATH_WORKING_DIRECTORY.resolve("src/test/Add.java");

		setLocationName(location);
		runScriptTask("build");
		assertEquals(getMetric().getInitializedProcessors(), setOf("test.ResourceGenProcessor"));
		assertEquals(files.getAllBytes(testMainResPath).toString(), "test.Main");

		files.delete(testMainResPath);
		runScriptTask("build");
		assertEquals(getMetric().getInitializedProcessors(), setOf("test.ResourceGenProcessor"));
		assertEquals(files.getAllBytes(testMainResPath).toString(), "test.Main");

		files.putFile(addjavapath, "package test; public class Add {}");
		runScriptTask("build");
		assertEquals(getMetric().getInitializedProcessors(), setOf("test.ResourceGenProcessor"));
		assertEquals(files.getAllBytes(testMainResPath).toString(), "test.Main");

		//remove for next calls
		files.delete(addjavapath);
	}

	private void setLocationName(Location location) {
		locationName = location.getName();
		String passidentifier = ("pass" + locationName).toLowerCase(Locale.ENGLISH);
		if (location == StandardLocation.CLASS_OUTPUT) {
			testMainResPath = getJavacBuildBinDirectory(passidentifier).resolve("test/Main.txt");
		} else {
			testMainResPath = getJavacBuildResourcesDirectory(passidentifier).resolve(locationName)
					.resolve("test/Main.txt");
		}
	}

}
