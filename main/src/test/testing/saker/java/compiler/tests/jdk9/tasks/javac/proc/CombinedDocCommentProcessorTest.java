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
package testing.saker.java.compiler.tests.jdk9.tasks.javac.proc;

import java.nio.file.NoSuchFileException;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

//The java.lang.Thread.Thread(ThreadGroup, Runnable, String, long, boolean) constructor 
//  was introduced in Java 9 so we have this test case here instead of the general package
@SakerTest
public class CombinedDocCommentProcessorTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		SakerPath packageinfo1path = PATH_WORKING_DIRECTORY.resolve("src/test/package-info.java");
		SakerPath packageinfo2path = PATH_WORKING_DIRECTORY.resolve("src/test2/package-info.java");
		SakerPath outpackage1txtpath = getJavacBuildGeneratedSourceDirectory("src").resolve("out_pkg_test.txt");
		SakerPath outpackage2txtpath = getJavacBuildGeneratedSourceDirectory("src").resolve("out_pkg_test2.txt");

		SakerPath class1path = PATH_WORKING_DIRECTORY.resolve("src/test/MyClass.java");
		SakerPath class2path = PATH_WORKING_DIRECTORY.resolve("src/test2/MyClass.java");
		SakerPath outclass1txtpath = getJavacBuildGeneratedSourceDirectory("src").resolve("out_class_test.MyClass.txt");
		SakerPath outclass2txtpath = getJavacBuildGeneratedSourceDirectory("src")
				.resolve("out_class_test2.MyClass.txt");

		runScriptTask("build");
		assertEquals(getMetric().getInitializedProcessors(),
				setOf("proc.ClassDocProcessor", "proc.PackageDocProcessor"));
		assertEquals(files.getAllBytes(outpackage1txtpath).toString().trim(), "DocPackagetest");
		assertEquals(files.getAllBytes(outpackage2txtpath).toString().trim(), "DocPackagetest2");
		assertEquals(files.getAllBytes(outclass1txtpath).toString().trim(), "MyClassDoc");
		assertEquals(files.getAllBytes(outclass2txtpath).toString().trim(), "MyClassDoc2");

		runScriptTask("build");
		assertEquals(getMetric().getInitializedProcessors(), setOf());

		//remove the comment of the package that is consumed on a separate thread that doesn't inherit thread locals
		files.putFile(packageinfo1path,
				files.getAllBytes(packageinfo1path).toString().replace("/**DocPackagetest*/", "//nodoc"));
		runScriptTask("build");
		//check that the class doc processor wasn't run
		assertEquals(getMetric().getInitializedProcessors(), setOf("proc.PackageDocProcessor"));
		//the file should be deleted
		assertException(NoSuchFileException.class, () -> files.getAllBytes(outpackage1txtpath));
		//the other package doc file should be kept
		assertEquals(files.getAllBytes(outpackage2txtpath).toString().trim(), "DocPackagetest2");

		files.putFile(packageinfo2path,
				files.getAllBytes(packageinfo2path).toString().replace("/**DocPackagetest2*/", "//nodoc"));
		runScriptTask("build");
		assertEquals(getMetric().getInitializedProcessors(), setOf("proc.PackageDocProcessor"));
		//the file should be deleted
		assertException(NoSuchFileException.class, () -> files.getAllBytes(outpackage1txtpath));
		assertException(NoSuchFileException.class, () -> files.getAllBytes(outpackage2txtpath));

		//re-add the doc
		files.putFile(packageinfo2path,
				files.getAllBytes(packageinfo2path).toString().replace("//nodoc", "/**DocPackagetest2_2*/"));
		runScriptTask("build");
		assertEquals(getMetric().getInitializedProcessors(), setOf("proc.PackageDocProcessor"));
		assertException(NoSuchFileException.class, () -> files.getAllBytes(outpackage1txtpath));
		assertEquals(files.getAllBytes(outpackage2txtpath).toString().trim(), "DocPackagetest2_2");

		//modify class doc where the doc is consumed in a separate thread, check that only that is run
		files.putFile(class1path, files.getAllBytes(class1path).toString().replace("/**MyClassDoc*/", "//nodoc"));
		runScriptTask("build");
		assertEquals(getMetric().getInitializedProcessors(), setOf("proc.ClassDocProcessor"));
		assertException(NoSuchFileException.class, () -> files.getAllBytes(outclass1txtpath));
		assertEquals(files.getAllBytes(outclass2txtpath).toString().trim(), "MyClassDoc2");
	}

}
