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
public class PackageAnnotationBugTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		SakerPath packinfopath = SRC_PATH_BASE.resolve("test/package-info.java");
		SakerPath subpackinfopath = SRC_PATH_BASE.resolve("test/sub/package-info.java");

		runScriptTask("build");
		assertEquals(getMetric().getReportedDiagnostics(),
				setOf("Warning on test.GroupBy", "Warning on test.Main", "Warning on test"));

		files.putFile(packinfopath, files.getAllBytes(packinfopath).toString().replace("@test.GroupBy", ""));
		runScriptTask("build");
		assertEquals(getMetric().getReportedDiagnostics(), setOf());

		files.putFile(subpackinfopath,
				files.getAllBytes(subpackinfopath).toString().replace("//annot", "@test.GroupBy"));
		runScriptTask("build");
		assertEquals(getMetric().getReportedDiagnostics(),
				setOf("Warning on test.sub.SubPackClass", "Warning on test.sub"));
	}

}
