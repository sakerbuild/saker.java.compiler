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
public class WildcardImportCompileTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	private static final SakerPath INTEGER_JAVA_PATH = SRC_PATH_BASE.resolve("test/Integer.java");
	private static final SakerPath MAIN_JAVA_PATH = SRC_PATH_BASE.resolve("test/Main.java");
	private static final SakerPath SECOND_JAVA_PATH = SRC_PATH_BASE.resolve("test/Second.java");

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		runScriptTask("build");
		assertCompiled(MAIN_JAVA_PATH, SECOND_JAVA_PATH);
		assertReused();

		runScriptTask("build");
		assertCompiled();
		assertReused();
		assertEmpty(getMetric().getRunTaskIdFactories());

		files.putFile(INTEGER_JAVA_PATH, "package test; public class Integer { }");
		runScriptTask("build");
		assertCompiled(INTEGER_JAVA_PATH, SECOND_JAVA_PATH);
		assertReused(MAIN_JAVA_PATH);

		files.putFile(MAIN_JAVA_PATH,
				files.getAllBytes(MAIN_JAVA_PATH).toString().replace("//add", "//add\npublic static class Short{}"));
		//compilation fails, as the short is ambiguous reference
		assertTaskException("saker.java.compiler.api.compile.exc.JavaCompilationFailedException",
				() -> runScriptTask("build"));

		files.putFile(MAIN_JAVA_PATH,
				files.getAllBytes(MAIN_JAVA_PATH).toString().replace("public static class Short{}", ""));
		runScriptTask("build");
		assertCompiled(MAIN_JAVA_PATH);
		assertReused(INTEGER_JAVA_PATH, SECOND_JAVA_PATH);

		files.putFile(MAIN_JAVA_PATH, files.getAllBytes(MAIN_JAVA_PATH).toString().replace("//add",
				"//add\npublic static void main(int i){}"));
		runScriptTask("build");
		assertCompiled(MAIN_JAVA_PATH, SECOND_JAVA_PATH);
		assertReused(INTEGER_JAVA_PATH);

		files.putFile(MAIN_JAVA_PATH,
				files.getAllBytes(MAIN_JAVA_PATH).toString().replace("//add", "//add\npublic static Object s;"));
		runScriptTask("build");
		assertCompiled(MAIN_JAVA_PATH, SECOND_JAVA_PATH);
		assertReused(INTEGER_JAVA_PATH);
	}
}
