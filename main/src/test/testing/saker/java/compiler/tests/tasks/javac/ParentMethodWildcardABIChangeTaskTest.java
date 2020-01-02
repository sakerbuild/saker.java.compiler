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
public class ParentMethodWildcardABIChangeTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	private static final SakerPath TESTIMPL_PATH = SRC_PATH_BASE.resolve("test/TestImpl.java");
	private static final SakerPath TESTITF_PATH = SRC_PATH_BASE.resolve("test/TestItf.java");

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		runScriptTask("build");
		assertCompiled(TESTITF_PATH, TESTIMPL_PATH);
		assertReused();

		files.putFile(TESTITF_PATH,
				files.getAllBytes(TESTITF_PATH).toString().replace("? extends Runnable", "? extends Thread"));
		assertTaskException("saker.java.compiler.api.compile.exc.JavaCompilationFailedException",
				() -> runScriptTask("build"));
		assertCompiled(TESTITF_PATH, TESTIMPL_PATH);
		assertReused();

		files.putFile(TESTIMPL_PATH,
				files.getAllBytes(TESTIMPL_PATH).toString().replace("? extends Runnable", "? extends Thread"));
		runScriptTask("build");
		assertCompiled(TESTITF_PATH, TESTIMPL_PATH);
		assertReused();
	}

}
