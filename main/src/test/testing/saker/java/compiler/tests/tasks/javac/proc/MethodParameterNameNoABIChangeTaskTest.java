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
public class MethodParameterNameNoABIChangeTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	private static final SakerPath MAIN_JAVA_PATH = SRC_PATH_BASE.resolve("test/Main.java");
	private static final SakerPath SECOND_JAVA_PATH = SRC_PATH_BASE.resolve("test/Second.java");

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		runScriptTask("build");
		assertCompiled(MAIN_JAVA_PATH, SECOND_JAVA_PATH);
		assertReused();

		files.putFile(MAIN_JAVA_PATH,
				files.getAllBytes(MAIN_JAVA_PATH).toString().replace("String[] args", "String[] argsrenamed"));
		runScriptTask("build");
		assertCompiled(MAIN_JAVA_PATH);
		assertReused(SECOND_JAVA_PATH);

		files.putFile(MAIN_JAVA_PATH, files.getAllBytes(MAIN_JAVA_PATH).toString().replace("<PT ext", "<RUNTYPE ext"));
		runScriptTask("build");
		assertCompiled(MAIN_JAVA_PATH);
		assertReused(SECOND_JAVA_PATH);

		runScriptTask("procbuild");
		assertCompiled(MAIN_JAVA_PATH, SECOND_JAVA_PATH,
				PATH_WORKING_DIRECTORY.resolve("proc/test/ExtractProcessor.java"));
		assertReused();

		files.putFile(MAIN_JAVA_PATH, files.getAllBytes(MAIN_JAVA_PATH).toString().replace("String[] argsrenamed",
				"String[] argsrenamedagain"));
		runScriptTask("procbuild");
		assertCompiled(MAIN_JAVA_PATH, SECOND_JAVA_PATH);
		assertReused();

		files.putFile(MAIN_JAVA_PATH,
				files.getAllBytes(MAIN_JAVA_PATH).toString().replace("<RUNTYPE ext", "<RUNTYPEAGAIN ext"));
		runScriptTask("procbuild");
		assertCompiled(MAIN_JAVA_PATH, SECOND_JAVA_PATH);
		assertReused();
	}

}
