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
package testing.saker.java.compiler.tests.jdk9.tasks.javac;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class SimpleModulePathTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		SakerPath firstmoduleinfo = PATH_WORKING_DIRECTORY.resolve("first/module-info.java");
		SakerPath secondmoduleinfo = PATH_WORKING_DIRECTORY.resolve("second/module-info.java");
		SakerPath firstclass = PATH_WORKING_DIRECTORY.resolve("first/firstpkg/FirstClass.java");
		SakerPath secondclass = PATH_WORKING_DIRECTORY.resolve("second/secondpkg/SecondClass.java");

		runScriptTask("build");
		assertCompiled(firstmoduleinfo, secondmoduleinfo, firstclass, secondclass);

		runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());

		//assert that the second hasn't been recompiled, as the module info hasn't changed semantically
		files.putFile(firstmoduleinfo, files.getAllBytes(firstmoduleinfo).toString().replace("\n", ""));
		runScriptTask("build");
		assertCompiled(firstmoduleinfo);

		//remove exports directive
		files.putFile(firstmoduleinfo, "module modle.first { }");
		assertTaskException("saker.java.compiler.api.compile.exc.JavaCompilationFailedException",
				() -> runScriptTask("build"));

		//remove reference to the first pkg
		files.putFile(secondclass,
				files.getAllBytes(secondclass).toString().replace("private firstpkg.FirstClass fc;", ""));
		runScriptTask("build");
	}

}
