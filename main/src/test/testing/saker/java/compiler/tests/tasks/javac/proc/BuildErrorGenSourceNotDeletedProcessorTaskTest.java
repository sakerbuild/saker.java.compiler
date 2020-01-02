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

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class BuildErrorGenSourceNotDeletedProcessorTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		SakerPath mainjavapath = PATH_WORKING_DIRECTORY.resolve("src/test/Main.java");

		runScriptTask("build");
		assertEquals(getMetric().getInitializedProcessors(), setOf("test.ResourceGenProcessor"));
		assertDirectoryRecursiveContents(getJavacBuildGeneratedSourceDirectory("main"), "test", "test/MainGen.java");
		assertDirectoryRecursiveContents(getJavacBuildBinDirectory("main"), "test", "test/MainGen.class",
				"test/Annot.class", "test/GenUser.class", "test/Main.class", "test/MainGen.ctxt");
		assertDirectoryRecursiveContents(getJavacBuildResourcesDirectory("main").resolve("RES_OUTPUT"), "test",
				"test/MainGen.txt");

		files.putFile(mainjavapath,
				files.getAllBytes(mainjavapath).toString().replace("test.MainGen", "test.MainGenModified"));
		assertTaskException("saker.java.compiler.api.compile.exc.JavaCompilationFailedException",
				() -> runScriptTask("build"));
		assertEquals(getMetric().getInitializedProcessors(), setOf("test.ResourceGenProcessor"));
		assertDirectoryRecursiveContents(getJavacBuildGeneratedSourceDirectory("main"), "test",
				"test/MainGenModified.java");
		assertDirectoryRecursiveContents(getJavacBuildResourcesDirectory("main").resolve("RES_OUTPUT"), "test",
				"test/MainGenModified.txt");

		files.putFile(mainjavapath,
				files.getAllBytes(mainjavapath).toString().replace("test.MainGenModified", "test.MainGen"));
		runScriptTask("build");
		assertEquals(getMetric().getInitializedProcessors(), setOf("test.ResourceGenProcessor"));
		assertDirectoryRecursiveContents(getJavacBuildGeneratedSourceDirectory("main"), "test", "test/MainGen.java");
		assertDirectoryRecursiveContents(getJavacBuildBinDirectory("main"), "test", "test/MainGen.class",
				"test/Annot.class", "test/GenUser.class", "test/Main.class", "test/MainGen.ctxt");
		assertDirectoryRecursiveContents(getJavacBuildResourcesDirectory("main").resolve("RES_OUTPUT"), "test",
				"test/MainGen.txt");
	}

}
