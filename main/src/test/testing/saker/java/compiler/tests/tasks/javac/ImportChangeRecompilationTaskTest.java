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
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class ImportChangeRecompilationTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		checkTypeImport();
		checkStaticImport();
		checkWildcardImport();
		checkStaticWildcardImport();
	}

	private void checkStaticWildcardImport() throws Throwable, AssertionError, IOException {
		runScriptTask("staticwildcardimport");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("staticwildcardimport"));

		SakerPath pathImporter = PATH_WORKING_DIRECTORY.resolve("staticwildcardimport/test/Importer.java");
		SakerPath pathUser = PATH_WORKING_DIRECTORY.resolve("staticwildcardimport/test/User.java");
		files.putFile(pathImporter, files.getAllBytes(pathImporter).toString()
				.replace("import static test.p1.ImportedClass.*;", "import static test.p2.ImportedClass.*;"));
		runScriptTask("staticwildcardimport");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("staticwildcardimport"));
		assertEquals(getMetric().getCompiledFiles(), setOf(pathImporter, pathUser));
	}

	private void checkWildcardImport() throws Throwable, AssertionError, IOException {
		runScriptTask("wildcardimport");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("wildcardimport"));

		SakerPath pathImporter = PATH_WORKING_DIRECTORY.resolve("wildcardimport/test/Importer.java");
		SakerPath pathUser = PATH_WORKING_DIRECTORY.resolve("wildcardimport/test/User.java");
		files.putFile(pathImporter, files.getAllBytes(pathImporter).toString()
				.replace("import test.p1.ImportedClass.*;", "import test.p2.ImportedClass.*;"));
		runScriptTask("wildcardimport");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("wildcardimport"));
		assertEquals(getMetric().getCompiledFiles(), setOf(pathImporter, pathUser));
	}

	private void checkStaticImport() throws Throwable, AssertionError, IOException {
		runScriptTask("staticimport");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("staticimport"));

		SakerPath pathImporter = PATH_WORKING_DIRECTORY.resolve("staticimport/test/Importer.java");
		SakerPath pathUser = PATH_WORKING_DIRECTORY.resolve("staticimport/test/User.java");
		files.putFile(pathImporter, files.getAllBytes(pathImporter).toString().replace(
				"import static test.p1.ImportedClass.CONSTANT;", "import static test.p2.ImportedClass.CONSTANT;"));
		runScriptTask("staticimport");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("staticimport"));
		assertEquals(getMetric().getCompiledFiles(), setOf(pathImporter, pathUser));
	}

	private void checkTypeImport() throws Throwable, AssertionError, IOException {
		runScriptTask("typeimport");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("typeimport"));

		SakerPath pathItf = PATH_WORKING_DIRECTORY.resolve("typeimport/test/Itf.java");
		SakerPath pathItfImpl = PATH_WORKING_DIRECTORY.resolve("typeimport/test/ItfImpl.java");
		files.putFile(pathItf, files.getAllBytes(pathItf).toString().replace("import test.p1.ImportedClass;",
				"import test.p2.ImportedClass;"));
		assertTaskException("saker.java.compiler.api.compile.exc.JavaCompilationFailedException",
				() -> runScriptTask("typeimport"));
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("typeimport"));
		assertEquals(getMetric().getCompiledFiles(), setOf(pathItf, pathItfImpl));

		files.putFile(pathItfImpl, files.getAllBytes(pathItfImpl).toString().replace("import test.p1.ImportedClass;",
				"import test.p2.ImportedClass;"));
		runScriptTask("typeimport");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("typeimport"));
		assertEquals(getMetric().getCompiledFiles(), setOf(pathItf, pathItfImpl));
	}

}
