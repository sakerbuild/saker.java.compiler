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
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import saker.build.file.path.SakerPath;
import saker.build.file.provider.LocalFileProvider;
import saker.build.thirdparty.saker.util.ObjectUtils;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import saker.build.thirdparty.saker.util.io.ByteSink;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayOutputStream;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;

@SakerTest
public class TransitiveClasspathModifyTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	private SakerPath jarPath;

	@Override
	protected Map<String, ?> getTaskVariables() {
		TreeMap<String, Object> result = ObjectUtils.newTreeMap(super.getTaskVariables());
		result.put("test.jar.path", Objects.toString(jarPath, null));
		return result;
	}

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		testSameJarModify();
		testJarPathModify();
	}

	private void testJarPathModify() throws IOException, Throwable, AssertionError {
		//the second pass should be reinvoked, even if there was no ABI change in the first one, as the classpath changed
		SakerPath path1 = PATH_WORKING_DIRECTORY.resolve("classpath-v1.jar");
		SakerPath path2 = PATH_WORKING_DIRECTORY.resolve("classpath-v2.jar");

		files.putFile(path1, getJarBytes("test/FirstClass.class"));
		files.putFile(path2, getJarBytes("test/FirstClass.class", "test/SecondClass.class"));

		jarPath = path1;
		runScriptTask("jarmodifybuild");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("cp_jm", "main_jm"));

		runScriptTask("jarmodifybuild");
		assertEmpty(getMetric().getRunTaskIdFactories());

		jarPath = path2;
		runScriptTask("jarmodifybuild");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("cp_jm", "main_jm"));

		runScriptTask("jarmodifybuild");
		assertEmpty(getMetric().getRunTaskIdFactories());
	}

	private void testSameJarModify() throws IOException, Throwable, AssertionError {
		files.putFile(PATH_WORKING_DIRECTORY.resolve("classpath.jar"), getJarBytes("test/FirstClass.class"));
		runScriptTask("samebuild");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("cp", "main"));

		runScriptTask("samebuild");
		assertEmpty(getMetric().getRunTaskIdFactories());

		files.putFile(PATH_WORKING_DIRECTORY.resolve("classpath.jar"),
				getJarBytes("test/FirstClass.class", "test/SecondClass.class"));
		runScriptTask("samebuild");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("cp", "main"));

		runScriptTask("samebuild");
		assertEmpty(getMetric().getRunTaskIdFactories());
	}

	private ByteArrayRegion getJarBytes(String... filenames) throws IOException {
		ByteArrayRegion jarbytes;
		try (UnsyncByteArrayOutputStream baos = new UnsyncByteArrayOutputStream()) {
			try (JarOutputStream jaros = new JarOutputStream(baos)) {
				for (String cname : filenames) {
					jaros.putNextEntry(new ZipEntry(cname));
					LocalFileProvider.getInstance().writeTo(getWorkingDirectory().resolve("jarclasses").resolve(cname),
							ByteSink.valueOf(jaros));
				}
			}
			jarbytes = baos.toByteArrayRegion();
		}
		return jarbytes;
	}

}
