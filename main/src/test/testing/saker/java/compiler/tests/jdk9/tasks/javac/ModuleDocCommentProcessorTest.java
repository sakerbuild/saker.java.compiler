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

import java.nio.file.NoSuchFileException;

import saker.build.file.path.SakerPath;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class ModuleDocCommentProcessorTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		SakerPath outtxtpath = getJavacBuildGeneratedSourceDirectory("src").resolve("out.txt");
		SakerPath moduleinfopath = PATH_WORKING_DIRECTORY.resolve("src/module-info.java");

		runScriptTask("build");
		assertEquals(getMetric().getInitializedProcessors(), setOf("proc.ModuleDocProcessor"));
		//trim to avoid non equality due to whitespace
		assertEquals(files.getAllBytes(outtxtpath).toString().trim(), "Doc");

		files.putFile(moduleinfopath, files.getAllBytes(moduleinfopath).toString().replace("Doc", "Doc2"));
		runScriptTask("build");
		assertEquals(getMetric().getInitializedProcessors(), setOf("proc.ModuleDocProcessor"));
		assertEquals(files.getAllBytes(outtxtpath).toString().trim(), "Doc2");

		//remove comment
		files.putFile(moduleinfopath, files.getAllBytes(moduleinfopath).toString().replace("/**Doc2*/", "//nodoc"));
		runScriptTask("build");
		assertEquals(getMetric().getInitializedProcessors(), setOf("proc.ModuleDocProcessor"));
		assertException(NoSuchFileException.class, () -> files.getAllBytes(outtxtpath));
		
		//re-add comment
		files.putFile(moduleinfopath, files.getAllBytes(moduleinfopath).toString().replace("//nodoc", "/**Doc3*/"));
		runScriptTask("build");
		assertEquals(getMetric().getInitializedProcessors(), setOf("proc.ModuleDocProcessor"));
		assertEquals(files.getAllBytes(outtxtpath).toString().trim(), "Doc3");
	}

}
