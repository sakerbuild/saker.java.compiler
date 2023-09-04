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

import java.util.TreeSet;

import saker.build.thirdparty.saker.util.ObjectUtils;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class ExhaustiveMessageSourcePositionsProcessorTaskTest
		extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		//there might be differences based on different java versions
		TreeSet<String> expectedlines = ObjectUtils.newTreeSet(
				files.getAllBytes(PATH_WORKING_DIRECTORY.resolve("expected_lines")).toString().split("[\r\n]+"));
		TreeSet<String> expectedlines2 = ObjectUtils.newTreeSet(
				files.getAllBytes(PATH_WORKING_DIRECTORY.resolve("expected_lines2")).toString().split("[\r\n]+"));

		runScriptTask("build");
		assertTrue(getMetric().getAllPrintedTaskLines().equals(expectedlines)
				|| getMetric().getAllPrintedTaskLines().equals(expectedlines2));

		runScriptTask("build");
		assertTrue(getMetric().getAllPrintedTaskLines().equals(expectedlines)
				|| getMetric().getAllPrintedTaskLines().equals(expectedlines2));
	}
}
