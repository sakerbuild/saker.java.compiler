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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import saker.build.thirdparty.saker.util.ReflectUtils;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayOutputStream;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;
import testing.saker.java.compiler.tests.tasks.javac.util.SimpleClass;
import testing.saker.java.compiler.tests.tasks.javac.util.SimpleSecondClass;

@SakerTest
public class LocalClassPathCompileTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {
	private Path localJarPath;

	@Override
	protected Map<String, ?> getTaskVariables() {
		TreeMap<String, Object> result = new TreeMap<>(super.getTaskVariables());
		result.put("test.local.path", localJarPath.toString());
		return result;
	}

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		localJarPath = getBuildDirectory().resolve("localcp.jar");
		exportClassPathJar(SimpleClass.class);

		runScriptTask("build");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("main"));
		
		runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());
		assertEquals(getMetric().getCompiledJavacPasses(), setOf());
		
		exportClassPathJar(SimpleClass.class, SimpleSecondClass.class);
		runScriptTask("build");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("main"));
	}

	private void exportClassPathJar(Class<?>... classes) throws IOException {
		byte[] jarbytes;
		try (UnsyncByteArrayOutputStream baos = new UnsyncByteArrayOutputStream()) {
			try (JarOutputStream jaros = new JarOutputStream(baos)) {
				for (Class<?> c : classes) {
					jaros.putNextEntry(new ZipEntry(c.getName().replace('.', '/') + ".class"));
					ReflectUtils.getClassBytesUsingClassLoader(SimpleClass.class).writeTo(jaros);
					jaros.closeEntry();
				}
			}
			jarbytes = baos.toByteArray();
		}
		try {
			if (Arrays.equals(Files.readAllBytes(localJarPath), jarbytes)) {
				return;
			}
		} catch (IOException e) {
		}
		Files.createDirectories(localJarPath.getParent());
		Files.write(localJarPath, jarbytes);
	}

}
