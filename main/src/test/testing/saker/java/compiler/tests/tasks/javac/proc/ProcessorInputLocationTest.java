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

import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class ProcessorInputLocationTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		// the processor itself will check for the valid behaviour
		runTest("");

		runTest("inc");
	}

	private void runTest(String passprefix) throws Throwable {
		String targetname = passprefix + "build";
		files.putFile(PATH_WORKING_DIRECTORY.resolve("workdirres.txt"), "wdres");

		System.out.println("---- ProcessorInputLocationTest.runTest() running for: " + targetname);

		runScriptTask(targetname);
		assertEquals(files.getAllBytes(getJavacBuildHeaderDirectory(passprefix + "src").resolve("file.txt")).toString(),
				"wdres");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf(passprefix + "proc", passprefix + "src"));

		runScriptTask(targetname);
		assertEquals(files.getAllBytes(getJavacBuildHeaderDirectory(passprefix + "src").resolve("file.txt")).toString(),
				"wdres");
		assertEmpty(getMetric().getRunTaskIdFactories());

		files.putFile(PATH_WORKING_DIRECTORY.resolve("workdirres.txt"), "wdmodified");
		runScriptTask(targetname);
		assertEquals(files.getAllBytes(getJavacBuildHeaderDirectory(passprefix + "src").resolve("file.txt")).toString(),
				"wdmodified");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf(passprefix + "src"));

		files.putFile(PATH_WORKING_DIRECTORY.resolve("workexistence.txt"), "x");
		runScriptTask(targetname);
		assertEquals(files.getAllBytes(getJavacBuildHeaderDirectory(passprefix + "src").resolve("file.txt")).toString(),
				"wdmodified");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf(passprefix + "src"));

		files.delete(PATH_WORKING_DIRECTORY.resolve("workexistence.txt"));
		runScriptTask(targetname);
		assertEquals(files.getAllBytes(getJavacBuildHeaderDirectory(passprefix + "src").resolve("file.txt")).toString(),
				"wdmodified");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf(passprefix + "src"));
	}

}
