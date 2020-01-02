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
public class AnnotationMethodABIChangeTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		SakerPath annotpath = PATH_WORKING_DIRECTORY.resolve("cp/test/Annot.java");
		SakerPath userpath = PATH_WORKING_DIRECTORY.resolve("cp/test/User.java");
		SakerPath defmethoduserpath = PATH_WORKING_DIRECTORY.resolve("cp/test/DefMethodUser.java");
		SakerPath srcuserpath = PATH_WORKING_DIRECTORY.resolve("src/test/SrcUser.java");
		SakerPath srcdefmethoduserpath = PATH_WORKING_DIRECTORY.resolve("src/test/SrcDefMethodUser.java");

		runScriptTask("build");
		assertEquals(getMetric().getCompiledFiles(),
				setOf(annotpath, userpath, defmethoduserpath, srcuserpath, srcdefmethoduserpath));

		runScriptTask("build");
		assertEmpty(getMetric().getCompiledFiles());

		files.putFile(annotpath, files.getAllBytes(annotpath).toString().replace("public String value();", "/*val*/"));
		//compilation fails as the classes which use the annotation in the same compile pass is not modified accordingly
		assertTaskException("saker.java.compiler.api.compile.exc.JavaCompilationFailedException", () -> runScriptTask("build"));

		files.putFile(userpath, files.getAllBytes(userpath).toString().replace("value = \"val\"", ""));
		files.putFile(defmethoduserpath, files.getAllBytes(defmethoduserpath).toString().replace("\"val\"", ""));
		assertTaskException("saker.java.compiler.api.compile.exc.JavaCompilationFailedException", () -> runScriptTask("build"));
		assertEquals(getMetric().getCompiledFiles(),
				setOf(annotpath, userpath, defmethoduserpath, srcuserpath, srcdefmethoduserpath));

		files.putFile(srcuserpath, files.getAllBytes(srcuserpath).toString().replace("value = \"val\"", ""));
		files.putFile(srcdefmethoduserpath, files.getAllBytes(srcdefmethoduserpath).toString().replace("\"val\"", ""));
		runScriptTask("build");
		assertEquals(getMetric().getCompiledFiles(), setOf(srcuserpath, srcdefmethoduserpath));
	}

}
