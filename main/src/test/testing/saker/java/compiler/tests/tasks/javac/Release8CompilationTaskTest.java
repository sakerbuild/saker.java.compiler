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
import testing.saker.java.compiler.tests.JavaCompilerTestUtils;

@SakerTest
public class Release8CompilationTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	//note: the --release option requires the file manager to implement StandardJavaFileManager
	//see Arguments:328
	//But ONLY on java 9. This is fixed in javac on Java 10+

	private static final SakerPath ADDED_PATH = PATH_WORKING_DIRECTORY.resolve("src/test/Added.java");

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		runScriptTask("releasebuild");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("releasebuild"));
		JavaCompilerTestUtils.assertClassBytesMajorVersion(
				files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("saker.java.compile/releasebuild/bin/test/Main.class")),
				52);

		runScriptTask("releasebuild");
		assertEmpty(getMetric().getRunTaskIdFactories());

		files.putFile(ADDED_PATH, "package test; public class Added { }");
		runScriptTask("releasebuild");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("releasebuild"));

		files.delete(ADDED_PATH);

		runScriptTask("sourcetargetbuild");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("sourcetargetbuild"));
		JavaCompilerTestUtils.assertClassBytesMajorVersion(files.getAllBytes(
				PATH_BUILD_DIRECTORY.resolve("saker.java.compile/sourcetargetbuild/bin/test/Main.class")), 52);

		runScriptTask("sourcetargetbuild");
		assertEmpty(getMetric().getRunTaskIdFactories());

		files.putFile(ADDED_PATH, "package test; public class Added { }");
		runScriptTask("sourcetargetbuild");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("sourcetargetbuild"));

		files.delete(ADDED_PATH);

		runScriptTask("bothbuild");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("bothbuild"));
		JavaCompilerTestUtils.assertClassBytesMajorVersion(
				files.getAllBytes(PATH_BUILD_DIRECTORY.resolve("saker.java.compile/bothbuild/bin/test/Main.class")),
				52);

		runScriptTask("bothbuild");
		assertEmpty(getMetric().getRunTaskIdFactories());

		files.putFile(ADDED_PATH, "package test; public class Added { }");
		runScriptTask("bothbuild");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("bothbuild"));
	}

}
