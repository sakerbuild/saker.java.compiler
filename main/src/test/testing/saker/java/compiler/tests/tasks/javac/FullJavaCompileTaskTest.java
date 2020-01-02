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
public class FullJavaCompileTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	private static final SakerPath MAIN_JAVA_PATH = SRC_PATH_BASE.resolve("test/Main.java");
	private static final SakerPath SECOND_JAVA_PATH = SRC_PATH_BASE.resolve("test/Second.java");

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		SakerPath binbuilddir = getJavacBuildBinDirectory("src");

		runScriptTask("build");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("src"));
		assertCompiled(MAIN_JAVA_PATH);
		assertReused();
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/Main.class");

		runScriptTask("build");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf());
		assertCompiled();
		assertReused();
		assertEmpty(getMetric().getRunTaskIdFactories());
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/Main.class");

		files.putFile(SECOND_JAVA_PATH, "package test; public class Second { }".getBytes());
		runScriptTask("build");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("src"));
		assertCompiled(MAIN_JAVA_PATH, SECOND_JAVA_PATH);
		assertReused();
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/Main.class", "test/Second.class");

		files.touch(MAIN_JAVA_PATH);
		runScriptTask("build");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("src"));
		assertCompiled(SECOND_JAVA_PATH, MAIN_JAVA_PATH);
		assertReused();
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/Main.class", "test/Second.class");

		files.delete(binbuilddir.resolve("test/Main.class"));
		runScriptTask("build");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("src"));
		assertCompiled(SECOND_JAVA_PATH, MAIN_JAVA_PATH);
		assertReused();
		assertDirectoryRecursiveContents(binbuilddir, "test", "test/Main.class", "test/Second.class");
	}

}
