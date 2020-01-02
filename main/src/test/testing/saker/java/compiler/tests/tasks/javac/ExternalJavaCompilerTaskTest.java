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
import testing.saker.java.compiler.CompilerCollectingTestMetric;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class ExternalJavaCompilerTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	private static final SakerPath MAIN_JAVA_PATH = SRC_PATH_BASE.resolve("test/Main.java");
	private static final SakerPath SECOND_JAVA_PATH = SRC_PATH_BASE.resolve("test/Second.java");

	@Override
	protected CompilerCollectingTestMetric createMetricImpl() {
		CompilerCollectingTestMetric result = super.createMetricImpl();
		result.setForceExternalCompilation(true);
		return result;
	}

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		runScriptTask("build");
		assertCompiled(MAIN_JAVA_PATH);
		assertReused();
		assertTrue(getMetric().isHadExternallyCompiled());

		runScriptTask("build");
		assertCompiled();
		assertReused();
		assertEquals(getMetric().getRunTaskIdFactories().keySet(), setOf());

		files.putFile(SECOND_JAVA_PATH, "package test; public class Second { }".getBytes());
		runScriptTask("build");
		assertCompiled(SECOND_JAVA_PATH);
		assertReused(MAIN_JAVA_PATH);
		assertTrue(getMetric().isHadExternallyCompiled());

		files.touch(MAIN_JAVA_PATH);
		runScriptTask("build");
		assertCompiled(MAIN_JAVA_PATH);
		assertReused(SECOND_JAVA_PATH);
	}

}
