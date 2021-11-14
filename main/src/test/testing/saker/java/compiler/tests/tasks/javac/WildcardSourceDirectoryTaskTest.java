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
public class WildcardSourceDirectoryTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		SakerPath main1path = PATH_WORKING_DIRECTORY.resolve("dirs/src1/test1/Main.java");
		SakerPath main2path = PATH_WORKING_DIRECTORY.resolve("dirs/src2/test2/Main.java");
		SakerPath main3path = PATH_WORKING_DIRECTORY.resolve("dirs/src3/test3/Main.java");
		SakerPath main2extpath = PATH_WORKING_DIRECTORY.resolve("dirs/src2/test2/MainExt.java");

		SakerPath nondirpath = PATH_WORKING_DIRECTORY.resolve("dirs/srcfile");

		runScriptTask("build");
		assertCompiled(main1path, main2path);

		runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());

		files.putFile(main3path, "package test3; public class Main { }");
		runScriptTask("build");
		assertCompiled(main3path);

		runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());

		files.putFile(main2extpath, "package test2; public class MainExt { }");
		runScriptTask("build");
		assertCompiled(main2extpath);
		//only a single task should re-run, that is the compiler task.
		assertEquals(getMetric().getRunTaskIdFactories().size(), 1,
				() -> getMetric().getRunTaskIdFactories().toString());

		//nothing should be recompiled
		files.putFile(nondirpath, "test");
		runScriptTask("build");
		assertCompiled();

		//nothing should be rerun
		files.putFile(nondirpath, "testmod");
		runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());

		//nothing should be rerun
		files.delete(nondirpath);
		runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());
	}

}
