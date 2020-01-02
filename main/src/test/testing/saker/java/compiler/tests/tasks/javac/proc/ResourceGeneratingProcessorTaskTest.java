/*
 * Copyright (C) 2020 Bence Sipka
 *
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package testing.saker.java.compiler.tests.tasks.javac.proc;

import java.util.Map;

import javax.tools.JavaFileManager.Location;
import javax.tools.StandardLocation;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.build.tests.TestUtils;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class ResourceGeneratingProcessorTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	private String locationName;

	private static final SakerPath binbuilddir = getJavacBuildBinDirectory("src");
	private static final SakerPath resbuilddir = getJavacBuildResourcesDirectory("src");
	private static final SakerPath genbuilddir = getJavacBuildGeneratedSourceDirectory("src");
	private static final SakerPath nativehbuilddir = getJavacBuildHeaderDirectory("src");

	@Override
	protected Map<String, ?> getTaskVariables() {
		return TestUtils.<String, Object>treeMapBuilder().put("test.LocationName", locationName).build();
	}

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		locationName = StandardLocation.CLASS_OUTPUT.getName();
		files.clearDirectoryRecursively(PATH_BUILD_DIRECTORY);

		runScriptTask("build");
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/Main.class", "test/test.Main.txt");
		assertEquals(getMetric().getInitializedProcessors(), setOf("test.ResourceGenProcessor"));
		assertCompiled(SRC_PATH_BASE.resolve("test/Main.java"),
				PATH_WORKING_DIRECTORY.resolve("proc").resolve("test/ResourceGenProcessor.java"));

		runScriptTask("build");
		assertEquals(getMetric().getRunTaskIdFactories().keySet(), setOf());

		SakerPath addpath = SRC_PATH_BASE.resolve("test/Added.java");
		files.putFile(addpath, "package test; public class Added { }");
		runScriptTask("build");
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/Main.class", "test/test.Main.txt",
				"test/Added.class", "test/test.Added.txt");
		assertEquals(getMetric().getInitializedProcessors(), setOf("test.ResourceGenProcessor"));
		assertCompiled(addpath);

		files.delete(addpath);
		runScriptTask("build");
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/Main.class", "test/test.Main.txt");
		assertEquals(getMetric().getInitializedProcessors(), setOf());
		assertCompiled();

		runWithLocation(StandardLocation.SOURCE_OUTPUT, genbuilddir);
		runWithLocation(StandardLocation.locationFor("TEST_OUTPUT"), resbuilddir.resolve("TEST_OUTPUT"));
		runWithLocation(StandardLocation.NATIVE_HEADER_OUTPUT, nativehbuilddir);
	}

	private void runWithLocation(Location location, SakerPath checkdir) throws Throwable {
		System.out.println("ResourceGeneratingProcessorTaskTest.runWithLocation() " + location);
		System.out.println();

		locationName = location.getName();
		files.clearDirectoryRecursively(PATH_BUILD_DIRECTORY);

		runScriptTask("build");
		assertDirectoryRecursiveContents(checkdir, "test", "test/test.Main.txt");
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/Main.class");
		assertEquals(getMetric().getInitializedProcessors(), setOf("test.ResourceGenProcessor"));
		assertCompiled(SRC_PATH_BASE.resolve("test/Main.java"),
				PATH_WORKING_DIRECTORY.resolve("proc").resolve("test/ResourceGenProcessor.java"));

		runScriptTask("build");
		assertEquals(getMetric().getRunTaskIdFactories().keySet(), setOf());

		SakerPath addpath = SRC_PATH_BASE.resolve("test/Added.java");
		files.putFile(addpath, "package test; public class Added { }");
		runScriptTask("build");
		assertDirectoryRecursiveContents(checkdir, "test", "test/test.Main.txt", "test/test.Added.txt");
		assertEquals(getMetric().getInitializedProcessors(), setOf("test.ResourceGenProcessor"));
		assertCompiled(addpath);

		files.delete(addpath);
		runScriptTask("build");
		assertDirectoryRecursiveContents(checkdir, "test", "test/test.Main.txt");
		assertEquals(getMetric().getInitializedProcessors(), setOf());
		assertCompiled();

		files.delete(checkdir.resolve("test/test.Main.txt"));
		runScriptTask("build");
		assertDirectoryRecursiveContents(checkdir, "test", "test/test.Main.txt");
		assertEquals(getMetric().getInitializedProcessors(), setOf("test.ResourceGenProcessor"));
		assertCompiled();
	}

}
