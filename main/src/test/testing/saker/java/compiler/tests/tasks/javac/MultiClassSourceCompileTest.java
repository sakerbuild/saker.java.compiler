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
public class MultiClassSourceCompileTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	private static final SakerPath MAIN_JAVA_PATH = SRC_PATH_BASE.resolve("test/Main.java");
	private static final SakerPath USER_JAVA_PATH = SRC_PATH_BASE.resolve("test/User.java");
	private static final SakerPath LANGINTUSER_JAVA_PATH = SRC_PATH_BASE.resolve("test/LangIntUser.java");

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		runScriptTask("build");
		assertCompiled(MAIN_JAVA_PATH, USER_JAVA_PATH, LANGINTUSER_JAVA_PATH);

		//modify some method implementation
		files.putFile(MAIN_JAVA_PATH, files.getAllBytes(MAIN_JAVA_PATH).toString().replace("System.out", "System.err"));
		runScriptTask("build");
		assertCompiled(MAIN_JAVA_PATH);

		//addition of a package level file should result in recompilation of User, as User uses Integer directly
		//but not of LangIntUser as it uses the java.lang.Integer by fully qualifying it
		files.putFile(MAIN_JAVA_PATH, files.getAllBytes(MAIN_JAVA_PATH).toString().replace("//add-class-here",
				"class Integer {Integer(int i) { }} //add-class-here"));
		runScriptTask("build");
		assertCompiled(MAIN_JAVA_PATH, USER_JAVA_PATH);

		files.putFile(MAIN_JAVA_PATH, files.getAllBytes(MAIN_JAVA_PATH).toString().replace("//add-class-here",
				"class MoreAdded {} //add-class-here"));
		runScriptTask("build");
		assertCompiled(MAIN_JAVA_PATH);
	}

}
