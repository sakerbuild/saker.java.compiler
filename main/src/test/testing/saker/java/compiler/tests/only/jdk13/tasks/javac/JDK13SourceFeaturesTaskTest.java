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
package testing.saker.java.compiler.tests.only.jdk13.tasks.javac;

import saker.build.util.java.JavaTools;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class JDK13SourceFeaturesTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		assertEquals(JavaTools.getCurrentJavaMajorVersion(), 13);

		runScriptTask("build");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("src"));
	}
}
