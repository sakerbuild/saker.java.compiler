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
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import saker.build.file.provider.LocalFileProvider;
import saker.build.thirdparty.saker.util.ReflectUtils;
import saker.build.thirdparty.saker.util.io.ByteArrayRegion;
import saker.build.thirdparty.saker.util.io.ByteSink;
import saker.build.thirdparty.saker.util.io.UnsyncByteArrayOutputStream;
import testing.saker.SakerTest;
import testing.saker.java.compiler.JavaCompilerVariablesMetricEnvironmentTaskTestCase;
import testing.saker.java.compiler.tests.tasks.javac.util.SimpleClass;

@SakerTest
public class JarClassPathTaskTest extends JavaCompilerVariablesMetricEnvironmentTaskTestCase {

	@Override
	protected void runNestTaskTestImpl() throws Throwable {
		files.putFile(PATH_WORKING_DIRECTORY.resolve("mpath.jar"), getInitialJarBytes());
		runScriptTask("build");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("second"));

		runScriptTask("build");
		assertEmpty(getMetric().getRunTaskIdFactories());

		files.putFile(PATH_WORKING_DIRECTORY.resolve("mpath.jar"), getAddedJarBytes());
		runScriptTask("build");
		assertEquals(getMetric().getCompiledJavacPasses(), setOf("second"));
	}

	private ByteArrayRegion getInitialJarBytes() throws IOException {
		ByteArrayRegion jarbytes;
		try (UnsyncByteArrayOutputStream baos = new UnsyncByteArrayOutputStream()) {
			try (JarOutputStream jaros = new JarOutputStream(baos)) {

				jaros.putNextEntry(new ZipEntry("firstpkg/FirstClass.class"));
				LocalFileProvider.getInstance().writeTo(
						getWorkingDirectory().resolve("jarfiles/firstpkg/FirstClass.class"), ByteSink.valueOf(jaros));
			}
			jarbytes = baos.toByteArrayRegion();
		}
		return jarbytes;
	}

	private ByteArrayRegion getAddedJarBytes() throws IOException {
		ByteArrayRegion jarbytes;
		try (UnsyncByteArrayOutputStream baos = new UnsyncByteArrayOutputStream()) {
			try (JarOutputStream jaros = new JarOutputStream(baos)) {

				jaros.putNextEntry(new ZipEntry("firstpkg/FirstClass.class"));
				LocalFileProvider.getInstance().writeTo(
						getWorkingDirectory().resolve("jarfiles/firstpkg/FirstClass.class"), ByteSink.valueOf(jaros));

				jaros.putNextEntry(new ZipEntry(SimpleClass.class.getName().replace('.', '/') + ".class"));
				ReflectUtils.getClassBytesUsingClassLoader(SimpleClass.class).writeTo(jaros);
			}
			jarbytes = baos.toByteArrayRegion();
		}
		return jarbytes;
	}

}
