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
public class OriginatingFileDoesntExistProcessorTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	//bug test encountered when working with command line processor 

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		SakerPath npath = PATH_WORKING_DIRECTORY.resolve("src/test/SuperParams.java");
		SakerPath subpath = PATH_WORKING_DIRECTORY.resolve("src/test/SubParams.java");

		runScriptTask("build");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("src"));

		files.moveFile(PATH_WORKING_DIRECTORY.resolve("src/test/ModifiedSuperParams.java"), npath);
		files.putFile(npath, files.getAllBytes(npath).toString().replace("Modified", ""));
		files.putFile(subpath, files.getAllBytes(subpath).toString().replace("Modified", ""));

		runScriptTask("build");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("src"));
	}

}
