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
public class DeadCodeCompileTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	private static final SakerPath MAIN_JAVA_PATH = SRC_PATH_BASE.resolve("test/Main.java");
	private static final SakerPath USER_JAVA_PATH = SRC_PATH_BASE.resolve("test/User.java");
	private static final SakerPath CONST_JAVA_PATH = SRC_PATH_BASE.resolve("test/Const.java");

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		runScriptTask("build");
		assertCompiled(MAIN_JAVA_PATH, USER_JAVA_PATH, CONST_JAVA_PATH);

		//no-op
		runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdResults());

		//check that the user code is recompiled as well, as the dead code references the changed function
		files.putFile(MAIN_JAVA_PATH, files.getAllBytes(MAIN_JAVA_PATH).toString().replace("String[]", "String..."));
		runScriptTask("build");
		assertCompiled(MAIN_JAVA_PATH, USER_JAVA_PATH);

		files.putFile(CONST_JAVA_PATH, files.getAllBytes(CONST_JAVA_PATH).toString().replace("true", "false"));
		runScriptTask("build");
		assertCompiled(CONST_JAVA_PATH, USER_JAVA_PATH);
	}

}
