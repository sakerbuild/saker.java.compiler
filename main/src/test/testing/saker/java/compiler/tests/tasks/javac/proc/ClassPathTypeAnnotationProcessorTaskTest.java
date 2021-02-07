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

import testing.saker.SakerTest;
import testing.saker.java.compiler.CompilerCollectingTestMetric;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class ClassPathTypeAnnotationProcessorTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	private boolean forceExternal = false;

	@Override
	protected CompilerCollectingTestMetric createMetricImpl() {
		CompilerCollectingTestMetric result = super.createMetricImpl();
		result.setForceExternalCompilation(forceExternal);
		return result;
	}

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		forceExternal = false;

		runScriptTask("build");
		assertTrue(getMetric().getInitializedProcessors().contains("test.MessagerProcessor"));

		forceExternal = true;
		runScriptTask("build2");
		assertTrue(getMetric().getInitializedProcessors().contains("test.MessagerProcessor"));
	}

}
