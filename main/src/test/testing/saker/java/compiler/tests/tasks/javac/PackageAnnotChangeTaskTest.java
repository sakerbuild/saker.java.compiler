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
public class PackageAnnotChangeTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	//checks that a package annotation change causes the recompilation of referencing files
	//it is required, as the compiler may issue new warnings for it
	//note: currently due to a limitation of the ABI usage tracking, the sources in subpackages are rebuilt too

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		SakerPath packinfopath = PATH_WORKING_DIRECTORY.resolve("src/test/package-info.java");

		runScriptTask("build");

		files.putFile(packinfopath, "@Deprecated package test;");
		runScriptTask("build");
		assertEquals(getMetric().getCompiledFiles(),
				setOf(packinfopath, PATH_WORKING_DIRECTORY.resolve("src/test/Main.java"),
						PATH_WORKING_DIRECTORY.resolve("src/test/sub/Sub.java"),
						PATH_WORKING_DIRECTORY.resolve("src/other/Other.java"),
						PATH_WORKING_DIRECTORY.resolve("src/other/WcImport.java")));
	}

}
