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
package testing.saker.java.compiler.tests.tasks.javac;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class ClassPathCompileTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	private static final SakerPath CP_PATH_BASE = PATH_WORKING_DIRECTORY.resolve("cp");

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		SakerPath fieldjavasrcpath = CP_PATH_BASE.resolve("test/Field.java");

		runScriptTask("build");
		assertCompiled(CP_PATH_BASE.resolve("test/Field.java"), SRC_PATH_BASE.resolve("test/Main.java"));
		assertReused();

		runScriptTask("build");
		assertEquals(getMetric().getRunTaskIdFactories().keySet(), setOf());

		files.putFile(fieldjavasrcpath,
				files.getAllBytes(fieldjavasrcpath).toString().replace("logging", "modifiedlogging"));
		runScriptTask("build");
		assertCompiled(CP_PATH_BASE.resolve("test/Field.java"));
	}

}
