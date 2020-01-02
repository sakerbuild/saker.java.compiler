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
public class TransitiveModulePathTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		SakerPath firstmoduleinfo = PATH_WORKING_DIRECTORY.resolve("first/module-info.java");
		SakerPath secondmoduleinfo = PATH_WORKING_DIRECTORY.resolve("second/module-info.java");
		SakerPath thirdmoduleinfo = PATH_WORKING_DIRECTORY.resolve("third/module-info.java");
		SakerPath firstclass = PATH_WORKING_DIRECTORY.resolve("first/firstpkg/FirstClass.java");
		SakerPath secondclass = PATH_WORKING_DIRECTORY.resolve("second/secondpkg/SecondClass.java");
		SakerPath thirdclass = PATH_WORKING_DIRECTORY.resolve("third/thirdpkg/ThirdClass.java");

		assertTaskException("saker.java.compiler.api.compile.exc.JavaCompilationFailedException",
				() -> runScriptTask("build"));
		assertCompiled(firstmoduleinfo, secondmoduleinfo, thirdmoduleinfo, firstclass, secondclass,
				thirdclass);

		files.putFile(secondmoduleinfo, files.getAllBytes(secondmoduleinfo).toString().replace("requires modle.first;",
				"requires transitive modle.first;"));
		runScriptTask("build");
		assertCompiled(secondmoduleinfo, thirdmoduleinfo, secondclass, thirdclass);
	}

}
