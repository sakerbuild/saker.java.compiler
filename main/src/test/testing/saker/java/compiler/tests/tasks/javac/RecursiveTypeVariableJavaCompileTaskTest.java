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
public class RecursiveTypeVariableJavaCompileTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		SakerPath mainjavapath = SRC_PATH_BASE.resolve("test/Main.java");
		SakerPath callerjavapath = SRC_PATH_BASE.resolve("test/Caller.java");

		runScriptTask("build");
		assertEquals(getMetric().getCompiledFiles(), setOf(mainjavapath, callerjavapath));

		files.putFile(mainjavapath, files.getAllBytes(mainjavapath).toString().replace("//replace",
				"//replace\npublic static <T extends E, E extends List<T>> void function(double d) {}"));
		runScriptTask("build");
		assertEquals(getMetric().getCompiledFiles(), setOf(mainjavapath, callerjavapath));
	}

}
