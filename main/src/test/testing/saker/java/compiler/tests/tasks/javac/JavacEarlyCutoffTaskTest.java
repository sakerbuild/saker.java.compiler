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

import java.io.IOException;

import saker.build.file.path.SakerPath;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class JavacEarlyCutoffTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	private static final SakerPath firstsrc = PATH_WORKING_DIRECTORY.resolve("first/test/First.java");
	private static final SakerPath secondsrc = PATH_WORKING_DIRECTORY.resolve("second/test/Second.java");

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		test("");
		test("_withlist");
		test("_withinclude");
	}

	private void test(String sufffix) throws Throwable, AssertionError, IOException {
		ByteArrayRegion firstsrcbytes = files.getAllBytes(firstsrc);
		ByteArrayRegion secondsrcbytes = files.getAllBytes(secondsrc);

		String targetname = "build" + sufffix;
		String firstid = "first" + sufffix;
		String secondid = "second" + sufffix;
		String thirdid = "third" + sufffix;

		System.out.println("JavacEarlyCutoffTaskTest.test() test " + targetname);

		runScriptTask(targetname);
		assertEquals(getMetric().getCompiledJavacPasses(), setOf(firstid, secondid, thirdid));

		runScriptTask(targetname);
		assertEmpty(getMetric().getRunTaskIdFactories());
		assertEquals(getMetric().getBootedJavacCompilePasses(), setOf());

		//API of first has not changed. only first should be recompiled
		files.putFile(firstsrc, firstsrcbytes.toString().replace("\"First.First()\"", "\"MODIFIED\""));
		runScriptTask(targetname);
		assertEquals(getMetric().getCompiledJavacPasses(), setOf(firstid));
		assertEquals(getMetric().getBootedJavacCompilePasses(), setOf());

		files.putFile(firstsrc, files.getAllBytes(firstsrc).toString().replace("//APIPLACEHOLDER", "public int i;"));
		runScriptTask(targetname);
		//as an ABI change was made, the other passes need to be recompiled as well
		assertEquals(getMetric().getCompiledJavacPasses(), setOf(firstid, secondid, thirdid));
		assertEquals(getMetric().getBootedJavacCompilePasses(), setOf());

		files.putFile(secondsrc, secondsrcbytes.toString().replace("//APIPLACEHOLDER", "public int j;"));
		runScriptTask(targetname);
		//as an ABI change was made, the other passes need to be recompiled as well
		assertEquals(getMetric().getCompiledJavacPasses(), setOf(secondid, thirdid));
		assertEquals(getMetric().getBootedJavacCompilePasses(), setOf());

		files.putFile(firstsrc, "\n" + files.getAllBytes(firstsrc));
		runScriptTask(targetname);
		assertEquals(getMetric().getCompiledJavacPasses(), setOf(firstid));
		assertEquals(getMetric().getBootedJavacCompilePasses(), setOf());

		//reset contents
		files.putFile(firstsrc, firstsrcbytes);
		files.putFile(secondsrc, secondsrcbytes);
	}
}
